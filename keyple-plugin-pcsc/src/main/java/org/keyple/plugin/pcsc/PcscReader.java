/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.plugin.pcsc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.xml.bind.DatatypeConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.keyple.seproxy.ApduRequest;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.ConfigurableReader;
import org.keyple.seproxy.ObservableReader;
import org.keyple.seproxy.ReaderEvent;
import org.keyple.seproxy.SeRequest;
import org.keyple.seproxy.SeResponse;
import org.keyple.seproxy.exceptions.ChannelStateReaderException;
import org.keyple.seproxy.exceptions.IOReaderException;
import org.keyple.seproxy.exceptions.InvalidApduReaderException;
import org.keyple.seproxy.exceptions.TimeoutReaderException;
import org.keyple.seproxy.exceptions.UnexpectedReaderException;

public class PcscReader extends ObservableReader implements ConfigurableReader {

    static final Logger logger = LogManager.getLogger(PcscReader.class);

    private String name;

    private CardTerminal terminal;
    private CardChannel channel;
    private Card card;

    private byte[] aidCurrentlySelected;
    private ApduResponse fciDataSelected;
    private boolean atrDefaultSelected = false;

    private Map<String, String> settings;

    // private Thread readerThread;
    private EventThread readerThread;


    protected PcscReader(CardTerminal terminal, String name) { // PcscReader constructor may be
                                                               // called only by PcscPlugin
        this.terminal = terminal;
        this.name = name;
        this.card = null;
        this.channel = null;
        this.settings = new HashMap<String, String>();

        // TODO je n'ai pas compris l'implémentation Ixxi
        // EventThread eventThread = new EventThread(this);
        // this.readerThread = new Thread(eventThread);
        // this.readerThread.start();
        readerThread = new EventThread(this);
        readerThread.start();

        // TODO to check start & stop of the thread
    }


