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
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;

/**
 * Contains a set of parameters to identify the communication protocols supported by the PC/SC
 * readers.
 *
 * <p>The application can choose to get all the parameters at the same time or only a subset.
 *
 * <p>As they are based on the virtual ATR created by the reader, the protocol identification values
 * are provided as is and may vary from one reader and SE to another. <br>
 * Thus, it may be necessary to create a set of context-specific custom settings.
 */
public final class PcscProtocolSetting {

  private static final Map<SeProtocol, String> PCSC_PROTOCOL_SETTING;

  /* Associates a protocol and a string defining how to identify it (here a regex to be applied on the ATR) */
  static {
    Map<SeProtocol, String> map = new HashMap<SeProtocol, String>();

    map.put(
        SeCommonProtocols.PROTOCOL_ISO14443_4,
        "3B8880....................|3B8B80.*|3B8C800150.*|.*4F4D4141544C4153.*");
    map.put(SeCommonProtocols.PROTOCOL_B_PRIME, "3B8F8001805A0...................829000..");

    map.put(SeCommonProtocols.PROTOCOL_MIFARE_UL, "3B8F8001804F0CA0000003060300030000000068");

    map.put(SeCommonProtocols.PROTOCOL_MIFARE_CLASSIC, "3B8F8001804F0CA000000306030001000000006A");

    map.put(SeCommonProtocols.PROTOCOL_MIFARE_DESFIRE, "3B8180018080");

    map.put(SeCommonProtocols.PROTOCOL_MEMORY_ST25, "3B8F8001804F0CA000000306070007D0020C00B6");

    map.put(SeCommonProtocols.PROTOCOL_ISO7816_3, "3.*");

    PCSC_PROTOCOL_SETTING = Collections.unmodifiableMap(map);
  }

  private PcscProtocolSetting() {}

  /**
   * Returns the subset of the settings map corresponding to the provided set of {@link SeProtocol}.
   *
   * <p>This makes it possible to retrieve all desired protocol settings in a single operation.
   *
   * @param specificProtocols A {@link Set} of {@link SeProtocol} (should be not null)
   * @return A {@link Map}
   */
  public static Map<SeProtocol, String> getSpecificSettings(
      Set<SeCommonProtocols> specificProtocols) {

    Map<SeProtocol, String> map = new HashMap<SeProtocol, String>();

    for (SeCommonProtocols seCommonProtocols : specificProtocols) {
      map.put(seCommonProtocols, PCSC_PROTOCOL_SETTING.get(seCommonProtocols));
    }
    return map;
  }

  /**
   * Return the whole settings map
   *
   * @return A {@link Map}
   */
  public static Map<SeProtocol, String> getSettings() {
    return PCSC_PROTOCOL_SETTING;
  }
}
