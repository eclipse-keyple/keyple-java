/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.seproxy.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * SeProtocol Map builder
 */
public class SeProtocolSetting {
    private Map<SeProtocol, String> protocolsMap = new HashMap<SeProtocol, String>();

    public SeProtocolSetting(Map<SeProtocol, String> seProtocolSettingList) {
        this.protocolsMap.putAll(seProtocolSettingList);
    }

    public SeProtocolSetting(SeProtocolSettingList seProtocolSetting) {
        this.protocolsMap.put(seProtocolSetting.getFlag(), seProtocolSetting.getValue());
    }

    public SeProtocolSetting(SeProtocolSettingList[] seProtocolSettingList) {
        for (SeProtocolSettingList seProtocolSetting : seProtocolSettingList) {
            this.protocolsMap.put(seProtocolSetting.getFlag(), seProtocolSetting.getValue());
        }
    }

    public Map<SeProtocol, String> getProtocolsMap() {
        return protocolsMap;
    }
}
