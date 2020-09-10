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

import org.eclipse.keyple.calypso.command.sam.AbstractSamResponseParser;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;

/**
 * SAM read event counter.
 */
public class SamReadEventCounterRespPars extends AbstractSamResponseParser {
    /**
     * Instantiates a new SamReadEventCounterRespPars.
     *
     * @param response of the SamReadEventCounterRespPars
     */
    public SamReadEventCounterRespPars(ApduResponse response) {
        super(response);
    }

    /**
     * Gets the key parameters.
     *
     * @return the counter data (Value or Record)
     */
    public byte[] getCounterData() {
        return isSuccessful() ? response.getDataOut() : null;
    }
}
