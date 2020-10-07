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
 * List of protocols supported by PC/SC readers.
 *
 * @since 1.0
 */
public enum PcscSupportedProtocols {
  ISO_14443_4,
  CALYPSO_OLD_CARD_PRIME,
  MIFARE_ULTRA_LIGHT,
  MIFARE_CLASSIC,
  MIFARE_DESFIRE,
  PROTOCOL_MEMORY_ST25,
  ISO_7816_3,
  ISO_7816_3_T0,
  ISO_7816_3_T1
}
