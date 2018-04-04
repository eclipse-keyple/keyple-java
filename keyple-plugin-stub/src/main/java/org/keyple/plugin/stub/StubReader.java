/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.plugin.stub;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keyple.seproxy.*;
import org.keyple.seproxy.exceptions.IOReaderException;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

public class StubReader extends ObservableReader implements ConfigurableReader {

    private static final ILogger logger = SLoggerFactory.getLogger(StubReader.class);

    private boolean logging;



    @Override
    public String getName() {
        return "";
    }

    @Override
    public SeResponse transmit(SeRequest request) throws IOReaderException {

        if(request == null){
            return null;
        }

        //prepare response
        boolean channelPreviouslyOpen = false;
        ApduResponse fci = new ApduResponse(ByteBuffer.allocate(0),false);
        List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
        return new SeResponse(channelPreviouslyOpen, fci,apduResponses);
    }



    @Override
    public boolean isSEPresent() throws IOReaderException {
      return false;
    }



    /**
     * Set a list of parameters on a reader.
     * <p>
     * See {@link #setAParameter(String, String)} for more details
     *
     * @param parameters the new parameters
     * @throws IOReaderException This method can fail when disabling the exclusive mode as it's
     *         executed instantly
     */
    @Override
    public void setParameters(Map<String, String> parameters) throws IOReaderException {
        for (Map.Entry<String, String> en : parameters.entrySet()) {
            setAParameter(en.getKey(), en.getValue());
        }
    }


    @Override
    public void setAParameter(String name, String value) throws IOReaderException {

    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> parameters = new HashMap<String, String>();

        return parameters;
    }


}
