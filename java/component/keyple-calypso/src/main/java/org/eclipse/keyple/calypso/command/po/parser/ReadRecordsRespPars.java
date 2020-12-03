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
package org.eclipse.keyple.calypso.command.po.parser;

import java.util.*;
import org.eclipse.keyple.calypso.command.po.AbstractPoResponseParser;
import org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.calypso.command.po.exception.*;
import org.eclipse.keyple.core.card.command.AbstractApduResponseParser;
import org.eclipse.keyple.core.card.message.ApduResponse;

/**
 * Parses the Read Records response.
 *
 * @since 0.9
 */
public final class ReadRecordsRespPars extends AbstractPoResponseParser {

  private static final Map<Integer, StatusProperties> STATUS_TABLE;

  static {
    Map<Integer, StatusProperties> m =
        new HashMap<Integer, StatusProperties>(AbstractApduResponseParser.STATUS_TABLE);
    m.put(
        0x6981,
        new StatusProperties(
            "Command forbidden on binary files", CalypsoPoDataAccessException.class));
    m.put(
        0x6982,
        new StatusProperties(
            "Security conditions not fulfilled (PIN code not presented, encryption required).",
            CalypsoPoSecurityContextException.class));
    m.put(
        0x6985,
        new StatusProperties(
            "Access forbidden (Never access mode, stored value log file and a stored value operation was done during the current session).",
            CalypsoPoAccessForbiddenException.class));
    m.put(
        0x6986,
        new StatusProperties(
            "Command not allowed (no current EF)", CalypsoPoDataAccessException.class));
    m.put(0x6A82, new StatusProperties("File not found", CalypsoPoDataAccessException.class));
    m.put(
        0x6A83,
        new StatusProperties(
            "Record not found (record index is 0, or above NumRec",
            CalypsoPoDataAccessException.class));
    m.put(
        0x6B00,
        new StatusProperties("P2 value not supported", CalypsoPoIllegalParameterException.class));
    STATUS_TABLE = m;
  }

  /**
   * {@inheritDoc}
   *
   * @since 0.9
   */
  @Override
  protected Map<Integer, StatusProperties> getStatusTable() {
    return STATUS_TABLE;
  }

  /**
   * Instantiates a new ReadRecordsRespPars.
   *
   * @param apduResponse the response from the PO
   * @param builder the reference to the builder that created this parser
   * @since 0.9
   */
  public ReadRecordsRespPars(ApduResponse apduResponse, ReadRecordsCmdBuild builder) {
    super(apduResponse, builder);
  }

  /**
   * Parses the Apdu response as a data record (single or multiple), retrieves the records and place
   * it in an map.
   *
   * <p>The map index follows the PO specification, i.e. starts at 1 for the first record.
   *
   * <p>An empty map is returned if no data is available.
   *
   * @return a map of records
   * @since 0.9
   */
  public SortedMap<Integer, byte[]> getRecords() {
    SortedMap<Integer, byte[]> records = new TreeMap<Integer, byte[]>();
    if (((ReadRecordsCmdBuild) builder).getReadMode() == ReadRecordsCmdBuild.ReadMode.ONE_RECORD) {
      records.put(((ReadRecordsCmdBuild) builder).getFirstRecordNumber(), response.getDataOut());
    } else {
      byte[] apdu = response.getDataOut();
      int apduLen = apdu.length;
      int index = 0;
      while (apduLen > 0) {
        byte recordNb = apdu[index++];
        byte len = apdu[index++];
        records.put((int) recordNb, Arrays.copyOfRange(apdu, index, index + len));
        index = index + len;
        apduLen = apduLen - 2 - len;
      }
    }
    return records;
  }
}
