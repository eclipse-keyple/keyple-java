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
package org.eclipse.keyple.example.common.calypso.pc.transaction;

import static org.eclipse.keyple.calypso.transaction.PoTransaction.SessionSetting.AccessLevel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.eclipse.keyple.calypso.transaction.CalypsoSam;
import org.eclipse.keyple.calypso.transaction.PoSecuritySettings;
import org.eclipse.keyple.core.selection.SeResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CalypsoUtilities {
  private static final Logger logger = LoggerFactory.getLogger(CalypsoUtilities.class);

  private static Properties properties;

  static {
    properties = new Properties();

    String propertiesFileName = "config.properties";

    InputStream inputStream =
        CalypsoUtilities.class.getClassLoader().getResourceAsStream(propertiesFileName);

    try {
      if (inputStream != null) {
        properties.load(inputStream);
      } else {
        throw new FileNotFoundException("property file '" + propertiesFileName + "' not found!");
      }
    } catch (FileNotFoundException e) {
      logger.error("File not found exception: {}", e.getMessage());
    } catch (IOException e) {
      logger.error("IO exception: {}", e.getMessage());
    }
  }

  private CalypsoUtilities() {}

  public static PoSecuritySettings getSecuritySettings(SeResource<CalypsoSam> samResource) {

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
