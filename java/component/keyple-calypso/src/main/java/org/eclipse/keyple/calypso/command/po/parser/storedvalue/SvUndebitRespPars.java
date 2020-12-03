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
package org.eclipse.keyple.calypso.command.po.parser.storedvalue;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.calypso.command.po.AbstractPoResponseParser;
import org.eclipse.keyple.calypso.command.po.builder.storedvalue.SvUndebitCmdBuild;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoIllegalParameterException;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoSecurityDataException;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoSessionBufferOverflowException;
import org.eclipse.keyple.calypso.command.sam.exception.CalypsoSamAccessForbiddenException;
import org.eclipse.keyple.calypso.command.sam.exception.CalypsoSamCounterOverflowException;
import org.eclipse.keyple.core.card.command.AbstractApduResponseParser;
import org.eclipse.keyple.core.card.message.ApduResponse;

/**
 * Parses the SV Undebit response.
 *
 * @since 0.9
 *     <p>See specs: Calypso Stored Value balance (signed binaries' coding based on the two's
 *     complement method)
 *     <p>balance - 3 bytes signed binary - Integer from -8,388,608 to 8,388,607
 *     <pre>
 * -8,388,608           %10000000.00000000.00000000
 * -8,388,607           %10000000.00000000.00000001
 * -8,388,606           %10000000.00000000.00000010
 *
 * -3           %11111111.11111111.11111101
 * -2           %11111111.11111111.11111110
 * -1           %11111111.11111111.11111111
 * 0           %00000000.00000000.00000000
 * 1           %00000000.00000000.00000001
 * 2           %00000000.00000000.00000010
 * 3           %00000000.00000000.00000011
 *
 * 8,388,605           %01111111.11111111.11111101
 * 8,388,606           %01111111.11111111.11111110
 * 8,388,607           %01111111.11111111.11111111
 * </pre>
 *     amount - 2 bytes signed binary
 *     <p>amount for debit - Integer 0..32767 =&gt; for negative value
 *     <pre>
 * -32767           %10000000.00000001
 * -32766           %10000000.00000010
 * -3           %11111111.11111101
 * -2           %11111111.11111110
 * -1           %11111111.11111111
 * 0           %00000000.00000000
 *
 * Notice: -32768 (%10000000.00000000) is not allowed.
 * </pre>
 *
 * @since 0.9
 */
public class SvUndebitRespPars extends AbstractPoResponseParser {

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
        new StatusProperties("Lc value not supported.", CalypsoPoIllegalParameterException.class));
    m.put(
        0x6900,
        new StatusProperties(
            "Transaction counter is 0 or SV TNum is FFFEh or FFFFh.",
            CalypsoSamCounterOverflowException.class));
    m.put(
        0x6985,
        new StatusProperties(
            "Preconditions not satisfied.", CalypsoSamAccessForbiddenException.class));
    m.put(
        0x6988,
        new StatusProperties("Incorrect signatureHi.", CalypsoPoSecurityDataException.class));
    m.put(
        0x6200,
        new StatusProperties(
            "Successful execution, response data postponed until session closing.", null));
    STATUS_TABLE = m;
  }

  /**
   * Constructor to build a parser of the SvUndebit command response.
   *
   * @param response response to parse
   * @param builder the reference to the builder that created this parser
   * @since 0.9
   */
  public SvUndebitRespPars(ApduResponse response, SvUndebitCmdBuild builder) {
    super(response, builder);
    /* the permitted lengths are 0 (in session), 3 (not 3.2) or 6 (3.2) */
    if (response.getDataOut().length != 0
        && response.getDataOut().length != 3
        && response.getDataOut().length != 6) {
      throw new IllegalStateException("Bad length in response to SV Undebit command.");
    }
  }

  /**
   * Gets the SV signature. <br>
   * The signature can be empty here in the case of a secure session where the transmission of the
   * signature is postponed until the end of the session.
   *
   * @return a byte array containing the signature
   * @since 0.9
   */
  public byte[] getSignatureLo() {
    return getApduResponse().getDataOut();
  }

  /**
   * {@inheritDoc}
   *
   * @since 0.9
   */
  @Override
  protected Map<Integer, StatusProperties> getStatusTable() {
    return STATUS_TABLE;
  }
}
