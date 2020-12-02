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
package org.eclipse.keyple.calypso.command.po.builder;

import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.*;
import org.eclipse.keyple.calypso.command.po.parser.DecreaseRespPars;
import org.eclipse.keyple.core.card.message.ApduResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds the Decrease APDU command.
 *
 * @since 0.9
 */
public final class DecreaseCmdBuild extends AbstractPoCommandBuilder<DecreaseRespPars> {

  private static final Logger logger = LoggerFactory.getLogger(DecreaseCmdBuild.class);

  /** The command. */
  private static final CalypsoPoCommand command = CalypsoPoCommand.DECREASE;

  /* Construction arguments */
  private final int sfi;
  private final int counterNumber;
  private final int decValue;

  /**
   * Instantiates a new decrease cmd build from command parameters.
   *
   * @param poClass indicates which CLA byte should be used for the Apdu
   * @param sfi SFI of the file to select or 00h for current EF
   * @param counterNumber &gt;= 01h: Counters file, number of the counter. 00h: Simulated Counter
   *     file.
   * @param decValue Value to subtract to the counter (defined as a positive int &lt;= 16777215
   *     [FFFFFFh])
   * @throws IllegalArgumentException - if the decrement value is out of range
   * @throws IllegalArgumentException - if the command is inconsistent
   * @since 0.9
   */
  public DecreaseCmdBuild(PoClass poClass, byte sfi, int counterNumber, int decValue) {
    super(command, null);

    byte cla = poClass.getValue();
    this.sfi = sfi;
    this.counterNumber = counterNumber;
    this.decValue = decValue;

    // convert the integer value into a 3-byte buffer
    byte[] decValueBuffer = new byte[3];
    decValueBuffer[0] = (byte) ((decValue >> 16) & 0xFF);
    decValueBuffer[1] = (byte) ((decValue >> 8) & 0xFF);
    decValueBuffer[2] = (byte) (decValue & 0xFF);

    byte p2 = (byte) (sfi * 8);

    /* this is a case4 command, we set Le = 0 */
    this.request = setApduRequest(cla, command, (byte) counterNumber, p2, decValueBuffer, (byte) 0);

    if (logger.isDebugEnabled()) {
      String extraInfo =
          String.format("SFI=%02X, COUNTER=%d, DECREMENT=%d", sfi, counterNumber, decValue);
      this.addSubName(extraInfo);
    }
  }

  /** {@inheritDoc} */
  @Override
  public DecreaseRespPars createResponseParser(ApduResponse apduResponse) {
    return new DecreaseRespPars(apduResponse, this);
  }

  /**
   * {@inheritDoc}
   *
   * <p>This command modified the contents of the PO and therefore uses the session buffer.
   *
   * @return true
   * @since 0.9
   */
  @Override
  public boolean isSessionBufferUsed() {
    return true;
  }

  /**
   * @return the SFI of the accessed file
   * @since 0.9
   */
  public int getSfi() {
    return sfi;
  }

  /**
   * @return the counter number
   * @since 0.9
   */
  public int getCounterNumber() {
    return counterNumber;
  }

  /**
   * @return the decrement value
   * @since 0.9
   */
  public int getDecValue() {
    return decValue;
  }
}
