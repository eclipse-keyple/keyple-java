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
import org.eclipse.keyple.calypso.command.po.builder.security.OpenSession32CmdBuild;
import org.eclipse.keyple.core.card.message.ApduResponse;

/**
 * Parses the Open session response from a PO revision 3.2 .
 *
 * @since 0.9
 */
public final class OpenSession32RespPars extends AbstractOpenSessionRespPars {

  /**
   * Instantiates a new OpenSession32RespPars from the response.
   *
   * @param response from OpenSession32RespPars
   * @param builder the reference to the builder that created this parser
   */
  public OpenSession32RespPars(ApduResponse response, OpenSession32CmdBuild builder) {
    super(response, builder, PoRevision.REV3_2);
  }

  /**
   * Method to get a Secure Session from the response in revision 3.2 mode.
   *
   * @param apduResponseData the apdu response data
   * @return a SecureSession
   */
  SecureSession toSecureSession(byte[] apduResponseData) {
    return createSecureSession(apduResponseData);
  }

  public static SecureSession createSecureSession(byte[] apduResponse) {

    byte flag = apduResponse[8];
    // ratification: if the bit 0 of flag is set then the previous session has been ratified
    boolean previousSessionRatified = (flag & 0x01) == (byte) 0x00;
    // secure session: if the bit 1 of flag is set then the secure session is authorized
    boolean manageSecureSessionAuthorized = (flag & 0x02) == (byte) 0x02;

    byte kif = apduResponse[9];
    byte kvc = apduResponse[10];
    int dataLength = apduResponse[11];
    byte[] data = Arrays.copyOfRange(apduResponse, 12, 12 + dataLength);

    return new SecureSession(
        Arrays.copyOfRange(apduResponse, 0, 3),
        Arrays.copyOfRange(apduResponse, 3, 8),
        previousSessionRatified,
        manageSecureSessionAuthorized,
        kif,
        kvc,
        data,
        apduResponse);
  }
}
