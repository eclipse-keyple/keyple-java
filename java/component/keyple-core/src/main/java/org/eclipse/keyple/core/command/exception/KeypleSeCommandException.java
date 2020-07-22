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
package org.eclipse.keyple.core.command.exception;

import org.eclipse.keyple.core.command.SeCommand;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * The exception {@code KeypleSeCommandException} is the parent abstract class of all Keyple SE APDU
 * commands exceptions.
 */
public abstract class KeypleSeCommandException extends KeypleException {

    /** The command */
    private final SeCommand command;

    /** The status code (optional) */
    private final Integer statusCode;

    /**
     * @param message the message to identify the exception context
     * @param command the command
     * @param statusCode the status code (optional)
     */
    protected KeypleSeCommandException(String message, SeCommand command, Integer statusCode) {
        super(message);
        this.command = command;
        this.statusCode = statusCode;
    }

    /**
     * @return the command
     */
    public SeCommand getCommand() {
        return command;
    }

    /**
     * @return the status code (nullable)
     */
    public Integer getStatusCode() {
        return statusCode;
    }

    public String toJson() {
        Gson parser = KeypleJsonParser.getParser();
        JsonObject json = new JsonObject();
        json.add("command", parser.toJsonTree(command, SeCommand.class));
        json.addProperty("statusCode", statusCode);
        json.addProperty("message", getMessage());
        json.addProperty("code", getErrorCode());
        return json.toString();
    }
}
