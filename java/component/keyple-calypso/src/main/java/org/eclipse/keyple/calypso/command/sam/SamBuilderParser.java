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
package org.eclipse.keyple.calypso.command.sam;

import org.eclipse.keyple.calypso.command.CalypsoBuilderParser;

public class SamBuilderParser
        implements CalypsoBuilderParser<AbstractSamCommandBuilder, AbstractSamResponseParser> {
    private final AbstractSamCommandBuilder samCommandBuilder;
    private AbstractSamResponseParser samResponseParser;

    public SamBuilderParser(AbstractSamCommandBuilder samCommandBuilder) {
        this.samCommandBuilder = samCommandBuilder;
    }

    public AbstractSamCommandBuilder getCommandBuilder() {
        return samCommandBuilder;
    }

    public AbstractSamResponseParser getResponseParser() {
        return samResponseParser;
    }

    public void setResponseParser(AbstractSamResponseParser poResponseParser) {
        this.samResponseParser = poResponseParser;
    }
}
