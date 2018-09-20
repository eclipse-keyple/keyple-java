/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.remote_se.transport;

import org.eclipse.keyple.plugin.remote_se.transport.json.SeProxyJsonParser;

public class KeypleDTOHelper {

    public static String READER_TRANSMIT = "reader_transmit";
    public static String READER_CONNECT = "reader_connect";
    public static String READER_DISCONNECT = "reader_disconnect";
    public static String READER_EVENT = "reader_event";

    static public int generateHash(KeypleDTO keypleDTO) {
        return keypleDTO.hashCode();
    }

    static public Boolean verifyHash(KeypleDTO keypleDTO, int hash) {
        return keypleDTO.hashCode() == hash;
    }

    static public String toJson(KeypleDTO keypleDTO) {
        return SeProxyJsonParser.getGson().toJson(keypleDTO);
    }

    static public KeypleDTO fromJson(String json) {
        return SeProxyJsonParser.getGson().fromJson(json, KeypleDTO.class);
    }

    static public KeypleDTO NoResponse() {
        return new KeypleDTO("", "", false);
    }

}
