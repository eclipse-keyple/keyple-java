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
import org.eclipse.keyple.core.util.json.SampleFactory;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeypleExceptionFactoryImplTest {

    private static final Logger logger =
            LoggerFactory.getLogger(KeypleExceptionFactoryImplTest.class);

    KeypleException.KeypleExceptionFactory factory;

    @Before
    public void setTup() {
        factory = new KeypleExceptionFactoryImpl();
    }

    @Test
    public void ioException_withResponses() {
        KeypleException source = SampleFactory.getIOExceptionWithResponses();
        logger.info(source.toJson());
        KeypleException target = factory.from(source.toJson());
        assertThat(source).isEqualToComparingFieldByField(target);

    }

    @Test
    public void ioException_withResponse() {
        KeypleException source = SampleFactory.getIOExceptionWithResponse();
        logger.info(source.toJson());
        KeypleException target = factory.from(source.toJson());
        assertThat(source).isEqualToComparingFieldByField(target);

    }

    @Test
    public void keypleSeCommandException() {
        KeypleException source = new AKeypleSeCommandException("message",
                AbstractIso7816CommandBuilderTest.CommandRef.COMMAND_1, 1);
        logger.info(source.toJson());
        KeypleException target = factory.from(source.toJson());
        assertThat(source).isEqualToComparingFieldByField(target);
    }


    class AKeypleSeCommandException extends KeypleSeCommandException {

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
