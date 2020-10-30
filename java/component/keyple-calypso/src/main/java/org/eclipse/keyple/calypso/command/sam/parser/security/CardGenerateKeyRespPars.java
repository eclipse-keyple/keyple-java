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
package org.eclipse.keyple.calypso.command.sam.parser.security;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.calypso.command.sam.AbstractSamResponseParser;
import org.eclipse.keyple.calypso.command.sam.builder.security.CardGenerateKeyCmdBuild;
import org.eclipse.keyple.calypso.command.sam.exception.CalypsoSamAccessForbiddenException;
import org.eclipse.keyple.calypso.command.sam.exception.CalypsoSamDataAccessException;
import org.eclipse.keyple.calypso.command.sam.exception.CalypsoSamIllegalParameterException;
import org.eclipse.keyple.calypso.command.sam.exception.CalypsoSamIncorrectInputDataException;
import org.eclipse.keyple.core.card.message.ApduResponse;

public class CardGenerateKeyRespPars extends AbstractSamResponseParser {

  private static final Map<Integer, StatusProperties> STATUS_TABLE;

  static {
    Map<Integer, StatusProperties> m =
        new HashMap<Integer, StatusProperties>(AbstractSamResponseParser.STATUS_TABLE);
    m.put(0x6700, new StatusProperties("Incorrect Lc.", CalypsoSamIllegalParameterException.class));
    m.put(
        0x6985,
        new StatusProperties(
            "Preconditions not satisfied.", CalypsoSamAccessForbiddenException.class));
    m.put(
        0x6A00,
        new StatusProperties("Incorrect P1 or P2", CalypsoSamIllegalParameterException.class));
    m.put(
        0x6A80,
        new StatusProperties(
            "Incorrect incoming data: unknown or incorrect format",
            CalypsoSamIncorrectInputDataException.class));
    m.put(
        0x6A83,
        new StatusProperties(
            "Record not found: ciphering key or key to cipher not found",
            CalypsoSamDataAccessException.class));
    STATUS_TABLE = m;
  }

  @Override
  protected Map<Integer, StatusProperties> getStatusTable() {
    return STATUS_TABLE;
  }

  /**
   * Instantiates a new CardGenerateKeyRespPars.
   *
   * @param response from the SAM
   * @param builder the reference to the builder that created this parser
   */
  public CardGenerateKeyRespPars(ApduResponse response, CardGenerateKeyCmdBuild builder) {
    super(response, builder);
  }

  /**
   * Gets the 32 bytes of ciphered data.
   *
   * @return the ciphered data byte array or null if the operation failed
   */
  public byte[] getCipheredData() {
    return isSuccessful() ? response.getDataOut() : null;
  }
}
