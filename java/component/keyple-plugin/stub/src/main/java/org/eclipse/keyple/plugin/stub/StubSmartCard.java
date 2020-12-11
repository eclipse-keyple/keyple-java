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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.util.ByteArrayUtil;

public abstract class StubSmartCard {

  /**
   * Getter for ATR
   *
   * @return Secured Element ATR
   */
  public abstract byte[] getATR();

  boolean isPhysicalChannelOpen = false;

  public boolean isPhysicalChannelOpen() {
    return isPhysicalChannelOpen;
  }

  public void openPhysicalChannel() {
    isPhysicalChannelOpen = true;
  }

  public void closePhysicalChannel() {
    isPhysicalChannelOpen = false;
  }

  /**
   * Gets the card protocol supported by the card
   *
   * @return A not empty String.
   */
  public abstract String getCardProtocol();

  Map<String, String> hexCommands = new ConcurrentHashMap<String, String>();

  /**
   * Add more simulated commands to the card Stub
   *
   * @param command hexadecimal command to react to
   * @param response hexadecimal response to be sent in reaction to command
   */
  public void addHexCommand(String command, String response) {
    if (command == null || response == null) {
      throw new IllegalArgumentException("Command and Response should not be null");
    }
    // add commands without space
    hexCommands.put(command.replace(" ", ""), response.replace(" ", ""));
  }

  /**
   * Remove simulated commands from the card Stub
   *
   * @param command hexadecimal command to be removed
   */
  public void removeHexCommand(String command) {
    if (command == null) {
      throw new IllegalArgumentException("Command should not be null");
    }
    hexCommands.remove(command.trim());
  }

  /**
   * Return APDU Response to APDU Request
   *
   * @param apduIn commands to be processed
   * @return APDU response
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed
   */
  public byte[] processApdu(byte[] apduIn) {

    if (apduIn == null) {
      return null;
    }

    // convert apduIn to hexa
    String hexApdu = ByteArrayUtil.toHex(apduIn);

    // return matching hexa response if the provided APDU matches the regex
    Pattern p;
    for (Map.Entry<String, String> hexCommand : hexCommands.entrySet()) {
      p = Pattern.compile(hexCommand.getKey());
      if (p.matcher(hexApdu).matches()) {
        return ByteArrayUtil.fromHex(hexCommand.getValue());
      }
    }

    // throw a KeypleReaderIOException if not found
    throw new KeypleReaderIOException("No response available for this request: " + hexApdu);
  }
}
