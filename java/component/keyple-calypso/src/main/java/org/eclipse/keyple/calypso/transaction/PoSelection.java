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
package org.eclipse.keyple.calypso.transaction;

import java.util.List;
import org.eclipse.keyple.calypso.SelectFileControl;
import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.AbstractPoCommandBuilder;
import org.eclipse.keyple.calypso.command.po.AbstractPoResponseParser;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoCommandException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoDesynchronizedExchangesException;
import org.eclipse.keyple.core.card.message.ApduResponse;
import org.eclipse.keyple.core.card.message.CardSelectionResponse;
import org.eclipse.keyple.core.card.selection.AbstractCardSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service extends {@link AbstractCardSelection} to manage specific features of Calypso POs
 * during the selection step.
 *
 * <p>The prefixed "prepare" methods allow to feed the global selection process by sending APDU
 * orders to the PO after the card selection. These APUDs will be sent to the PO in the order in
 * which they were prepared.
 *
 * <p>The parse method creates a {@link CalypsoPo} from the {@link CardSelectionResponse} received.
 *
 * @since 0.9
 */
public class PoSelection
    extends AbstractCardSelection<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> {
  private static final Logger logger = LoggerFactory.getLogger(PoSelection.class);
  private final PoClass poClass;

  /**
   * Constructor.
   *
   * <p>The {@link PoSelector} provided contains the selection data to target a particular PO.
   *
   * @param poSelector A reference to a {@link PoSelector}
   * @since 0.9
   */
  public PoSelection(PoSelector poSelector) {

    super(poSelector);

    /* No AID selector for a legacy Calypso PO */
    if (cardSelector.getAidSelector() == null) {
      poClass = PoClass.LEGACY;
    } else {
      poClass = PoClass.ISO;
    }

    if (logger.isTraceEnabled()) {
      logger.trace("Calypso {} selector", poClass);
    }
  }

  /**
   * Adds a command APDU to read a single record from the indicated EF.
   *
   * @param sfi the SFI of the EF to read
   * @param recordNumber the record number to read
   * @throws IllegalArgumentException if one of the provided argument is out of range
   * @since 0.9
   */
  public final void prepareReadRecordFile(byte sfi, int recordNumber) {
    addCommandBuilder(CalypsoPoUtils.prepareReadRecordFile(poClass, sfi, recordNumber));
  }

  /**
   * Adds a command APDU to select file with an LID provided as a 2-byte byte array.
   *
   * @param lid LID of the EF to select as a byte array
   * @throws IllegalArgumentException if the argument is not an array of 2 bytes
   * @since 0.9
   */
  public void prepareSelectFile(byte[] lid) {
    addCommandBuilder(CalypsoPoUtils.prepareSelectFile(poClass, lid));
  }

  /**
   * Adds a command APDU to select file with an LID provided as a short.
   *
   * @param lid A short
   * @since 0.9
   */
  public void prepareSelectFile(short lid) {
    byte[] bLid =
        new byte[] {
          (byte) ((lid >> 8) & 0xff), (byte) (lid & 0xff),
        };
    prepareSelectFile(bLid);
  }

  /**
   * Adds a command APDU to select file according to the provided {@link SelectFileControl} enum
   * entry indicating the navigation case: FIRST, NEXT or CURRENT.
   *
   * @param selectControl A {@link SelectFileControl} enum entry
   * @since 0.9
   */
  public void prepareSelectFile(SelectFileControl selectControl) {
    addCommandBuilder(CalypsoPoUtils.prepareSelectFile(poClass, selectControl));
  }

  /**
   * Parses the provided {@link CardSelectionResponse} and create a {@link CalypsoPo} object.
   *
   * <p>The {@link CalypsoPo} is filled with the PO identification data from the FCI and the
   * possible responses to additional APDU commands executed after the selection.
   *
   * @param cardSelectionResponse A reference to a {@link CardSelectionResponse}
   * @return A new {@link CalypsoPo}
   * @throws CalypsoDesynchronizedExchangesException if the number of responses is different from
   *     the number of requests
   * @throws CalypsoPoCommandException if a response from the PO was unexpected
   */
  @Override
  protected CalypsoPo parse(CardSelectionResponse cardSelectionResponse) {

    List<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> commandBuilders =
        getCommandBuilders();
    List<ApduResponse> apduResponses = cardSelectionResponse.getCardResponse().getApduResponses();

    if (commandBuilders.size() != apduResponses.size()) {
      throw new CalypsoDesynchronizedExchangesException(
          "Mismatch in the number of requests/responses");
    }

    CalypsoPo calypsoPo = new CalypsoPo(cardSelectionResponse);

    if (!commandBuilders.isEmpty()) {
      CalypsoPoUtils.updateCalypsoPo(calypsoPo, commandBuilders, apduResponses);
    }

    return calypsoPo;
  }
}
