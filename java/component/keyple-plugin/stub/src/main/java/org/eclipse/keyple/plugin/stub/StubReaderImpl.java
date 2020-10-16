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
package org.eclipse.keyple.plugin.stub;

import java.util.concurrent.atomic.AtomicBoolean;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderProtocolNotFoundException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderProtocolNotSupportedException;
import org.eclipse.keyple.core.seproxy.plugin.reader.AbstractObservableLocalReader;
import org.eclipse.keyple.core.seproxy.plugin.reader.ObservableReaderStateService;
import org.eclipse.keyple.core.seproxy.plugin.reader.SmartInsertionReader;
import org.eclipse.keyple.core.seproxy.plugin.reader.SmartRemovalReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulates communication with a {@link StubSecureElement}. StubReader is observable, it raises
 * {@link org.eclipse.keyple.core.seproxy.event.ReaderEvent} : CARD_INSERTED, CARD_REMOVED
 */
class StubReaderImpl extends AbstractObservableLocalReader
    implements StubReader, SmartInsertionReader, SmartRemovalReader {

  private static final Logger logger = LoggerFactory.getLogger(StubReaderImpl.class);

  private StubSecureElement card;
  boolean isContactless = true;

  private final AtomicBoolean loopWaitCard = new AtomicBoolean();
  private final AtomicBoolean loopWaitCardRemoval = new AtomicBoolean();

  /**
   * Do not use directly
   *
   * @param readerName
   */
  StubReaderImpl(String pluginName, String readerName) {
    super(pluginName, readerName);
  }

  /**
   * Specify
   *
   * @param pluginName
   * @param name
   * @param isContactless
   */
  StubReaderImpl(String pluginName, String name, boolean isContactless) {
    this(pluginName, name);
    this.isContactless = isContactless;
  }

  @Override
  protected byte[] getATR() {
    return card.getATR();
  }

  /** {@inheritDoc} */
  @Override
  protected boolean isPhysicalChannelOpen() {
    return card != null && card.isPhysicalChannelOpen();
  }

  /** {@inheritDoc} */
  @Override
  protected void openPhysicalChannel() {
    if (card != null) {
      card.openPhysicalChannel();
    }
  }

  /** {@inheritDoc} */
  @Override
  public void closePhysicalChannel() {
    if (card != null) {
      card.closePhysicalChannel();
    }
  }

  /** {@inheritDoc} */
  @Override
  public byte[] transmitApdu(byte[] apduIn) {
    if (card == null) {
      throw new KeypleReaderIOException("No card available.");
    }
    return card.processApdu(apduIn);
  }

  @Override
  protected boolean isCurrentProtocol(String readerProtocolName) {
    if (card != null && card.getCardProtocol() != null) {
      return card.getCardProtocol().equals(readerProtocolName);
    } else {
      return false;
    }
  }

  @Override
  protected synchronized boolean checkSePresence() {
    return card != null;
  }

  @Override
  protected final void activateReaderProtocol(String readerProtocolName) {

    if (!StubProtocolSetting.getSettings().containsKey(readerProtocolName)) {
      throw new KeypleReaderProtocolNotSupportedException(readerProtocolName);
    }

    if (logger.isDebugEnabled()) {
      logger.debug(
          "{}: Activate protocol {} with rule \"{}\".",
          getName(),
          readerProtocolName,
          StubProtocolSetting.getSettings().get(readerProtocolName));
    }
  }

  @Override
  protected final void deactivateReaderProtocol(String readerProtocolName) {

    if (!StubProtocolSetting.getSettings().containsKey(readerProtocolName)) {
      throw new KeypleReaderProtocolNotSupportedException(readerProtocolName);
    }

    if (logger.isDebugEnabled()) {
      logger.debug("{}: Deactivate protocol {}.", getName(), readerProtocolName);
    }
  }

  /** @return the current transmission mode */
  @Override
  public boolean isContactless() {
    return isContactless;
  }

  /*
   * STATE CONTROLLERS FOR INSERTING AND REMOVING CARD
   */

  /**
   * Inserts the provided card.<br>
   *
   * @param _se stub card to be inserted in the reader
   * @throws KeypleReaderProtocolNotFoundException if the card protocol is not found
   */
  public synchronized void insertSe(StubSecureElement _se) {
    logger.debug("Insert card {}", _se);
    /* clean channels status */
    if (isPhysicalChannelOpen()) {
      try {
        closePhysicalChannel();
      } catch (KeypleReaderException e) {
        logger.error("Error while closing channel reader", e);
      }
    }
    if (_se != null) {
      card = _se;
    }
  }

  public synchronized void removeSe() {
    logger.debug("Remove card {}", card != null ? card : "none");
    card = null;
  }

  public StubSecureElement getSe() {
    return card;
  }

  /**
   * This method is called by the monitoring thread to check the card presence
   *
   * @return true if the card is present
   */
  @Override
  public boolean waitForCardPresent() {
    loopWaitCard.set(true);
    while (loopWaitCard.get()) {
      if (checkSePresence()) {
        return true;
      }
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        logger.debug("Sleep was interrupted");
        // Restore interrupted state...
        Thread.currentThread().interrupt();
      }
    }
    return false;
  }

  @Override
  public void stopWaitForCard() {
    loopWaitCard.set(false);
  }

  /**
   * Defined in the {@link SmartRemovalReader} interface, this method is called by the monitoring
   * thread to check the card absence
   *
   * @return true if the card is absent
   */
  @Override
  public boolean waitForCardAbsentNative() {
    loopWaitCardRemoval.set(true);
    while (loopWaitCardRemoval.get()) {
      if (!checkSePresence()) {
        logger.trace("[{}] card removed", this.getName());
        return true;
      }
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        logger.debug("Sleep was interrupted");
        // Restore interrupted state...
        Thread.currentThread().interrupt();
      }
    }
    return false;
  }

  @Override
  public void stopWaitForCardRemoval() {
    loopWaitCardRemoval.set(false);
  }

  @Override
  protected final ObservableReaderStateService initStateService() {
    return ObservableReaderStateService.builder(this)
        .WaitForCardInsertionWithSmartDetection()
        .WaitForCardProcessingWithSmartDetection()
        .WaitForCardRemovalWithSmartDetection()
        .build();
  }
}
