/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */
package org.eclipse.keyple.calypso.command.csm.parser;


import org.eclipse.keyple.command.AbstractApduResponseParser;
import org.eclipse.keyple.seproxy.ApduResponse;

/**
 * Digest init response parser. See specs: Calypso / page 54 / 7.4.2 - Session MAC computation
 */
public class DigestInitRespPars extends AbstractApduResponseParser {
    /**
     * Instantiates a new DigestInitRespPars.
     *
     * @param response from DigestInitCmdBuild
     */
    public DigestInitRespPars(ApduResponse response) {
        super(response);
    }
}
