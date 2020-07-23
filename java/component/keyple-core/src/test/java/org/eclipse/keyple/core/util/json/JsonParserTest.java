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
package org.eclipse.keyple.core.util.json;

import java.util.List;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.message.DefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.message.DefaultSelectionsResponse;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
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
    public void testHoplinkSeRequestList() {
        List<SeRequest> seRequests = SampleFactory.getASeRequestList_ISO14443_4();
        testSerializeDeserializeObj(seRequests, List.class);
    }

    @Test
    public void testCompleteSeRequestList() {
        List<SeRequest> seRequests = SampleFactory.getCompleteRequestList();
        testSerializeDeserializeObj(seRequests, List.class);
    }

    @Test
    public void testSeResponses() {
        List<SeResponse> seResponses = SampleFactory.getCompleteResponseSet();
        testSerializeDeserializeObj(seResponses, List.class);

    }

    @Test
    public void testSelectionByAidRequest() {
        AbstractDefaultSelectionsRequest defaultSelectionsRequest =
                SampleFactory.getSelectionRequest();
        testSerializeDeserializeObj(defaultSelectionsRequest, DefaultSelectionsRequest.class);
    }

    @Test
    public void testSelectionByAtrRequest() {
        AbstractDefaultSelectionsRequest defaultSelectionsRequest =
                SampleFactory.getSelectionRequest();
        testSerializeDeserializeObj(defaultSelectionsRequest, DefaultSelectionsRequest.class);
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
    public void testReaderEvent() {
        ReaderEvent readerEvent =
                new ReaderEvent("PLUGIN", "READER", ReaderEvent.EventType.SE_INSERTED,
                        new DefaultSelectionsResponse(SampleFactory.getCompleteResponseSet()));
        testSerializeDeserializeObj(readerEvent, ReaderEvent.class);
    }

    /*
     * Utility Method
     */

    public Object testSerializeDeserializeObj(Object obj, Class objectClass) {
        Gson gson = KeypleJsonParser.getParser();
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
