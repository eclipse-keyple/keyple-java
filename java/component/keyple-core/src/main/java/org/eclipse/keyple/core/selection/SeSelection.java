/* **************************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.core.selection;

import java.util.*;
import org.eclipse.keyple.core.command.AbstractApduCommandBuilder;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.Reader;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsResponse;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.message.*;
import org.eclipse.keyple.core.seproxy.message.ChannelControl;
import org.eclipse.keyple.core.seproxy.message.DefaultSelectionsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SeSelection class handles the card selection process.
 *
 * <p>It provides a way to do an explicit card selection or to post process a default card
 * selection. <br>
 * The channel is kept open by default, but can be closed after each selection cases (see
 * PrepareReleaseSeChannel).
 */
public final class SeSelection {
  private static final Logger logger = LoggerFactory.getLogger(SeSelection.class);

  /*
   * list of selection requests used to build the AbstractMatchingSe list in return of
   * processSelection methods
   */
  private final List<AbstractSeSelectionRequest<? extends AbstractApduCommandBuilder>>
      seSelectionRequests =
          new ArrayList<AbstractSeSelectionRequest<? extends AbstractApduCommandBuilder>>();
  private final MultiSeRequestProcessing multiSeRequestProcessing;
  private ChannelControl channelControl = ChannelControl.KEEP_OPEN;

  /**
   * Constructor.
   *
   * @param multiSeRequestProcessing the multi card processing mode
   */
  public SeSelection(MultiSeRequestProcessing multiSeRequestProcessing) {
    this.multiSeRequestProcessing = multiSeRequestProcessing;
  }

  /** Alternate constructor for standard usages. */
  public SeSelection() {
    this(MultiSeRequestProcessing.FIRST_MATCH);
  }

  /**
   * Prepare a selection: add the selection request from the provided selector to the selection
   * request set.
   *
   * <p>
   *
   * @param seSelectionRequest the selector to prepare
   * @return the selection index giving the current selection position in the selection request.
   */
  public int prepareSelection(
      AbstractSeSelectionRequest<? extends AbstractApduCommandBuilder> seSelectionRequest) {
    if (logger.isTraceEnabled()) {
      logger.trace("SELECTORREQUEST = {}", seSelectionRequest.getSelectionRequest());
    }
    /* keep the selection request */
    seSelectionRequests.add(seSelectionRequest);
    /* return the selection index (starting at 0) */
    return seSelectionRequests.size() - 1;
  }

  /**
   * Prepare to close the card channel.<br>
   * If this command is called before a "process" selection command then the last transmission to
   * the PO will be associated with the indication CLOSE_AFTER in order to close the card channel.
   * <br>
   * This makes it possible to chain several selections on the same card if necessary.
   */
  public final void prepareReleaseSeChannel() {
    channelControl = ChannelControl.CLOSE_AFTER;
  }

  /**
   * Process the selection response either from a {@link
   * org.eclipse.keyple.core.seproxy.event.ReaderEvent} (default selection) or from an explicit
   * selection.
   *
   * <p>The responses from the List of {@link SeResponse} is parsed and checked.
   *
   * <p>A {@link AbstractMatchingSe} list is build and returned. Non matching card are signaled by a
   * null element in the list
   *
   * @param defaultSelectionsResponse the selection response
   * @return the {@link SelectionsResult} containing the result of all prepared selection cases,
   *     including {@link AbstractMatchingSe} and {@link SeResponse}.
   * @throws KeypleException if the selection process failed
   */
  private SelectionsResult processSelection(
      AbstractDefaultSelectionsResponse defaultSelectionsResponse) {
    SelectionsResult selectionsResult = new SelectionsResult();

    int index = 0;

    /* Check SeResponses */
    for (SeResponse seResponse :
        ((DefaultSelectionsResponse) defaultSelectionsResponse).getSelectionSeResponses()) {
      /* test if the selection is successful: we should have either a FCI or an ATR */
      if (seResponse != null
          && seResponse.getSelectionStatus() != null
          && seResponse.getSelectionStatus().hasMatched()) {
        /*
         * create a AbstractMatchingSe with the class deduced from the selection request
         * during the selection preparation
         */
        AbstractMatchingSe matchingSe = seSelectionRequests.get(index).parse(seResponse);

        // determine if the current matching card is selected
        SelectionStatus selectionStatus = seResponse.getSelectionStatus();
        boolean isSelected;
        if (selectionStatus != null) {
          isSelected = selectionStatus.hasMatched() && seResponse.isLogicalChannelOpen();
        } else {
          isSelected = false;
        }

        selectionsResult.addMatchingSe(index, matchingSe, isSelected);
      }
      index++;
    }
    return selectionsResult;
  }

