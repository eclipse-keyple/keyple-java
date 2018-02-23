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

import java.nio.ByteBuffer;

/**
 * This class provides status code properties and the getters to access to the structured fields of
 * a Digest Close response.
 *
 * @author Ixxi
 *
 */
public class DigestCloseRespPars extends ApduResponseParser {
    /** The SAM signture */
    ByteBuffer signature;

    /**
     * Instantiates a new DigestCloseRespPars.
     *
     * @param response from the DigestCloseCmdBuild
     */
    public DigestCloseRespPars(ApduResponse response) {
        super(response);
        if (isSuccessful()) {
            signature = response.getBuffer();
        }
    }

    /**
     * Gets the sam signature.
     *
     * @return the sam half session signature
     */
    public ByteBuffer getSignature() {
        return signature;
    }

}
