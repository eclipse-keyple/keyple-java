/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.csm.parser;

import org.keyple.commands.ApduResponseParser;
import org.keyple.seproxy.ApduResponse;

/**
 * This class provides the dedicated constructor to parse the Digest Authenticate response.
 *
 * @author Ixxi
 *
 */
public class DigestAuthenticateRespPars extends ApduResponseParser {

    /**
     * Instantiates a new DigestAuthenticateRespPars.
     *
     * @param response from the CSM DigestAuthenticateCmdBuild
     */
    public DigestAuthenticateRespPars(ApduResponse response) {
        super(response);
        initStatusTable();
    }

    /**
     * Initializes the status table.
     */
    private void initStatusTable() {
        statusTable.put(new byte[] {(byte) 0x90, (byte) 0x00},
                new StatusProperties(true, "Successful execution."));
    }

}
