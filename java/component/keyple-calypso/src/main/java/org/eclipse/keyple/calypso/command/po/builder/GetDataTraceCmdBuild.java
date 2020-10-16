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
package org.eclipse.keyple.calypso.command.po.builder;

import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.AbstractPoCommandBuilder;
import org.eclipse.keyple.calypso.command.po.CalypsoPoCommand;
import org.eclipse.keyple.calypso.command.po.parser.GetDataTraceRespPars;
import org.eclipse.keyple.core.reader.message.ApduResponse;

/**
 * This class provides the dedicated constructor to build the Get data APDU commands.
 *
 * <p>This command can not be sent in session because it would generate a 6Cxx status in contact
 * mode and thus make calculation of the digest impossible.
 */
public final class GetDataTraceCmdBuild extends AbstractPoCommandBuilder<GetDataTraceRespPars> {

  private static final CalypsoPoCommand command = CalypsoPoCommand.GET_DATA_TRACE;

  /**
   * Instantiates a new GetDataTraceCmdBuild.
   *
   * @param poClass indicates which CLA byte should be used for the Apdu
   */
  public GetDataTraceCmdBuild(PoClass poClass) {
    super(command, null);

    this.request =
        setApduRequest(poClass.getValue(), command, (byte) 0x01, (byte) 0x85, null, (byte) 0x00);
  }

  @Override
  public GetDataTraceRespPars createResponseParser(ApduResponse apduResponse) {
    return new GetDataTraceRespPars(apduResponse, this);
  }

  /**
   * This command doesn't modify the contents of the PO and therefore doesn't uses the session
   * buffer.
   *
   * @return false
   */
  @Override
  public boolean isSessionBufferUsed() {
    return false;
  }
}
