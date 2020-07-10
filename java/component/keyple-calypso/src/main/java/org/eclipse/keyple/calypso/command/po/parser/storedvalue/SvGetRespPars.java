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
package org.eclipse.keyple.calypso.command.po.parser.storedvalue;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.calypso.command.po.AbstractPoResponseParser;
import org.eclipse.keyple.calypso.command.po.builder.storedvalue.SvGetCmdBuild;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoIllegalParameterException;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoSecurityContextException;
import org.eclipse.keyple.calypso.command.sam.exception.CalypsoSamAccessForbiddenException;
import org.eclipse.keyple.calypso.transaction.SvDebitLogRecord;
import org.eclipse.keyple.calypso.transaction.SvLoadLogRecord;
import org.eclipse.keyple.core.command.AbstractApduResponseParser;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;

/**
 * SV Get (007C) response parser. See specs: Calypso
 */
public final class SvGetRespPars extends AbstractPoResponseParser {

    private static final Map<Integer, StatusProperties> STATUS_TABLE;

    static {
        Map<Integer, StatusProperties> m =
                new HashMap<Integer, StatusProperties>(AbstractApduResponseParser.STATUS_TABLE);
        m.put(0x6982, new StatusProperties("Security conditions not fulfilled.",
                CalypsoPoSecurityContextException.class));
        m.put(0x6985, new StatusProperties(
                "Preconditions not satisfied (a store value operation was already done in the current session).",
                CalypsoSamAccessForbiddenException.class));
        m.put(0x6A81, new StatusProperties("Incorrect P1 or P2.",
                CalypsoPoIllegalParameterException.class));
        m.put(0x6A86, new StatusProperties("Le inconsistent with P2.",
                CalypsoPoIllegalParameterException.class));
        m.put(0x6D00, new StatusProperties("SV function not present.",
                CalypsoPoIllegalParameterException.class));
        STATUS_TABLE = m;
    }

    @Override
    protected Map<Integer, StatusProperties> getStatusTable() {
        return STATUS_TABLE;
    }

    private final byte currentKVC;
    private final int transactionNumber;

    private final byte[] previousSignatureLo;
    private final byte[] challengeOut;
    private final int balance;
    private final byte[] svCommandHeader;
    private final SvLoadLogRecord loadLog;
    private final SvDebitLogRecord debitLog;

    /**
     * Constructor to build a parser of the SvGet command response.
     *
     * @param svCommandHeader the SvGet command header bytes
     * @param response response to parse
     * @param builder the reference to the builder that created this parser
     */
    public SvGetRespPars(byte[] svCommandHeader, ApduResponse response, SvGetCmdBuild builder) {
        super(response, builder);
        byte[] poResponse = response.getDataOut();
        // keep the command header
        this.svCommandHeader = svCommandHeader;
        switch (poResponse.length) {
            case 0x21: /* Compatibility mode, Reload */
            case 0x1E: /* Compatibility mode, Debit or Undebit */
                challengeOut = new byte[2];
                previousSignatureLo = new byte[3];
                currentKVC = poResponse[0];
                transactionNumber = ByteArrayUtil.twoBytesToInt(poResponse, 1);
                System.arraycopy(poResponse, 3, previousSignatureLo, 0, 3);
                challengeOut[0] = poResponse[6];
                challengeOut[1] = poResponse[7];
                balance = ByteArrayUtil.threeBytesSignedToInt(poResponse, 8);
                if (poResponse.length == 0x21) {
                    /* Reload */
                    loadLog = new SvLoadLogRecord(poResponse, 11);
                    debitLog = null;
                } else {
                    /* Debit */
                    loadLog = null;
                    debitLog = new SvDebitLogRecord(poResponse, 11);
                }
                break;
            case 0x3D: /* Revision 3.2 mode */
                challengeOut = new byte[8];
                previousSignatureLo = new byte[6];
                System.arraycopy(poResponse, 0, challengeOut, 0, 8);
                currentKVC = poResponse[8];
                transactionNumber = ByteArrayUtil.twoBytesToInt(poResponse, 9);
                System.arraycopy(poResponse, 11, previousSignatureLo, 0, 6);
                balance = ByteArrayUtil.threeBytesSignedToInt(poResponse, 17);
                loadLog = new SvLoadLogRecord(poResponse, 20);
                debitLog = new SvDebitLogRecord(poResponse, 42);
                break;
            default:
                throw new IllegalStateException("Incorrect data length in response to SVGet");
        }
    }

    /**
     * Gets the command header used to build the prepare load/debit/undebit SAM commands
     * 
     * @return a byte array containing the header data
     */
    public byte[] getSvGetCommandHeader() {
        return svCommandHeader;
    }

    /**
     * Gets the current SV KVC
     * 
     * @return the value of the current KVC
     */
    public byte getCurrentKVC() {
        return currentKVC;
    }

    /**
     * Gets the SV transaction number
     * 
     * @return the value of the SV transaction number
     */
    public int getTransactionNumber() {
        return transactionNumber;
    }

    /**
     * Gets the SignatureLo value of the last SV transaction (reload, debit, undebit)
     * 
     * @return a byte array containing the signature data
     */
    public byte[] getPreviousSignatureLo() {
        return previousSignatureLo;
    }

    /**
     * Gets the new challenge value generated by the command
     * 
     * @return a byte array containing the challenge
     */
    public byte[] getChallengeOut() {
        return challengeOut;
    }

    /**
     * Gets the current SV balance
     * 
     * @return the value of the SV balance
     */
    public int getBalance() {
        return balance;
    }

    /**
     * Gets a {@link SvLoadLogRecord} containing the load record <br>
     * May return null if the load record is not available (debit/undebit case for PO rev &lt; 3.2)
     * 
     * @return a {@link SvLoadLogRecord} object containing the log data or null
     */
    public SvLoadLogRecord getLoadLog() {
        return loadLog;
    }

    /**
     * Gets a {@link SvDebitLogRecord} containing the last debit record <br>
     * May return null if the debit record is not available (load case for PO rev &lt; 3.2)
     * 
     * @return a {@link SvDebitLogRecord} object containing the log data or null
     */
    public SvDebitLogRecord getDebitLog() {
        return debitLog;
    }
}
