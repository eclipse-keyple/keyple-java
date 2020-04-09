/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.command.po.parser;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.calypso.command.po.AbstractPoResponseParser;
import org.eclipse.keyple.calypso.command.po.builder.SelectFileCmdBuild;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoDataAccessException;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoIllegalParameterException;
import org.eclipse.keyple.core.command.AbstractApduResponseParser;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides status code properties and the getters to access to the structured fields of
 * data from response to a Select File command (available from the parent class).
 * <p>
 * The FCI structure is analyzed and all subfields are made available through as many getters.
 */
public final class SelectFileRespPars extends AbstractPoResponseParser {
    private static final Logger logger = LoggerFactory.getLogger(SelectFileRespPars.class);
    private static final Map<Integer, StatusProperties> STATUS_TABLE;

    static {
        Map<Integer, StatusProperties> m =
                new HashMap<Integer, StatusProperties>(AbstractApduResponseParser.STATUS_TABLE);
        m.put(0x6700, new StatusProperties("Lc value not supported.",
                CalypsoPoIllegalParameterException.class));
        m.put(0x6A82, new StatusProperties("File not found.", CalypsoPoDataAccessException.class));
        m.put(0x6119, new StatusProperties("Correct execution (ISO7816 T=0).", null));
        STATUS_TABLE = m;
    }

    @Override
    protected Map<Integer, StatusProperties> getStatusTable() {
        return STATUS_TABLE;
    }

    // File Type Values
    public static final int FILE_TYPE_MF = 1;
    public static final int FILE_TYPE_DF = 2;
    public static final int FILE_TYPE_EF = 4;

    // EF Type Values
    public static final int EF_TYPE_DF = 0;
    public static final int EF_TYPE_BINARY = 1;
    public static final int EF_TYPE_LINEAR = 2;
    public static final int EF_TYPE_CYCLIC = 4;
    public static final int EF_TYPE_SIMULATED_COUNTERS = 8;
    public static final int EF_TYPE_COUNTERS = 9;

    private byte[] fileBinaryData;

    private int lid;

    private byte sfi;

    private byte fileType;

    private byte efType;

    private int recSize;

    private byte numRec;

    private byte[] accessConditions;

    private byte[] keyIndexes;

    private byte simulatedCounterFileSfi;

    private byte simulatedCounterNumber;

    private int sharedEf;

    private byte dfStatus;

    private byte[] rfu;

    private byte[] kvcInfo;

    private byte[] kifInfo;

    private boolean selectionSuccessful;

    /**
     * Method extracting the various fields from the FCI structure returned by the PO.
     * <p>
     * The successful flag (see isSelectionSuccessful) is based on the response status word.
     * <p>
     * The parsingDone flag is set to avoid multiple call to this method while getting several
     * attributes. TODO Handle Rev1/Rev2 PO
     */
    private void parseResponse() {
        byte[] inFileParameters = response.getDataOut();
        int iter = 0;

        if (!response.isSuccessful()) {
            // the command was not successful, we stop here
            return;
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Parsing FCI: {}", ByteArrayUtil.toHex(inFileParameters));
        }

        // Check File TLV Tag and length
        if (inFileParameters[iter++] != (byte) 0x85 || inFileParameters[iter++] != (byte) 0x17) {
            throw new IllegalStateException(
                    "Unexpected FCI format: " + ByteArrayUtil.toHex(inFileParameters));
        }

        fileBinaryData = new byte[inFileParameters.length];
        System.arraycopy(inFileParameters, 0, fileBinaryData, 0, inFileParameters.length);

        sfi = inFileParameters[iter++];
        fileType = inFileParameters[iter++];
        efType = inFileParameters[iter++];

        if (fileType == FILE_TYPE_EF && efType == EF_TYPE_BINARY) {

            recSize = ((inFileParameters[iter] << 8) & 0x0000ff00)
                    | (inFileParameters[iter + 1] & 0x000000ff);
            numRec = 1;
            iter += 2;

        } else if (fileType == FILE_TYPE_EF) {

            recSize = inFileParameters[iter++];
            numRec = inFileParameters[iter++];
        } else {
            // no record for non EF types
            recSize = 0;
            numRec = 0;
            iter += 2;
        }

        accessConditions = new byte[4];
        System.arraycopy(inFileParameters, iter, accessConditions, 0, 4);
        iter += 4;

        keyIndexes = new byte[4];
        System.arraycopy(inFileParameters, iter, keyIndexes, 0, 4);
        iter += 4;

        dfStatus = inFileParameters[iter++];

        if (fileType == FILE_TYPE_EF) {

            if (efType == EF_TYPE_SIMULATED_COUNTERS) {

                simulatedCounterFileSfi = inFileParameters[iter++];
                simulatedCounterNumber = inFileParameters[iter++];

            } else {

                sharedEf = ((inFileParameters[iter] << 8) & 0x0000ff00)
                        | (inFileParameters[iter + 1] & 0x000000ff);
                iter += 2;
            }

            rfu = new byte[5];
            System.arraycopy(inFileParameters, iter, rfu, 0, 5);
            iter += 5;

        } else {

            kvcInfo = new byte[3];
            System.arraycopy(inFileParameters, iter, kvcInfo, 0, 3);
            iter += 3;

            kifInfo = new byte[3];
            System.arraycopy(inFileParameters, iter, kifInfo, 0, 3);
            iter += 3;


            rfu = new byte[1];
            rfu[0] = inFileParameters[iter++];
        }

        lid = ((inFileParameters[iter] << 8) & 0x0000ff00)
                | (inFileParameters[iter + 1] & 0x000000ff);

        selectionSuccessful = true;
    }

    /**
     * Instantiates a new SelectFileRespPars.
     * 
     * @param response the response from the PO
     * @param builderReference the reference to the builder that created this parser
     */
    public SelectFileRespPars(ApduResponse response, SelectFileCmdBuild builderReference) {
        super(response, builderReference);
        parseResponse();
    }

    public boolean isSelectionSuccessful() {
        return selectionSuccessful;
    }

    public int getLid() {
        return lid;
    }

    public byte getSfi() {
        return sfi;
    }

    public byte getFileType() {
        return fileType;
    }

    public byte getEfType() {
        return efType;
    }

    public int getRecSize() {
        return recSize;
    }

    public byte getNumRec() {
        return numRec;
    }

    public byte[] getAccessConditions() {
        return accessConditions;
    }

    public byte[] getKeyIndexes() {
        return keyIndexes;
    }

    public byte getSimulatedCounterFileSfi() {
        return simulatedCounterFileSfi;
    }

    public byte getSimulatedCounterNumber() {
        return simulatedCounterNumber;
    }

    public int getSharedEf() {
        return sharedEf;
    }

    public byte getDfStatus() {
        return dfStatus;
    }

    public byte[] getFileBinaryData() {
        return fileBinaryData;
    }

    public byte[] getRfu() {
        return rfu;
    }

    public byte[] getKvcInfo() {
        return kvcInfo;
    }

    public byte[] getKifInfo() {
        return kifInfo;
    }

    public byte[] getSelectionData() {
        return response.getDataOut();
    }
}
