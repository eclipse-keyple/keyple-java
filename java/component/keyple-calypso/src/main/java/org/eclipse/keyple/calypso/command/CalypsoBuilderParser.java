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
package org.eclipse.keyple.calypso.command;


import org.eclipse.keyple.command.AbstractApduResponseParser;
import org.eclipse.keyple.command.AbstractIso7816CommandBuilder;

public interface CalypsoBuilderParser<B extends AbstractIso7816CommandBuilder, P extends AbstractApduResponseParser> {
    B getCommandBuilder();

    P getResponseParser();

    void setResponseParser(P parser);
}
