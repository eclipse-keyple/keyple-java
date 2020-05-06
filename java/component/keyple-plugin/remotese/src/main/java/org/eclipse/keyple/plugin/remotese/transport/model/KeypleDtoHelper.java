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
 * Utility class to manipulate KeypleDto. Use this class to build the different types of
 * {@link KeypleDto}
 * <ul>
 * <li>Request</li>
 * <li>Response</li>
 * <li>Notification</li>
 * <li>NoResponse</li>
 * <li>Exception</li>
 * </ul>
 */
public final class KeypleDtoHelper {

    /* ----------- Constructors Helpers */

    /**
     * Build a KeypleDto of type "Request"
     * 
     * @param action : name of the remote method
     * @param body : parameters of the remote method
     * @param sessionId : virtual session id (if exists)
     * @param nativeReaderName : name of the local reader
     * @param virtualReaderName : name of the virtual reader (if exists)
     * @param requesterNodeId : node id of the sender
     * @param targetNodeId : node id of the destinee
     * @param id : unique id for this request
     * @return keypleDto request
     */
    public static KeypleDto buildRequest(String action, String body, String sessionId,
            String nativeReaderName, String virtualReaderName, String requesterNodeId,
            String targetNodeId, String id) {
        return new KeypleDto(action, body, true, sessionId, nativeReaderName, virtualReaderName,
                requesterNodeId, targetNodeId, id, null);
    }

    /**
     * Build a KeypleDto of type "Response"
     * 
     * @param action : name of the remote method
     * @param body : parameters of the remote method
     * @param sessionId : virtual session id (if exists)
     * @param nativeReaderName : name of the local reader
     * @param virtualReaderName : name of the virtual reader (if exists)
     * @param requesterNodeId : node id of the sender
     * @param targetNodeId : node id of the destinee
     * @param id : id of the request
     * @return keypleDto response
     */
    public static KeypleDto buildResponse(String action, String body, String sessionId,
            String nativeReaderName, String virtualReaderName, String requesterNodeId,
            String targetNodeId, String id) {
        return build(action, body, false, sessionId, nativeReaderName, virtualReaderName,
                requesterNodeId, targetNodeId, id, null);
    }

    /**
     * Build a KeypleDto of type "Notification", (without id)
     * 
     * @param action : name of the notification
     * @param body : parameters of the notification
     * @param sessionId : virtual session id
     * @param nativeReaderName : name of the local reader
     * @param virtualReaderName : name of the virtual reader
     * @param requesterNodeId : node id of the sender
     * @param targetNodeId : node id of the destinee
     * @return keypleDto notification
     */
    public static KeypleDto buildNotification(String action, String body, String sessionId,
            String nativeReaderName, String virtualReaderName, String requesterNodeId,
            String targetNodeId) {
        return new KeypleDto(action, body, true, sessionId, nativeReaderName, virtualReaderName,
                requesterNodeId, targetNodeId, null, null);
    }

    /**
     * Build a keypleDto of type "Exception" containing a Java Throwable
     * <p>
     * This keypleDto send the exception to the other node
     * 
     * @param action : name of the remote method that failed
     * @param sessionId : virtual session id (if exists)
     * @param nativeReaderName : name of the local reader
     * @param virtualReaderName : name of the virtual reader (if exists)
     * @param requesterNodeId : node id of the sender
     * @param targetNodeId : node id of the destinee
     * @param id : id of the failed request
     * @param exception : throwable that occurs during the execution of the method
     * @return keypleDto of type "Exception"
     */
    public static KeypleDto ExceptionDTO(String action, Throwable exception, String sessionId,
            String nativeReaderName, String virtualReaderName, String requesterNodeId,
            String targetNodeId, String id) {

        return build(action, null, false, sessionId, nativeReaderName, virtualReaderName,
                requesterNodeId, targetNodeId, id, JsonParser.getGson().toJson(exception));
    }

    /**
     * Build a keypleDto of type "NoResponse". This Dto should be not be sent by the
     * {@link org.eclipse.keyple.plugin.remotese.transport.DtoNode}.
     * 
     * @param id : id of the request
     * @return NoResponse KeypleDto
     */
    public static KeypleDto NoResponse(String id) {

        return buildResponse("", "", "", "", "", "", "", id);
    }

    /**
     * Check if the keypleDto is of type "NoResponse"
     * 
     * @param dto keypleDto to test
     * @return true of the keypleDto is of type "NoResponse"
     */
    public static Boolean isNoResponse(KeypleDto dto) {
        return dto == null || dto.getAction() == null || dto.getAction().isEmpty();
    }

    /**
     * Check if the keypleDto is of type "Exception"
     * 
     * @param keypleDto keypleDto to test
     * @return true of the keypleDto is of type "Exception"
     */
    public static Boolean containsException(KeypleDto keypleDto) {
        return keypleDto.getError() != null && !keypleDto.getError().isEmpty();
    }



    /* ----------- Serialization Helpers */

    /**
     * Serialize keypleDto to json
     * 
     * @param keypleDto keypleDto to serialize
     * @return json serialization of the keypleDto
     */
    public static String toJson(KeypleDto keypleDto) {
        return JsonParser.getGson().toJson(keypleDto);
    }

    /**
     * Parse a json serialized keypleDto
     * 
     * @param json json String representation of the keypleDto
     * @return keypleDto object
     */
    public static KeypleDto fromJson(String json) {
        return JsonParser.getGson().fromJson(json, KeypleDto.class);
    }

    /**
     * Parse a json keypleDto
     * 
     * @param jsonObj json Object representation of the keypleDto
     * @return keypleDto object
     */
    public static KeypleDto fromJsonObject(JsonObject jsonObj) {
        return JsonParser.getGson().fromJson(jsonObj, KeypleDto.class);
    }

    /**
     * The parameter in the keypleDto has not been specified
     * 
     * @return "notSpecified" String
     */
    public static String notSpecified() {
        return "notSpecified";
    }

    public static Boolean isKeypleDTO(JsonObject json) {
        return json.has("action");
    }


    /* --------- private method ---------- */

    private static KeypleDto build(String action, String body, boolean isRequest, String sessionId,
            String nativeReaderName, String virtualReaderName, String requesterNodeId,
            String targetNodeId, String id, String error) {
        return new KeypleDto(action, body, isRequest, sessionId, nativeReaderName,
                virtualReaderName, requesterNodeId, targetNodeId, id, error);
    }

    private KeypleDtoHelper() {
        throw new IllegalStateException("Utility class");
    }

}
