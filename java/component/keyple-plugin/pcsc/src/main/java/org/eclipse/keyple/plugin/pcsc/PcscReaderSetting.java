/* **************************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.plugin.pcsc;

/**
 * This class defines the constants useful for the configuration of the PC/SC readers.<br>
 * These constants are strings defining the keys (prefix KEY_) and values to be used with the
 * setParameter method.
 */
public final class PcscReaderSetting {
  /** key to set the transmission mode */
  public static final String KEY_TRANSMISSION_MODE = "transmission_mode";
  /** contact transmission mode */
  public static final String TRANSMISSION_MODE_CONTACTS = "contacts";
  /** contactless transmission mode */
  public static final String TRANSMISSION_MODE_CONTACTLESS = "contactless";
  // The following parameters correspond to the PC/SC parameters provided by smartcard.io
  /** key to set the PC/SC protocol parameter */
  public static final String KEY_PROTOCOL = "protocol";
  /** PC/SC protocol T=0 */
  public static final String PROTOCOL_T0 = "T0";
  /** PC/SC protocol T=1 */
  public static final String PROTOCOL_T1 = "T1";
  /** PC/SC protocol T=CL */
  public static final String PROTOCOL_T_CL = "TCL";
  /** PC/SC protocol '*' */
  public static final String PROTOCOL_TX = "Tx";
  /** key to set the PC/SC sharing mode */
  public static final String KEY_MODE = "mode";
  /** PC/SC exclusive mode */
  public static final String MODE_EXCLUSIVE = "exclusive";
  /** PC/SC shared mode */
  public static final String MODE_SHARED = "shared";
  /** key to set the PC/SC action to perform when disconnecting */
  public static final String KEY_DISCONNECT = "disconnect";
  /** Reset the SE after disconnect */
  public static final String DISCONNECT_RESET = "reset";
  /** Unpower the SE after disconnect */
  public static final String DISCONNECT_UNPOWER = "unpower";
  /** Leave the SE after disconnect */
  public static final String DISCONNECT_LEAVE = "leave";
  /** Eject the SE after disconnect */
  public static final String DISCONNECT_EJECT = "eject";

  /** (private) */
  private PcscReaderSetting() {}
}
