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
package org.eclipse.keyple.calypso.command.po.builder.security;

import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.AbstractPoCommandBuilder;
import org.eclipse.keyple.calypso.command.po.CalypsoPoCommand;
import org.eclipse.keyple.calypso.command.po.parser.security.PoGetChallengeRespPars;
import org.eclipse.keyple.core.card.message.ApduResponse;

/**
 * Builds the Get Challenge APDU command.
 *
 * @since 0.9
 */
public final class PoGetChallengeCmdBuild extends AbstractPoCommandBuilder<PoGetChallengeRespPars> {

  private static final CalypsoPoCommand command = CalypsoPoCommand.GET_CHALLENGE;

  /**
   * Instantiates a new PoGetChallengeCmdBuild.
   *
   * @param poClass indicates which CLA byte should be used for the Apdu
   * @since 0.9
   */
  public PoGetChallengeCmdBuild(PoClass poClass) {
    super(command, null);

    byte p1 = (byte) 0x00;
    byte p2 = (byte) 0x00;
    byte le = (byte) 0x08;

    this.request = setApduRequest(poClass.getValue(), command, p1, p2, null, le);
  }

  /**
   * {@inheritDoc}
   *
   * @since 0.9
   */
  @Override
  public PoGetChallengeRespPars createResponseParser(ApduResponse apduResponse) {
    return new PoGetChallengeRespPars(apduResponse, this);
  }

  /**
   * {@inheritDoc}
   *
   * <p>This command doesn't modify the contents of the PO and therefore doesn't uses the session
   * buffer.
   *
   * @return false
   * @since 0.9
   */
  @Override
  public boolean isSessionBufferUsed() {
    return false;
  }
}
