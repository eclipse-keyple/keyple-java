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
package org.eclipse.keyple.core.plugin.reader;

import org.eclipse.keyple.core.service.event.ReaderEvent;

public class BlankObservableLocalReader extends AbstractObservableLocalReader {

  /**
   * Reader constructor
   *
   * @param pluginName the name of the plugin that instantiated the reader
   * @param readerName the name of the reader
   */
  public BlankObservableLocalReader(String pluginName, String readerName) {
    super(pluginName, readerName);
  }

  @Override
  public final ObservableReaderStateService initStateService() {

    return ObservableReaderStateService.builder(this)
        .waitForCardInsertionWithNativeDetection()
        .waitForCardProcessingWithNativeDetection()
        .waitForCardRemovalWithNativeDetection()
        .build();
  }

  @Override
  public boolean checkCardPresence() {
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
  protected boolean isCurrentProtocol(String readerProtocolName) {
    return false;
  }

  @Override
  public byte[] transmitApdu(byte[] apduIn) {
    return new byte[0];
  }

  @Override
  protected void activateReaderProtocol(String readerProtocolName) {}

  @Override
  protected void deactivateReaderProtocol(String readerProtocolName) {}

  @Override
  public boolean isContactless() {
    return true;
  }

  /**
   * The purpose of this method is to provide certain test methods with public access to
   * processCardInserted that is package-private.
   *
   * @return ReaderEvent returned by processCardInserted
   */
  public ReaderEvent processCardInsertedTest() {
    return processCardInserted();
  }
}
