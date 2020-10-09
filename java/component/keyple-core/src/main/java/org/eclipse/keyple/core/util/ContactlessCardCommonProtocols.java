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
package org.eclipse.keyple.core.util;

/**
 * This enum contains a non-exhaustive list of contactless smartcard communication protocols.
 *
 * @since 1.0
 */
public enum ContactlessCardCommonProtocols {

  /* ---- contactless standard ------------- */
  ISO_14443_4,

  /* ---- contactless  NFC compliant ------------- */
  NFC_A_ISO_14443_3A,
  NFC_B_ISO_14443_3B,
  NFC_F_JIS_6319_4,
  NFC_V_ISO_15693,

  /* ---- other contactless proprietary protocols -------- */
  CALYPSO_OLD_CARD_PRIME,
}
