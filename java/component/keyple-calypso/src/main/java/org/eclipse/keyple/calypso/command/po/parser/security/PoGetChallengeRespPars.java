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

import org.eclipse.keyple.calypso.command.po.AbstractPoResponseParser;
import org.eclipse.keyple.calypso.command.po.builder.security.PoGetChallengeCmdBuild;
import org.eclipse.keyple.core.card.message.ApduResponse;

/** PO Get challenge response parser. See specs: Calypso / page 108 / 9.54 - Get challenge */
public final class PoGetChallengeRespPars extends AbstractPoResponseParser {

  /**
   * Instantiates a new PoGetChallengeRespPars.
   *
   * @param response the response from PO Get Challenge APDU Command
   * @param builder the reference to the builder that created this parser
   */
  public PoGetChallengeRespPars(ApduResponse response, PoGetChallengeCmdBuild builder) {
    super(response, builder);
  }

  public byte[] getPoChallenge() {
    return getApduResponse().getDataOut();
  }
}