    // TODO
    @SuppressWarnings("deprecation")
    protected void finalize() throws Throwable {
        readerThread.stopPolling();
        readerThread.stop(); // TODO faut-il quand même fermer la thread
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public SeResponse transmit(SeRequest seApplicationRequest)
            throws ChannelStateReaderException, InvalidApduReaderException, IOReaderException,
            TimeoutReaderException, UnexpectedReaderException {
        List<ApduResponse> apduResponseList = new ArrayList<ApduResponse>();

        if (isSEPresent()) { // TODO si vrai ET pas vrai => retourne un SeResponse vide de manière
                             // systématique - return new SeResponse(false, fciDataSelected,
                             // apduResponseList);
            try {
                this.prepareAndConnectToTerminalAndSetChannel();
            } catch (CardException e) {
                throw new ChannelStateReaderException(e.getMessage());
            }
            // gestion du select application ou du getATR
            if (seApplicationRequest.getAidToSelect() != null && aidCurrentlySelected == null) {
                fciDataSelected = this.connect(seApplicationRequest.getAidToSelect());
            } else if (!atrDefaultSelected) {
                fciDataSelected = new ApduResponse(card.getATR().getBytes(), true,
                        new byte[] {(byte) 0x90, (byte) 0x00});
                atrDefaultSelected = true;
            }
            for (ApduRequest apduRequest : seApplicationRequest.getApduRequests()) {
                logger.info(getName() + " : Sending : " + formatLogRequest(apduRequest.getbytes()));
                ResponseAPDU apduResponseData;
                try {
                    System.out.println(terminal.getName());
                    System.out.println(settings.get("protocol") + " > "
                            + DatatypeConverter.printHexBinary(apduRequest.getbytes()));
                    apduResponseData = channel.transmit(new CommandAPDU(apduRequest.getbytes()));
                    System.out.println(settings.get("protocol") + " < "
                            + DatatypeConverter.printHexBinary(apduResponseData.getBytes()));

                    byte[] statusCode = new byte[] {(byte) apduResponseData.getSW1(),
                            (byte) apduResponseData.getSW2()};
                    logger.info(getName() + " : Recept : "
                            + DatatypeConverter.printHexBinary(apduResponseData.getData()) + " "
                            + DatatypeConverter.printHexBinary(statusCode));

                    // gestion du getResponse en case 4 avec reponse valide et
                    // retour vide
                    hackCase4AndGetResponse(apduRequest.isCase4(), statusCode, apduResponseData,
                            channel);

                    apduResponseList
                            .add(new ApduResponse(apduResponseData.getData(), true, statusCode));
                } catch (CardException e) {
                    throw new ChannelStateReaderException(e.getMessage());
                } catch (NullPointerException e) {
                    logger.error(getName() + " : Error executing command", e);
                    apduResponseList.add(new ApduResponse(null, false, null));
                    break;
                }
            }

            if (!seApplicationRequest.askKeepChannelOpen()) {
                logger.info("disconnect");
                this.disconnect();
            }
        }

        return new SeResponse(false, fciDataSelected, apduResponseList);
    }

    private void prepareAndConnectToTerminalAndSetChannel() throws CardException {
        String protocol = "";
        if (card == null) {
            protocol = settings.containsKey("protocol") ? settings.get("protocol") : "*";
            System.out.println(terminal.getName());
            System.out.println(settings.get("protocol")
                    + " - connect(protocol)\t\tfrom prepareAndConnectToTerminalAndSetChannel()");
            this.card = this.terminal.connect(protocol);
        }
        this.channel = card.getBasicChannel();
    }

    private static void hackCase4AndGetResponse(boolean isCase4, byte[] statusCode,
            ResponseAPDU responseFciData, CardChannel channel) throws CardException {
        if (isCase4 && statusCode[0] == (byte) 0x90 && statusCode[1] == (byte) 0x00
                && responseFciData.getData().length == 0) {
            byte[] command = new byte[5];
            command[0] = (byte) 0x00;
            command[1] = (byte) 0xC0;
            command[2] = (byte) 0x00;
            command[3] = (byte) 0x00;
            command[4] = (byte) 0x00;
            logger.info(" Send GetResponse : " + formatLogRequest(command));

            System.out.println("Hack - Get Response");
            responseFciData = channel.transmit(new CommandAPDU(command));

            statusCode[0] = (byte) responseFciData.getSW1();
            statusCode[1] = (byte) responseFciData.getSW2();
        }
    }

    private static String formatLogRequest(byte[] request) {
        String c = DatatypeConverter.printHexBinary(request);
        String log = c;

        if (request.length == 4) {
            log = extractData(c, 0, 2) + " " + extractData(c, 2, 4) + " " + extractData(c, 4, 8);
        }
        if (request.length == 5) {
            log = extractData(c, 0, 2) + " " + extractData(c, 2, 4) + " " + extractData(c, 4, 8)
                    + " " + extractData(c, 8, 10);
        } else if (request.length > 5) {
            log = extractData(c, 0, 2) + " " + extractData(c, 2, 4) + " " + extractData(c, 4, 8)
                    + " " + extractData(c, 8, 10) + " " + extractData(c, 10, c.length());
        }

        return log;
    }

    private static String extractData(String str, int pos1, int pos2) {
        String data = "";
        for (int i = pos1; i < pos2; i++) {
            data += str.charAt(i);
        }
        return data;
    }

    @Override
    public boolean isSEPresent() throws IOReaderException {
        boolean sePresent = false;

        try {
            sePresent = terminal.isCardPresent();
        } catch (CardException e) {
            throw new IOReaderException(e.getMessage(), e);
        }

        return sePresent;
    }

    /**
     * method to connect to the card from the terminal
     *
     * @throws ChannelStateReaderException
     */
    private ApduResponse connect(byte[] aid) throws ChannelStateReaderException {
        try {
            // if (aid != null) {
            // generate select application command
            byte[] command = new byte[aid.length + 5];
            command[0] = (byte) 0x00;
            command[1] = (byte) 0xA4;
            command[2] = (byte) 0x04;
            command[3] = (byte) 0x00;
            command[4] = Byte.decode("" + aid.length);
            System.arraycopy(aid, 0, command, 5, aid.length);
            logger.info(getName() + " : Send AID : " + formatLogRequest(command));

            System.out.println(terminal.getName()
                    + "\t\t PC/SC Select Application\t\tfrom PcscReader.connect(byte[] aid)");
            System.out.println(
                    settings.get("protocol") + " > " + DatatypeConverter.printHexBinary(command));

            ResponseAPDU res = channel.transmit(new CommandAPDU(command));
            System.out.println(settings.get("protocol") + " < "
                    + DatatypeConverter.printHexBinary(res.getBytes()));

            byte[] statusCode = new byte[] {(byte) res.getSW1(), (byte) res.getSW2()};
            hackCase4AndGetResponse(true, statusCode, res, channel);
            logger.info(
                    getName() + " : Recept : " + DatatypeConverter.printHexBinary(res.getBytes()));
            ApduResponse fciResponse =
                    new ApduResponse(res.getData(), true, new byte[] {(byte) 0x90, (byte) 0x00});
            aidCurrentlySelected = aid;
            return fciResponse;

            // }
        } catch (CardException e1) {
            throw new ChannelStateReaderException(e1.getMessage());
        }
        // return null;
    }

    /**
     * method to disconnect the card from the terminal
     *
     * @throws IOReaderException
     *
     * @throws CardException
     *
     */
    private void disconnect() throws IOReaderException {

        try {
            aidCurrentlySelected = null;
            fciDataSelected = null;
            atrDefaultSelected = false;

            if (this.card != null) {
                this.channel = null;
                System.out.println(terminal.getName());
                System.out.println(settings.get("protocol")
                        + " - disconnect(FALSE)\t\tfrom PcscReader.disconnect()");
                this.card.disconnect(false);
                this.card = null;
            }
        } catch (CardException e) {
            throw new IOReaderException(e.getMessage(), e);
        }

    }

    /*
     * TODO Paramètres PC/SC dont le support est à intégré paramètre 'Protocol' pouvant prendre les
     * valeurs String 'T0', 'T1', 'Tx' paramètre 'Mode' pouvant prendre les valeurs String 'Shared',
     * 'Exclusive', 'Direct' paramètre 'Disconnect' pouvant prendre les valeurs String 'Leave',
     * 'Reset', 'Unpower', 'Eject' Il s'agit des valeurs de paramètre définies par le standard
     * 'PC/SC'.
     * 
     * Si on traduit ses paramètres pour l'API SmartCard IO cela donne: pour 'Protocol' :
     * javax.smartcardio.CardTerminal.connect(String protocol) paramétré avec "T=0" si 'T0', "T=1"
     * si 'T1', "*" si 'Tx' => voir définition
     * https://docs.oracle.com/javase/6/docs/jre/api/security/smartcardio/spec/javax/smartcardio/
     * CardTerminal.html#connect(java.lang.String) le comportement par défaut pour 'Protocol' doit
     * être 'Tx'
     * 
     * paramètre 'Mode' : le comportement par défaut pour 'Protocol' doit être 'Exclusive', dans ce
     * cas une exclusivité d'accès est gérée via javax.smartcardio.Card.beginExclusive() et
     * endExclusive() cf.
     * https://docs.oracle.com/javase/6/docs/jre/api/security/smartcardio/spec/javax/smartcardio/
     * Card.html#beginExclusive() sinon le 'Mode' doit être considéré comme 'Shared' à vérifier avec
     * Jean-Pierre Fortune, le mode 'Direct' ne devrait pas être supporté pour un
     * ProxyReader.transmit(), (l'envoi de commandes directes de paramétrage du lecteur PC/SC
     * devrait se faire avec un setParameter spécial)
     * 
     * Pour 'Disconnect', un paramétrage 'Reset', fera que la commande
     * javax.smartcardio.Carddisconnect(boolean reset) sera paramétrée à 'true' si 'Reset', à
     * 'false' si 'Unpower' Les valeurs 'Leave' et 'Eject' ne serait pas gérée.
     * 
     */
    @Override
    public void setParameters(Map<String, String> settings) {
        this.settings.putAll(settings);
    }

    @Override
    public void setAParameter(String key, String value) {
        this.settings.put(key, value);
    }

    @Override
    public Map<String, String> getParameters() {
        return this.settings;
    }

    /**
     * @author yann.herriau
     *
     *         To implement Notifications, SmarcardIoReader use a Thread for card insertion or
     *         removal detection
     *
     */
    // public class EventThread implements Runnable { // TODO implémentation Ixxi, pas compris
    public class EventThread extends Thread {
        PcscReader reader;

        public EventThread(PcscReader reader) {
            this.reader = reader;
        }

        // TODO vérifier conditions de fermeture
        private volatile boolean running = true;

        public void stopPolling() {
            running = false;
        }

        public void run() {
            // while (true) {
            // try {
            // terminal.waitForCardPresent(0);
            // reader.notifyObservers(new ReaderEvent(reader, ReaderEvent.EventType.SE_INSERTED));
            // terminal.waitForCardAbsent(0);
            // System.out.println(terminal.getName());
            // System.out.println(settings.get("protocol") + " - disconnect()\t\tfrom
            // PcscReader.EventThread.run()");
            // reader.disconnect();
            // reader.notifyObservers(new ReaderEvent(reader, ReaderEvent.EventType.SE_REMOVAL));

            while (running) {
                try {
                    if (terminal.isCardPresent()) {
                        terminal.waitForCardAbsent(0);
                        // TODO to clean logs
                        System.out.println(terminal.getName() + "\tSE removed");
                        System.out.println(settings.get("protocol")
                                + " - disconnect()\t\tfrom PcscReader.EventThread.run()");
                        reader.disconnect();
                        reader.notifyObservers(
                                new ReaderEvent(reader, ReaderEvent.EventType.SE_REMOVAL));
                    } else {
                        terminal.waitForCardPresent(0);
                        reader.notifyObservers(
                                new ReaderEvent(reader, ReaderEvent.EventType.SE_INSERTED));
                        // TODO to clean logs
                        System.out.println(terminal.getName() + "\tSE inserted");
                    }
                } catch (CardException e) {
                    reader.notifyObservers(new ReaderEvent(reader, ReaderEvent.EventType.IO_ERROR));
                } catch (IOReaderException e) {
                    reader.notifyObservers(new ReaderEvent(reader, ReaderEvent.EventType.IO_ERROR));
                }
            }
        }
    }

}
