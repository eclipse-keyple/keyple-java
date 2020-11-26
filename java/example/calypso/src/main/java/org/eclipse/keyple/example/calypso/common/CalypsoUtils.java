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

import static org.eclipse.keyple.calypso.transaction.PoTransaction.SessionSetting.AccessLevel;

import org.eclipse.keyple.calypso.transaction.CalypsoSam;
import org.eclipse.keyple.calypso.transaction.PoSecuritySettings;
import org.eclipse.keyple.core.card.selection.CardResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CalypsoUtils {

  private static final Logger logger = LoggerFactory.getLogger(CalypsoUtils.class);

  /**
   * Define the security parameters to provide when creating {@link
   * org.eclipse.keyple.calypso.transaction.PoTransaction}
   *
   * @param samResource sam resource to build Po Security from
   * @return PoSecuritySettings settings the set the security on the PO
   */
  public static PoSecuritySettings getSecuritySettings(CardResource<CalypsoSam> samResource) {

    // The default KIF values for personalization, loading and debiting
    final byte DEFAULT_KIF_PERSO = (byte) 0x21;
    final byte DEFAULT_KIF_LOAD = (byte) 0x27;
    final byte DEFAULT_KIF_DEBIT = (byte) 0x30;
    // The default key record number values for personalization, loading and debiting
    // The actual value should be adjusted.
    final byte DEFAULT_KEY_RECORD_NUMBER_PERSO = (byte) 0x01;
    final byte DEFAULT_KEY_RECORD_NUMBER_LOAD = (byte) 0x02;
    final byte DEFAULT_KEY_RECORD_NUMBER_DEBIT = (byte) 0x03;
    /* define the security parameters to provide when creating PoTransaction */
    return new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)
        .sessionDefaultKif(AccessLevel.SESSION_LVL_PERSO, DEFAULT_KIF_PERSO)
        .sessionDefaultKif(AccessLevel.SESSION_LVL_LOAD, DEFAULT_KIF_LOAD)
        .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT)
        .sessionDefaultKeyRecordNumber(
            AccessLevel.SESSION_LVL_PERSO, DEFAULT_KEY_RECORD_NUMBER_PERSO)
        .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_LOAD, DEFAULT_KEY_RECORD_NUMBER_LOAD)
        .sessionDefaultKeyRecordNumber(
            AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KEY_RECORD_NUMBER_DEBIT)
        .build();
  }
}
