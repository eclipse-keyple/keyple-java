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
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;

/**
 * This class contains all the parameters to identify the communication protocols supported by STUB
 * readers.
 *
 * <p>The application can choose to add all parameters or only a subset.
 */
public final class StubProtocolSetting {

  public static final Map<SeProtocol, String> STUB_PROTOCOL_SETTING;

  /** (private) */
  private StubProtocolSetting() {}

  /**
   * Associates a protocol and a string defining how to identify it (here a regex to be applied on
   * the ATR)
   */
  static {
    Map<SeProtocol, String> map = new HashMap<SeProtocol, String>();

    map.put(SeCommonProtocols.PROTOCOL_ISO14443_4, "PROTOCOL_ISO14443_4");

    map.put(SeCommonProtocols.PROTOCOL_B_PRIME, "PROTOCOL_B_PRIME");

    map.put(SeCommonProtocols.PROTOCOL_MIFARE_UL, "PROTOCOL_MIFARE_UL");

    map.put(SeCommonProtocols.PROTOCOL_MIFARE_CLASSIC, "PROTOCOL_MIFARE_CLASSIC");

    map.put(SeCommonProtocols.PROTOCOL_MIFARE_DESFIRE, "PROTOCOL_MIFARE_DESFIRE");

    map.put(SeCommonProtocols.PROTOCOL_MEMORY_ST25, "PROTOCOL_MEMORY_ST25");

    map.put(SeCommonProtocols.PROTOCOL_ISO7816_3, "PROTOCOL_ISO7816_3");

    STUB_PROTOCOL_SETTING = Collections.unmodifiableMap(map);
  }

  /**
   * Return a subset of the settings map
   *
   * @param specificProtocols subset of protocols
   * @return a settings map
   */
  public static Map<SeProtocol, String> getSpecificSettings(
      Set<SeCommonProtocols> specificProtocols) {
    Map<SeProtocol, String> map = new HashMap<SeProtocol, String>();
    for (SeCommonProtocols seCommonProtocols : specificProtocols) {
      map.put(seCommonProtocols, STUB_PROTOCOL_SETTING.get(seCommonProtocols));
    }
    return map;
  }

  /**
   * Return the whole settings map
   *
   * @return a settings map
   */
  public static Map<SeProtocol, String> getSettings() {
    return STUB_PROTOCOL_SETTING;
  }
}
