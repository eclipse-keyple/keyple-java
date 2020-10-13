/* **************************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.example.common.generic;

import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsResponse;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This abstract class is intended to be extended by the applications classes in which the card
 * insertion, selection, removal is factorized here.<br>
 * In this implementation of the reader observation, the method {@link
 * ObservableReader.ReaderObserver#update(ReaderEvent)} is processed synchronously from the
 * monitoring thread.
 */
public abstract class AbstractReaderObserverSynchronousEngine
    implements ObservableReader.ReaderObserver {

  private static final Logger logger = LoggerFactory.getLogger(AbstractReaderObserverEngine.class);

  /**
   * Method to be implemented by the application to handle the SE_MATCHED reader event.<br>
   * The response to the default selections request is provided in argument.
   *
   * @param defaultSelectionsResponse the default selections response
   */
  protected abstract void processSeMatch(
      AbstractDefaultSelectionsResponse defaultSelectionsResponse);

  /** Method to be implemented by the application to handle the SE_INSERTED reader event */
  protected abstract void processSeInserted(); // alternative AID selection

  /** Method to be implemented by the application to handle the SE_REMOVED reader event */
  protected abstract void processSeRemoved();

  /**
   * Implementation of the {@link ObservableReader.ReaderObserver#update(ReaderEvent)} method.<br>
   * Its role is to call the abstract methods implemented by the application according to the
   * received event.<br>
   * Processing is done in the monitoring thread and any exceptions raised by the application are
   * caught.<br>
   * Note: in the case of SE_MATCHED, the received event also carries the response to the default
   * selection.
   *
   * @param event the reader event, either SE_MATCHED, SE_INSERTED, SE_REMOVED or TIMEOUT_ERROR
   */
  @Override
  public final void update(final ReaderEvent event) {
    logger.info("New reader event: {}", event.getReaderName());

    switch (event.getEventType()) {
      case SE_INSERTED:
        try {
          processSeInserted(); // optional, to process alternative AID selection
        } catch (KeypleException e) {
          logger.error("Keyple exception: {}", e.getMessage());
          /*
           * Informs the underlying layer of the end of the card processing, in order to
           * manage the removal sequence.
           */
          try {
            ((ObservableReader) (event.getReader())).finalizeSeProcessing();
          } catch (KeypleReaderNotFoundException ex) {
            logger.error("Reader not found exception: {}", ex.getMessage());
          } catch (KeyplePluginNotFoundException ex) {
            logger.error("Plugin not found exception: {}", ex.getMessage());
          }
        }
        break;

      case SE_MATCHED:
        try {
          processSeMatch(event.getDefaultSelectionsResponse()); // to process the
        } catch (KeypleException e) {
          logger.error("Keyple exception: {}", e.getMessage());
          /*
           * Informs the underlying layer of the end of the card processing, in order to
           * manage the removal sequence.
           */
          try {
            ((ObservableReader) (event.getReader())).finalizeSeProcessing();
          } catch (KeypleReaderNotFoundException ex) {
            logger.error("Reader not found exception: {}", ex.getMessage());
          } catch (KeyplePluginNotFoundException ex) {
            logger.error("Plugin not found exception: {}", ex.getMessage());
          }
        }
        break;

      case SE_REMOVED:
        processSeRemoved();
        if (logger.isInfoEnabled()) {
          logger.info("Waiting for a card...");
        }
        break;
      case TIMEOUT_ERROR:
        logger.error(
            "Timeout Error: the processing time or the time limit for removing the card"
                + " has been exceeded.");
        // do the appropriate processing here but do not prevent the return of this update
        // method (e. g. by
        // raising an exception)
    }
  }
}
