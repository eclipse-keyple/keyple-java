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

import java.util.List;
import java.util.Set;
import org.eclipse.keyple.core.seproxy.event.DefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.core.seproxy.message.DefaultSelectionsRequestImpl;
import org.eclipse.keyple.core.seproxy.message.DefaultSelectionsResponseImpl;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
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
        Set<SeRequest> seRequestSet = SampleFactory.getASeRequestSet_ISO14443_4();
        testSerializeDeserializeObj(seRequestSet, Set.class);
    }

    @Test
    public void testCompleteSeRequestSet() {
        Set<SeRequest> seRequestSet = SampleFactory.getCompleteRequestSet();
        testSerializeDeserializeObj(seRequestSet, Set.class);
    }

    @Test
    public void testSeResponseSet() {
        List<SeResponse> responseSet = SampleFactory.getCompleteResponseSet();
        testSerializeDeserializeObj(responseSet, List.class);

    }

    @Test
    public void testSelectionByAidRequest() {
        DefaultSelectionsRequest defaultSelectionsRequest = SampleFactory.getSelectionRequest();
        testSerializeDeserializeObj(defaultSelectionsRequest, DefaultSelectionsRequestImpl.class);
    }

    @Test
    public void testSelectionByAtrRequest() {
        DefaultSelectionsRequest defaultSelectionsRequest = SampleFactory.getSelectionRequest();
        testSerializeDeserializeObj(defaultSelectionsRequest, DefaultSelectionsRequestImpl.class);
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

    @Test
    public void testReaderEvent() {
        ReaderEvent readerEvent = new ReaderEvent("PLUGIN", "READER",
                ReaderEvent.EventType.SE_INSERTED,
                new DefaultSelectionsResponseImpl(SampleFactory.getCompleteResponseSet()));
        testSerializeDeserializeObj(readerEvent, ReaderEvent.class);
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
