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
package org.eclipse.keyple.plugin.stub;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.eclipse.keyple.core.util.ContactsCardCommonProtocols;

/**
 * This class contains all the parameters to identify the communication protocols supported by STUB
 * readers.
 *
 * <p>The application can choose to add all parameters or only a subset.
 */
public final class StubProtocolSetting {

  public static final Map<String, String> STUB_PROTOCOL_SETTING;

  /** (private) */
  private StubProtocolSetting() {}

  /**
   * Associates a protocol and a string defining how to identify it (here a regex to be applied on
   * the ATR)
   */
  static {
    Map<String, String> map = new HashMap<String, String>();

    map.put(StubSupportedProtocols.ISO_14443_4.name(), "ISO_14443_4");

    map.put(StubSupportedProtocols.CALYPSO_OLD_CARD_PRIME.name(), "CALYPSO_OLD_CARD_PRIME");

    map.put(StubSupportedProtocols.MIFARE_ULTRA_LIGHT.name(), "MIFARE_ULTRA_LIGHT");

    map.put(StubSupportedProtocols.MIFARE_CLASSIC.name(), "MIFARE_CLASSIC");

    map.put(StubSupportedProtocols.MIFARE_DESFIRE.name(), "MIFARE_DESFIRE");

    map.put(StubSupportedProtocols.MEMORY_ST25.name(), "MEMORY_ST25");

    map.put(ContactsCardCommonProtocols.ISO_7816_3.name(), "PROTOCOL_ISO7816_3");

    STUB_PROTOCOL_SETTING = Collections.unmodifiableMap(map);
  }

  /**
   * Return a subset of the settings map
   *
   * @param specificProtocols subset of protocols
   * @return a settings map
   */
  public static Map<String, String> getSpecificSettings(
      Set<StubSupportedProtocols> specificProtocols) {
    Map<String, String> map = new HashMap<String, String>();
    for (StubSupportedProtocols seCommonProtocols : specificProtocols) {
      map.put(seCommonProtocols.name(), STUB_PROTOCOL_SETTING.get(seCommonProtocols));
    }
    return map;
  }

  /**
   * Return the whole settings map
   *
   * @return a settings map
   */
  public static Map<String, String> getSettings() {
    return STUB_PROTOCOL_SETTING;
  }
}
