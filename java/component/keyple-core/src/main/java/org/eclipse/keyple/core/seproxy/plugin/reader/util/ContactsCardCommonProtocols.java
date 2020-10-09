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
package org.eclipse.keyple.core.seproxy.plugin.reader.util;

/**
 * This enum contains a non-exhaustive list of contacts smartcard communication protocols.
 *
 * @since 1.0
 */
public enum ContactsCardCommonProtocols {
  /* ---- contacts ISO standard ---------------------------- */
  ISO_7816_3,
  ISO_7816_3_TO,
  ISO_7816_3_T1,

  /* ---- contacts proprietary old Calypso SAM ---------------- */
  CALYPSO_OLD_SAM_HSP; // High Speed Protocol
}
