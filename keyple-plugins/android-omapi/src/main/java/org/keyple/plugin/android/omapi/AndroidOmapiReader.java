/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.android.omapi;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.seproxy.ApduRequest;
import org.eclipse.keyple.seproxy.ApduResponse;
import org.eclipse.keyple.seproxy.SeRequest;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponse;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.util.ByteBufferUtils;
import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.Session;
import android.util.Log;

/**
 * TODO although AndroidOmapiReader extends AbstractObservable, may refuse any addition of observer
 */
public class AndroidOmapiReader extends AbstractObservableReader {


    private static final String TAG = AndroidOmapiReader.class.getSimpleName();

    private Reader omapiReader;
    private Channel channel = null;


    protected AndroidOmapiReader(Reader omapiReader) {
        this.omapiReader = omapiReader;
    }

    @Override
    public String getName() {
        return omapiReader.getName();
    }

    @Override
    public SeResponseSet transmit(SeRequestSet seApplicationRequest) throws IOReaderException {

        Log.i(TAG, "Create Session from reader...");
        Session session = null;
        List<SeResponse> seResponseElements = new ArrayList<SeResponse>();


        for (SeRequest seRequestElement : seApplicationRequest.getElements()) {

            ApduResponse fci = null;
            try {
                Log.i(TAG, "Create session...");
                session = omapiReader.openSession();

                Log.i(TAG, "Create logical channel within the session...");
                channel = session.openLogicalChannel(
                        ByteBufferUtils.toBytes(seRequestElement.getAidToSelect()));
                fci = new ApduResponse(ByteBuffer.wrap(channel.getSelectResponse()), true);

            } catch (IOException e) {
                throw new IOReaderException(e.getMessage(), e.getCause());
            }

            Log.i(TAG, "Send APDU commands from SeRequestSet objects");
            List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
            for (ApduRequest seRequest : seRequestElement.getApduRequests()) {
                byte[] respApdu = new byte[0];
                try {
                    respApdu = channel.transmit(ByteBufferUtils.toBytes(seRequest.getBuffer()));
                    apduResponses.add(new ApduResponse(respApdu, true));
                } catch (IOException e) {
                    e.printStackTrace();
                    apduResponses.add(new ApduResponse(ByteBuffer.allocate(0), false));
                }
            }

            seResponseElements.add(new SeResponse(false, fci, apduResponses));

            if (!seRequestElement.keepChannelOpen()) {
                channel.close();
            }
        }


        return new SeResponseSet(seResponseElements);

    }

    @Override
    public boolean isSePresent() throws IOReaderException {
        return omapiReader.isSecureElementPresent();
    }

}
