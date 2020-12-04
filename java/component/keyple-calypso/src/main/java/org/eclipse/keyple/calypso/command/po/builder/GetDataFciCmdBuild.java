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
import org.eclipse.keyple.calypso.command.po.parser.GetDataFciRespPars;
import org.eclipse.keyple.core.card.message.ApduResponse;

/**
 * Builds the Get data APDU commands.
 *
 * <p>This command can not be sent in session because it would generate a 6Cxx status in contact
 * mode and thus make calculation of the digest impossible.
 *
 * @since 0.9
 */
public final class GetDataFciCmdBuild extends AbstractPoCommandBuilder<GetDataFciRespPars> {

  private static final CalypsoPoCommand command = CalypsoPoCommand.GET_DATA_FCI;

  /**
   * Instantiates a new GetDataFciCmdBuild.
   *
   * @param poClass indicates which CLA byte should be used for the Apdu
   * @since 0.9
   */
  public GetDataFciCmdBuild(PoClass poClass) {
    super(command, null);

    request =
        setApduRequest(poClass.getValue(), command, (byte) 0x00, (byte) 0x6F, null, (byte) 0x00);
  }

  /**
   * {@inheritDoc}
   *
   * @since 0.9
   */
  @Override
  public GetDataFciRespPars createResponseParser(ApduResponse apduResponse) {
    return new GetDataFciRespPars(apduResponse, this);
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
