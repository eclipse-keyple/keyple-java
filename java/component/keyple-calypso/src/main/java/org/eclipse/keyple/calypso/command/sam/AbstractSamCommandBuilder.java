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
package org.eclipse.keyple.calypso.command.sam;

import org.eclipse.keyple.core.command.AbstractApduResponseParser;
import org.eclipse.keyple.core.command.AbstractIso7816CommandBuilder;
import org.eclipse.keyple.core.seproxy.message.ApduRequest;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;

/**
 * Superclass for all SAM command builders.
 * <p>
 * Used directly, this class can serve as low level command builder.
 */
public abstract class AbstractSamCommandBuilder<T extends AbstractSamResponseParser>
        extends AbstractIso7816CommandBuilder {

    protected SamRevision defaultRevision = SamRevision.C1;

    public AbstractSamCommandBuilder(CalypsoSamCommand reference, ApduRequest request) {
        super(reference, request);
    }

    /**
     * Create the response parser matching the builder
     *
     * @param apduResponse the response data from the SE
     * @return an {@link AbstractApduResponseParser}
     */
    public abstract T createResponseParser(ApduResponse apduResponse);

    /**
     * {@inheritDoc}
     */
    @Override
    public CalypsoSamCommand getCommandRef() {
        return (CalypsoSamCommand) commandRef;
    }
}
