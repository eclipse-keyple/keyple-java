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
import org.eclipse.keyple.calypso.command.po.CalypsoPoCommand;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.parser.security.AbstractOpenSessionRespPars;
import org.eclipse.keyple.calypso.command.po.parser.security.OpenSession24RespPars;
import org.eclipse.keyple.core.card.message.ApduResponse;

/**
 * Builds the Open Session command for a PO revision 2.4.
 *
 * @since 0.9
 */
public final class OpenSession24CmdBuild
    extends AbstractOpenSessionCmdBuild<AbstractOpenSessionRespPars> {

  // Construction arguments used for parsing
  private final int sfi;
  private final int recordNumber;

  /**
   * Instantiates a new AbstractOpenSessionCmdBuild.
   *
   * @param keyIndex the key index
   * @param samChallenge the sam challenge returned by the SAM Get Challenge APDU command
   * @param sfi the sfi to select
   * @param recordNumber the record number to read
   * @throws IllegalArgumentException - if key index is 0 (rev 2.4)
   * @throws IllegalArgumentException - if the request is inconsistent
   * @since 0.9
   */
  public OpenSession24CmdBuild(byte keyIndex, byte[] samChallenge, int sfi, int recordNumber) {
    super(PoRevision.REV2_4);

    if (keyIndex == 0x00) {
      throw new IllegalArgumentException("Key index can't be null for rev 2.4!");
    }

    this.sfi = sfi;
    this.recordNumber = recordNumber;

    byte p1 = (byte) (0x80 + (recordNumber * 8) + keyIndex);
    byte p2 = (byte) (sfi * 8);
    /*
     * case 4: this command contains incoming and outgoing data. We define le = 0, the actual
     * length will be processed by the lower layers.
     */
    byte le = 0;

    this.request =
        setApduRequest(
            PoClass.LEGACY.getValue(),
            CalypsoPoCommand.getOpenSessionForRev(PoRevision.REV2_4),
            p1,
            p2,
            samChallenge,
            le);

    if (logger.isDebugEnabled()) {
      String extraInfo =
          String.format("KEYINDEX=%d, SFI=%02X, REC=%d", keyIndex, sfi, recordNumber);
      this.addSubName(extraInfo);
    }
  }

  /** {@inheritDoc} */
  @Override
  public OpenSession24RespPars createResponseParser(ApduResponse apduResponse) {
    return new OpenSession24RespPars(apduResponse, this);
  }

  /**
   * {@inheritDoc}
   *
   * <p>This command can't be executed in session and therefore doesn't uses the session buffer.
   *
   * @return false
   * @since 0.9
   */
  @Override
  public boolean isSessionBufferUsed() {
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public int getSfi() {
    return sfi;
  }

  /** {@inheritDoc} */
  @Override
  public int getRecordNumber() {
    return recordNumber;
  }
}
