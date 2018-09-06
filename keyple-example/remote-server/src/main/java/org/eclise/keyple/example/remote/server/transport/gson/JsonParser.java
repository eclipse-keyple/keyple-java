package org.eclise.keyple.example.remote.server.transport.gson;



import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.seproxy.SeRequest;

import java.nio.ByteBuffer;

public class JsonParser {

    static public Gson getGson(){
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ByteBuffer.class, new GsonByteBufferTypeAdapter());
        gsonBuilder.registerTypeAdapter(SeRequest.Selector.class, new GsonSelectorTypeAdapter());
        gsonBuilder.registerTypeAdapter(SeProtocol.class, new GsonSeProtocolTypeAdapter());
        gsonBuilder.setPrettyPrinting();
        return  gsonBuilder.create();
    }

}
