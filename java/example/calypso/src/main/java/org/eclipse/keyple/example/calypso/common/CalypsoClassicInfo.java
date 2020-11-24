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
package org.eclipse.keyple.example.calypso.common;

/**
 * Helper class to provide specific elements to handle Calypso cards.
 *
 * <ul>
 *   <li>AID application selection (default Calypso AID)
 *   <li>SAM_C1_ATR_REGEX regular expression matching the expected C1 SAM ATR
 *   <li>Files infos (SFI, rec number, etc) for
 *       <ul>
 *         <li>Environment and Holder
 *         <li>Event Log
 *         <li>Contract List
 *         <li>Contracts
 *       </ul>
 * </ul>
 */
public final class CalypsoClassicInfo {
  /** AID: Keyple test kit profile 1, Application 2 */
  public static final String AID = "315449432E49434131";
  /// ** 1TIC.ICA AID */
  // public static final String AID = "315449432E494341";
  /** SAM C1 regular expression: platform, version and serial number values are ignored */
  public static final String SAM_C1_ATR_REGEX =
      "3B3F9600805A[0-9a-fA-F]{2}80C1[0-9a-fA-F]{14}829000";

  public static final String ATR_REV1_REGEX = "3B8F8001805A0A0103200311........829000..";

  public static final byte RECORD_NUMBER_1 = 1;
  public static final byte RECORD_NUMBER_2 = 2;
  public static final byte RECORD_NUMBER_3 = 3;
  public static final byte RECORD_NUMBER_4 = 4;

  public static final byte SFI_EnvironmentAndHolder = (byte) 0x07;
  public static final byte SFI_EventLog = (byte) 0x08;
  public static final byte SFI_ContractList = (byte) 0x1E;
  public static final byte SFI_Contracts = (byte) 0x09;

  public static final short LID_DF_RT = 0x2000;
  public static final short LID_EventLog = 0x2010;

  public static final String eventLog_dataFill =
      "00112233445566778899AABBCCDDEEFF00112233445566778899AABBCC";

  private CalypsoClassicInfo() {}
}
