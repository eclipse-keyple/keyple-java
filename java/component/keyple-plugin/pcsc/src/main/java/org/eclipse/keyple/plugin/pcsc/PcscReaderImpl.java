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
package org.eclipse.keyple.plugin.pcsc;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.smartcardio.*;
import org.eclipse.keyple.core.plugin.reader.WaitForCardInsertionBlocking;
import org.eclipse.keyple.core.service.event.ReaderObservationExceptionHandler;
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * (package-private)<br>
 * Implementation of {@link AbstractPcscReader} for all non-MacOS platforms.
 *
 * <p>Implements {@link WaitForCardInsertionBlocking} to enable efficient blocking detection of card
 * insertion.
 *
 * @since 1.0
 */
final class PcscReaderImpl extends AbstractPcscReader implements WaitForCardInsertionBlocking {

  private static final Logger logger = LoggerFactory.getLogger(PcscReaderImpl.class);

  // the latency delay value (in ms) determines the maximum time during which the
  // waitForCardPresent blocking functions will execute.
  // This will correspond to the capacity to react to the interrupt signal of
  // the thread (see cancel method of the Future object)
  private static final long INSERT_LATENCY = 500;
  private final AtomicBoolean loopWaitCard = new AtomicBoolean();

  /**
   * This constructor should only be called a PcscPlugin on non-macOS platforms.
   *
   * @param pluginName the name of the plugin
   * @param terminal the PC/SC terminal
   * @param readerObservationExceptionHandler A not reference to an object implementing the {@link
   *     ReaderObservationExceptionHandler} interface.
   * @since 0.9
   */
  protected PcscReaderImpl(
      String pluginName,
      CardTerminal terminal,
      ReaderObservationExceptionHandler readerObservationExceptionHandler) {
    super(pluginName, terminal, readerObservationExceptionHandler);
  }

  /**
   * Implements from InsertionSmartDetectionReader<br>
   * {@inheritDoc}
   *
   * @since 0.9
   */
  @Override
  public boolean waitForCardPresent() {

    logger.debug(
        "[{}] waitForCardPresent => loop with latency of {} ms.", this.getName(), INSERT_LATENCY);

    // activate loop
    loopWaitCard.set(true);

    try {
      while (loopWaitCard.get()) {
        if (logger.isTraceEnabled()) {
          logger.trace("[{}] waitForCardPresent => looping", this.getName());
        }
        if (terminal.waitForCardPresent(INSERT_LATENCY)) {
          // card inserted
          return true;
        } else {
          if (Thread.interrupted()) {
            logger.debug("[{}] waitForCardPresent => task has been cancelled", this.getName());
            // task has been cancelled
            return false;
          }
        }
      }
      // if loop was stopped
      return false;
    } catch (CardException e) {
      throw new KeypleReaderIOException(
          "["
              + this.getName()
              + "] Exception occurred in waitForCardPresent. "
              + "Message: "
              + e.getMessage());
    } catch (Throwable t) {
      // can or can not happen depending on terminal.waitForCardPresent
      logger.debug("[{}] waitForCardPresent => Throwable caught.", this.getName(), t);
      return false;
    }
  }

  /**
   * Implements from InsertionSmartDetectionReader<br>
   * {@inheritDoc}
   *
   * @since 0.9
   */
  @Override
  public void stopWaitForCard() {
    loopWaitCard.set(false);
  }
}
