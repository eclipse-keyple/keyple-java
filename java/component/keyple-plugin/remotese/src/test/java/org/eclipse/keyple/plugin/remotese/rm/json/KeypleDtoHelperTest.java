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
package org.eclipse.keyple.plugin.remotese.rm.json;


import java.io.IOException;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class KeypleDtoHelperTest {

    private static final Logger logger = LoggerFactory.getLogger(KeypleDtoHelperTest.class);

    @Test
    public void testContainsException() {

        Exception ex = new KeypleReaderException("keyple Reader Exception message",
                new IOException("error io"));
        KeypleDto dtoWithException =
                KeypleDtoHelper.ExceptionDTO("any", ex, "any", "any", "any", "any");
        logger.debug(KeypleDtoHelper.toJson(dtoWithException));
        assert KeypleDtoHelper.containsException(dtoWithException);


    }

}
