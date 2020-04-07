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
package org.eclipse.keyple.calypso.command.po;

import org.eclipse.keyple.core.command.AbstractApduResponseParser;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;

public abstract class AbstractPoResponseParser extends AbstractApduResponseParser {
    /**
     * Parsers are usually created by their associated builder. The builderReference field maintains
     * a link between the builder and the parser in order to allow the parser to access the builder
     * parameters that were used to create the command (e.g. SFI, registration number, etc.).
     */
    protected final AbstractPoCommandBuilder builderReference;

    /**
     * the generic abstract constructor to build a parser of the APDU response.
     *
     * @param response response to parse
     * @param builderReference the reference of the build that created the parser
     */
    public AbstractPoResponseParser(ApduResponse response,
            AbstractPoCommandBuilder builderReference) {
        super(response);
        this.builderReference = builderReference;
    }
}
