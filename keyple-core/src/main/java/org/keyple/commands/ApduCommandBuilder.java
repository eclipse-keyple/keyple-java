/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.commands;

// import org.keyple.calypso.commands.CalypsoCommands;
import org.keyple.seproxy.ApduRequest;

/**
 * The Class ApduCommandBuilder. This abstract class has to be extended by all PO and CSM command
 * builder classes, it provides the generic getters to retrieve: the name of the command, the built
 * APDURequest, the corresponding ApduResponseParser class.
 *
 * @author Ixxi
 *
 */

public abstract class ApduCommandBuilder {

    /**
     * the reference of the command in the matrix array enumeration, in order to get the name and
     * the response parser class of the command.
     */
    private CommandsTable commandReference;

    /** the byte array APDU request. */
    protected ApduRequest request;

    /**
     * the generic abstract constructor to build an APDU request with a command reference and a byte
     * array.
     *
     * @param commandReference command reference
     * @param request request
     */
    // public ApduCommandBuilder(CalypsoCommands commandReference, ApduRequest request) {
    public ApduCommandBuilder(CommandsTable commandReference, ApduRequest request) {
        this.commandReference = commandReference;
        this.request = request;
    }

    /**
     * Gets the name.
     *
     * @return the name of the APDU command from the CalypsoCommands information.
     */
    public final String getName() {
        return commandReference.getName();
    }

    /**
     * Gets the apdu response parser class.
     *
     * @return the corresponding ApduResponseParser class of the APDU command from the CommandsTable
     *         information
     */
    public final Class<? extends ApduResponseParser> getApduResponseParserClass() {
        return commandReference.getResponseParserClass();
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
