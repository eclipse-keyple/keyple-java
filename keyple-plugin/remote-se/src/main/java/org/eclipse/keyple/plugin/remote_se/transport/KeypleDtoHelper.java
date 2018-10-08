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
 * Utility class to manipulate KeypleDto
 */
public class KeypleDtoHelper {

    public final static String READER_TRANSMIT = "reader_transmit";
    public final static String READER_CONNECT = "reader_connect";
    public final static String READER_DISCONNECT = "reader_disconnect";
    public final static String READER_EVENT = "reader_event";

    static public String toJson(KeypleDto keypleDto) {
        return JsonParser.getGson().toJson(keypleDto);
    }

    static public KeypleDto fromJson(String json) {
        return JsonParser.getGson().fromJson(json, KeypleDto.class);
    }

    static public KeypleDto fromJsonObject(JsonObject jsonObj) {
        return JsonParser.getGson().fromJson(jsonObj, KeypleDto.class);
    }

    static public KeypleDto NoResponse() {
        return new KeypleDto("", "", false);
    }

    static public KeypleDto ErrorDTO() {
        return new KeypleDto("ERROR", "", false);// todo statuscode
    }

    static public KeypleDto ACK() {
        return new KeypleDto("ACK", "", false);// todo statuscode
    }

    static public Boolean isNoResponse(KeypleDto dto) {
        return dto.getAction().isEmpty();
    }

    static public Boolean isKeypleDTO(String json) {
        return isKeypleDTO(JsonParser.getGson().toJson(json));
    }

    static public Boolean isKeypleDTO(JsonObject json) {
        return json.has("action");
    }


}
