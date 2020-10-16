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
package org.eclipse.keyple.core.command;

import org.eclipse.keyple.core.reader.message.ApduRequest;

/**
 * Iso7816 APDU command builder.
 *
 * <p>It has to be extended by all PO and SAM command builder classes.
 *
 * <p>It provides, through the AbstractApduCommandBuilder superclass, the generic getters to
 * retrieve:
 *
 * <ul>
 *   <li>the name of the command,
 *   <li>the built APDURequest,
 *   <li>the corresponding AbstractApduResponseParser class.
 * </ul>
 *
 * @since 0.9
 */
public abstract class AbstractIso7816CommandBuilder extends AbstractApduCommandBuilder {

  /**
   * Abstract constructor to build a command with a command reference and an {@link ApduRequest}.
   *
   * @param commandReference The {@link CardCommand} used as Command reference (should be not null)
   * @param request The {@link ApduRequest} used for the request
   * @since 0.9
   */
  public AbstractIso7816CommandBuilder(CardCommand commandReference, ApduRequest request) {
    super(commandReference, request);
  }

  /**
   * Returns a byte array having the expected length determined by dataIn an le
   *
   * @param dataIn incoming data array (could be null)
   * @param le expected outgoing length (could be null)
   * @return a new byte array
   * @since 0.9
   */
  private byte[] allocateBuffer(byte[] dataIn, Byte le) {
    int length = 4; // header
    if (dataIn == null && le == null) {
      // case 1: 5-byte apdu, le=0
      length += 1; // Le
    } else {
      if (dataIn != null) {
        length += dataIn.length + 1; // Lc + data
      }
      if (le != null) {
        length += 1; // Le
      }
    }
    return new byte[length];
  }

  /**
   * Helper method to create an ApduRequest from separated elements.
   *
   * <p>The ISO7816-4 case for data in a command-response pair is determined from the provided
   * arguments:
   *
   * <ul>
   *   <li><code>dataIn &nbsp;= null, le &nbsp;= null</code>&nbsp;&nbsp;&rarr;&nbsp;&nbsp;case 1: no
   *       command data, no response data expected.
   *   <li><code>dataIn &nbsp;= null, le != null</code>&nbsp;&nbsp;&rarr;&nbsp;&nbsp;case 2: no
   *       command data, expected response data.
   *   <li><code>dataIn != null, le &nbsp;= null</code>&nbsp;&nbsp;&rarr;&nbsp;&nbsp;case 3: command
   *       data, no response data expected.
   *   <li><code>dataIn != null, le &nbsp;= 0&nbsp;&nbsp;&nbsp;</code>
   *       &nbsp;&nbsp;&rarr;&nbsp;&nbsp;case 4: command data, expected response data.
   * </ul>
   *
   * <p>If dataIn is not null and Le &gt; 0 an IllegalArgumentException is thrown
   *
   * @param cla class of instruction (should not be null)
   * @param command {@link CardCommand} used for Instruction (INS) (should not be null)
   * @param p1 instruction parameter 1 (should not be null)
   * @param p2 instruction parameter 2 (should not be null)
   * @param dataIn bytes sent in the data field of the command. dataIn.limit will be Lc (Number of
   *     bytes present in the data field of the command).
   * @param le maximum number of bytes expected in the data field of the response to the command
   *     (set to 0 is the case where ingoing and outgoing are present. Let the lower layer to handle
   *     the actual length [case4]).
   * @return a non null value
   * @since 0.9
   */
  protected ApduRequest setApduRequest(
      byte cla, CardCommand command, byte p1, byte p2, byte[] dataIn, Byte le) {
    boolean case4;
    /* sanity check */
    if (dataIn != null && le != null && le != 0) {
      throw new IllegalArgumentException(
          "Le must be equal to 0 when not null and ingoing data are present.");
    }

    /* Buffer allocation */
    byte[] apdu = allocateBuffer(dataIn, le);

    /* Build APDU buffer from provided arguments */
    apdu[0] = cla;
    apdu[1] = command.getInstructionByte();
    apdu[2] = p1;
    apdu[3] = p2;

    /* ISO7618 case determination and Le management */
    if (dataIn != null) {
      /* append Lc and ingoing data */
      apdu[4] = (byte) dataIn.length;
      System.arraycopy(dataIn, 0, apdu, 5, dataIn.length);
      if (le != null) {
        /*
         * case4: ingoing and outgoing data, Le is always set to 0 (see Calypso Reader
         * Recommendations - T84)
         */
        case4 = true;
        apdu[apdu.length - 1] = le;
      } else {
        /* case3: ingoing data only, no Le */
        case4 = false;
      }
    } else {
      if (le != null) {
        /* case2: outgoing data only */
        apdu[4] = le;
      } else {
        /* case1: no ingoing, no outgoing data, P3/Le = 0 */
        apdu[4] = (byte) 0x00;
      }
      case4 = false;
    }

    return new ApduRequest(command.getName(), apdu, case4);
  }
}
