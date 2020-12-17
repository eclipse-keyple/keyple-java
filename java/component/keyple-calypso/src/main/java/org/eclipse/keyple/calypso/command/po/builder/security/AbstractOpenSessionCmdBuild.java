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

import org.eclipse.keyple.calypso.command.po.AbstractPoCommandBuilder;
import org.eclipse.keyple.calypso.command.po.AbstractPoResponseParser;
import org.eclipse.keyple.calypso.command.po.CalypsoPoCommand;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.parser.security.AbstractOpenSessionRespPars;

/** Builds the Open Secure Session APDU command. */
public abstract class AbstractOpenSessionCmdBuild<T extends AbstractPoResponseParser>
    extends AbstractPoCommandBuilder<T> {

  /**
   * Instantiates a new AbstractOpenSessionCmdBuild.
   *
   * @param revision the revision of the PO
   * @throws IllegalArgumentException - if the key index is 0 and rev is 2.4
   * @throws IllegalArgumentException - if the request is inconsistent
   */
  public AbstractOpenSessionCmdBuild(PoRevision revision) {
    super(CalypsoPoCommand.getOpenSessionForRev(revision), null);
  }

  public static AbstractOpenSessionCmdBuild<AbstractOpenSessionRespPars> create(
      PoRevision revision,
      byte debitKeyIndex,
      byte[] sessionTerminalChallenge,
      int sfi,
      int recordNumber) {
    switch (revision) {
      case REV1_0:
        return new OpenSession10CmdBuild(
            debitKeyIndex, sessionTerminalChallenge, sfi, recordNumber);
      case REV2_4:
        return new OpenSession24CmdBuild(
            debitKeyIndex, sessionTerminalChallenge, sfi, recordNumber);
      case REV3_1:
      case REV3_1_CLAP:
        return new OpenSession31CmdBuild(
            debitKeyIndex, sessionTerminalChallenge, sfi, recordNumber);
      case REV3_2:
        return new OpenSession32CmdBuild(
            debitKeyIndex, sessionTerminalChallenge, sfi, recordNumber);
      default:
        throw new IllegalArgumentException("Revision " + revision + " isn't supported");
    }
  }

  /** @return the SFI of the file read while opening the secure session */
  public abstract int getSfi();

  /** @return the record number to read */
  public abstract int getRecordNumber();
}
