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

import org.eclipse.keyple.core.seproxy.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlankSmartInsertionTheadedReader extends AbstractObservableLocalReader
    implements SmartInsertionReader {

  private static final Logger logger =
      LoggerFactory.getLogger(BlankSmartInsertionTheadedReader.class);

  Integer mockDetect; // TODO check why mockDetect is not initialized!
  Integer detectCount = 0;

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
  public BlankSmartInsertionTheadedReader(
      String pluginName, String readerName, Integer mockDetect) {
    super(pluginName, readerName);
  }

  @Override
  public final ObservableReaderStateService initStateService() {
    // To be fixed with KEYP-349
    return ObservableReaderStateService.builder(this)
        .waitForSeInsertionWithSmartDetection()
        .waitForSeProcessingWithNativeDetection()
        .waitForSeRemovalWithPollingDetection()
        .build();
  }

  private Runnable waitForCardPresentFuture() {
    return new Runnable() {
      @Override
      public void run() {
        logger.trace(
            "[{}] Invoke waitForCardPresent asynchronously",
            BlankSmartInsertionTheadedReader.this.getName());
        try {
          if (BlankSmartInsertionTheadedReader.this.waitForCardPresent()) {
            onEvent(AbstractObservableLocalReader.InternalEvent.CARD_INSERTED);
          }
        } catch (KeypleReaderIOException e) {
          logger.trace(
              "[{}] waitForCardPresent => Error while polling card with waitForCardPresent",
              BlankSmartInsertionTheadedReader.this.getName());
          onEvent(AbstractObservableLocalReader.InternalEvent.STOP_DETECT);
        }
      }
    };
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

  /*
   * @Override public boolean waitForCardPresent(long timeout) { // Obtain a number between [0 -
   * 49]. int n = new Random().nextInt(10); boolean isCardPresent = (n==2);
   * logger.trace("is card present {}",isCardPresent); return isCardPresent; }
   */

  @Override
  public boolean waitForCardPresent() {
    detectCount++;
    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return detectCount <= mockDetect;
  }

  @Override
  public void stopWaitForCard() {}
}
