/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.csm.parser;


import java.nio.ByteBuffer;
import org.keyple.commands.ApduResponseParser;
import org.keyple.seproxy.ApduResponse;

/**
 * Digest close response parser. See specs: Calypso / page 54 / 7.4.2 - Session MAC computation
 */
public class DigestCloseRespPars extends ApduResponseParser {
    /**
     * Instantiates a new DigestCloseRespPars.
     *
     * @param response from the DigestCloseCmdBuild
     */
    public DigestCloseRespPars(ApduResponse response) {
        super(response);
    }

    /**
     * Gets the sam signature.
     *
     * @return the sam half session signature
     */
    public ByteBuffer getSignature() {
        return isSuccessful() ? response.getDataOut() : null;
    }
}
