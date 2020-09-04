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

import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;

/**
 * This class defines the constants useful for the configuration of the PC/SC readers.<br>
 * These constants are strings defining the keys (suffix _KEY) and values (suffix _VAL) to be used
 * with the setParameter method.
 */
public final class PcscReaderConstants {
  /** key to set the transmission mode used with the SEs */
  public static final String TRANSMISSION_MODE_KEY = "transmission_mode";
  /** contact transmission mode */
  public static final String TRANSMISSION_MODE_VAL_CONTACTS = TransmissionMode.CONTACTS.toString();
  /** contactless transmission mode */
  public static final String TRANSMISSION_MODE_VAL_CONTACTLESS =
      TransmissionMode.CONTACTLESS.toString();
  // The following parameters correspond to the PC/SC parameters provided by smartcard.io
  /** key to set the PC/SC protocol parameter */
  public static final String PROTOCOL_KEY = "protocol";
  /** PC/SC protocol T=0 */
  public static final String PROTOCOL_VAL_T0 = "T0";
  /** PC/SC protocol T=1 */
  public static final String PROTOCOL_VAL_T1 = "T1";
  /** PC/SC protocol T=CL */
  public static final String PROTOCOL_VAL_T_CL = "TCL";
  /** PC/SC protocol '*' */
  public static final String PROTOCOL_VAL_TX = "Tx";
  /** key to set the PC/SC sharing mode */
  public static final String MODE_KEY = "mode";
  /** PC/SC exclusive mode */
  public static final String MODE_VAL_EXCLUSIVE = "exclusive";
  /** PC/SC shared mode */
  public static final String MODE_VAL_SHARED = "shared";
  /** key to set the PC/SC action to perform when disconnecting */
  public static final String DISCONNECT_KEY = "disconnect";
  /** Reset the SE after disconnect */
  public static final String DISCONNECT_VAL_RESET = "reset";
  /** Unpower the SE after disconnect */
  public static final String DISCONNECT_VAL_UNPOWER = "unpower";
  /** Leave the SE after disconnect */
  public static final String DISCONNECT_VAL_LEAVE = "leave";
  /** Eject the SE after disconnect */
  public static final String DISCONNECT_VAL_EJECT = "eject";

  /** (private) */
  private PcscReaderConstants() {}
}
