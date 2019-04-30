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
package org.eclipse.keyple.core.seproxy.protocol;

/**
 * Interface to be implemented by plugin creators to manage protocol parameter enum lists
 */
public interface SeProtocolPluginSetting {
    /**
     * @return the protocol identifier
     */
    SeProtocol getFlag();

    /**
     * This value is used to identify the protocol at low level (e.g. ATR regex [PC/SC] or tech name
     * [Android]).
     * <p>
     * However, this value depends on the plugin and can take any other form for other plugins.
     * 
     * @return a string
     */
    String getValue();
}
