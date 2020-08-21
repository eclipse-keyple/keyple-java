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
package org.eclipse.keyple.calypso.command.po.builder.security;

import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.AbstractPoCommandBuilder;
import org.eclipse.keyple.calypso.command.po.CalypsoPoCommand;
import org.eclipse.keyple.calypso.command.po.parser.security.RehabilitateRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;

/**
 * The Class {@link RehabilitateCmdBuild}. This class provides the dedicated constructor to build
 * the PO Rehabilitate command.
 */
public final class RehabilitateCmdBuild extends AbstractPoCommandBuilder<RehabilitateRespPars> {

  private static final CalypsoPoCommand command = CalypsoPoCommand.REHABILITATE;

  /**
   * Instantiates a new RehabilitateCmdBuild.
   *
   * @param poClass indicates which CLA byte should be used for the Apdu
   */
  public RehabilitateCmdBuild(PoClass poClass) {
    super(command, null);

    byte p1 = (byte) 0x00;
    byte p2 = (byte) 0x00;

    this.request = setApduRequest(poClass.getValue(), command, p1, p2, null, null);
  }

  @Override
  public RehabilitateRespPars createResponseParser(ApduResponse apduResponse) {
    return new RehabilitateRespPars(apduResponse, this);
  }

  /**
   * This command modified the contents of the PO and therefore uses the session buffer.
   *
   * @return true
   */
  @Override
  public boolean isSessionBufferUsed() {
    return true;
  }
}
