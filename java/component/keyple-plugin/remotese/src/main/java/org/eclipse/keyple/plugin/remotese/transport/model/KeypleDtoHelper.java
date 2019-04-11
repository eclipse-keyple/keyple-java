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
package org.eclipse.keyple.plugin.remotese.transport.model;

import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import com.google.gson.JsonObject;

/**
 * Utility class to manipulate KeypleDto
 */
public class KeypleDtoHelper {


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
        return new KeypleDto("", "", false, "", "", "", "");
    }

    static public KeypleDto ExceptionDTO(String action, Throwable exception, String sessionId,
            String nativeReaderName, String virtualReaderName, String clientNodeId) {
        return new KeypleDto(action, JsonParser.getGson().toJson(exception), false, sessionId,
                nativeReaderName, virtualReaderName, clientNodeId);
    }

    static public Boolean isNoResponse(KeypleDto dto) {
        return dto == null || dto.getAction() == null || dto.getAction().isEmpty();
    }



    static public Boolean isKeypleDTO(JsonObject json) {
        return json.has("action");
    }

    static public Boolean containsException(KeypleDto keypleDto) {
        return keypleDto.getBody().contains("stackTrace");
    }


}
