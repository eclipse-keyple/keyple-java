/* **************************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.core.plugin.reader;

import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Detect the card insertion thanks to the method {@link
 * WaitForCardInsertionBlocking#waitForCardPresent()}. This method is invoked in another thread.
 *
 * <p>The job waits indefinitely for the waitForCardPresent method to return.
 *
 * <p>When a card is present, an internal CARD_INSERTED event is fired.
 *
 * <p>If a communication problem with the reader occurs (KeypleReaderIOException) an internal
 * STOP_DETECT event is fired.
 *
 * <p>All runtime exceptions that may occur during the monitoring process are caught and notified at
 * the application level through the {@link
 * org.eclipse.keyple.core.service.event.ReaderObservationExceptionHandler} mechanism.
 */
class SmartInsertionMonitoringJob extends AbstractMonitoringJob {

  private static final Logger logger = LoggerFactory.getLogger(SmartInsertionMonitoringJob.class);

  private final WaitForCardInsertionBlocking reader;

  public SmartInsertionMonitoringJob(WaitForCardInsertionBlocking reader) {
    this.reader = reader;
  }

  /** (package-private)<br> */
  @Override
  Runnable getMonitoringJob(final AbstractObservableState state) {
    /*
     * Invoke the method WaitForCardInsertionBlocking#waitForCardPresent() in another thread
     */
    return new Runnable() {
      @Override
      public void run() {
        try {
          boolean isCardFound = false;
          while (!isCardFound && !Thread.currentThread().isInterrupted()) {
            if (logger.isTraceEnabled()) {
              logger.trace("[{}] Invoke waitForCardPresent asynchronously", reader.getName());
            }

            boolean isCardPresent = reader.waitForCardPresent();

            try {
              if (isCardPresent) {
                state.onEvent(AbstractObservableLocalReader.InternalEvent.CARD_INSERTED);
                isCardFound = true;
              }
            } catch (KeypleReaderIOException e) {
              logger.warn(
                  "[{}] waitForCardPresent => Error while processing card insertion event",
                  reader.getName());
            }
          }
        } catch (RuntimeException e) {
          ((AbstractObservableLocalReader) reader)
              .getObservationExceptionHandler()
              .onReaderObservationError(
                  ((AbstractReader) reader).getPluginName(), reader.getName(), e);
        }
      }
    };
  }

  /** (package-private)<br> */
  @Override
  void stop() {
    if (logger.isTraceEnabled()) {
      logger.trace("[{}] stopWaitForCard on reader", reader.getName());
    }
    reader.stopWaitForCard();
  }
}
