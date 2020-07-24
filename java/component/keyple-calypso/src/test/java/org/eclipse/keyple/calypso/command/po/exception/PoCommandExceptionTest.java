/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.command.po.exception;

import static org.assertj.core.api.Assertions.assertThat;
import org.eclipse.keyple.calypso.command.po.CalypsoPoCommand;
import org.eclipse.keyple.core.command.exception.KeypleSeCommandException;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;

public class PoCommandExceptionTest {

    private static final Logger logger = LoggerFactory.getLogger(PoCommandExceptionTest.class);

    Gson parser;

    @Before
    public void setUp() {
        parser = KeypleJsonParser.getParser();
    }

    @Test
    public void serializeException() {
        CalypsoPoAccessForbiddenException source =
                new CalypsoPoAccessForbiddenException("message", CalypsoPoCommand.APPEND_RECORD, 1);
        String json = parser.toJson(source, KeypleSeCommandException.class);
        logger.debug(json);
        KeypleSeCommandException target =
                (KeypleSeCommandException) parser.fromJson(json, KeypleException.class);
        assertThat(target).isEqualToComparingFieldByFieldRecursively(source);
        assertThat(target.getCommand()).isEqualToComparingFieldByField(source.getCommand());
    }

}
