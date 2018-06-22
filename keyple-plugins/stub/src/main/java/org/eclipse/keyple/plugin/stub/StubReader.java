/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.stub;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.ChannelStateReaderException;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.exception.InvalidMessageException;
import org.eclipse.keyple.seproxy.local.AbstractThreadedLocalReader;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

public class StubReader extends AbstractThreadedLocalReader {


    private static final ILogger logger = SLoggerFactory.getLogger(StubReader.class);

    private boolean isSePresent = false;
    private ByteBuffer aid;

    private Map<String, String> parameters = new HashMap<String, String>();

    public static final String ALLOWED_PARAMETER_1 = "parameter1";
    public static final String ALLOWED_PARAMETER_2 = "parameter2";


    @Override
    public String getName() {
        return "";
    }

    public ByteBuffer transmit(ByteBuffer apduIn) throws ChannelStateReaderException {
        return null;
    }

    @Override
    protected ByteBuffer getATR() {
        return null;
    }

    @Override
    protected boolean isPhysicalChannelOpen() {
        return false;
    }

    @Override
    protected void openPhysicalChannel() throws IOReaderException, ChannelStateReaderException {

    }

    @Override
    public void closePhysicalChannel() throws IOReaderException {

    }

    @Override
    public ByteBuffer transmitApdu(ByteBuffer apduIn) throws ChannelStateReaderException {
        return null;
    }

    @Override
    public boolean protocolFlagMatches(SeProtocol protocolFlag) throws InvalidMessageException {
        return false;
    }

    // @Override
    // public SeResponseSet transmit(SeRequestSet request) throws IOReaderException {
    //
    // if (request == null) {
    // logger.error("SeRequestSet is null");
    // throw new IOReaderException("SeRequestSet is null");
    // }
    //
    // if (!isSePresent) {
    // throw new IOReaderException("SE is not present");
    // }
    //
    // if (test_WillTimeout) {
    // logger.info("Timeout test is enabled, transmit raises TimeoutException");
    // throw new IOReaderException("timeout while processing SeRequestSet");
    // }
    //
    // boolean channelPreviouslyOpen;
    // ApduResponse fci;
    // List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
    //
    // // Open channel commands
    // if (!test_ChannelIsOpen) {
    // channelPreviouslyOpen = false;
    // logger.debug("Logical channel is not open");
    //
    // if (test_ApplicationError) {
    // logger.info(
    // "Application error test is enabled, transmit will fail at open application");
    // fci = new ApduResponse(ByteBuffer.allocate(0), false);
    // return new SeResponseSet(new SeResponse(channelPreviouslyOpen, fci, apduResponses));
    // } else {
    // logger.info("Logical channel is opened with aid : " + aid);
    // fci = new ApduResponse(ByteBuffer.allocate(0), true);
    // }
    // } else {
    // channelPreviouslyOpen = true;
    // aid = ByteBuffer.allocate(0);
    // logger.info("Logical channel is already opened with aid : " + aid);
    // fci = new ApduResponse(aid, true);
    // }
    //
    //
    // // Prepare succesfull responses
    // for (ApduRequest apduRequest : request.getSingleRequest().getApduRequests()) {
    // logger.debug("Processing request : " + apduRequest.toString());
    // apduResponses.add(new ApduResponse(ByteBuffer.allocate(0), true));
    // }
    //
    // return new SeResponseSet(new SeResponse(channelPreviouslyOpen, fci, apduResponses));
    // }



    @Override
    public boolean isSePresent() throws IOReaderException {
        return isSePresent;
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
        isSePresent = true;
        logger.debug("Test - insert SE");
        notifyObservers(ReaderEvent.SE_INSERTED);
    }

    public void test_RemoveSE() {
        isSePresent = false;
        logger.debug("Test - remove SE");
        notifyObservers(ReaderEvent.SE_REMOVAL);
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

    @Override
    public boolean waitForCardPresent(long timeout) throws IOReaderException {
        return false;
    }

    @Override
    public boolean waitForCardAbsent(long timeout) throws IOReaderException {
        return false;
    }
}
