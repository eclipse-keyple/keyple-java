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

import org.eclipse.keyple.calypso.command.CalypsoBuilderParser;
import org.eclipse.keyple.calypso.command.po.AbstractPoCommandBuilder;
import org.eclipse.keyple.calypso.command.po.AbstractPoResponseParser;
import org.eclipse.keyple.calypso.command.po.PoSendableInSession;

/**
 * The PoBuilderParser class contains the builder of a {@link PoSendableInSession} command
 * <p>
 * A setter allows to associate the parser object.
 */
public class PoCommand<T>
        implements CalypsoBuilderParser<AbstractPoCommandBuilder, AbstractPoResponseParser> {
    private final AbstractPoCommandBuilder poCommandBuilder;
    private AbstractPoResponseParser poResponseParser;

    public PoCommand(AbstractPoCommandBuilder poCommandBuilder) {
        this.poCommandBuilder = poCommandBuilder;
    }

    public AbstractPoCommandBuilder getCommandBuilder() {
        return poCommandBuilder;
    }

    public AbstractPoResponseParser getResponseParser() {
        return poResponseParser;
    }

    public void setResponseParser(AbstractPoResponseParser poResponseParser) {
        this.poResponseParser = poResponseParser;
    }
}
