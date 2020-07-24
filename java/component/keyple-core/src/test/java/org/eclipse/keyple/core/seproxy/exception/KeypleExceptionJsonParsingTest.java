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
package org.eclipse.keyple.core.seproxy.exception;

import static org.assertj.core.api.Assertions.assertThat;
import org.eclipse.keyple.core.command.AbstractIso7816CommandBuilderTest;
import org.eclipse.keyple.core.command.SeCommand;
import org.eclipse.keyple.core.command.exception.KeypleSeCommandException;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.core.util.json.SampleFactory;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;

public class KeypleExceptionJsonParsingTest {

    private static final Logger logger =
            LoggerFactory.getLogger(KeypleExceptionJsonParsingTest.class);

    Gson parser;

    @Before
    public void setTup() {
        parser = KeypleJsonParser.getParser();
    }

    @Test
    public void readerException() {
        KeypleException source = SampleFactory.getAReaderKeypleException();
        String json = parser.toJson(source, KeypleException.class);
        logger.debug(json);
        KeypleException target = parser.fromJson(json, KeypleException.class);
        assertThat(target).isEqualToComparingFieldByFieldRecursively(source);
    }

    @Test
    public void ioException_withResponses() {
        KeypleReaderIOException source = SampleFactory.getIOExceptionWithResponses();
        KeypleException target = parser.fromJson(
                parser.toJson(source, KeypleReaderIOException.class), KeypleException.class);
        assertThat(target).isEqualToComparingFieldByFieldRecursively(source);
    }

    @Test
    public void ioException_withResponse() {
        KeypleException source = SampleFactory.getIOExceptionWithResponse();
        KeypleException target = parser.fromJson(parser.toJson(source), KeypleException.class);
        assertThat(target).isEqualToComparingFieldByFieldRecursively(source);
    }

    @Test
    public void keypleSeCommandException() {
        KeypleSeCommandException source = new AKeypleSeCommandException("message",
                AbstractIso7816CommandBuilderTest.CommandRef.COMMAND_1, 1);
        KeypleSeCommandException target = (KeypleSeCommandException) parser.fromJson(
                parser.toJson(source, KeypleSeCommandException.class), KeypleException.class);
        assertThat(target).isEqualToComparingFieldByFieldRecursively(source);
        assertThat(target.getCommand()).isEqualToComparingFieldByField(source.getCommand());
    }


    static class AKeypleSeCommandException extends KeypleSeCommandException {

        /**
         * @param message the message to identify the exception context
         * @param command the command
         * @param statusCode the status code (optional)
         */
        protected AKeypleSeCommandException(String message, SeCommand command, Integer statusCode) {
            super(message, command, statusCode);
        }
    }


}
