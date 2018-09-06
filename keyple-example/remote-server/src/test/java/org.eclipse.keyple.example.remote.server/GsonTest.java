package org.eclipse.keyple.example.remote.server;

import com.google.gson.Gson;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclise.keyple.example.remote.server.transport.gson.JsonParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GsonTest {

    @Test
     public void testHoplinkSeRequest(){

        SeRequestSet seRequestSet = SampleFactory.getRequestIsoDepSetSample();
        testSerializeDeserializeObj(seRequestSet, SeRequestSet.class);

    }

    @Test
     public void testCompleteSeRequest(){
        SeRequestSet seRequestSet = SampleFactory.getCompleteRequestSet();
        testSerializeDeserializeObj(seRequestSet,SeRequestSet.class);

    }

     public void testSerializeDeserializeObj(Object obj, Class objectClass ){
        Gson gson = JsonParser.getGson();
        String json = gson.toJson(obj);
        System.out.println(json);
        Object deserializeObj = gson.fromJson(json, objectClass);
        //System.out.println(deserializeObj);
        String json2 = gson.toJson(deserializeObj);
        System.out.println(json2);
         assert json.equals(json2);
    }

}
