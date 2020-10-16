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
package org.eclipse.keyple.core.reader.plugin.reader;

import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ping the card to detect removal thanks to the method {@link
 * AbstractObservableLocalReader#isCardPresentPing()}. This method is invoked in another thread.
 *
 * <p>This job should be used by readers who do not have the ability to natively detect the
 * disappearance of the card at the end of the transaction.
 *
 * <p>It is based on sending a neutral APDU command as long as the card is responding, an internal
 * CARD_REMOVED event is fired when the card is no longer responding.
 *
 * <p>By default a delay of 200 ms is inserted between each APDU sending .
 */
class CardAbsentPingMonitoringJob extends AbstractMonitoringJob {

  private static final Logger logger = LoggerFactory.getLogger(CardAbsentPingMonitoringJob.class);

  private final AbstractObservableLocalReader reader;
  private Runnable job;
  private final AtomicBoolean loop = new AtomicBoolean();
  private long removalWait = 200;

  /**
   * Create a job monitor job that ping the card with the method isCardPresentPing()
   *
   * @param reader : reference to the reader
   */
  public CardAbsentPingMonitoringJob(AbstractObservableLocalReader reader) {
    this.reader = reader;
  }

  /**
   * Create a job monitor job that ping the card with the method isCardPresentPing()
   *
   * @param reader : reference to the reader
   * @param removalWait : delay between between each APDU sending
   */
  public CardAbsentPingMonitoringJob(AbstractObservableLocalReader reader, long removalWait) {
    this.reader = reader;
    this.removalWait = removalWait;
  }

  /** (package-private)<br> */
  @Override
  Runnable getMonitoringJob(final AbstractObservableState state) {

    /*
     * Loop until one the following condition is met : -
     * AbstractObservableLocalReader#isCardPresentPing returns false, meaning that the card ping has
     * failed - InterruptedException is caught
     */
    job =
        new Runnable() {
          long retries = 0;

          @Override
          public void run() {
            if (logger.isDebugEnabled()) {
              logger.debug("[{}] Polling from isCardPresentPing", reader.getName());
            }
            // re-init loop value to true
            loop.set(true);
            while (loop.get()) {
              if (!reader.isCardPresentPing()) {
                if (logger.isDebugEnabled()) {
                  logger.debug("[{}] the card stopped responding", reader.getName());
                }
                loop.set(false);
                state.onEvent(AbstractObservableLocalReader.InternalEvent.CARD_REMOVED);
                return;
              }
              retries++;

              if (logger.isTraceEnabled()) {
                logger.trace("[{}] Polling retries : {}", reader.getName(), retries);
              }
              try {
                // wait a bit
                Thread.sleep(removalWait);
              } catch (InterruptedException ignored) {
                // Restore interrupted state...
                Thread.currentThread().interrupt();
                loop.set(false);
              }
            }

            if (logger.isDebugEnabled()) {
              logger.debug("[{}] Polling loop has been stopped", reader.getName());
            }
          }
        };
    return job;
  }

  /** (package-private)<br> */
  @Override
  void stop() {
    if (logger.isDebugEnabled()) {
      logger.debug("[{}] Stop Polling ", reader.getName());
    }
    loop.set(false);
  }
}
