package org.eclipse.keyple.plugin.remotese.core.impl;

import com.google.gson.Gson;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.junit.Before;
import org.junit.Test;

public class AbstractKeypleMessageHandlerTest  {

    AbstractKeypleMessageHandler handler;
    Gson parser = KeypleJsonParser.getParser();

    KeypleMessageDto response = new KeypleMessageDto()
                .setAction(KeypleMessageDto.Action.SET_DEFAULT_SELECTION.name());

    KeypleMessageDto    responseWithKRIoExceptionException = new KeypleMessageDto()
                .setAction(KeypleMessageDto.Action.ERROR.name())
                .setBody(parser.toJson(new KeypleReaderIOException("keyple io reader")));

    KeypleMessageDto   responseWithUnknownError = new KeypleMessageDto()
                .setAction(KeypleMessageDto.Action.ERROR.name())
                .setBody(parser.toJson(new MyRuntimeException("my runtime exception")));


    @Before
    public void setUp(){
        handler = new AbstractKeypleMessageHandler() {
            @Override
            protected void onMessage(KeypleMessageDto msg) {}
        };
    }

    @Test
    public void checkError_noError_doNothing(){
        handler.checkError(response);
    }

    @Test(expected = KeypleReaderIOException.class)
    public void checkError_knownError_throwException(){
        handler.checkError(responseWithKRIoExceptionException);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkError_unknownError_throwISE(){
        handler.checkError(responseWithUnknownError);
    }


    class MyRuntimeException extends RuntimeException{

        public MyRuntimeException(String message) {
            super(message);
        }
    }

}