  /**
   * Parses the response to a selection operation sent to a card and return a list of {@link
   * AbstractMatchingSe}
   *
   * <p>Selection cases that have not matched the current card are set to null.
   *
   * @param defaultSelectionsResponse the response from the reader to the {@link
   *     AbstractDefaultSelectionsRequest}
   * @return the {@link SelectionsResult} containing the result of all prepared selection cases,
   *     including {@link AbstractMatchingSe} and {@link SeResponse}.
   * @throws KeypleException if an error occurs during the selection process
   */
  public SelectionsResult processDefaultSelection(
      AbstractDefaultSelectionsResponse defaultSelectionsResponse) {

    /* null pointer exception protection */
    if (defaultSelectionsResponse == null) {
      logger.error("defaultSelectionsResponse shouldn't be null in processSelection.");
      return null;
    }

    if (logger.isTraceEnabled()) {
      logger.trace(
          "Process default SELECTIONRESPONSE ({} response(s))",
          ((DefaultSelectionsResponse) defaultSelectionsResponse).getSelectionSeResponses().size());
    }

    return processSelection(defaultSelectionsResponse);
  }

  /**
   * Execute the selection process and return a list of {@link AbstractMatchingSe}.
   *
   * <p>Selection requests are transmitted to the card through the supplied Reader.
   *
   * <p>The process stops in the following cases:
   *
   * <ul>
   *   <li>All the selection requests have been transmitted
   *   <li>A selection request matches the current card and the keepChannelOpen flag was true
   * </ul>
   *
   * <p>
   *
   * @param reader the Reader on which the selection is made
   * @return the {@link SelectionsResult} containing the result of all prepared selection cases,
   *     including {@link AbstractMatchingSe} and {@link SeResponse}.
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed
   * @throws KeypleException if an error occurs during the selection process
   */
  public SelectionsResult processExplicitSelection(Reader reader) {
    List<SeRequest> selectionRequests = new ArrayList<SeRequest>();
    for (AbstractSeSelectionRequest<? extends AbstractApduCommandBuilder> seSelectionRequest :
        seSelectionRequests) {
      selectionRequests.add(seSelectionRequest.getSelectionRequest());
    }
    if (logger.isTraceEnabled()) {
      logger.trace("Transmit SELECTIONREQUEST ({} request(s))", selectionRequests.size());
    }

    /* Communicate with the card to do the selection */
    List<SeResponse> seResponses =
        ((ProxyReader) reader)
            .transmitSeRequests(selectionRequests, multiSeRequestProcessing, channelControl);

    return processSelection(new DefaultSelectionsResponse(seResponses));
  }

  /**
   * The SelectionOperation is the {@link AbstractDefaultSelectionsRequest} to process in ordered to
   * select a card among others through the selection process. This method is useful to build the
   * prepared selection to be executed by a reader just after a card insertion.
   *
   * @return the {@link AbstractDefaultSelectionsRequest} previously prepared with prepareSelection
   */
  public AbstractDefaultSelectionsRequest getSelectionOperation() {
    List<SeRequest> selectionRequests = new ArrayList<SeRequest>();
    for (AbstractSeSelectionRequest<? extends AbstractApduCommandBuilder> seSelectionRequest :
        seSelectionRequests) {
      selectionRequests.add(seSelectionRequest.getSelectionRequest());
    }
    return new DefaultSelectionsRequest(
        selectionRequests, multiSeRequestProcessing, channelControl);
  }
}
