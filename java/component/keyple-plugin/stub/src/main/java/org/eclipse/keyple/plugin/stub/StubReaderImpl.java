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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderProtocolNotFoundException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderProtocolNotSupportedException;
import org.eclipse.keyple.core.seproxy.plugin.reader.AbstractObservableLocalReader;
import org.eclipse.keyple.core.seproxy.plugin.reader.AbstractObservableState;
import org.eclipse.keyple.core.seproxy.plugin.reader.ObservableReaderStateService;
import org.eclipse.keyple.core.seproxy.plugin.reader.SmartInsertionMonitoringJob;
import org.eclipse.keyple.core.seproxy.plugin.reader.SmartInsertionReader;
import org.eclipse.keyple.core.seproxy.plugin.reader.SmartRemovalMonitoringJob;
import org.eclipse.keyple.core.seproxy.plugin.reader.SmartRemovalReader;
import org.eclipse.keyple.core.seproxy.plugin.reader.WaitForSeInsertion;
import org.eclipse.keyple.core.seproxy.plugin.reader.WaitForSeProcessing;
import org.eclipse.keyple.core.seproxy.plugin.reader.WaitForSeRemoval;
import org.eclipse.keyple.core.seproxy.plugin.reader.WaitForStartDetect;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.core.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulates communication with a {@link StubSecureElement}. StubReader is observable, it raises
 * {@link org.eclipse.keyple.core.seproxy.event.ReaderEvent} : SE_INSERTED, SE_REMOVED
 */
class StubReaderImpl extends AbstractObservableLocalReader
    implements StubReader, SmartInsertionReader, SmartRemovalReader {

  private static final Logger logger = LoggerFactory.getLogger(StubReaderImpl.class);

  private StubSecureElement se;
  private String currentProtocol;
  boolean isContactless = true;
  private final Map<String, String> protocolsMap;

  protected final ExecutorService executorService;

  private final AtomicBoolean loopWaitSe = new AtomicBoolean();
  private final AtomicBoolean loopWaitSeRemoval = new AtomicBoolean();

  /**
   * Do not use directly
   *
   * @param readerName
   */
  StubReaderImpl(String pluginName, String readerName) {
    super(pluginName, readerName);

    // create a executor service with one thread whose name is customized
    executorService =
        Executors.newSingleThreadExecutor(new NamedThreadFactory("MonitoringThread-" + readerName));

    stateService = initStateService();

    protocolsMap = new HashMap<String, String>();
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
    return se.getATR();
  }

  /** {@inheritDoc} */
  @Override
  protected boolean isPhysicalChannelOpen() {
    return se != null && se.isPhysicalChannelOpen();
  }

  /** {@inheritDoc} */
  @Override
  protected void openPhysicalChannel() {
    if (se != null) {
      se.openPhysicalChannel();
    }
  }

  /** {@inheritDoc} */
  @Override
  public void closePhysicalChannel() {
    if (se != null) {
      se.closePhysicalChannel();
    }
  }

  /** {@inheritDoc} */
  @Override
  public byte[] transmitApdu(byte[] apduIn) {
    if (se == null) {
      throw new KeypleReaderIOException("No SE available.");
    }
    return se.processApdu(apduIn);
  }

  @Override
  protected boolean isCurrentProtocol(String readerProtocolName) {
    if (se != null && se.getSeProtocol() != null) {
      return se.getSeProtocol().equals(readerProtocolName);
    } else {
      return false;
    }
  }

  @Override
  protected synchronized boolean checkSePresence() {
    return se != null;
  }

  @Override
  protected final void activateReaderProtocol(String seProtocol) {

    Assert.getInstance().notNull(seProtocol, "seProtocol");

    String protocolRule = StubProtocolSetting.getSettings().get(seProtocol);

    if (protocolRule == null || protocolRule.isEmpty()) {
      throw new KeypleReaderProtocolNotSupportedException(seProtocol);
    }

    if (logger.isInfoEnabled()) {
      logger.info(
          "{}: Activate protocol {} with rule \"{}\".", getName(), seProtocol, protocolRule);
    }
    protocolsMap.put(seProtocol, protocolRule);
  }

  @Override
  protected final void deactivateReaderProtocol(String seProtocol) {

    Assert.getInstance().notNull(seProtocol, "seProtocol");

    if (logger.isInfoEnabled()) {
      logger.info("{}: Deactivate protocol {}.", getName(), seProtocol);
    }
    if (protocolsMap.remove(seProtocol) == null && logger.isInfoEnabled()) {
      logger.info("{}: Protocol {} was not active", getName(), seProtocol);
    }
  }

  /** @return the current transmission mode */
  @Override
  public boolean isContactless() {
    return isContactless;
  }

  /*
   * STATE CONTROLLERS FOR INSERTING AND REMOVING SECURE ELEMENT
   */

  /**
   * Inserts the provided SE.<br>
   *
   * @param _se stub secure element to be inserted in the reader
   * @throws KeypleReaderProtocolNotFoundException if the SE protocol is not found
   */
  public synchronized void insertSe(StubSecureElement _se) {
    logger.debug("Insert SE {}", _se);
    /* clean channels status */
    if (isPhysicalChannelOpen()) {
      try {
        closePhysicalChannel();
      } catch (KeypleReaderException e) {
        logger.error("Error while closing channel reader", e);
      }
    }
    if (_se != null) {
      se = _se;
    }
  }

  public synchronized void removeSe() {
    logger.debug("Remove SE {}", se != null ? se : "none");
    currentProtocol = null;
    se = null;
  }

  public StubSecureElement getSe() {
    return se;
  }

  /**
   * This method is called by the monitoring thread to check SE presence
   *
   * @return true if the SE is present
   */
  @Override
  public boolean waitForCardPresent() {
    loopWaitSe.set(true);
    while (loopWaitSe.get()) {
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
    loopWaitSe.set(false);
  }

  /**
   * Defined in the {@link SmartRemovalReader} interface, this method is called by the monitoring
   * thread to check SE absence
   *
   * @return true if the SE is absent
   */
  @Override
  public boolean waitForCardAbsentNative() {
    loopWaitSeRemoval.set(true);
    while (loopWaitSeRemoval.get()) {
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
    loopWaitSeRemoval.set(false);
  }

  @Override
  protected final ObservableReaderStateService initStateService() {
    if (executorService == null) {
      throw new IllegalArgumentException("Executor service has not been initialized");
    }

    Map<AbstractObservableState.MonitoringState, AbstractObservableState> states =
        new HashMap<AbstractObservableState.MonitoringState, AbstractObservableState>();

    states.put(
        AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION,
        new WaitForStartDetect(this));

    states.put(
        AbstractObservableState.MonitoringState.WAIT_FOR_SE_INSERTION,
        new WaitForSeInsertion(this, new SmartInsertionMonitoringJob(this), executorService));

    states.put(
        AbstractObservableState.MonitoringState.WAIT_FOR_SE_PROCESSING,
        new WaitForSeProcessing(this, new SmartRemovalMonitoringJob(this), executorService));

    states.put(
        AbstractObservableState.MonitoringState.WAIT_FOR_SE_REMOVAL,
        new WaitForSeRemoval(this, new SmartRemovalMonitoringJob(this), executorService));

    return new ObservableReaderStateService(
        this, states, AbstractObservableState.MonitoringState.WAIT_FOR_SE_INSERTION);
  }
}
