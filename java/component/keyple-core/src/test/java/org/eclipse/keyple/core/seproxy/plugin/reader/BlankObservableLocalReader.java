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
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;

public class BlankObservableLocalReader extends AbstractObservableLocalReader {

  /**
   * Reader constructor
   *
   * @param pluginName the name of the plugin that instantiated the reader
   * @param readerName the name of the reader
   */
  public BlankObservableLocalReader(String pluginName, String readerName) {
    super(pluginName, readerName);

    stateService = initStateService();
  }

  @Override
  public final ObservableReaderStateService initStateService() {

    Map<AbstractObservableState.MonitoringState, AbstractObservableState> states =
        new HashMap<AbstractObservableState.MonitoringState, AbstractObservableState>();
    states.put(
        AbstractObservableState.MonitoringState.WAIT_FOR_SE_INSERTION,
        new WaitForSeInsertion(this));
    states.put(
        AbstractObservableState.MonitoringState.WAIT_FOR_SE_PROCESSING,
        new WaitForSeProcessing(this));
    states.put(
        AbstractObservableState.MonitoringState.WAIT_FOR_SE_REMOVAL, new WaitForSeRemoval(this));
    states.put(
        AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION,
        new WaitForStartDetect(this));

    return new ObservableReaderStateService(
        this, states, AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION);
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
  protected String getCurrentProtocol() {
    return null;
  }

  @Override
  public byte[] transmitApdu(byte[] apduIn) {
    return new byte[0];
  }

  @Override
  public void activateProtocol(String seProtocol) {}

  @Override
  public void deactivateProtocol(String seProtocol) {}

  @Override
  public boolean isContactless() {
    return true;
  }

  /**
   * The purpose of this method is to provide certain test methods with public access to
   * processSeInserted that is package-private.
   *
   * @return ReaderEvent returned by processSeInserted
   */
  public ReaderEvent processSeInsertedTest() {
    return processSeInserted();
  }
}
