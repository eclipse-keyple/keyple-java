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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlankPresenceWaitForCardBlockingThreadedReader extends AbstractObservableLocalReader
    implements WaitForCardInsertionBlocking,
        WaitForCardRemovalDuringProcessing,
        WaitForCardRemovalBlocking {

  private static final Logger logger =
      LoggerFactory.getLogger(BlankPresenceWaitForCardBlockingThreadedReader.class);

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
  public BlankPresenceWaitForCardBlockingThreadedReader(
      String pluginName, String readerName, Integer mockDetect) {
    super(pluginName, readerName);
    this.mockDetect = mockDetect;
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
  protected void onStartDetection() {
    // Do nothing
  }

  @Override
  protected void onStopDetection() {
    // Do nothing
  }
}
