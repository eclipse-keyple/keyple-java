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
package org.eclipse.keyple.example.common.generic;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;

/**
 * This class contains all the parameters to identify the communication protocols supported by a
 * custom reader.
 *
 * <p>The application can choose to add all parameters or only a subset.
 */
public class CustomPluginSetting {

  public static final Map<SeProtocol, String> CUSTOM_PROTOCOL_SETTING;

  /**
   * Associates a protocol and a string defining how to identify it (here a regex to be applied on
   * the ATR)
   */
  static {
    Map<SeProtocol, String> map = new HashMap<SeProtocol, String>();

    map.put(
        SeCommonProtocols.PROTOCOL_ISO14443_4,
        "3B8880....................|3B8C800150.*|.*4F4D4141544C4153.*");

    map.put(SeCommonProtocols.PROTOCOL_B_PRIME, "3B8F8001805A0A0103200311........829000..");

    CUSTOM_PROTOCOL_SETTING = Collections.unmodifiableMap(map);
  }

  /**
   * Return a subset of the settings map
   *
   * @param specificProtocols
   * @return a settings map
   */
  public static Map<SeProtocol, String> getSpecificSettings(
      EnumSet<SeCommonProtocols> specificProtocols) {
    Map<SeProtocol, String> map = new HashMap<SeProtocol, String>();
    for (SeCommonProtocols seCommonProtocols : specificProtocols) {
      map.put(seCommonProtocols, CUSTOM_PROTOCOL_SETTING.get(seCommonProtocols));
    }
    return map;
  }

  /**
   * Return the whole settings map
   *
   * @return a settings map
   */
  public static Map<SeProtocol, String> getAllSettings() {
    return CUSTOM_PROTOCOL_SETTING;
  }
}
