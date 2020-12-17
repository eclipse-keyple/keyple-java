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
package org.eclipse.keyple.plugin.pcsc;

import java.util.*;
import org.eclipse.keyple.core.util.Assert;

/**
 * Contains a set of parameters to identify the communication protocols supported by the PC/SC
 * readers.
 *
 * <p>The application can choose to get all the parameters at the same time or only a subset.
 *
 * <p>As they are based on the virtual ATR created by the reader, the protocol identification values
 * are provided as is and may vary from one reader and one card to another. <br>
 * Thus, it may be necessary to create a set of context-specific custom settings using {@link
 * #setProtocolIdentificationRule(String, String)}.
 *
 * @since 0.9
 */
final class PcscProtocolSetting {

  private static final Map<String, String> settings = new HashMap<String, String>();

  /* Associates a protocol and a string defining how to identify it (here a regex to be applied on the ATR) */
  static {
    // contactless protocols
    settings.put(
        PcscSupportedContactlessProtocols.ISO_14443_4.name(),
        "3B8880....................|3B8B80.*|3B8C800150.*|.*4F4D4141544C4153.*");
    settings.put(
        PcscSupportedContactlessProtocols.INNOVATRON_B_PRIME_CARD.name(),
        "3B8F8001805A0...................829000..");
    settings.put(
        PcscSupportedContactlessProtocols.MIFARE_ULTRA_LIGHT.name(),
        "3B8F8001804F0CA0000003060300030000000068");
    settings.put(
        PcscSupportedContactlessProtocols.MIFARE_CLASSIC.name(),
        "3B8F8001804F0CA000000306030001000000006A");
    settings.put(PcscSupportedContactlessProtocols.MIFARE_DESFIRE.name(), "3B8180018080");
    settings.put(
        PcscSupportedContactlessProtocols.MEMORY_ST25.name(),
        "3B8F8001804F0CA000000306070007D0020C00B6");

    // contacts protocols
    settings.put(PcscSupportedContactProtocols.ISO_7816_3.name(), "3.*");
    settings.put(PcscSupportedContactProtocols.ISO_7816_3_T0.name(), "3.*");
    settings.put(PcscSupportedContactProtocols.ISO_7816_3_T1.name(), "3.*");
  }

  private PcscProtocolSetting() {}

  /**
   * Set a rule for the provided protocol in the settings Map.
   *
   * <p>If a rule already exists for the provided protocol, it is replaced.
   *
   * <p>If there is no rule for the provided protocol, it is added.
   *
   * @param readerProtocolName A not empty String.
   * @param rule A not empty String.
   * @since 1.0
   */
  static void setProtocolIdentificationRule(String readerProtocolName, String rule) {
    Assert.getInstance().notEmpty(readerProtocolName, "readerProtocolName").notEmpty(rule, "rule");
    settings.put(readerProtocolName, rule);
  }

  /**
   * Return the whole settings map
   *
   * @return A {@link Map}
   * @since 0.9
   */
  static Map<String, String> getSettings() {
    return settings;
  }
}
