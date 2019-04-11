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
package org.eclipse.keyple.calypso.command.sam.parser.security;


import org.eclipse.keyple.command.AbstractApduResponseParser;
import org.eclipse.keyple.seproxy.message.ApduResponse;

/**
 * Digest update multiple response parser. See specs: Calypso / page 54 / 7.4.2 - Session MAC
 * computation
 */
public class DigestUpdateMultipleRespPars extends AbstractApduResponseParser {
    /**
     * Instantiates a new DigestUpdateMultipleRespPars.
     *
     * @param response the response
     */
    public DigestUpdateMultipleRespPars(ApduResponse response) {
        super(response);
    }
}
