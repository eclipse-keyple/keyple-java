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
package org.eclipse.keyple.core.plugin;

import java.util.concurrent.atomic.AtomicBoolean;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This monitoring job polls the {@link Reader#isCardPresent()} method to detect
 * CARD_INSERTED/CARD_REMOVED
 *
 * <p>All runtime exceptions that may occur during the monitoring process are caught and notified at
 * the application level through the {@link
 * org.eclipse.keyple.core.service.event.ReaderObservationExceptionHandler} mechanism.
 */
class CardPresentMonitoringJob extends AbstractMonitoringJob {

  private static final Logger logger = LoggerFactory.getLogger(CardPresentMonitoringJob.class);

  private final long waitTimeout;
  private final boolean monitorInsertion;
  private final Reader reader;
  private final AtomicBoolean loop = new AtomicBoolean();

  /**
   * Build a monitoring job to detect the card insertion
   *
   * @param reader : reader that will be polled with the method isCardPresent()
   * @param waitTimeout : wait time during two hit of the polling
   * @param monitorInsertion : if true, polls for CARD_INSERTED, else CARD_REMOVED
   */
  public CardPresentMonitoringJob(Reader reader, long waitTimeout, boolean monitorInsertion) {
    this.waitTimeout = waitTimeout;
    this.reader = reader;
    this.monitorInsertion = monitorInsertion;
  }

  /** (package-private)<br> */
  @Override
  Runnable getMonitoringJob(final AbstractObservableState state) {
    return new Runnable() {
      long retries = 0;

      @Override
      public void run() {
        try {
          if (logger.isDebugEnabled()) {
            logger.debug("[{}] Polling from isCardPresent", reader.getName());
          }
          // re-init loop value to true
          loop.set(true);
          while (loop.get()) {
            // polls for CARD_INSERTED
            if (monitorInsertion && reader.isCardPresent()) {
              if (logger.isDebugEnabled()) {
                logger.debug("[{}] The card is present ", reader.getName());
              }
              try {
                state.onEvent(AbstractObservableLocalReader.InternalEvent.CARD_INSERTED);
                return;
              } catch (KeypleReaderIOException e) {
                logger.warn(
                    "[{}] waitForCardPresent => Error while processing card insertion event",
                    reader.getName());
              }
            }
            // polls for CARD_REMOVED
            if (!monitorInsertion && !reader.isCardPresent()) {
              if (logger.isDebugEnabled()) {
                logger.debug("[{}] The card is not present ", reader.getName());
              }
              loop.set(false);
              try {
                state.onEvent(AbstractObservableLocalReader.InternalEvent.CARD_REMOVED);
                return;
              } catch (KeypleReaderIOException e) {
                logger.warn(
                    "[{}] waitForCardAbsent => Error while processing card removal event",
                    reader.getName());
              }
            }
            retries++;

            if (logger.isTraceEnabled()) {
              logger.trace("[{}] isCardPresent polling retries : {}", reader.getName(), retries);
            }
            try {
              // wait a bit
              Thread.sleep(waitTimeout);
            } catch (InterruptedException ignored) {
              // Restore interrupted state...
              Thread.currentThread().interrupt();
              loop.set(false);
            }
          }
          if (logger.isTraceEnabled()) {
            logger.trace("[{}] Looping has been stopped", reader.getName());
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
    if (logger.isDebugEnabled()) {
      logger.debug("[{}] Stop polling ", reader.getName());
    }
    loop.set(false);
  }
}
