/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.command;

import org.eclipse.keyple.seproxy.ApduRequest;

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
        this.commandParserClass = commandReference.getResponseParserClass();
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
     * Gets the apdu response parser class.
     *
     * @return the corresponding AbstractApduResponseParser class of the APDU command from the
     *         CommandsTable information
     */
    public final Class<? extends AbstractApduResponseParser> getApduResponseParserClass() {
        return this.commandParserClass;
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
