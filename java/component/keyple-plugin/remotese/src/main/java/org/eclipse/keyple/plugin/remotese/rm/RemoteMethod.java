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

public enum RemoteMethod {

    READER_TRANSMIT("reader_transmit"), READER_CONNECT("reader_connect"), READER_DISCONNECT(
            "reader_disconnect"), READER_EVENT(
                    "reader_event"), DEFAULT_SELECTION_REQUEST("default_selection_request");

    private String name;

    RemoteMethod(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }


    // ****** Reverse Lookup Implementation************//

    // Lookup table
    private static final Map<String, RemoteMethod> lookup = new HashMap<String, RemoteMethod>();

    // Populate the lookup table on loading time
    static {
        for (RemoteMethod env : RemoteMethod.values()) {
            lookup.put(env.getName(), env);
        }
    }

    // This method can be used for reverse lookup purpose
    public static RemoteMethod get(String name) {
        return lookup.get(name);
    }
}
