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

import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.seproxy.event.DefaultSelectionRequest;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.message.SeRequestSet;
import org.eclipse.keyple.seproxy.message.SeResponseSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;

@RunWith(MockitoJUnitRunner.class)
public class JsonParserTest {

    private static final Logger logger = LoggerFactory.getLogger(JsonParserTest.class);


    /**
     * Test Serialization of Keyple Se Proxy Objects
     */

    @Test
    public void testHoplinkSeRequestSet() {
        SeRequestSet seRequestSet = SampleFactory.getASeRequestSet_ISO14443_4();
        testSerializeDeserializeObj(seRequestSet, SeRequestSet.class);
    }

    @Test
    public void testCompleteSeRequestSet() {
        SeRequestSet seRequestSet = SampleFactory.getCompleteRequestSet();
        testSerializeDeserializeObj(seRequestSet, SeRequestSet.class);
    }

    @Test
    public void testSeResponseSet() {
        SeResponseSet responseSet = SampleFactory.getCompleteResponseSet();
        testSerializeDeserializeObj(responseSet, SeResponseSet.class);

    }

    @Test
    public void testSelectionByAidRequest() {
        DefaultSelectionRequest defaultSelectionRequest = SampleFactory.getSelectionRequest();
        testSerializeDeserializeObj(defaultSelectionRequest, DefaultSelectionRequest.class);
    }

    @Test
    public void testSelectionByAtrRequest() {
        DefaultSelectionRequest defaultSelectionRequest = SampleFactory.getSelectionRequest();
        testSerializeDeserializeObj(defaultSelectionRequest, DefaultSelectionRequest.class);
    }

    @Test
    public void testNotificationMode() {
        ObservableReader.NotificationMode notificationMode = SampleFactory.getNotificationMode();
        testSerializeDeserializeObj(notificationMode, ObservableReader.NotificationMode.class);
    }

    /**
     * Test Serialization of Keyple Reader Exceptions
     */
    @Test
    public void testSimpleKeypleException() {
        KeypleBaseException exception = SampleFactory.getASimpleKeypleException();
        testSerializeDeserializeObj(exception, KeypleBaseException.class);

    }

    @Test
    public void testStackedKeypleException() {
        KeypleBaseException exception = SampleFactory.getAStackedKeypleException();
        testSerializeDeserializeObj(exception, KeypleBaseException.class);

    }

    /*
     * Utility Method
     */

    public Object testSerializeDeserializeObj(Object obj, Class objectClass) {
        Gson gson = JsonParser.getGson();
        String json = gson.toJson(obj);
        logger.debug("json 1 : {}", json);
        Object deserializeObj = gson.fromJson(json, objectClass);
        logger.debug("deserializeObj : {}", deserializeObj.toString());
        String json2 = gson.toJson(deserializeObj);
        logger.debug("json 2 : {}", json2);
        assert json.equals(json2);
        return deserializeObj;
    }

}
