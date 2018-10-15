/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.integration.calypso;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Pattern;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.plugin.pcsc.PcscProtocolSetting;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclipse.keyple.util.ByteArrayUtils;

public class TestEngine {

    public static ProxyReader poReader, csmReader;

    public static PoFileStructureInfo selectPO()
            throws IllegalArgumentException, KeypleReaderException {

        // operate PO multiselection
        String CSM_ATR_REGEX = "3B3F9600805A[0-9a-fA-F]{2}80[0-9a-fA-F]{16}829000";

        // check the availability of the CSM, open its physical and logical channels and keep it
        // open
        SeRequest csmCheckRequest =
                new SeRequest(new SeRequest.AtrSelector(CSM_ATR_REGEX), null, true);
        SeResponse csmCheckResponse =
                csmReader.transmit(new SeRequestSet(csmCheckRequest)).getSingleResponse();

        if (csmCheckResponse == null) {
            System.out.println("Unable to open a logical channel for CSM!");
            throw new IllegalStateException("CSM channel opening failure");
        }

        // Create a SeRequest list
        Set<SeRequest> selectionRequests = new LinkedHashSet<SeRequest>();

        // Add Audit C0 AID to the list
        SeRequest seRequest = new SeRequest(
                new SeRequest.AidSelector(ByteArrayUtils.fromHex(PoFileStructureInfo.poAuditC0Aid)),
                null, false);
        selectionRequests.add(seRequest);

        // Add CLAP AID to the list
        seRequest = new SeRequest(
                new SeRequest.AidSelector(ByteArrayUtils.fromHex(PoFileStructureInfo.clapAid)),
                null, false);
        selectionRequests.add(seRequest);

        // Add cdLight AID to the list
        seRequest = new SeRequest(
                new SeRequest.AidSelector(ByteArrayUtils.fromHex(PoFileStructureInfo.cdLightAid)),
                null, false);
        selectionRequests.add(seRequest);

        List<SeResponse> responses =
                poReader.transmit(new SeRequestSet(selectionRequests)).getResponses();

        for (int i = 0; i < responses.size(); i++) {

            if (responses.get(i) != null) {

                return new PoFileStructureInfo(responses.get(i));
            }
        }

        throw new IllegalArgumentException("No recognizable PO detected.");
    }

    private static ProxyReader getReader(SeProxyService seProxyService, String pattern)
            throws KeypleReaderException {

        Pattern p = Pattern.compile(pattern);
        for (ReaderPlugin plugin : seProxyService.getPlugins()) {
            for (ProxyReader reader : plugin.getReaders()) {
                if (p.matcher(reader.getName()).matches()) {
                    return reader;
                }
            }
        }
        return null;
    }

    public static void configureReaders()
            throws IOException, InterruptedException, KeypleBaseException {

        SeProxyService seProxyService = SeProxyService.getInstance();
        SortedSet<ReaderPlugin> pluginsSet = new ConcurrentSkipListSet<ReaderPlugin>();
        pluginsSet.add(PcscPlugin.getInstance());
        seProxyService.setPlugins(pluginsSet);

        String PO_READER_NAME_REGEX = ".*(ASK|ACS).*";
        String CSM_READER_NAME_REGEX = ".*(Cherry TC|SCM Microsystems|Identive|HID).*";

        poReader = getReader(seProxyService, PO_READER_NAME_REGEX);
        csmReader = getReader(seProxyService, CSM_READER_NAME_REGEX);


        if (poReader == csmReader || poReader == null || csmReader == null) {
            throw new IllegalStateException("Bad PO/CSM setup");
        }

        System.out.println(
                "\n==================================================================================");
        System.out.println("PO Reader  : " + poReader.getName());
        System.out.println("CSM Reader : " + csmReader.getName());
        System.out.println(
                "==================================================================================");

        poReader.setParameter(PcscReader.SETTING_KEY_PROTOCOL, PcscReader.SETTING_PROTOCOL_T1);
        csmReader.setParameter(PcscReader.SETTING_KEY_PROTOCOL, PcscReader.SETTING_PROTOCOL_T0);

        // provide the reader with the map
        poReader.addSeProtocolSetting(
                new SeProtocolSetting(PcscProtocolSetting.SETTING_PROTOCOL_ISO14443_4));
    }

}
