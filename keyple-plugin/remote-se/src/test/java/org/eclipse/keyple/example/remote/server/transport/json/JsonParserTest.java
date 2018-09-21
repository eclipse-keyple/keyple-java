/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.example.remote.server.transport.json;

import org.eclipse.keyple.plugin.remote_se.transport.json.JsonParser;
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
        SeRequestSet seRequestSet = SampleFactory.getRequestIsoDepSetSample();
        testSerializeDeserializeObj(seRequestSet, SeRequestSet.class);
    }

    @Test
    public void testCompleteSeRequestSet() {
        SeRequestSet seRequestSet = SampleFactory.getCompleteRequestSet();
        testSerializeDeserializeObj(seRequestSet, SeRequestSet.class);
    }

    @Test
    public void testSeResponseSet() {
        SeResponseSet responseSet = SampleFactory.getCompeleteResponseSet();
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
