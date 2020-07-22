/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.remotese.core;

/**
 * This POJO object contains data exchanged between **Native SE** and **Virtual SE** components.
 * <p>
 * It is built and processed by the plugin and you don't need to modified it.
 * <p>
 * You only need to transfer it via the network by serializing and deserializing it on your own.<br>
 * However, you can extend it or encapsulate it in a personal object if you need to transport other
 * technical information related to the network infrastructure for example.
 *
 * @since 1.0
 */
public class KeypleMessageDto {

    private String sessionId;
    private String action;
    private String clientNodeId;
    private String serverNodeId;
    private String nativeReaderName;
    private String virtualReaderName;
    private String body;

    /**
     * Action enum (for internal use only).
     *
     * @since 1.0
     */
    public enum Action {
        EXECUTE_REMOTE_SERVICE, //
        CHECK_PLUGIN_EVENT, //
        PLUGIN_EVENT, //
        CHECK_READER_EVENT, //
        READER_EVENT, //
        TRANSMIT, //
        TRANSMIT_RESPONSE, //
        TRANSMIT_SET, //
        TRANSMIT_SET_RESPONSE, //
        SET_DEFAULT_SELECTION, //
        SET_DEFAULT_SELECTION_RESPONSE, //
        TERMINATE_SERVICE, //
        ERROR;
    }

    /**
     * Error enum (for internal use only).
     *
     * @since 1.0
     */
    public enum ErrorCode {

        TIMEOUT_CLIENT_TASK(1), //
        TIMEOUT_SERVER_TASK(2), //

        // Core

        // Plugin

        // Reader
        KeypleReaderIOException(300), //

        UNKNOWN(99);

        private int code;

        ErrorCode(int code) {
            this.code = code;
        }

        public String getCode() {
            return String.valueOf(code);
        }

        public ErrorCode valueOfCode(String code) {
            int val = Integer.parseInt(code);
            for (ErrorCode ec : values()) {
                if (ec.code == val) {
                    return ec;
                }
            }
            return null;
        }
    }

    /**
     * Constructor.
     *
     * @since 1.0
     */
    public KeypleMessageDto() {}

    /**
     * Constructor by copy.
     *
     * @param from The source dto to copy.
     * @since 1.0
     */
    public KeypleMessageDto(KeypleMessageDto from) {
        sessionId = from.getSessionId();
        action = from.getAction();
        clientNodeId = from.getClientNodeId();
        serverNodeId = from.getServerNodeId();
        nativeReaderName = from.getNativeReaderName();
        virtualReaderName = from.getVirtualReaderName();
        body = from.getBody();
    }

    /**
     * Gets the session id.<br>
     * In case of a full duplex communication, this field will permit to client and server to
     * identify the socket.<br>
     * This id is also useful for debugging.
     *
     * @return a not empty string.
     * @since 1.0
     */
    public final String getSessionId() {
        return sessionId;
    }

    /**
     * This setter method must only be used during the deserialization process.
     *
     * @param sessionId The session id to set.
     * @return the object instance.
     * @since 1.0
     */
    public final KeypleMessageDto setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    /**
     * Gets the name of the internal action to perform in case of a request, or the original action
     * performed in case of a response.
     *
     * @return a not empty string.
     * @since 1.0
     */
    public final String getAction() {
        return action;
    }

    /**
     * This setter method must only be used during the deserialization process.
     *
     * @param action The action to set.
     * @return the object instance.
     * @since 1.0
     */
    public final KeypleMessageDto setAction(String action) {
        this.action = action;
        return this;
    }

    /**
     * Gets the client node id.
     *
     * @return a not empty string.
     * @since 1.0
     */
    public final String getClientNodeId() {
        return clientNodeId;
    }

    /**
     * This setter method must only be used during the deserialization process.
     *
     * @param clientNodeId The client node id to set.
     * @return the object instance.
     * @since 1.0
     */
    public final KeypleMessageDto setClientNodeId(String clientNodeId) {
        this.clientNodeId = clientNodeId;
        return this;
    }

    /**
     * Gets the server node id.<br>
     * In case of a multi-server environment, this field will permit to client or load balancer to
     * identify the target server to access.
     *
     * @return a null string in case of the first transaction call.
     * @since 1.0
     */
    public final String getServerNodeId() {
        return serverNodeId;
    }

    /**
     * This setter method must only be used during the deserialization process.
     *
     * @param serverNodeId The server node id to set.
     * @return the object instance.
     * @since 1.0
     */
    public final KeypleMessageDto setServerNodeId(String serverNodeId) {
        this.serverNodeId = serverNodeId;
        return this;
    }

    /**
     * Gets the name of the native SE reader name associated to the transaction.
     *
     * @return a null string in case of a discovering readers call.
     * @since 1.0
     */
    public final String getNativeReaderName() {
        return nativeReaderName;
    }

    /**
     * This setter method must only be used during the deserialization process.
     *
     * @param nativeReaderName The native SE reader name to set.
     * @return the object instance.
     * @since 1.0
     */
    public final KeypleMessageDto setNativeReaderName(String nativeReaderName) {
        this.nativeReaderName = nativeReaderName;
        return this;
    }

    /**
     * Gets the name of the virtual reader associated to the transaction.
     *
     * @return a null string in case of a discovering readers call.
     * @since 1.0
     */
    public final String getVirtualReaderName() {
        return virtualReaderName;
    }

    /**
     * This setter method must only be used during the deserialization process.
     *
     * @param virtualReaderName The virtual SE reader name to set.
     * @return the object instance.
     * @since 1.0
     */
    public final KeypleMessageDto setVirtualReaderName(String virtualReaderName) {
        this.virtualReaderName = virtualReaderName;
        return this;
    }

    /**
     * Gets the body content.
     *
     * @return a null string in case of an error message.
     * @since 1.0
     */
    public final String getBody() {
        return body;
    }

    /**
     * This setter method must only be used during the deserialization process.
     *
     * @param body The body to set.
     * @return the object instance.
     * @since 1.0
     */
    public final KeypleMessageDto setBody(String body) {
        this.body = body;
        return this;
    }
}
