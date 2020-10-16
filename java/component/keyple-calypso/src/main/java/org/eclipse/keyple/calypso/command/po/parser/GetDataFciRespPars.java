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

import static org.eclipse.keyple.core.util.bertlv.Tag.TagType.CONSTRUCTED;
import static org.eclipse.keyple.core.util.bertlv.Tag.TagType.PRIMITIVE;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.calypso.command.po.AbstractPoResponseParser;
import org.eclipse.keyple.calypso.command.po.builder.GetDataFciCmdBuild;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoDataAccessException;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoIllegalParameterException;
import org.eclipse.keyple.core.card.command.AbstractApduResponseParser;
import org.eclipse.keyple.core.card.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.core.util.bertlv.TLV;
import org.eclipse.keyple.core.util.bertlv.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts information from the FCI data returned is response to the selection application command.
 *
 * <p>Provides getter methods for all relevant information.
 */
public final class GetDataFciRespPars extends AbstractPoResponseParser {
  private static final Logger logger = LoggerFactory.getLogger(GetDataFciRespPars.class);

  private static final Map<Integer, StatusProperties> STATUS_TABLE;

  static {
    Map<Integer, StatusProperties> m =
        new HashMap<Integer, StatusProperties>(AbstractApduResponseParser.STATUS_TABLE);
    m.put(
        0x6A88,
        new StatusProperties(
            "Data object not found (optional mode not available).",
            CalypsoPoDataAccessException.class));
    m.put(
        0x6B00,
        new StatusProperties(
            "P1 or P2 value not supported (<>004fh, 0062h, 006Fh, 00C0h, 00D0h, 0185h and 5F52h, according to "
                + "available optional modes).",
            CalypsoPoIllegalParameterException.class));
    m.put(
        0x6283,
        new StatusProperties("Successful execution, FCI request and DF is invalidated.", null));
    STATUS_TABLE = m;
  }

  @Override
  protected Map<Integer, StatusProperties> getStatusTable() {
    return STATUS_TABLE;
  }

  /* BER-TLV tags definitions */
  /* FCI Template: application class, constructed, tag number Fh => tag field 6Fh */
  private static final Tag TAG_FCI_TEMPLATE = new Tag(0x0F, Tag.APPLICATION, CONSTRUCTED, 1);
  /* DF Name: context-specific class, primitive, tag number 4h => tag field 84h */
  private static final Tag TAG_DF_NAME = new Tag(0x04, Tag.CONTEXT, PRIMITIVE, 1);
  /*
   * FCI Proprietary Template: context-specific class, constructed, tag number 5h => tag field A5h
   */
  private static final Tag TAG_FCI_PROPRIETARY_TEMPLATE =
      new Tag(0x05, Tag.CONTEXT, CONSTRUCTED, 1);
  /*
   * FCI Issuer Discretionary Data: context-specific class, constructed, tag number Ch => tag
   * field BF0Ch
   */
  private static final Tag TAG_FCI_ISSUER_DISCRETIONARY_DATA =
      new Tag(0x0C, Tag.CONTEXT, CONSTRUCTED, 2);
  /* Application Serial Number: private class, primitive, tag number 7h => tag field C7h */
  private static final Tag TAG_APPLICATION_SERIAL_NUMBER = new Tag(0x07, Tag.PRIVATE, PRIMITIVE, 1);
  /* Discretionary Data: application class, primitive, tag number 13h => tag field 53h */
  private static final Tag TAG_DISCRETIONARY_DATA = new Tag(0x13, Tag.APPLICATION, PRIMITIVE, 1);

  /** attributes result of th FCI parsing */
  private boolean isDfInvalidated = false;

  private boolean isValidCalypsoFCI = false;

  private byte[] dfName = null;
  private byte[] applicationSN = null;
  private byte[] discretionaryData = null;

