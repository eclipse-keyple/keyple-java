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
package org.eclipse.keyple.calypso.transaction;

import java.util.List;
import org.eclipse.keyple.calypso.command.sam.AbstractSamCommandBuilder;
import org.eclipse.keyple.calypso.command.sam.AbstractSamResponseParser;
import org.eclipse.keyple.calypso.command.sam.builder.security.UnlockCmdBuild;
import org.eclipse.keyple.calypso.command.sam.exception.CalypsoSamCommandException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoDesynchronizedExchangesException;
import org.eclipse.keyple.core.selection.AbstractSeSelectionRequest;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.seproxy.message.CardResponse;

/**
 * Specialized selection request to manage the specific characteristics of Calypso SAMs<br>
 * Beyond the creation of a {@link CalypsoSam} object, this class also allows to execute a command
 * to Unlock the SAM if unlockData are present in the {@link SamSelector}.<br>
 * This unlock command is currently the only one allowed during the SAM selection process.
 */
public class SamSelectionRequest
    extends AbstractSeSelectionRequest<
        AbstractSamCommandBuilder<? extends AbstractSamResponseParser>> {
  /**
   * Create a {@link SamSelectionRequest}
   *
   * @param samSelector the SAM selector
   */
  public SamSelectionRequest(SamSelector samSelector) {
    super(samSelector);
    byte[] unlockData = samSelector.getUnlockData();
    if (unlockData != null) {
      // a unlock data value has been set, let's add the unlock command to be executed
      // following the selection
      addCommandBuilder(
          new UnlockCmdBuild(samSelector.getTargetSamRevision(), samSelector.getUnlockData()));
    }
  }

  /**
   * Create a CalypsoSam object containing the selection data received from the plugin<br>
   * If an Unlock command has been prepared, its status is checked.
   *
   * @param cardResponse the card response received
   * @return a {@link CalypsoSam}
   * @throws CalypsoDesynchronizedExchangesException if the APDU SAM exchanges are out of sync
   * @throws CalypsoSamCommandException if the SAM has responded with an error status
   */
  @Override
  protected CalypsoSam parse(CardResponse cardResponse) {
    List<AbstractSamCommandBuilder<? extends AbstractSamResponseParser>> commandBuilders =
        getCommandBuilders();

    if (commandBuilders.size() == 1) {
      // an unlock command has been requested
      List<ApduResponse> apduResponses = cardResponse.getApduResponses();
      if (apduResponses == null) {
        throw new CalypsoDesynchronizedExchangesException(
            "Mismatch in the number of requests/responses");
      }
      // check the SAM response to the unlock command
      commandBuilders.get(0).createResponseParser(apduResponses.get(0)).checkStatus();
    }

    return new CalypsoSam(cardResponse);
  }
}
