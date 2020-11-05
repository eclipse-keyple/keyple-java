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
package org.eclipse.keyple.plugin.pcsc;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import javax.smartcardio.*;
import org.eclipse.keyple.core.plugin.reader.AbstractObservableLocalReader;
import org.eclipse.keyple.core.plugin.reader.ObservableReaderStateService;
import org.eclipse.keyple.core.plugin.reader.SmartInsertionReader;
import org.eclipse.keyple.core.plugin.reader.SmartRemovalReader;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.service.exception.KeypleReaderProtocolNotSupportedException;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Package private class implementing the {@link Reader} interface for PC/SC based readers.
 *
 * <p>A PC/SC reader is observable ({@link AbstractObservableLocalReader}), autonomous to detect the
 * insertion of cards ({@link SmartInsertionReader}, able to detect the removal of a card prior an
 * attempt to communicate with it ({@link SmartRemovalReader} and has specific settings ({@link
 * PcscReader}.
 *
 * @since 0.9
 */
final class PcscReaderImpl extends AbstractObservableLocalReader
    implements PcscReader, SmartInsertionReader, SmartRemovalReader {

  private static final Logger logger = LoggerFactory.getLogger(PcscReaderImpl.class);

  private final CardTerminal terminal;
  private String parameterCardProtocol;
  private boolean cardExclusiveMode;
  private boolean cardReset;
  private Boolean isContactless;

  private Card card;
  private CardChannel channel;

  // the latency delay value (in ms) determines the maximum time during which the
  // waitForCardPresent and waitForCardPresent blocking functions will execute.
  // This will correspond to the capacity to react to the interrupt signal of
  // the thread (see cancel method of the Future object)
  private static final long INSERT_LATENCY = 500;
  private static final long REMOVAL_LATENCY = 500;

  private final AtomicBoolean loopWaitCard = new AtomicBoolean();
  private final AtomicBoolean loopWaitCardRemoval = new AtomicBoolean();

  private final boolean usePingPresence;

  /**
   * This constructor should only be called by PcscPlugin PCSC reader parameters are initialized
   * with their default values as defined in setParameter.
   *
   * @param pluginName the name of the plugin
   * @param terminal the PC/SC terminal
   * @since 0.9
   */
  protected PcscReaderImpl(String pluginName, CardTerminal terminal) {

    super(pluginName, terminal.getName());

    this.terminal = terminal;
    this.card = null;
    this.channel = null;
    this.parameterCardProtocol = IsoProtocol.ANY.getValue();
    this.cardExclusiveMode = true;
    this.cardReset = false;
    this.isContactless = null;

    String os = System.getProperty("os.name").toLowerCase();
    usePingPresence = os.contains("mac");
    logger.info(
        "System detected : {}, is macOs checkPresence ping activated {}", os, usePingPresence);

    logger.debug("[{}] constructor => using terminal ", terminal);
  }

  /**
   * {@inheritDoc}
   *
   * @since 0.9
   */
  @Override
  public ObservableReaderStateService initStateService() {
    ObservableReaderStateService observableReaderStateService = null;

    if (!usePingPresence) {
      observableReaderStateService =
          ObservableReaderStateService.builder(this)
              .waitForCardInsertionWithSmartDetection()
              .waitForCardProcessingWithSmartDetection()
              .waitForCardRemovalWithSmartDetection()
              .build();
    } else {
      observableReaderStateService =
          ObservableReaderStateService.builder(this)
              .waitForCardInsertionWithPollingDetection()
              .waitForCardProcessingWithSmartDetection()
              .waitForCardRemovalWithSmartDetection()
              .build();
    }

    return observableReaderStateService;
  }

  /**
   * {@inheritDoc}
   *
   * @since 0.9
   */
  @Override
  protected void closePhysicalChannel() {

    try {
      if (card != null) {
        logger.debug("[{}] closePhysicalChannel => closing the channel.", this.getName());

        channel = null;
        card.disconnect(cardReset);
        card = null;
      } else {
        logger.debug("[{}] closePhysicalChannel => card object is null.", this.getName());
      }
    } catch (CardException e) {
      throw new KeypleReaderIOException("Error while closing physical channel", e);
    }
  }

  /**
   * {@inheritDoc}
   *
   * @since 0.9
   */
  @Override
  protected boolean checkCardPresence() {
    try {
      return terminal.isCardPresent();
    } catch (CardException e) {
      throw new KeypleReaderIOException("Exception occurred in isCardPresent", e);
    }
  }

  /**
   * Implements from SmartInsertionReader<br>
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
   * Implements from SmartInsertionReader<br>
   * {@inheritDoc}
   *
   * @since 0.9
   */
  @Override
  public void stopWaitForCard() {
    loopWaitCard.set(false);
  }

  /**
   * Wait for the card absent event from smartcard.io<br>
   * {@inheritDoc}
   *
   * @since 0.9
   */
  @Override
  public boolean waitForCardAbsentNative() {

    logger.debug(
        "[{}] waitForCardAbsentNative => loop with latency of {} ms.",
        this.getName(),
        REMOVAL_LATENCY);

    loopWaitCardRemoval.set(true);

    try {
      while (loopWaitCardRemoval.get()) {
        if (logger.isTraceEnabled()) {
          logger.trace("[{}] waitForCardAbsentNative => looping", this.getName());
        }
        if (terminal.waitForCardAbsent(REMOVAL_LATENCY)) {
          // card removed
          return true;
        } else {
          if (Thread.interrupted()) {
            logger.debug("[{}] waitForCardAbsentNative => task has been cancelled", this.getName());
            // task has been cancelled
            return false;
          }
        }
      }
      return false;
    } catch (CardException e) {
      throw new KeypleReaderIOException(
          "["
              + this.getName()
              + "] Exception occurred in waitForCardAbsentNative. "
              + "Message: "
              + e.getMessage());
    } catch (Throwable t) {
      // can or can not happen depending on terminal.waitForCardAbsent
      logger.debug("[{}] waitForCardAbsentNative => Throwable caught.", this.getName(), t);
      return false;
    }
  }

  /**
   * Implements from SmartRemovalReader<br>
   * {@inheritDoc}
   *
   * @since 0.9
   */
  @Override
  public void stopWaitForCardRemoval() {
    loopWaitCardRemoval.set(false);
  }

  /**
   * {@inheritDoc}
   *
   * @since 0.9
   */
  @Override
  protected byte[] transmitApdu(byte[] apduIn) {

    ResponseAPDU apduResponseData;

    if (channel != null) {
      try {
        apduResponseData = channel.transmit(new CommandAPDU(apduIn));
      } catch (CardException e) {
        throw new KeypleReaderIOException(this.getName() + ":" + e.getMessage());
      } catch (IllegalArgumentException e) {
        // card could have been removed prematurely
        throw new KeypleReaderIOException(this.getName() + ":" + e.getMessage());
      }
    } else {
      // could occur if the card was removed
      throw new KeypleReaderIOException(this.getName() + ": null channel.");
    }

    return apduResponseData.getBytes();
  }

  /**
   * {@inheritDoc}
   *
   * <p>In the PC/SC case, this method fills the internal protocols map with the provided protocol
   * and its associated rule (regular expression).
   *
   * @param readerProtocolName A not empty String.
   * @since 1.0
   */
  @Override
  protected void activateReaderProtocol(String readerProtocolName) {

    if (!PcscProtocolSetting.getSettings().containsKey(readerProtocolName)) {
      throw new KeypleReaderProtocolNotSupportedException(readerProtocolName);
    }

    if (logger.isDebugEnabled()) {
      logger.debug(
          "{}: Activate protocol {} with rule \"{}\".",
          getName(),
          readerProtocolName,
          PcscProtocolSetting.getSettings().get(readerProtocolName));
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>Remove the protocol from the active protocols list.<br>
   * Does nothing if the provided protocol is not active.
   *
   * @since 1.0
   */
  @Override
  protected void deactivateReaderProtocol(String readerProtocolName) {
    if (!PcscProtocolSetting.getSettings().containsKey(readerProtocolName)) {
      throw new KeypleReaderProtocolNotSupportedException(readerProtocolName);
    }

    if (logger.isDebugEnabled()) {
      logger.debug("{}: Deactivate protocol {}.", getName(), readerProtocolName);
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>The standard interface of the PC/SC readers does not allow to know directly the type of
   * protocol used by a card.
   *
   * <p>This is especially true in contactless mode. Moreover, in this mode, the Answer To Reset
   * (ATR) returned by the reader is not produced by the card but reconstructed by the reader from
   * low level internal data and with elements defined in the standard (see <b>Interoperability
   * Specification for ICCs and Personal Computer Systems</b>, Part 3).
   *
   * <p>We therefore use ATR (real or reconstructed) to identify the card protocol using regular
   * expressions. These regular expressions are managed in {@link PcscProtocolSetting}.
   *
   * @return True if the provided protocol matches the current protocol, false if not.
   * @since 1.0
   */
  @Override
  protected boolean isCurrentProtocol(String readerProtocolName) {

    String protocolRule = PcscProtocolSetting.getSettings().get(readerProtocolName);
    String atr = ByteArrayUtil.toHex(card.getATR().getBytes());
    return Pattern.compile(protocolRule).matcher(atr).matches();
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public PcscReader setSharingMode(SharingMode sharingMode) {

    Assert.getInstance().notNull(sharingMode, "sharingMode");

    if (sharingMode == SharingMode.SHARED) {
      // if a card is present, change the mode immediately
      if (card != null) {
        try {
          card.endExclusive();
        } catch (CardException e) {
          throw new KeypleReaderIOException("Couldn't disable exclusive mode", e);
        }
      }
      cardExclusiveMode = false;
    } else if (sharingMode == SharingMode.EXCLUSIVE) {
      cardExclusiveMode = true;
    }
    return this;
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public PcscReader setContactless(boolean contactless) {

    this.isContactless = contactless;
    return this;
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public PcscReader setIsoProtocol(IsoProtocol isoProtocol) {

    Assert.getInstance().notNull(isoProtocol, "isoProtocol");

    parameterCardProtocol = isoProtocol.getValue();
    return this;
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public PcscReader setDisconnectionMode(DisconnectionMode disconnectionMode) {

    Assert.getInstance().notNull(disconnectionMode, "disconnectionMode");

    cardReset = disconnectionMode == DisconnectionMode.RESET;
    return this;
  }

  @Override
  protected byte[] getATR() {
    return card.getATR().getBytes();
  }

  /**
   * Tells if a physical channel is open
   *
   * <p>This status may be wrong if the card has been removed.
   *
   * <p>The caller should test the card presence with isCardPresent before calling this method.
   *
   * @return true if the physical channel is open
   * @since 0.9
   */
  @Override
  protected boolean isPhysicalChannelOpen() {
    return card != null;
  }

  /**
   * Opens a physical channel
   *
   * <p>The card access may be set to 'Exclusive' through the reader's settings.
   *
   * <p>In this case be aware that on some platforms (ex. Windows 8+), the exclusivity is granted
   * for a limited time (ex. 5 seconds). After this delay, the card is automatically resetted.
   *
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed
   * @since 0.9
   */
  @Override
  protected void openPhysicalChannel() {

    /* init of the card physical channel: if not yet established, opening of a new physical channel */
    try {
      if (card == null) {
        this.card = this.terminal.connect(parameterCardProtocol);
        if (cardExclusiveMode) {
          card.beginExclusive();
          logger.debug(
              "[{}] Opening of a card physical channel in exclusive mode.", this.getName());

        } else {
          logger.debug("[{}] Opening of a card physical channel in shared mode.", this.getName());
        }
      }
      this.channel = card.getBasicChannel();
    } catch (CardException e) {
      throw new KeypleReaderIOException("Error while opening Physical Channel", e);
    }
  }

  /**
   * Return the mode of transmission used to communicate with the cards<br>
   * The transmission mode can set explicitly with setParameter(SETTING_KEY_TRANSMISSION_MODE,
   * MODE). In this case, this parameter has priority.
   *
   * <p>When the transmission mode has not explicitly set, we try to determine it from the name of
   * the reader and a parameter defined at the plugin level.
   *
   * @return the current transmission mode
   * @throws IllegalStateException if the transmission mode could not be determined
   * @since 0.9
   */
  @Override
  public boolean isContactless() {

    if (isContactless == null) {
      /* First time initialisation, the transmission mode has not yet been determined or fixed explicitly, let's ask the plugin to determine it (only once) */
      isContactless =
          ((PcscPluginImpl) SmartCardService.getInstance().getPlugin(getPluginName()))
              .isContactless(getName());
    }
    return isContactless;
  }
}
