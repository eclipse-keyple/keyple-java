/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.examples.pc;

import java.util.*;
import java.util.regex.Pattern;
import org.keyple.calypso.commands.po.PoRevision;
import org.keyple.calypso.commands.po.builder.ReadRecordsCmdBuild;
import org.keyple.calypso.commands.po.builder.UpdateRecordCmdBuild;
import org.keyple.plugin.pcsc.PcscPlugin;
import org.keyple.seproxy.*;
import org.keyple.seproxy.exceptions.IOReaderException;
import org.keyple.util.ByteBufferUtils;
import org.keyple.util.Observable;

public class KeypleGenericDemo_SeProtocolDetection implements Observable.Observer<ReaderEvent> {
    private ProxyReader poReader, csmReader;

    public KeypleGenericDemo_SeProtocolDetection() {
        super();
    }

    @Override
    public void update(Observable<? extends ReaderEvent> observable, ReaderEvent event) {
        switch (event.getEventType()) {
            case SE_INSERTED:
                System.out.println("SE INSERTED");
                System.out.println("\nStart processing of a PO");
                operatePoTransaction();
                break;
            case SE_REMOVAL:
                System.out.println("SE REMOVED");
                System.out.println("\nWait for PO");
                break;
            default:
                System.out.println("IO Error");
        }
    }

    public void operatePoTransaction() {

        try {

            // create a list of SeRequest
            List<SeRequest> poRequestElements = new ArrayList<SeRequest>();


            // create request
            // Hoplink
            String poAid = "A000000291A000000191";
            String t2UsageRecord1_dataFill = "0102030405060708090A0B0C0D0E0F10"
                    + "1112131415161718191A1B1C1D1E1F20" + "2122232425262728292A2B2C2D2E2F30";

            ReadRecordsCmdBuild poReadRecordCmd_T2Env = new ReadRecordsCmdBuild(PoRevision.REV3_1,
                    (byte) 0x01, true, (byte) 0x14, (byte) 0x20);
            ReadRecordsCmdBuild poReadRecordCmd_T2Usage = new ReadRecordsCmdBuild(PoRevision.REV3_1,
                    (byte) 0x01, true, (byte) 0x1A, (byte) 0x30);
            UpdateRecordCmdBuild poUpdateRecordCmd_T2UsageFill =
                    new UpdateRecordCmdBuild(PoRevision.REV3_1, (byte) 0x01, (byte) 0x1A,
                            ByteBufferUtils.fromHex(t2UsageRecord1_dataFill));

            // Get PO ApduRequest List
            List<ApduRequest> poApduRequestListHoplink =
                    Arrays.asList(poReadRecordCmd_T2Env.getApduRequest(),
                            poReadRecordCmd_T2Usage.getApduRequest(),
                            poUpdateRecordCmd_T2UsageFill.getApduRequest());

            // create request
            SeRequest poRequestElementHoplink =
                    new SeRequest(ByteBufferUtils.fromHex(poAid), poApduRequestListHoplink, false);
            poRequestElementHoplink.setSeProtocolFlag(ContactlessProtocols.PROTOCOL_ISO14443_4);

            poRequestElements.add(poRequestElementHoplink);

            // add 2 more identical Hoplink request elements for test
            // when keepChannelOpen is set to false, we should have the same scenario 3 times
            // when keepChannelOpen is set to true, we should only have it once
            poRequestElements.add(poRequestElementHoplink);
            poRequestElements.add(poRequestElementHoplink);


            SeRequestSet poRequest = new SeRequestSet(poRequestElements);
            // execute request and get response
            SeResponseSet poResponse = poReader.transmit(poRequest);

            // output results
            Iterator<SeRequest> seReqIterator = poRequestElements.iterator();
            for (SeResponse respElement : poResponse.getElements()) {
                SeRequest reqElement = seReqIterator.next();

                if (respElement != null) {
                    List<ApduRequest> poApduRequestList = reqElement.getApduRequests();
                    List<ApduResponse> poApduResponseList = respElement.getApduResponses();
                    for (int i = 0; i < poApduResponseList.size(); i++) {
                        System.out.println("######## CMD: "
                                + ByteBufferUtils.toHex(poApduRequestList.get(i).getBuffer()));
                        System.out.println("####### RESP: "
                                + ByteBufferUtils.toHex(poApduResponseList.get(i).getBuffer()));
                    }
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // This is where you should add patterns of readers you want to use for tests
    private static final String poReaderName = ".*(ASK|ACS|SCM).*";

    /**
     * Get the terminal which names match the expected pattern
     *
     * @param seProxyService SE Proxy service
     * @param pattern Pattern
     * @return ProxyReader
     * @throws IOReaderException Any error with the card communication
     */
    private static ProxyReader getReader(SeProxyService seProxyService, String pattern)
            throws IOReaderException {
        Pattern p = Pattern.compile(pattern);
        for (ReadersPlugin plugin : seProxyService.getPlugins()) {
            for (ProxyReader reader : plugin.getReaders()) {
                if (p.matcher(reader.getName()).matches()) {
                    return reader;
                }
            }
        }
        return null;
    }

    private static final Object waitForEnd = new Object();

    public static void main(String[] args) throws IOReaderException, InterruptedException {
        SeProxyService seProxyService = SeProxyService.getInstance();
        List<ReadersPlugin> pluginsSet = new ArrayList<ReadersPlugin>();
        pluginsSet.add(PcscPlugin.getInstance().setLogging(true));
        seProxyService.setPlugins(pluginsSet);

        ProxyReader poReader = getReader(seProxyService, poReaderName);

        if (poReader == null) {
            throw new IllegalStateException("Bad PO/CSM setup");
        }

        System.out.println("PO Reader  : " + poReader.getName());

        KeypleTest_ObservablePoMultiPoSolution observer =
                new KeypleTest_ObservablePoMultiPoSolution();

        observer.poReader = poReader;
        ((ConfigurableReader) observer.poReader).setParameter("protocol", "T1");

        // Configure the reader to handle various application cases
        // create and fill a protocol map
        Map<SeProtocol, String> protocolsMap = new HashMap<SeProtocol, String>();

        protocolsMap.put(ContactlessProtocols.PROTOCOL_MIFARE_1K,
                "3B8F8001804F0CA000000306030001000000006A");
        protocolsMap.put(ContactlessProtocols.PROTOCOL_MIFARE_UL,
                "3B8F8001804F0CA0000003060300030000000068");
        protocolsMap.put(ContactlessProtocols.PROTOCOL_MEMORY_ST25,
                "3B8F8001804F0CA000000306070007D0020C00B6");
        protocolsMap.put(ContactlessProtocols.PROTOCOL_ISO14443_4,
                "3B8880010000000000718100F9|3B8C800150........00000000007181..");
        protocolsMap.put(ContactlessProtocols.PROTOCOL_B_PRIME,
                "3B8F8001805A0A0103200311........829000..");
        protocolsMap.put(ContactlessProtocols.PROTOCOL_MIFARE_DESFIRE, "3B8180018080");

        // provide the reader with the map
        ((ConfigurableReader) observer.poReader).setProtocols(protocolsMap);

        // Set terminal as Observer of the first reader
        ((AbstractObservableReader) observer.poReader).addObserver(observer);
        synchronized (waitForEnd) {
            waitForEnd.wait();
        }
    }


}
