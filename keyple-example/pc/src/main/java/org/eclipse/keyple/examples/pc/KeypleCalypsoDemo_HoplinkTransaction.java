/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.examples.pc;
// PTE

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.eclipse.keyple.calypso.commands.SendableInSession;
import org.eclipse.keyple.calypso.commands.po.PoRevision;
import org.eclipse.keyple.calypso.commands.po.builder.AbstractOpenSessionCmdBuild;
import org.eclipse.keyple.calypso.commands.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.calypso.transaction.PoSecureSession;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.plugin.pcsc.PcscProtocolSetting;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.event.AbstractObservableReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.plugin.AbstractLocalReader;
import org.eclipse.keyple.util.ByteBufferUtils;
import org.eclipse.keyple.util.Observable;

public class KeypleCalypsoDemo_HoplinkTransaction
        implements AbstractObservableReader.Observer<ReaderEvent> {
    private ProxyReader poReader, csmReader;

    public KeypleCalypsoDemo_HoplinkTransaction() {
        super();
    }

    @Override
    public void update(Observable observable, ReaderEvent event) {
        switch (event) {
            case SE_INSERTED:
                System.out.println("SE INSERTED");
                System.out.println("\nStart processing of a Calypso PO");
                operatePoTransaction();
                break;
            case SE_REMOVAL:
                System.out.println("SE REMOVED");
                System.out.println("\nWait for Calypso PO");
                break;
            default:
                System.out.println("IO Error");
        }
    }

    public void operatePoTransaction() {
        PoSecureSession poTransaction = new PoSecureSession(poReader, csmReader, (byte) 0x00);

        try {
            // AID - profile Multi 1 App 1
            String poAid = "A000000291A000000191";// "315449432E49434101FFFFFF0000";
            // Read first record of SFI 06h - for 78h bytes
            ReadRecordsCmdBuild poReadRecordCmd_T2EnvR1 = new ReadRecordsCmdBuild(PoRevision.REV3_1,
                    (byte) 0x01, true, (byte) 0x14, (byte) 0x20);
            // ReadRecordsCmdBuild poReadRecordCmd_06 = new ReadRecordsCmdBuild(PoRevision.REV3_1,
            // (byte) 0x01, true, (byte) 0x06, (byte) 0x78);
            // Read first record of SFI 08h - for 15h bytes
            ReadRecordsCmdBuild poReadRecordCmd_T2UsaR1 = new ReadRecordsCmdBuild(PoRevision.REV3_1,
                    (byte) 0x01, true, (byte) 0x1A, (byte) 0x30);
            // ReadRecordsCmdBuild poReadRecordCmd_08 = new ReadRecordsCmdBuild(PoRevision.REV3_1,
            // (byte) 0x01, true, (byte) 0x08, (byte) 0x15);
            List<SendableInSession> filesToReadInSession = new ArrayList<SendableInSession>();
            filesToReadInSession.add(poReadRecordCmd_T2EnvR1);
            filesToReadInSession.add(poReadRecordCmd_T2UsaR1);

            // Step 1
            System.out.println(
                    "\n\n========= PO Transaction ======= Identification =====================");
            poTransaction.processIdentification(ByteBufferUtils.fromHex(poAid), null);

            // Step 2
            System.out.println(
                    "========= PO Transaction ======= Opening ============================");
            // Read first record of SFI 08h - for 15h bytes
            byte debitKeyIndex = 0x03;
            // Open Session for the debit key #1 - with read of the first record of the cyclic EF of
            // SFI 0Ah
            AbstractOpenSessionCmdBuild poOpenSession =
                    AbstractOpenSessionCmdBuild.create(poTransaction.getRevision(), debitKeyIndex,
                            poTransaction.sessionTerminalChallenge, (byte) 0x1A, (byte) 0x01);
            poTransaction.processOpening(poOpenSession, filesToReadInSession);
            // poTransaction.processOpening(poOpenSession, null);

            // DONE: Find something better
            // poReadRecordCmd_T2EnvR1.getApduRequest().getBuffer().position(0);
            // poReadRecordCmd_T2UsaR1.getApduRequest().getBuffer().position(0);

            // Step 3
            System.out.println(
                    "========= PO Transaction ======= Continuation =======================");
            poTransaction.processProceeding(filesToReadInSession);

            // DONE: Find something better
            // poReadRecordCmd_T2EnvR1.getApduRequest().getBuffer().position(0);

            // Step 4
            System.out.println(
                    "========= PO Transaction ======= Closing ============================");
            // poTransaction.processClosing(filesToReadInSession,
            // poAnticipatedResponseInsideSession, poReadRecordCmd_06); // TODO - to complete
            // support of poAnticipatedResponseInsideSession
            poTransaction.processClosing(null, null, poReadRecordCmd_T2EnvR1);

            if (poTransaction.isSuccessful()) {
                System.out.println(
                        "========= PO Transaction ======= SUCCESS !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            } else {
                System.out.println(
                        "========= PO Transaction ======= ERROR !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // This is where you should add patterns of readers you want to use for tests
    private static final String poReaderName = ".*(ASK|ACS).*";
    private static final String csmReaderName = ".*(Cherry TC|SCM Microsystems|Identive).*";

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

    public static void main(String[] args)
            throws IOException, IOReaderException, InterruptedException {
        SeProxyService seProxyService = SeProxyService.getInstance();
        List<ReadersPlugin> pluginsSet = new ArrayList<ReadersPlugin>();
        pluginsSet.add(PcscPlugin.getInstance().setLogging(true));
        seProxyService.setPlugins(pluginsSet);

        ProxyReader poReader = getReader(seProxyService, poReaderName);
        ProxyReader csmReader = getReader(seProxyService, csmReaderName);


        if (poReader == csmReader || poReader == null || csmReader == null) {
            throw new IllegalStateException("Bad PO/CSM setup");
        }

        System.out.println("PO Reader  : " + poReader.getName());
        System.out.println("CSM Reader : " + csmReader.getName());

        poReader.setParameter(PcscReader.SETTING_KEY_PROTOCOL, PcscReader.SETTING_PROTOCOL_T1);
        csmReader.setParameter(PcscReader.SETTING_KEY_PROTOCOL, PcscReader.SETTING_PROTOCOL_T0);

        // provide the reader with the map
        ((AbstractLocalReader) poReader)
                .addSeProtocolSetting(PcscProtocolSetting.SETTING_PROTOCOL_ISO14443_4);

        // Setting up ourself as an observer
        KeypleCalypsoDemo_HoplinkTransaction observer = new KeypleCalypsoDemo_HoplinkTransaction();
        observer.poReader = poReader;
        observer.csmReader = csmReader;

        // Set terminal as Observer of the first reader
        ((AbstractObservableReader) poReader).addObserver(observer);
        synchronized (waitForEnd) {
            waitForEnd.wait();
        }
    }


}
