/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.transaction;

import org.eclipse.keyple.calypso.command.po.AbstractPoCommandBuilder;
import org.eclipse.keyple.calypso.command.po.AbstractPoResponseParser;
import org.eclipse.keyple.calypso.command.po.PoSendableInSession;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;

/**
 * The PoCommand class contains the builder of a {@link PoSendableInSession} command
 * <p>
 * A setter is used to define the response to the command received from the PO and then create the
 * parser to access the resulting data..
 * <p>
 * The purpose of this class is to allow PoTransaction to manipulate unique lists of commands built
 * by "prepare" methods and containing their results..
 */
class PoCommand {
    private AbstractPoCommandBuilder poCommandBuilder;
    private AbstractPoResponseParser poResponseParser;

    /**
     * Constructor
     *
     * @param poCommandBuilder the command builder to be stored
     */
    public PoCommand(AbstractPoCommandBuilder poCommandBuilder) {
        this.poCommandBuilder = poCommandBuilder;
        this.poResponseParser = null;
    }

    /**
     * Gets the builder
     * <p>
     * The builder passed as an argument at the construction of the object PoCommand
     *
     * @return the builder
     */
    public AbstractPoCommandBuilder getCommandBuilder() {
        return poCommandBuilder;
    }

    /**
     * Gets the parser
     * <p>
     * The command parser is created when the setResponse method is invoked
     *
     * @return the parser
     */
    public AbstractPoResponseParser getResponseParser() {
        return poResponseParser;
    }

    /**
     * Sets the response
     *
     * @param poResponse the PO's response to the command
     */
    public void setResponse(ApduResponse poResponse) {
        if (poResponseParser == null) {
            poResponseParser = poCommandBuilder.createResponseParser(poResponse);
        }
    }
}