  /**
   * Instantiates a new GetDataFciRespPars from the ApduResponse to a selection application command.
   *
   * <p>The expected FCI structure of a Calypso PO follows this scheme: <code>
   * T=6F L=XX (C)                FCI Template
   *      T=84 L=XX (P)           DF Name
   *      T=A5 L=22 (C)           FCI Proprietary Template
   *           T=BF0C L=19 (C)    FCI Issuer Discretionary Data
   *                T=C7 L=8 (P)  Application Serial Number
   *                T=53 L=7 (P)  Discretionary Data (Startup Information)
   * </code>
   *
   * <p>The ApduResponse provided in argument is parsed according to the above expected structure.
   *
   * <p>DF Name, Application Serial Number and Startup Information are extracted.
   *
   * <p>The 7-byte startup information field is also split into 7 private field made available
   * through dedicated getter methods.
   *
   * <p>All fields are pre-initialized to handle the case where the parsing fails.
   *
   * <p>
   *
   * @param response the select application response from Get Data APDU command
   * @param builder the reference to the builder that created this parser
   */
  public GetDataFciRespPars(ApduResponse response, GetDataFciCmdBuild builder) {
    super(response, builder);
    TLV tlv;

    /* check the command status to determine if the DF has been invalidated */
    if (response.getStatusCode() == 0x6283) {
      logger.debug(
          "The response to the select application command status word indicates that the DF has been invalidated.");
      isDfInvalidated = true;
    }

    /* parse the raw data with the help of the TLV class */
    try {
      /* init TLV object with the raw data and extract the FCI Template */
      final byte[] responseData = response.getBytes();
      tlv = new TLV(responseData);

      /* Get the FCI template */
      if (!tlv.parse(TAG_FCI_TEMPLATE, 0)) {
        logger.error("FCI parsing error: FCI template tag not found.");
        return;
      }

      /* Get the DF Name */
      if (!tlv.parse(TAG_DF_NAME, tlv.getPosition())) {
        logger.error("FCI parsing error: DF name tag not found.");
        return;
      }

      dfName = tlv.getValue();

      /* Get the FCI Proprietary Template */
      if (!tlv.parse(TAG_FCI_PROPRIETARY_TEMPLATE, tlv.getPosition())) {
        logger.error("FCI parsing error: FCI proprietary template tag not found.");
        return;
      }

      /* Get the FCI Issuer Discretionary Data */
      if (!tlv.parse(TAG_FCI_ISSUER_DISCRETIONARY_DATA, tlv.getPosition())) {
        logger.error("FCI parsing error: FCI issuer discretionary data tag not found.");
        return;
      }

      /* Get the Application Serial Number */
      if (!tlv.parse(TAG_APPLICATION_SERIAL_NUMBER, tlv.getPosition())) {
        logger.error("FCI parsing error: serial number tag not found.");
        return;
      }

      applicationSN = tlv.getValue();

      if (logger.isDebugEnabled()) {
        logger.debug("Application Serial Number = {}", ByteArrayUtil.toHex(applicationSN));
      }

      /* Get the Discretionary Data */
      if (!tlv.parse(TAG_DISCRETIONARY_DATA, tlv.getPosition())) {
        logger.error("FCI parsing error: discretionary data tag not found.");
        return;
      }

      discretionaryData = tlv.getValue();

      if (logger.isDebugEnabled()) {
        logger.debug("Discretionary Data = {}", ByteArrayUtil.toHex(discretionaryData));
      }

      /* all 3 main fields were retrieved */
      isValidCalypsoFCI = true;

    } catch (Exception e) {
      /* Silently ignore problems decoding TLV structure. Just log. */
      logger.debug("Error while parsing the FCI BER-TLV data structure ({})", e.getMessage());
    }
  }

  public boolean isValidCalypsoFCI() {
    return isValidCalypsoFCI;
  }

  public byte[] getDfName() {
    return dfName;
  }

  public byte[] getApplicationSerialNumber() {
    return applicationSN;
  }

  public byte[] getDiscretionaryData() {
    return discretionaryData;
  }

  public boolean isDfInvalidated() {
    return isDfInvalidated;
  }
}
