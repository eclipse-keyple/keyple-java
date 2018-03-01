/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.examples.pc;

import java.util.ArrayList;
import java.util.List;
import org.keyple.calypso.commands.po.PoRevision;
import org.keyple.calypso.commands.po.builder.ReadRecordsCmdBuild;
import org.keyple.calypso.commands.po.builder.UpdateRecordCmdBuild;
import org.keyple.plugin.pcsc.PcscPlugin;
import org.keyple.seproxy.*;

public class BasicCardAccess {
    private static final Object sync = new Object();

    public static void main(String[] args) throws Exception {
        SeProxyService seProxyService = SeProxyService.getInstance();
        System.out.println("SeProxyServ v" + seProxyService.getVersion());
        List<ReadersPlugin> plugins = new ArrayList<ReadersPlugin>();
        plugins.add(PcscPlugin.getInstance().setLogging(true));
        seProxyService.setPlugins(plugins);
        for (ReadersPlugin rp : seProxyService.getPlugins()) {
            System.out.println("Reader plugin: " + rp.getName());
            for (final ProxyReader pr : rp.getReaders()) {
                System.out
                        .println("Reader name: " + pr.getName() + ", present: " + pr.isSEPresent());
                if (pr instanceof ObservableReader) {
                    ((ObservableReader) pr).addObserver(new ReaderObserver() {
                        @Override
                        public void notify(ReaderEvent event) {
                            if (event.getEventType() == ReaderEvent.EventType.SE_INSERTED) {
                                parseInfo(pr);
                            }
                        }
                    });
                } else {
                    parseInfo(pr);
                }
            }
        }

        synchronized (sync) {
            sync.wait();
        }
    }

    private static void parseInfo(ProxyReader poReader) {
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
        List<ApduRequest> poApduRequestList = new ArrayList<ApduRequest>();
        poApduRequestList.add(poReadRecordCmd_T2Env.getApduRequest());
        poApduRequestList.add(poReadRecordCmd_T2Usage.getApduRequest());
        poApduRequestList.add(poUpdateRecordCmd_T2UsageFill.getApduRequest());

        SeRequest poRequest =
                new SeRequest(ByteBufferUtils.fromHex(poAid), poApduRequestList, false);
        try {
            SeResponse poResponse = poReader.transmit(poRequest);
            System.out.println("PoResponse: " + poResponse.getApduResponses());
            synchronized (sync) {
                sync.notify();
            }
        } catch (Exception ex) {
            System.out.println("Error: " + ex.getClass() + ":" + ex.getMessage());
        }
    }
}
