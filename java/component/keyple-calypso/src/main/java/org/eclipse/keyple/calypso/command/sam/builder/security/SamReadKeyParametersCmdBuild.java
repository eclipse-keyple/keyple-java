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
import org.eclipse.keyple.calypso.command.sam.parser.security.SamReadKeyParametersRespPars;
import org.eclipse.keyple.core.card.message.ApduResponse;

/** Builder for the SAM Read Key Parameters APDU command. */
public class SamReadKeyParametersCmdBuild
    extends AbstractSamCommandBuilder<SamReadKeyParametersRespPars> {
  /** The command reference. */
  private static final CalypsoSamCommand command = CalypsoSamCommand.READ_KEY_PARAMETERS;

  public static final int MAX_WORK_KEY_REC_NUMB = 126;

  public enum SourceRef {
    WORK_KEY,
    SYSTEM_KEY
  }

  public enum NavControl {
    FIRST,
    NEXT
  }

  public SamReadKeyParametersCmdBuild(SamRevision revision) {

    super(command, null);
    if (revision != null) {
      this.defaultRevision = revision;
    }

    byte cla = this.defaultRevision.getClassByte();

    byte p2 = (byte) 0xE0;
    byte[] sourceKeyId = new byte[] {0x00, 0x00};

    request = setApduRequest(cla, command, (byte) 0x00, p2, sourceKeyId, (byte) 0x00);
  }

  public SamReadKeyParametersCmdBuild(SamRevision revision, byte kif) {

    super(command, null);
    if (revision != null) {
      this.defaultRevision = revision;
    }

    byte cla = this.defaultRevision.getClassByte();

    byte p2 = (byte) 0xC0;
    byte[] sourceKeyId = new byte[] {0x00, 0x00};

    sourceKeyId[0] = kif;

    request = setApduRequest(cla, command, (byte) 0x00, p2, sourceKeyId, (byte) 0x00);
  }

  public SamReadKeyParametersCmdBuild(SamRevision revision, byte kif, byte kvc) {

    super(command, null);
    if (revision != null) {
      this.defaultRevision = revision;
    }

    byte cla = this.defaultRevision.getClassByte();

    byte p2 = (byte) 0xF0;
    byte[] sourceKeyId = new byte[] {0x00, 0x00};

    sourceKeyId[0] = kif;
    sourceKeyId[1] = kvc;

    request = setApduRequest(cla, command, (byte) 0x00, p2, sourceKeyId, (byte) 0x00);
  }

  public SamReadKeyParametersCmdBuild(
      SamRevision revision, SourceRef sourceKeyRef, int recordNumber) {

    super(command, null);

    if (revision != null) {
      this.defaultRevision = revision;
    }

    if (recordNumber < 1 || recordNumber > MAX_WORK_KEY_REC_NUMB) {
      throw new IllegalArgumentException(
          "Record Number must be between 1 and " + MAX_WORK_KEY_REC_NUMB + ".");
    }

    byte cla = this.defaultRevision.getClassByte();

    byte p2 = 0x00;
    byte[] sourceKeyId = new byte[] {0x00, 0x00};

    switch (sourceKeyRef) {
      case WORK_KEY:
        p2 = (byte) recordNumber;
        break;

      case SYSTEM_KEY:
        p2 = (byte) (0xC0 + (byte) recordNumber);
        break;

      default:
        throw new IllegalStateException(
            "Unsupported SourceRef parameter " + sourceKeyRef.toString());
    }

    request = setApduRequest(cla, command, (byte) 0x00, p2, sourceKeyId, (byte) 0x00);
  }

  public SamReadKeyParametersCmdBuild(SamRevision revision, byte kif, NavControl navControl) {

    super(command, null);
    if (revision != null) {
      this.defaultRevision = revision;
    }

    byte cla = this.defaultRevision.getClassByte();

    byte p2 = 0x00;
    byte[] sourceKeyId = new byte[] {0x00, 0x00};

    switch (navControl) {
      case FIRST:
        p2 = (byte) 0xF8;
        break;

      case NEXT:
        p2 = (byte) 0xFA;
        break;

      default:
        throw new IllegalStateException(
            "Unsupported NavControl parameter " + navControl.toString());
    }

    sourceKeyId[0] = kif;

    request = setApduRequest(cla, command, (byte) 0x00, p2, sourceKeyId, (byte) 0x00);
  }

  @Override
  public SamReadKeyParametersRespPars createResponseParser(ApduResponse apduResponse) {
    return new SamReadKeyParametersRespPars(apduResponse, this);
  }
}
