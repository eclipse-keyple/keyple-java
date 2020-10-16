/* **************************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
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

import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.calypso.command.po.AbstractPoResponseParser;
import org.eclipse.keyple.calypso.command.po.builder.security.RehabilitateCmdBuild;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoAccessForbiddenException;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoDataAccessException;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoSecurityContextException;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoSessionBufferOverflowException;
import org.eclipse.keyple.core.command.AbstractApduResponseParser;
import org.eclipse.keyple.core.reader.message.ApduResponse;

/** Rehabilitate response parser. */
public final class RehabilitateRespPars extends AbstractPoResponseParser {

  private static final Map<Integer, StatusProperties> STATUS_TABLE;

  static {
    Map<Integer, StatusProperties> m =
        new HashMap<Integer, StatusProperties>(AbstractApduResponseParser.STATUS_TABLE);
    m.put(
        0x6400,
        new StatusProperties(
            "Too many modifications in session.", CalypsoPoSessionBufferOverflowException.class));
    m.put(
        0x6700,
        new StatusProperties("Lc value not supported.", CalypsoPoDataAccessException.class));
    m.put(
        0x6982,
        new StatusProperties(
            "Security conditions not fulfilled (no session, wrong key).",
            CalypsoPoSecurityContextException.class));
    m.put(
        0x6985,
        new StatusProperties(
            "Access forbidden (DF context is invalid).", CalypsoPoAccessForbiddenException.class));
    STATUS_TABLE = m;
  }

  /**
   * Instantiates a new RehabilitateRespPars.
   *
   * @param response the response from Rehabilitate APDU Command
   * @param builder the reference to the builder that created this parser
   */
  public RehabilitateRespPars(ApduResponse response, RehabilitateCmdBuild builder) {
    super(response, builder);
  }

  @Override
  protected Map<Integer, StatusProperties> getStatusTable() {
    return STATUS_TABLE;
  }
}
