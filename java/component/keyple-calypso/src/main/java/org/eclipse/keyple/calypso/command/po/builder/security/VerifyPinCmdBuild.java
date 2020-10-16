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
package org.eclipse.keyple.calypso.command.po.builder.security;

import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.AbstractPoCommandBuilder;
import org.eclipse.keyple.calypso.command.po.CalypsoPoCommand;
import org.eclipse.keyple.calypso.command.po.parser.security.VerifyPinRespPars;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.core.reader.message.ApduResponse;

public class VerifyPinCmdBuild extends AbstractPoCommandBuilder<VerifyPinRespPars> {
  private static final CalypsoPoCommand command = CalypsoPoCommand.VERIFY_PIN;

  private final byte cla;
  private final boolean readCounterOnly;

  /**
   * Verify the PIN
   *
   * @param poClass indicates which CLA byte should be used for the Apdu
   * @param pinTransmissionMode defines the way the PIN code is transmitted: in clear or encrypted
   *     form.
   * @param pin the PIN data. The PIN is always 4-byte long here, even in the case of a encrypted
   *     transmission (@see setCipheredPinData).
   */
  public VerifyPinCmdBuild(
      PoClass poClass, PoTransaction.PinTransmissionMode pinTransmissionMode, byte[] pin) {
    super(command, null);

    if (pin == null
        || (pinTransmissionMode == PoTransaction.PinTransmissionMode.PLAIN && pin.length != 4)
        || (pinTransmissionMode == PoTransaction.PinTransmissionMode.ENCRYPTED
            && pin.length != 8)) {
      throw new IllegalArgumentException("The PIN must be 4 bytes long");
    }

    cla = poClass.getValue();

    byte p1 = (byte) 0x00;
    byte p2 = (byte) 0x00;

    this.request = setApduRequest(cla, command, p1, p2, pin, null);
    if (logger.isDebugEnabled()) {
      this.addSubName(pinTransmissionMode.toString());
    }

    readCounterOnly = false;
  }

  /**
   * Alternate builder dedicated to the reading of the wrong presentation counter
   *
   * @param poClass indicates which CLA byte should be used for the Apdu
   */
  public VerifyPinCmdBuild(PoClass poClass) {
    super(command, null);
    cla = poClass.getValue();

    byte p1 = (byte) 0x00;
    byte p2 = (byte) 0x00;

    this.request = setApduRequest(cla, command, p1, p2, null, null);
    if (logger.isDebugEnabled()) {
      this.addSubName("Read presentation counter");
    }

    readCounterOnly = true;
  }

  @Override
  public VerifyPinRespPars createResponseParser(ApduResponse apduResponse) {
    return new VerifyPinRespPars(apduResponse, this);
  }

  @Override
  public boolean isSessionBufferUsed() {
    return false;
  }

  /**
   * Indicates if the command is used to read the attempt counter only
   *
   * @return true if the command is used to read the attempt counter
   */
  public boolean isReadCounterOnly() {
    return readCounterOnly;
  }
}
