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
package org.eclipse.keyple.plugin.remotese.common.json;

import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import com.google.gson.Gson;

@RunWith(MockitoJUnitRunner.class)
public class JsonParserTest {

    @Test
    public void testHoplinkSeRequestSet() {
        SeRequestSet seRequestSet = SampleFactory.getASeRequest();
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
        Object deserialized = testSerializeDeserializeObj(responseSet, SeResponseSet.class);
        assert responseSet.getResponses().get(0)
                .equals(((SeResponseSet) deserialized).getResponses().get(0));
    }

    public Object testSerializeDeserializeObj(Object obj, Class objectClass) {
        Gson gson = JsonParser.getGson();
        String json = gson.toJson(obj);
        System.out.println(json);
        Object deserializeObj = gson.fromJson(json, objectClass);
        // System.out.println(deserializeObj);
        String json2 = gson.toJson(deserializeObj);
        System.out.println(json2);
        assert json.equals(json2);
        return deserializeObj;
    }

}
