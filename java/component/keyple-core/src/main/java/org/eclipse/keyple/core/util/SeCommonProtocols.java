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
package org.eclipse.keyple.core.util;

/** This enum contains a non-exhaustive list of smartcard communication protocols. */
public enum SeCommonProtocols {

  /* ---- contactless standard / NFC compliant ------------- */
  PROTOCOL_ISO14443_4("ISO 14443-4"),

  PROTOCOL_ISO15693("ISO 15693 Type V"),

  /* ---- contactless proprietary NFC compliant ------------ */
  PROTOCOL_ISO14443_3A("ISO 14443-3 Type A"),

  PROTOCOL_ISO14443_3B("ISO 14443-3 Type B"),

  PROTOCOL_JIS_6319_4("JIS 6319-4 Felica"),

  PROTOCOL_NDEF("NFC NDEF TAG"),

  PROTOCOL_NDEF_FORMATABLE("NFC NDEF FORMATABLE"),

  PROTOCOL_NFC_BARCODE("NFC BARCODE"),

  PROTOCOL_MIFARE_UL("Mifare Ultra Light"),

  PROTOCOL_MIFARE_CLASSIC("Mifare Classic"),

  PROTOCOL_MIFARE_DESFIRE("Mifare Desfire"),

  /* ---- contactless proprietary not NFC compliant -------- */
  PROTOCOL_B_PRIME("Old Calypso B Prime"),

  PROTOCOL_MEMORY_ST25("Memory ST25"),

  /* ---- contacts ISO standard ---------------------------- */
  PROTOCOL_ISO7816_3("ISO 7816-3"),

  /* ---- contacts proprietary ---------------- */
  PROTOCOL_HSP("Old Calypso SAM HSP");

  private final String descriptor;

  /** Constructor */
  SeCommonProtocols(String descriptor) {
    this.descriptor = descriptor;
  }

  /**
   * Get the protocol name.
   *
   * @return A not empty String.
   */
  public String getDescriptor() {
    return descriptor;
  }
}
