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
package org.eclipse.keyple.calypso.command.sam.builder.security;

import org.eclipse.keyple.calypso.command.sam.AbstractSamCommandBuilder;
import org.eclipse.keyple.calypso.command.sam.CalypsoSamCommand;
import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.calypso.command.sam.parser.security.UnlockRespPars;
import org.eclipse.keyple.core.reader.message.ApduResponse;

/** Builder for the SAM Unlock APDU command. */
public class UnlockCmdBuild extends AbstractSamCommandBuilder<UnlockRespPars> {
  /** The command reference. */
  private static final CalypsoSamCommand command = CalypsoSamCommand.UNLOCK;

  /**
   * Builder constructor
   *
   * @param revision the SAM revision
   * @param unlockData the unlock data
   */
  public UnlockCmdBuild(SamRevision revision, byte[] unlockData) {
    super(command, null);
    if (revision != null) {
      this.defaultRevision = revision;
    }
    byte cla = this.defaultRevision.getClassByte();
    byte p1 = (byte) 0x00;
    byte p2 = (byte) 0x00;

    if (unlockData == null) {
      throw new IllegalArgumentException("Unlock data null!");
    }

    if (unlockData.length != 8 && unlockData.length != 16) {
      throw new IllegalArgumentException("Unlock data should be 8 ou 16 bytes long!");
    }

    request = setApduRequest(cla, command, p1, p2, unlockData, null);
  }

  @Override
  public UnlockRespPars createResponseParser(ApduResponse apduResponse) {
    return new UnlockRespPars(apduResponse, this);
  }
}
