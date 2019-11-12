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
package org.eclipse.keyple.plugin.remotese.rm;

import java.util.HashMap;
import java.util.Map;

public enum RemoteMethodName {

    READER_TRANSMIT_SET("reader_transmitSet"),

    READER_TRANSMIT("reader_transmit"),

    READER_CONNECT("reader_connect"),

    READER_DISCONNECT("reader_disconnect"),

    READER_EVENT("reader_event"),

    DEFAULT_SELECTION_REQUEST("default_selection_request"),

    POOL_ALLOCATE_READER("pool_allocate_reader"),

    POOL_RELEASE_READER("pool_release_reader");

    private String name;

    RemoteMethodName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }


    // ****** Reverse Lookup Implementation************//

    // Lookup table
    private static final Map<String, RemoteMethodName> lookup =
            new HashMap<String, RemoteMethodName>();

    // Populate the lookup table on loading time
    static {
        for (RemoteMethodName env : RemoteMethodName.values()) {
            lookup.put(env.getName(), env);
        }
    }

    // This method can be used for reverse lookup purpose
    public static RemoteMethodName get(String name) {
        return lookup.get(name);
    }
}
