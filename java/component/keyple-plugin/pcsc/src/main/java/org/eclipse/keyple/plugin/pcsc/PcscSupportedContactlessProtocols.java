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
 * List of contactless protocols supported by PC/SC readers.
 *
 * @since 1.0
 */
public enum PcscSupportedContactlessProtocols {
  ISO_14443_4,
  INNOVATRON_B_PRIME_CARD,
  MIFARE_ULTRA_LIGHT,
  MIFARE_CLASSIC,
  MIFARE_DESFIRE,
  MEMORY_ST25
}
