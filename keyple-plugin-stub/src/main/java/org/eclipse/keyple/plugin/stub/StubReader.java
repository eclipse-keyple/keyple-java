/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.stub;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.exceptions.IOReaderException;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

public class StubReader extends AbstractReader implements ConfigurableReader {


    private static final ILogger logger = SLoggerFactory.getLogger(StubReader.class);

    private boolean isSEPresent = false;
    private ByteBuffer aid;

    private Map<String, String> parameters = new HashMap<String, String>();

    public static final String ALLOWED_PARAMETER_1 = "parameter1";
    public static final String ALLOWED_PARAMETER_2 = "parameter2";


    @Override
    public String getName() {
        return "";
    }

    @Override
    public void setProtocols(Map<SeProtocol, String> seProtocolSettings) throws IOReaderException {

    }

    @Override
    public SeResponseSet transmit(SeRequestSet request) throws IOReaderException {

        if (request == null) {
            logger.error("SeRequestSet is null");
            throw new IOReaderException("SeRequestSet is null");
        }

        if (!isSEPresent) {
            throw new IOReaderException("SE is not present");
        }

        if (test_WillTimeout) {
            logger.info("Timeout test is enabled, transmit raises TimeoutException");
            throw new IOReaderException("timeout while processing SeRequestSet");
        }

        boolean channelPreviouslyOpen;
        ApduResponse fci;
        List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();

        // Open channel commands
        if (!test_ChannelIsOpen) {
            channelPreviouslyOpen = false;
            logger.debug("Logical channel is not open");

            if (test_ApplicationError) {
                logger.info(
                        "Application error test is enabled, transmit will fail at open application");
                fci = new ApduResponse(ByteBuffer.allocate(0), false);
                return new SeResponseSet(new SeResponse(channelPreviouslyOpen, fci, apduResponses));
            } else {
                logger.info("Logical channel is opened with aid : " + aid);
                fci = new ApduResponse(ByteBuffer.allocate(0), true);
            }
        } else {
            channelPreviouslyOpen = true;
            aid = ByteBuffer.allocate(0);
            logger.info("Logical channel is already opened with aid : " + aid);
            fci = new ApduResponse(aid, true);
        }


        // Prepare succesfull responses
        for (ApduRequest apduRequest : request.getSingleElement().getApduRequests()) {
            logger.debug("Processing request : " + apduRequest.toString());
            apduResponses.add(new ApduResponse(ByteBuffer.allocate(0), true));
        }

        return new SeResponseSet(new SeResponse(channelPreviouslyOpen, fci, apduResponses));
    }



    @Override
    public boolean isSEPresent() throws IOReaderException {
        return isSEPresent;
    }



    /**
     * Set a list of parameters on a reader.
     * <p>
     * See {@link #setParameter(String, String)} for more details
     *
     * @param parameters the new parameters
     * @throws IOReaderException This method can fail when disabling the exclusive mode as it's
     *         executed instantly
     */
    @Override
    public void setParameters(Map<String, String> parameters) throws IOReaderException {
        for (Map.Entry<String, String> en : parameters.entrySet()) {
            setParameter(en.getKey(), en.getValue());
        }
    }

    @Override
    public void setParameter(String name, String value) throws IOReaderException {
        if (name.equals(ALLOWED_PARAMETER_1) || name.equals(ALLOWED_PARAMETER_2)) {
            parameters.put(name, value);
        } else {
            throw new IOReaderException("parameter name not supported : " + name);
        }
    }

    @Override
    public Map<String, String> getParameters() {
        return parameters;
    }


    private boolean test_WillTimeout = false;
    private boolean test_ApplicationError = false;
    private boolean test_ChannelIsOpen = false;

    public void test_InsertSE() {
        isSEPresent = true;
        logger.debug("Test - insert SE");
        notifyObservers(new ReaderEvent(this, ReaderEvent.EventType.SE_INSERTED));
    }

    public void test_RemoveSE() {
        isSEPresent = false;
        logger.debug("Test - remove SE");
        notifyObservers(new ReaderEvent(this, ReaderEvent.EventType.SE_REMOVAL));
    }

    public void test_SetWillTimeout(Boolean willTimeout) {
        logger.debug("Test - set will timeout to " + willTimeout);
        test_WillTimeout = willTimeout;
    }

    public void test_SetApplicationError(Boolean applicationError) {
        logger.debug("Test - set applicationError to " + applicationError);
        test_ApplicationError = applicationError;
    }

    public void test_SetChannelIsOpen(Boolean channelIsOpen) {
        logger.debug("Test - set channelIsOpen to " + channelIsOpen);
        test_ChannelIsOpen = channelIsOpen;
    }
}
