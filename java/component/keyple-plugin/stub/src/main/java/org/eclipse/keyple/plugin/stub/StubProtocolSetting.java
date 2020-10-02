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
import org.eclipse.keyple.core.util.SeCommonProtocols;

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

    map.put(SeCommonProtocols.PROTOCOL_ISO14443_4.getDescriptor(), "PROTOCOL_ISO14443_4");

    map.put(SeCommonProtocols.PROTOCOL_B_PRIME.getDescriptor(), "PROTOCOL_B_PRIME");

    map.put(SeCommonProtocols.PROTOCOL_MIFARE_UL.getDescriptor(), "PROTOCOL_MIFARE_UL");

    map.put(SeCommonProtocols.PROTOCOL_MIFARE_CLASSIC.getDescriptor(), "PROTOCOL_MIFARE_CLASSIC");

    map.put(SeCommonProtocols.PROTOCOL_MIFARE_DESFIRE.getDescriptor(), "PROTOCOL_MIFARE_DESFIRE");

    map.put(SeCommonProtocols.PROTOCOL_MEMORY_ST25.getDescriptor(), "PROTOCOL_MEMORY_ST25");

    map.put(SeCommonProtocols.PROTOCOL_ISO7816_3.getDescriptor(), "PROTOCOL_ISO7816_3");

    STUB_PROTOCOL_SETTING = Collections.unmodifiableMap(map);
  }

  /**
   * Return a subset of the settings map
   *
   * @param specificProtocols subset of protocols
   * @return a settings map
   */
  public static Map<String, String> getSpecificSettings(Set<SeCommonProtocols> specificProtocols) {
    Map<String, String> map = new HashMap<String, String>();
    for (SeCommonProtocols seCommonProtocols : specificProtocols) {
      map.put(seCommonProtocols.getDescriptor(), STUB_PROTOCOL_SETTING.get(seCommonProtocols));
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
