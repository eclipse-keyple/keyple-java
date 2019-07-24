/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.integration.tools.calypso.sam;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.calypso.command.sam.builder.security.SamWriteKeyCmdBuild;
import org.eclipse.keyple.calypso.transaction.CalypsoSam;
import org.eclipse.keyple.calypso.transaction.SamResource;
import org.eclipse.keyple.calypso.transaction.SamSelectionRequest;
import org.eclipse.keyple.calypso.transaction.SamSelector;
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.selection.SelectionsResult;
import org.eclipse.keyple.core.seproxy.ChannelState;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.message.ApduRequest;
import org.eclipse.keyple.core.seproxy.message.ProxyReader;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.integration.IntegrationUtils;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.plugin.pcsc.PcscProtocolSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tool_SAM_EnableLock {

    private static final Logger logger = LoggerFactory.getLogger(Tool_SAM_EnableLock.class);

    private static boolean enableSamLock(SamResource samResource, byte lockRef, byte[] lockData)
            throws KeypleReaderException {

        final String ENABLE_LOCK_DATA =
                "0000000000000000EF000000000000800000000000000000000000000000000000000000000000000000000000800000";

        byte[] setLockData = ByteArrayUtil.fromHex(ENABLE_LOCK_DATA);
        setLockData[9] = lockRef;
        System.arraycopy(lockData, 0, setLockData, 21, 16);

        // create an apdu requests list to handle SAM command
        List<ApduRequest> apduRequests = new ArrayList<ApduRequest>();

        // get the challenge from the PO
        apduRequests
                .add(new SamWriteKeyCmdBuild(SamRevision.C1, (byte) 0x80, (byte) 0xE0, setLockData)
                        .getApduRequest());

        SeRequest seRequest = new SeRequest(apduRequests, ChannelState.KEEP_OPEN);

        SeResponse seResponse = ((ProxyReader) samResource.getSeReader()).transmit(seRequest);

        if (seResponse == null) {
            throw new IllegalStateException("SAM Write Key command command failed. Null response");
        }

        return seResponse.getApduResponses().get(0).isSuccessful();
    }


    public static void main(String[] args) throws KeypleBaseException {

        // the unlocking data must be set to the expected value
        final String UNLOCK_DATA = "00112233445566778899AABBCCDDEEFF";
        final byte lockValueReference = 0x00;

        /* Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        /* Get the instance of the PC/SC plugin */
        PcscPlugin pcscPlugin = PcscPlugin.getInstance();

        /* Assign PcscPlugin to the SeProxyService */
        seProxyService.addPlugin(pcscPlugin);

        ProxyReader samReader = (ProxyReader) IntegrationUtils.getReader(seProxyService,
                IntegrationUtils.SAM_READER_NAME_REGEX);

        /* Check if the readers exist */
        if (samReader == null) {
            throw new IllegalStateException("Bad SAM reader setup");
        }

        logger.info("= SAM Reader  NAME = {}", samReader.getName());

        samReader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO7816_3,
                PcscProtocolSetting.PCSC_PROTOCOL_SETTING
                        .get(SeCommonProtocols.PROTOCOL_ISO7816_3));

        // do the SAM selection to open the logical channel
        final String SAM_ATR_REGEX = "3B3F9600805A[0-9a-fA-F]{2}80[0-9a-fA-F]{16}829000";

        SeSelection samSelection = new SeSelection();

        SamSelectionRequest samSelectionRequest = new SamSelectionRequest(
                new SamSelector(SamRevision.C1, null, "SAM Selection"), ChannelState.KEEP_OPEN);

        /* Prepare selector, ignore MatchingSe here */
        samSelection.prepareSelection(samSelectionRequest);

        SelectionsResult samSelectionsResult;
        try {
            samSelectionsResult = samSelection.processExplicitSelection(samReader);
            if (!samSelectionsResult.hasActiveSelection()) {
                System.out.println("Unable to open a logical channel for SAM!");
                throw new IllegalStateException("SAM channel opening failure");
            }
        } catch (KeypleReaderException e) {
            throw new IllegalStateException("Reader exception: " + e.getMessage());
        }

        CalypsoSam calypsoSam =
                (CalypsoSam) samSelectionsResult.getActiveSelection().getMatchingSe();

        SamResource samResource = new SamResource(samReader, calypsoSam);

        if (!enableSamLock(samResource, lockValueReference, ByteArrayUtil.fromHex(UNLOCK_DATA))) {
            throw new IllegalStateException("The SAM Key Change command failed!");
        }

        logger.info("Lock Secret enabled");
    }
}
