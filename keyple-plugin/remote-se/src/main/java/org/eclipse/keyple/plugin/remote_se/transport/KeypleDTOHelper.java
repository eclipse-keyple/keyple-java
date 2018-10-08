/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.remote_se.transport;

import org.eclipse.keyple.plugin.remote_se.transport.json.JsonParser;
import com.google.gson.JsonObject;

/**
 * Utility class to manipulate KeypleDTO
 */
public class KeypleDTOHelper {

    public final static String READER_TRANSMIT = "reader_transmit";
    public final static String READER_CONNECT = "reader_connect";
    public final static String READER_DISCONNECT = "reader_disconnect";
    public final static String READER_EVENT = "reader_event";

    static public int generateHash(KeypleDTO keypleDTO) {
        return keypleDTO.hashCode();
    }

    static public Boolean verifyHash(KeypleDTO keypleDTO, int hash) {
        return keypleDTO.hashCode() == hash;
    }

    static public String toJson(KeypleDTO keypleDTO) {
        return JsonParser.getGson().toJson(keypleDTO);
    }

    static public KeypleDTO fromJson(String json) {
        return JsonParser.getGson().fromJson(json, KeypleDTO.class);
    }

    static public KeypleDTO fromJsonObject(JsonObject jsonObj) {
        return JsonParser.getGson().fromJson(jsonObj, KeypleDTO.class);
    }

    static public KeypleDTO NoResponse() {
        return new KeypleDTO("", "", false);
    }

    static public KeypleDTO ErrorDTO() {
        return new KeypleDTO("ERROR", "", false);// todo statuscode
    }

    static public KeypleDTO ACK() {
        return new KeypleDTO("ACK", "", false);// todo statuscode
    }

    static public Boolean isNoResponse(KeypleDTO dto) {
        return dto.getAction().isEmpty();
    }

    static public Boolean isKeypleDTO(String json) {
        return isKeypleDTO(JsonParser.getGson().toJson(json));
    }

    static public Boolean isKeypleDTO(JsonObject json) {
        return json.has("action");
    }


}
