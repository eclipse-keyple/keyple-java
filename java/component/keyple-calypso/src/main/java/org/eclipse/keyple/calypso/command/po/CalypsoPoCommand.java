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
package org.eclipse.keyple.calypso.command.po;

import org.eclipse.keyple.core.command.CardCommand;

public enum CalypsoPoCommand implements CardCommand {

  /** The po get data. */
  GET_DATA_FCI("Get Data'FCI'", (byte) 0xCA),

  /** The po open session. */
  OPEN_SESSION_10("Open Secure Session V1", (byte) 0x8A),

  /** The po open session. */
  OPEN_SESSION_24("Open Secure Session V2.4", (byte) 0x8A),

  /** The po open session. */
  OPEN_SESSION_31("Open Secure Session V3.1", (byte) 0x8A),

  /** The po open session. */
  OPEN_SESSION_32("Open Secure Session V3.2", (byte) 0x8A),

  /** The po close session. */
  CLOSE_SESSION("Close Secure Session", (byte) 0x8E),

  /** The po read records. */
  READ_RECORDS("Read Records", (byte) 0xB2),

  /** The po update record. */
  UPDATE_RECORD("Update Record", (byte) 0xDC),

  /** The po write record. */
  WRITE_RECORD("Write Record", (byte) 0xD2),

  /** The po append record. */
  APPEND_RECORD("Append Record", (byte) 0xE2),

  /** The po get challenge. */
  GET_CHALLENGE("Get Challenge", (byte) 0x84),

  /** The po increase counter. */
  INCREASE("Increase", (byte) 0x32),

  /** The po decrease counter. */
  DECREASE("Decrease", (byte) 0x30),

  /** The po decrease counter. */
  SELECT_FILE("Select File", (byte) 0xA4),

  /* The po change key */
  CHANGE_KEY("Change Key", (byte) 0xD8),

  /* The po verify PIN */
  VERIFY_PIN("Verify PIN", (byte) 0x20),

  /* The po get data for traceability */
  GET_DATA_TRACE("Get Data'Trace'", (byte) 0xCA),

  /* The po SV Get */
  SV_GET("SV Get", (byte) 0x7C),

  /* The po SV Debit */
  SV_DEBIT("SV Debit", (byte) 0xBA),

  /* The po SV Reload */
  SV_RELOAD("SV Reload", (byte) 0xB8),

  /* The po SV Undebit */
  SV_UNDEBIT("SV Undebit", (byte) 0xBC),

  /* The po Invalidate */
  INVALIDATE("Invalidate", (byte) 0x04),

  /* The po Rehabilitate */
  REHABILITATE("Invalidate", (byte) 0x44);

  /** The name. */
  private final String name;

  /** The instruction byte. */
  private final byte instructionByte;

  /**
   * The generic constructor of CalypsoCommands.
   *
   * @param name the name
   * @param instructionByte the instruction byte
   */
  CalypsoPoCommand(String name, byte instructionByte) {
    this.name = name;
    this.instructionByte = instructionByte;
  }

  /**
   * Gets the name.
   *
   * @return the command name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the instruction byte.
   *
   * @return the value of INS byte
   */
  public byte getInstructionByte() {
    return instructionByte;
  }

  /**
   * Get the right open-session command for a given {@link PoRevision}
   *
   * @param rev Command revision
   * @return Returned command
   */
  public static CalypsoPoCommand getOpenSessionForRev(PoRevision rev) {
    switch (rev) {
      case REV1_0:
        return OPEN_SESSION_10;
      case REV2_4:
        return OPEN_SESSION_24;
      case REV3_1:
      case REV3_1_CLAP:
        return OPEN_SESSION_31;
      case REV3_2:
        return OPEN_SESSION_32;
      default:
        throw new IllegalStateException("Any revision should have a matching command");
    }
  }
}
