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

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import javax.smartcardio.*;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.exception.*;
import org.eclipse.keyple.core.seproxy.plugin.reader.AbstractObservableLocalReader;
import org.eclipse.keyple.core.seproxy.plugin.reader.AbstractObservableState;
import org.eclipse.keyple.core.seproxy.plugin.reader.CardPresentMonitoringJob;
import org.eclipse.keyple.core.seproxy.plugin.reader.ObservableReaderStateService;
import org.eclipse.keyple.core.seproxy.plugin.reader.SmartInsertionMonitoringJob;
import org.eclipse.keyple.core.seproxy.plugin.reader.SmartInsertionReader;
import org.eclipse.keyple.core.seproxy.plugin.reader.SmartRemovalMonitoringJob;
import org.eclipse.keyple.core.seproxy.plugin.reader.SmartRemovalReader;
import org.eclipse.keyple.core.seproxy.plugin.reader.WaitForSeInsertion;
import org.eclipse.keyple.core.seproxy.plugin.reader.WaitForSeProcessing;
import org.eclipse.keyple.core.seproxy.plugin.reader.WaitForSeRemoval;
import org.eclipse.keyple.core.seproxy.plugin.reader.WaitForStartDetect;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Package private class implementing the {@link org.eclipse.keyple.core.seproxy.SeReader} interface
 * for PC/SC based readers.
 *
 * <p>A PC/SC reader is observable ({@link AbstractObservableLocalReader}), autonomous to detect the
 * insertion of secure elements ({@link SmartInsertionReader}, able to detect the removal of a
 * secure element prior an attempt to communicate with it ({@link SmartRemovalReader} and has
 * specific settings ({@link PcscReader}.
 */
final class PcscReaderImpl extends AbstractObservableLocalReader
    implements PcscReader, SmartInsertionReader, SmartRemovalReader {

  private static final Logger logger = LoggerFactory.getLogger(PcscReaderImpl.class);

  private final CardTerminal terminal;
  private String parameterCardProtocol;
  private boolean cardExclusiveMode;
  private boolean cardReset;
  private TransmissionMode transmissionMode;

  private Card card;
  private CardChannel channel;

  // the latency delay value (in ms) determines the maximum time during which the
  // waitForCardPresent and waitForCardPresent blocking functions will execute.
  // This will correspond to the capacity to react to the interrupt signal of
  // the thread (see cancel method of the Future object)
  private static final long INSERT_LATENCY = 500;
  private static final long REMOVAL_LATENCY = 500;

  private static final long INSERT_WAIT_TIMEOUT = 200;
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();

  private final AtomicBoolean loopWaitSe = new AtomicBoolean();
  private final AtomicBoolean loopWaitSeRemoval = new AtomicBoolean();

  private final boolean usePingPresence;

  /**
   * This constructor should only be called by PcscPlugin PCSC reader parameters are initialized
   * with their default values as defined in setParameter.
   *
   * @param pluginName the name of the plugin
   * @param terminal the PC/SC terminal
   */
  protected PcscReaderImpl(String pluginName, CardTerminal terminal) {

    super(pluginName, terminal.getName());

    this.terminal = terminal;
    this.card = null;
    this.channel = null;
    this.parameterCardProtocol = IsoProtocol.ANY.getValue();
    this.cardExclusiveMode = true;
    this.cardReset = false;

    String os = System.getProperty("os.name").toLowerCase();
    usePingPresence = os.contains("mac");
    logger.info(
        "System detected : {}, is macOs checkPresence ping activated {}", os, usePingPresence);

    this.stateService = initStateService();

    logger.debug("[{}] constructor => using terminal ", terminal);
  }

  @Override
  public ObservableReaderStateService initStateService() {

    Map<AbstractObservableState.MonitoringState, AbstractObservableState> states =
        new EnumMap<AbstractObservableState.MonitoringState, AbstractObservableState>(
            AbstractObservableState.MonitoringState.class);
    states.put(
        AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION,
        new WaitForStartDetect(this));

    // should the SmartInsertionMonitoringJob be used?
    if (!usePingPresence) {
      // use the SmartInsertionMonitoringJob
      states.put(
          AbstractObservableState.MonitoringState.WAIT_FOR_SE_INSERTION,
          new WaitForSeInsertion(this, new SmartInsertionMonitoringJob(this), executorService));
    } else {
      // use the CardPresentMonitoring job (only on Mac due to jvm crash)
      // https://github.com/eclipse/keyple-java/issues/153
      states.put(
          AbstractObservableState.MonitoringState.WAIT_FOR_SE_INSERTION,
          new WaitForSeInsertion(
              this,
              new CardPresentMonitoringJob(this, INSERT_WAIT_TIMEOUT, true),
              executorService));
    }

    states.put(
        AbstractObservableState.MonitoringState.WAIT_FOR_SE_PROCESSING,
        new WaitForSeProcessing(this, new SmartRemovalMonitoringJob(this), executorService));

    states.put(
        AbstractObservableState.MonitoringState.WAIT_FOR_SE_REMOVAL,
        new WaitForSeRemoval(this, new SmartRemovalMonitoringJob(this), executorService));

    return new ObservableReaderStateService(
        this, states, AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION);
  }

  /** {@inheritDoc} */
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

  /** {@inheritDoc} */
  @Override
  protected boolean checkSePresence() {
    try {
      return terminal.isCardPresent();
    } catch (CardException e) {
      throw new KeypleReaderIOException("Exception occurred in isSePresent", e);
    }
  }

  /**
   * Implements from SmartInsertionReader<br>
   * {@inheritDoc}
   */
  @Override
  public boolean waitForCardPresent() {

    logger.debug(
        "[{}] waitForCardPresent => loop with latency of {} ms.", this.getName(), INSERT_LATENCY);

    // activate loop
    loopWaitSe.set(true);

    try {
      while (loopWaitSe.get()) {
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
   */
  @Override
  public void stopWaitForCard() {
    loopWaitSe.set(false);
  }

  /**
   * Wait for the card absent event from smartcard.io<br>
   * {@inheritDoc}
   */
  @Override
  public boolean waitForCardAbsentNative() {

    logger.debug(
        "[{}] waitForCardAbsentNative => loop with latency of {} ms.",
        this.getName(),
        REMOVAL_LATENCY);

    loopWaitSeRemoval.set(true);

    try {
      while (loopWaitSeRemoval.get()) {
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
   */
  @Override
  public void stopWaitForCardRemoval() {
    loopWaitSeRemoval.set(false);
  }

  /** {@inheritDoc} */
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
      // could occur if the SE was removed
      throw new KeypleReaderIOException(this.getName() + ": null channel.");
    }
    return apduResponseData.getBytes();
  }

  /**
   * Tells if the current SE protocol matches the provided protocol flag. If the protocol flag is
   * not defined (null), we consider here that it matches. An exception is returned when the
   * provided protocolFlag is not found in the current protocolMap.
   *
   * @param protocolFlag the protocol flag
   * @return true if the current SE matches the protocol flag
   * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
   * @since 0.9
   */
  @Override
  protected boolean protocolFlagMatches(SeProtocol protocolFlag) {

    boolean result;
    // Test protocolFlag to check if ATR based protocol filtering is required
    if (protocolFlag != null) {
      if (!isPhysicalChannelOpen()) {
        openPhysicalChannel();
      }
      // the request will be executed only if the protocol match the requestElement
      String selectionMask = getProtocolsMap().get(protocolFlag);
      if (selectionMask == null) {
        throw new KeypleReaderIOException("Target selector mask not found: " + protocolFlag, null);
      }
      Pattern p = Pattern.compile(selectionMask);
      String atr = ByteArrayUtil.toHex(card.getATR().getBytes());
      if (!p.matcher(atr).matches()) {
        logger.debug(
            "[{}] protocolFlagMatches => unmatching SE. PROTOCOLFLAG = {}, ATR = {}, MASK = {}",
            this.getName(),
            protocolFlag,
            atr,
            selectionMask);

        result = false;
      } else {
        logger.debug(
            "[{}] protocolFlagMatches => matching SE. PROTOCOLFLAG = {}",
            this.getName(),
            protocolFlag);

        result = true;
      }
    } else {
      // no protocol defined returns true
      result = true;
    }
    return result;
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
      // if an SE is present, change the mode immediately
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
  public PcscReader setTransmissionMode(TransmissionMode transmissionMode) {

    Assert.getInstance().notNull(transmissionMode, "transmissionMode");

    this.transmissionMode = TransmissionMode.CONTACTLESS;
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
   * <p>The caller should test the card presence with isSePresent before calling this method.
   *
   * @return true if the physical channel is open
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
   * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
   */
  @Override
  protected void openPhysicalChannel() {

    /* init of the physical SE channel: if not yet established, opening of a new physical channel */
    try {
      if (card == null) {
        this.card = this.terminal.connect(parameterCardProtocol);
        if (cardExclusiveMode) {
          card.beginExclusive();
          logger.debug("[{}] Opening of a physical SE channel in exclusive mode.", this.getName());

        } else {
          logger.debug("[{}] Opening of a physical SE channel in shared mode.", this.getName());
        }
      }
      this.channel = card.getBasicChannel();
    } catch (CardException e) {
      throw new KeypleReaderIOException("Error while opening Physical Channel", e);
    }
  }

  /**
   * Return the mode of transmission used to communicate with the SEs<br>
   * The transmission mode can set explicitly with setParameter(SETTING_KEY_TRANSMISSION_MODE,
   * MODE). In this case, this parameter has priority.
   *
   * <p>When the transmission mode has not explicitly set, we try to determine it from the name of
   * the reader and a parameter defined at the plugin level.
   *
   * @return the current transmission mode
   * @throws IllegalStateException if the transmission mode could not be determined
   */
  @Override
  public TransmissionMode getTransmissionMode() {

    if (transmissionMode == null) {
      /* the transmission mode has not yet been determined or fixed explicitly, let's ask the plugin to determine it (only once) */
      transmissionMode =
          ((PcscPluginImpl) SeProxyService.getInstance().getPlugin(getPluginName()))
              .findTransmissionMode(getName());
    }
    return transmissionMode;
  }
}
