package org.keyple.commands.calypso;

import org.keyple.seproxy.ApduRequest;

//
/**
 * The Class ApduCommandBuilder. This abstract class has to be extended by all
 * PO and CSM command builder classes, it provides the generic getters to
 * retrieve: the name of the command, the built APDURequest, the corresponding
 * ApduResponseParser class.
 *
 * @author Ixxi
 *
 */

public abstract class ApduCommandBuilder {

    /**
     * the reference of the command in the matrix array enumeration, in order to
     * get the name and the response parser class of the command.
     */
    protected CalypsoCommands commandReference;

    /** the byte array APDU request. */
    protected ApduRequest request;

    /**
     * the generic abstract constructor to build an APDU request with a command
     * reference and a byte array.
     *
     * @param commandReference
     *            command reference
     * @param request
     *            request
     */
    public ApduCommandBuilder(CalypsoCommands commandReference, ApduRequest request) {
        this.commandReference = commandReference;
        this.request = request;
    }


    /**
     * Instantiates a new apdu command builder.
     * @param reference Command reference for builder
     */
    public ApduCommandBuilder(CalypsoCommands reference) {
        commandReference = reference;
    }

    /**
     * Gets the name.
     *
     * @return the name of the APDU command from the CalypsoCommands
     *         information.
     */
    public final String getName() {
        return commandReference.getName();
    }

    /**
     * Gets the apdu response parser class.
     *
     * @return the corresponding ApduResponseParser class of the APDU command
     *         from the CalypsoCommands information
     */
    public final Class<?> getApduResponseParserClass() {
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
