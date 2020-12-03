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
package org.eclipse.keyple.calypso.command.sam.parser.security;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.calypso.command.sam.AbstractSamResponseParser;
import org.eclipse.keyple.calypso.command.sam.builder.security.SamReadEventCounterCmdBuild;
import org.eclipse.keyple.calypso.command.sam.exception.CalypsoSamCounterOverflowException;
import org.eclipse.keyple.calypso.command.sam.exception.CalypsoSamIllegalParameterException;
import org.eclipse.keyple.core.card.message.ApduResponse;

/**
 * Parses the Read event counter.
 *
 * @since 0.9
 */
public class SamReadEventCounterRespPars extends AbstractSamResponseParser {

  private static final Map<Integer, StatusProperties> STATUS_TABLE;

  static {
    Map<Integer, StatusProperties> m =
        new HashMap<Integer, StatusProperties>(AbstractSamResponseParser.STATUS_TABLE);
    m.put(
        0x6900,
        new StatusProperties(
            "An event counter cannot be incremented.", CalypsoSamCounterOverflowException.class));
    m.put(0x6A00, new StatusProperties("Incorrect P2.", CalypsoSamIllegalParameterException.class));
    m.put(0x6200, new StatusProperties("Correct execution with warning: data not signed.", null));
    STATUS_TABLE = m;
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

  /**
   * Instantiates a new SamReadEventCounterRespPars.
   *
   * @param response of the SamReadEventCounterRespPars
   * @param builder the reference to the builder that created this parser
   * @since 0.9
   */
  public SamReadEventCounterRespPars(ApduResponse response, SamReadEventCounterCmdBuild builder) {
    super(response, builder);
  }

  /**
   * Gets the key parameters.
   *
   * @return the counter data (Value or Record)
   * @since 0.9
   */
  public byte[] getCounterData() {
    return isSuccessful() ? response.getDataOut() : null;
  }
}
