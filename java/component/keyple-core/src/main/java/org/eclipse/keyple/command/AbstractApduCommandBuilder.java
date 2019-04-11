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
package org.eclipse.keyple.command;

import org.eclipse.keyple.seproxy.message.ApduRequest;

/**
 * Generic APDU command builder.
 * <p>
 * It provides the generic getters to retrieve:
 * <ul>
 * <li>the name of the command,</li>
 * <li>the built APDURequest,</li>
 * <li>the corresponding AbstractApduResponseParser class.</li>
 * </ul>
 */

public abstract class AbstractApduCommandBuilder {

    /**
     * The command name (will appear in logs)
     */
    private String name;
    /**
     * The command parser class
     */
    private Class<? extends AbstractApduResponseParser> commandParserClass;

    /** the byte array APDU request. */
    protected ApduRequest request;

    /**
     * the generic abstract constructor to build an APDU request with a command reference and a byte
     * array.
     *
     * @param commandReference command reference
     * @param request request
     */
    // public AbstractApduCommandBuilder(CalypsoCommands commandReference, ApduRequest request) {
    public AbstractApduCommandBuilder(CommandsTable commandReference, ApduRequest request) {
        this.name = commandReference.getName();
        this.request = request;
        // set APDU name for non null request
        if (request != null) {
            this.request.setName(commandReference.getName());
        }
    }

    public AbstractApduCommandBuilder(String name, ApduRequest request) {
        this.name = name;
        this.request = request;
        this.commandParserClass = null;
        // set APDU name for non null request
        if (request != null) {
            this.request.setName(name);
        }
    }

    /**
     * Append a string to the current name
     * 
     * @param subName the string to append
     */
    public final void addSubName(String subName) {
        if (subName.length() != 0) {
            this.name = this.name + " - " + subName;
            if (request != null) {
                this.request.setName(this.name);
            }
        }
    }

    /**
     * Gets the name.
     *
     * @return the name of the APDU command from the CalypsoCommands information.
     */
    public final String getName() {
        return this.name;
    }

    /**
     * Gets the request.
     *
     * @return the request
     */
    public final ApduRequest getApduRequest() {
        return request;
    }

}
