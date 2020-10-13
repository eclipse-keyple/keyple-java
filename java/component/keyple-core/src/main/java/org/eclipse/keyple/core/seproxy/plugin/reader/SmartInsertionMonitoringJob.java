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
package org.eclipse.keyple.core.seproxy.plugin.reader;

import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Detect the card insertion thanks to the method {@link SmartInsertionReader#waitForCardPresent()}.
 * This method is invoked in another thread.
 *
 * <p>The job waits indefinitely for the waitForCardPresent method to return.
 *
 * <p>When a card is present, an internal SE_INSERTED event is fired.
 *
 * <p>If a communication problem with the reader occurs (KeypleReaderIOException) an internal
 * STOP_DETECT event is fired.
 */
class SmartInsertionMonitoringJob extends AbstractMonitoringJob {

  private static final Logger logger = LoggerFactory.getLogger(SmartInsertionMonitoringJob.class);

  private final SmartInsertionReader reader;

  public SmartInsertionMonitoringJob(SmartInsertionReader reader) {
    this.reader = reader;
  }

  /** (package-private)<br> */
  @Override
  Runnable getMonitoringJob(final AbstractObservableState state) {
    /*
     * Invoke the method SmartInsertionReader#waitForCardPresent() in another thread
     */
    return new Runnable() {
      @Override
      public void run() {
        if (logger.isTraceEnabled()) {
          logger.trace("[{}] Invoke waitForCardPresent asynchronously", reader.getName());
        }
        try {
          if (reader.waitForCardPresent()) {
            state.onEvent(AbstractObservableLocalReader.InternalEvent.SE_INSERTED);
          }
        } catch (KeypleReaderIOException e) {
          if (logger.isTraceEnabled()) {
            logger.trace(
                "[{}] waitForCardPresent => Error while polling card with waitForCardPresent",
                reader.getName());
          }
          state.onEvent(AbstractObservableLocalReader.InternalEvent.STOP_DETECT);
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
