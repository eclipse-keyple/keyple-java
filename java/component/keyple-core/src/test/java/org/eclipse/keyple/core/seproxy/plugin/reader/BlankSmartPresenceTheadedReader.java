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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlankSmartPresenceTheadedReader extends AbstractObservableLocalReader
    implements SmartRemovalReader, SmartInsertionReader {

  private static final Logger logger =
      LoggerFactory.getLogger(BlankSmartPresenceTheadedReader.class);

  protected ExecutorService executorService = Executors.newSingleThreadExecutor();

  Integer mockDetect;
  Integer detectCount = 0;
  volatile boolean removedOnlyOnce = false;

  /**
   * Reader constructor
   *
   * <p>Force the definition of a name through the use of super method.
   *
   * <p>
   *
   * @param pluginName the name of the plugin that instantiated the reader
   * @param readerName the name of the reader
   */
  public BlankSmartPresenceTheadedReader(String pluginName, String readerName, Integer mockDetect) {
    super(pluginName, readerName);
    this.mockDetect = mockDetect;
  }

  @Override
  public boolean checkSePresence() {
    return false;
  }

  @Override
  public byte[] getATR() {
    return new byte[0];
  }

  @Override
  public void openPhysicalChannel() {}

  @Override
  public void closePhysicalChannel() {}

  @Override
  public boolean isPhysicalChannelOpen() {
    return false;
  }

  @Override
  protected SeProtocol getCurrentProtocol() {
    return null;
  }

  @Override
  public byte[] transmitApdu(byte[] apduIn) {
    return new byte[0];
  }

  @Override
  public void activateProtocol(SeProtocol seProtocol) {}

  @Override
  public void deactivateProtocol(SeProtocol seProtocol) {}

  @Override
  public TransmissionMode getTransmissionMode() {
    return null;
  }

  @Override
  public boolean waitForCardAbsentNative() {
    if (!removedOnlyOnce) {
      removedOnlyOnce = true;
      return true;
    }
    return false;
  }

  @Override
  public void stopWaitForCardRemoval() {}

  @Override
  public void stopWaitForCard() {}

  @Override
  public boolean waitForCardPresent() {
    detectCount++;
    return detectCount <= mockDetect;
  }

  @Override
  public ObservableReaderStateService initStateService() {

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
