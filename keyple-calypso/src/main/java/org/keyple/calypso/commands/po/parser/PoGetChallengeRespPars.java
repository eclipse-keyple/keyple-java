/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.po.parser;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.keyple.commands.ApduResponseParser;
import org.keyple.seproxy.ApduResponse;

/**
 * The Class PoGetChallengeRespPars. This class provides status code properties and the getters to
 * access to the structured fields of a Get Challenge response.
 *
 * @author Ixxi
 *
 */
public class PoGetChallengeRespPars extends ApduResponseParser {


    private static final Map<Integer, StatusProperties> STATUS_TABLE;
    static {
        HashMap<Integer, StatusProperties> m = new HashMap<Integer, StatusProperties>();
        m.put(0x9000, new StatusProperties(true, "Success"));
        STATUS_TABLE = m;
    }

    /**
     * Instantiates a new PoGetChallengeRespPars.
     *
     * @param response the response from PO Get Challenge APDU Command
     */
    public PoGetChallengeRespPars(ApduResponse response) {
        super(response);
        initStatusTable();
    }

    /**
     * Initializes the status table.
     */
    private void initStatusTable() {
        statusTable.put(new byte[] {(byte) 0x90, (byte) 0x00},
                new StatusProperties(true, "Successful execution."));
    }

    public Map<Integer, StatusProperties> getStatusTable() {
        return STATUS_TABLE;
    }

    @Override
    public boolean isSuccessful() {
        StatusProperties p = getStatusTable().get(getStatusCodeV2());
        return p != null && p.isSuccessful();
    }

    /**
     * Gets the po challenge.
     *
     * @return the po challenge
     */
    public byte[] getPoChallenge() {
        if (isSuccessful()) {
            return getApduResponse().getBytesBeforeStatus();
        }
        return null;
    }

    public ByteBuffer getPoChallengeV2() {
        return getApduResponse().getDataBeforeStatus();
    }
}
