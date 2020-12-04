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

import org.eclipse.keyple.core.card.command.CardCommand;

/**
 * Defines all supported Calypso PO APDU commands.
 *
 * @since 0.9
 */
public enum CalypsoPoCommand implements CardCommand {

  /** get data. */
  GET_DATA_FCI("Get Data'FCI'", (byte) 0xCA),

  /** open session. */
  OPEN_SESSION_10("Open Secure Session V1", (byte) 0x8A),

  /** open session. */
  OPEN_SESSION_24("Open Secure Session V2.4", (byte) 0x8A),

  /** open session. */
  OPEN_SESSION_31("Open Secure Session V3.1", (byte) 0x8A),

  /** open session. */
  OPEN_SESSION_32("Open Secure Session V3.2", (byte) 0x8A),

  /** close session. */
  CLOSE_SESSION("Close Secure Session", (byte) 0x8E),

  /** read records. */
  READ_RECORDS("Read Records", (byte) 0xB2),

  /** update record. */
  UPDATE_RECORD("Update Record", (byte) 0xDC),

  /** write record. */
  WRITE_RECORD("Write Record", (byte) 0xD2),

  /** append record. */
  APPEND_RECORD("Append Record", (byte) 0xE2),

  /** get challenge. */
  GET_CHALLENGE("Get Challenge", (byte) 0x84),

  /** increase counter. */
  INCREASE("Increase", (byte) 0x32),

  /** decrease counter. */
  DECREASE("Decrease", (byte) 0x30),

  /** decrease counter. */
  SELECT_FILE("Select File", (byte) 0xA4),

  /** change key */
  CHANGE_KEY("Change Key", (byte) 0xD8),

  /** verify PIN */
  VERIFY_PIN("Verify PIN", (byte) 0x20),

  /** get data for traceability */
  GET_DATA_TRACE("Get Data'Trace'", (byte) 0xCA),

  /** SV Get */
  SV_GET("SV Get", (byte) 0x7C),

  /** SV Debit */
  SV_DEBIT("SV Debit", (byte) 0xBA),

  /** SV Reload */
  SV_RELOAD("SV Reload", (byte) 0xB8),

  /** SV Undebit */
  SV_UNDEBIT("SV Undebit", (byte) 0xBC),

  /** invalidate */
  INVALIDATE("Invalidate", (byte) 0x04),

  /** rehabilitate */
  REHABILITATE("Invalidate", (byte) 0x44);

  /** The command name. */
  private final String name;

  /** The instruction byte. */
  private final byte instructionByte;

  /**
   * The generic constructor of CalypsoCommands.
   *
   * @param name the name
   * @param instructionByte the instruction byte
   * @since 0.9
   */
  CalypsoPoCommand(String name, byte instructionByte) {
    this.name = name;
    this.instructionByte = instructionByte;
  }

  /**
   * Gets the name.
   *
   * @return A String
   * @since 0.9
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the instruction byte (INS).
   *
   * @return A byte
   * @since 0.9
   */
  public byte getInstructionByte() {
    return instructionByte;
  }

  /**
   * Get the open session command for a given {@link PoRevision}
   *
   * @param rev Command revision
   * @return Returned command
   * @since 0.9
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
