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
import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.calypso.command.po.AbstractPoResponseParser;
import org.eclipse.keyple.calypso.command.po.builder.security.CloseSessionCmdBuild;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoAccessForbiddenException;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoIllegalParameterException;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoSecurityDataException;
import org.eclipse.keyple.core.command.AbstractApduResponseParser;
import org.eclipse.keyple.core.reader.message.ApduResponse;

/**
 * Close Secure Session (008E) response parser. See specs: Calypso / page 104 / 9.5.2 - Close Secure
 * Session
 */
public final class CloseSessionRespPars extends AbstractPoResponseParser {

  private static final Map<Integer, StatusProperties> STATUS_TABLE;

  static {
    Map<Integer, StatusProperties> m =
        new HashMap<Integer, StatusProperties>(AbstractApduResponseParser.STATUS_TABLE);
    m.put(
        0x6700,
        new StatusProperties(
            "Lc signatureLo not supported (e.g. Lc=4 with a Revision 3.2 mode for Open Secure Session).",
            CalypsoPoIllegalParameterException.class));
    m.put(
        0x6B00,
        new StatusProperties(
            "P1 or P2 signatureLo not supported.", CalypsoPoIllegalParameterException.class));
    m.put(
        0x6988,
        new StatusProperties("incorrect signatureLo.", CalypsoPoSecurityDataException.class));
    m.put(
        0x6985,
        new StatusProperties("No session was opened.", CalypsoPoAccessForbiddenException.class));
    STATUS_TABLE = m;
  }

  @Override
  protected Map<Integer, StatusProperties> getStatusTable() {
    return STATUS_TABLE;
  }

  /** The signatureLo. */
  private byte[] signatureLo;

  /** The postponed data. */
  private byte[] postponedData;

  /**
   * Instantiates a new CloseSessionRespPars from the response.
   *
   * @param response from CloseSessionCmdBuild
   * @param builder the reference to the builder that created this parser
   */
  public CloseSessionRespPars(ApduResponse response, CloseSessionCmdBuild builder) {
    super(response, builder);
    parse(response.getDataOut());
  }

  private void parse(byte[] response) {
    if (response.length == 8) {
      signatureLo = Arrays.copyOfRange(response, 4, 8);
      postponedData = Arrays.copyOfRange(response, 1, 4);
    } else if (response.length == 4) {
      signatureLo = Arrays.copyOfRange(response, 0, 4);
    } else {
      if (response.length != 0) {
        throw new IllegalArgumentException(
            "Unexpected length in response to CloseSecureSession command: " + response.length);
      }
    }
  }

  public byte[] getSignatureLo() {
    return signatureLo;
  }

  public byte[] getPostponedData() {
    return postponedData;
  }
}
