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
package org.eclipse.keyple.calypso.command.po.parser.security;

import java.util.Arrays;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.builder.security.OpenSession10CmdBuild;
import org.eclipse.keyple.core.reader.message.ApduResponse;

public final class OpenSession10RespPars extends AbstractOpenSessionRespPars {

  /**
   * Instantiates a new OpenSession10RespPars from the response.
   *
   * @param response from OpenSession10RespPars
   * @param builder the reference to the builder that created this parser
   */
  public OpenSession10RespPars(ApduResponse response, OpenSession10CmdBuild builder) {
    super(response, builder, PoRevision.REV1_0);
  }

  @Override
  SecureSession toSecureSession(byte[] apduResponseData) {
    return createSecureSession(apduResponseData);
  }

  public static SecureSession createSecureSession(byte[] apduResponseData) {
    boolean previousSessionRatified;

    /**
     * In rev 1.0 mode, the response to the Open Secure Session command is as follows:
     *
     * <p><code>CC CC CC CC [RR RR] [NN..NN]</code>
     *
     * <p>Where:
     *
     * <ul>
     *   <li><code>CC CC CC CC</code> = PO challenge
     *   <li><code>RR RR</code> = ratification bytes (may be absent)
     *   <li><code>NN..NN</code> = record data (29 bytes)
     * </ul>
     *
     * Legal length values are:
     *
     * <ul>
     *   <li>4: ratified, 4-byte challenge, no data
     *   <li>33: ratified, 4-byte challenge, 29 bytes of data
     *   <li>6: not ratified (2 ratification bytes), 4-byte challenge, no data
     *   <li>35 not ratified (2 ratification bytes), 4-byte challenge, 29 bytes of data
     * </ul>
     */
    byte[] data;

    switch (apduResponseData.length) {
      case 4:
        previousSessionRatified = true;
        data = new byte[0];
        break;
      case 33:
        previousSessionRatified = true;
        data = Arrays.copyOfRange(apduResponseData, 4, 33);
        break;
      case 6:
        previousSessionRatified = false;
        data = new byte[0];
        break;
      case 35:
        previousSessionRatified = false;
        data = Arrays.copyOfRange(apduResponseData, 6, 35);
        break;
      default:
        throw new IllegalStateException(
            "Bad response length to Open Secure Session: " + apduResponseData.length);
    }

    /* KVC doesn't exist and is set to null for this type of PO */
    return new SecureSession(
        Arrays.copyOfRange(apduResponseData, 0, 3),
        Arrays.copyOfRange(apduResponseData, 3, 4),
        previousSessionRatified,
        false,
        null,
        data,
        apduResponseData);
  }
}
