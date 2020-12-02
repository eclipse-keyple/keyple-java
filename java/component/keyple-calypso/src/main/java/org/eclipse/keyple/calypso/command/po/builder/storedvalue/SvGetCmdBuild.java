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
package org.eclipse.keyple.calypso.command.po.builder.storedvalue;

import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.*;
import org.eclipse.keyple.calypso.command.po.parser.storedvalue.SvGetRespPars;
import org.eclipse.keyple.calypso.transaction.PoTransaction.SvSettings;
import org.eclipse.keyple.core.card.message.ApduResponse;

/**
 * Builds the SV Get command.
 *
 * @since 0.9
 */
public final class SvGetCmdBuild extends AbstractPoCommandBuilder<SvGetRespPars> {

  /** The command. */
  private static final CalypsoPoCommand command = CalypsoPoCommand.SV_GET;

  private final SvSettings.Operation svOperation;
  private final byte[] header;

  /**
   * Instantiates a new SvGetCmdBuild.
   *
   * @param poClass the PO class
   * @param poRevision the PO revision
   * @param svOperation the desired SV operation
   * @throws IllegalArgumentException - if the command is inconsistent
   * @since 0.9
   */
  public SvGetCmdBuild(PoClass poClass, PoRevision poRevision, SvSettings.Operation svOperation) {
    super(command, null);
    byte cla = poClass.getValue();
    byte p1 = poRevision == PoRevision.REV3_2 ? (byte) 0x01 : (byte) 0x00;
    byte p2 = svOperation == SvSettings.Operation.RELOAD ? (byte) 0x07 : (byte) 0x09;

    this.request = setApduRequest(cla, command, p1, p2, null, (byte) 0x00);
    if (logger.isDebugEnabled()) {
      this.addSubName(String.format("OPERATION=%s", svOperation.toString()));
    }
    header = new byte[4];
    header[0] = command.getInstructionByte();
    header[1] = p1;
    header[2] = p2;
    header[3] = (byte) 0x00;

    this.svOperation = svOperation;
  }

  /**
   * Gets the request SV operation (used to check the SV command sequence)
   *
   * @return the current SvSettings.Operation enum value
   * @since 0.9
   */
  public SvSettings.Operation getSvOperation() {
    return svOperation;
  }

  /** {@inheritDoc} */
  @Override
  public SvGetRespPars createResponseParser(ApduResponse apduResponse) {
    return new SvGetRespPars(header, apduResponse, this);
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
