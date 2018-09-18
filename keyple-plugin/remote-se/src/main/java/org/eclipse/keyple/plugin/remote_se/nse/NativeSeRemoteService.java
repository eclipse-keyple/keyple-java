package org.eclipse.keyple.plugin.remote_se.nse;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.plugin.remote_se.transport.DtoReceiver;
import org.eclipse.keyple.plugin.remote_se.transport.DtoSender;
import org.eclipse.keyple.plugin.remote_se.transport.KeypleDTO;
import org.eclipse.keyple.plugin.remote_se.transport.KeypleDTOHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class NativeSeRemoteService implements NseAPI, RseClient, DtoReceiver{

    private static final Logger logger = LoggerFactory.getLogger(NativeSeRemoteService.class);

    private DtoSender dtoSender;
    private ProxyReader connectedReader;
    private NseProcessor nseProcessor;

    public NativeSeRemoteService(DtoSender dtoSender){
        this.dtoSender = dtoSender;
        this.nseProcessor = new NseProcessor(this, this);
    }

    //RseClient
    @Override
    public void update(ReaderEvent event) {
        logger.info("Send Reader Event {}", event.getEventType());
        // construct json data
        JsonObject jsonObject = new JsonObject();

        jsonObject.add("pluginName", new JsonPrimitive(event.getPluginName()));
        jsonObject.add("readerName", new JsonPrimitive(event.getReaderName()));
        jsonObject.add("eventType", new JsonPrimitive(event.getEventType().toString()));
        String data = jsonObject.toString();

        dtoSender.sendDTO(new KeypleDTO(KeypleDTOHelper.READER_CONNECT, data,true),null);

    }

    //RseClient
    @Override
    public String connectReader(ProxyReader localReader, Map<String, Object> options) {
        logger.info("connectReader {} {}", localReader, options);

        Boolean isDuplex = (Boolean) options.get("isAsync");
        String transmitUrl = (String) options.get("transmitUrl");
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("localReaderName", new JsonPrimitive(localReader.getName()));
        jsonObject.add("isAsync", new JsonPrimitive(isDuplex));
        if(isDuplex){
            jsonObject.add("transmitUrl", new JsonPrimitive(transmitUrl));
        }
        String data = jsonObject.toString();

        dtoSender.sendDTO(new KeypleDTO(KeypleDTOHelper.READER_CONNECT, data,true),null);
        //we assume it was ok
        //async resposne?
        connectedReader = localReader;
        return null;
    }

    //RseClient
    @Override
    public void disconnectReader(ProxyReader localReader)  {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("localReaderName", new JsonPrimitive(localReader.getName()));
        String data = jsonObject.toString();

        dtoSender.sendDTO(new KeypleDTO(KeypleDTOHelper.READER_DISCONNECT, data,true),null);
    }



    //NseAPI
    @Override
    public SeResponseSet onTransmit(SeRequestSet req) throws IOReaderException {
        return connectedReader.transmit(req);
    }


    @Override
    public void onDTO(KeypleDTO message) {
        KeypleDTO response = nseProcessor.processMessage(message);
        if(!response.getAction().isEmpty()){
            dtoSender.sendDTO(response,null);
        }
    }
}
