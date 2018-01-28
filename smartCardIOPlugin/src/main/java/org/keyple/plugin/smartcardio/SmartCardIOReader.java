package org.keyple.plugin.smartcardio;

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

import org.keyple.seproxy.ApduRequest;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.ConfigurableReader;
import org.keyple.seproxy.NotifierReader;
import org.keyple.seproxy.ReaderEvent;
import org.keyple.seproxy.SeRequest;
import org.keyple.seproxy.SeResponse;
import org.keyple.seproxy.exceptions.ChannelStateReaderException;
import org.keyple.seproxy.exceptions.IOReaderException;
import org.keyple.seproxy.exceptions.InvalidApduReaderException;
import org.keyple.seproxy.exceptions.TimeoutReaderException;
import org.keyple.seproxy.exceptions.UnexpectedReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmartCardIOReader extends NotifierReader implements ConfigurableReader {

    static final Logger logger = LoggerFactory.getLogger(SmartCardIOReader.class);

    private String name;

    private CardTerminal terminal;
    private CardChannel channel;
    private Card card;

    private byte[] aidCurrentlySelected;
    private ApduResponse fciDataSelected;
    private boolean atrDefaultSelected = false;

    private Map<String, String> settings;

    private Thread readerThread;

    public SmartCardIOReader(CardTerminal terminal, String name) {
        this.terminal = terminal;
        this.name = name;
        this.card = null;
        this.channel = null;
        this.settings = new HashMap<String, String>();
        EventThread eventThread = new EventThread(this);
        this.readerThread = new Thread(eventThread);
        this.readerThread.start();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SeResponse transmit(SeRequest seApplicationRequest) throws ChannelStateReaderException,
            InvalidApduReaderException, IOReaderException, TimeoutReaderException, UnexpectedReaderException {
        List<ApduResponse> apduResponseList = new ArrayList<ApduResponse>();

        if (isSEPresent()) {
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
                        new byte[] { (byte) 0x90, (byte) 0x00 });
                atrDefaultSelected = true;
            }
            for (ApduRequest apduRequest : seApplicationRequest.getApduRequests()) {
                logger.info(getName() + " : Sending : " + formatLogRequest(apduRequest.getbytes()));
                ResponseAPDU apduResponseData;
                try {
                    apduResponseData = channel.transmit(new CommandAPDU(apduRequest.getbytes()));
                    byte[] statusCode = new byte[] { (byte) apduResponseData.getSW1(),
                            (byte) apduResponseData.getSW2() };
                    logger.info(
                            getName() + " : Recept : " + DatatypeConverter.printHexBinary(apduResponseData.getData())
                                    + " " + DatatypeConverter.printHexBinary(statusCode));

                    // gestion du getResponse en case 4 avec reponse valide et
                    // retour vide
                    if (apduRequest.isCase4() && statusCode[0] == (byte) 0x90 && statusCode[1] == (byte) 0x00
                            && apduResponseData.getData().length == 0) {
                        hackCase4AndGetResponse(apduRequest.isCase4(), statusCode, apduResponseData, channel);
                    }

                    apduResponseList.add(new ApduResponse(apduResponseData.getData(), true, statusCode));
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
            this.card = this.terminal.connect(protocol);
        }
        this.channel = card.getBasicChannel();
    }

    private static void hackCase4AndGetResponse(boolean isCase4, byte[] statusCode, ResponseAPDU responseFciData,
            CardChannel channel) throws CardException {

        byte[] command = new byte[5];
        command[0] = (byte) 0x00;
        command[1] = (byte) 0xC0;
        command[2] = (byte) 0x00;
        command[3] = (byte) 0x00;
        command[4] = (byte) 0x00;
        logger.info(" Send GetResponse : " + formatLogRequest(command));
        responseFciData = channel.transmit(new CommandAPDU(command));
        statusCode[0] = (byte) responseFciData.getSW1();
        statusCode[1] = (byte) responseFciData.getSW2();

    }

    private static String formatLogRequest(byte[] request) {
        String c = DatatypeConverter.printHexBinary(request);
        String log = c;

        if (request.length == 4) {
            log = extractData(c, 0, 2) + " " + extractData(c, 2, 4) + " " + extractData(c, 4, 8);
        }
        if (request.length == 5) {
            log = extractData(c, 0, 2) + " " + extractData(c, 2, 4) + " " + extractData(c, 4, 8) + " "
                    + extractData(c, 8, 10);
        } else if (request.length > 5) {
            log = extractData(c, 0, 2) + " " + extractData(c, 2, 4) + " " + extractData(c, 4, 8) + " "
                    + extractData(c, 8, 10) + " " + extractData(c, 10, c.length());
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
            // generate select application command
            byte[] command = new byte[aid.length + 5];
            command[0] = (byte) 0x00;
            command[1] = (byte) 0xA4;
            command[2] = (byte) 0x04;
            command[3] = (byte) 0x00;
            command[4] = Byte.decode("" + aid.length);
            System.arraycopy(aid, 0, command, 5, aid.length);
            logger.info(getName() + " : Send AID : " + formatLogRequest(command));

            ResponseAPDU res = channel.transmit(new CommandAPDU(command));
            byte[] statusCode = new byte[] { (byte) res.getSW1(), (byte) res.getSW2() };
            if (statusCode[0] == (byte) 0x90 && statusCode[1] == (byte) 0x00 && res.getData().length == 0) {
                hackCase4AndGetResponse(true, statusCode, res, channel);
            }
            logger.info(getName() + " : Recept : " + DatatypeConverter.printHexBinary(res.getBytes()));
            ApduResponse fciResponse = new ApduResponse(res.getData(), true, new byte[] { (byte) 0x90, (byte) 0x00 });
            aidCurrentlySelected = aid;
            return fciResponse;

        } catch (CardException e1) {
            throw new ChannelStateReaderException(e1.getMessage());
        }
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
                this.card.disconnect(false);
                this.card = null;
            }
        } catch (CardException e) {
            throw new IOReaderException(e.getMessage(), e);
        }

    }

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
     *         To implement Notifications, SmarcardIoReader use a Thread for
     *         card insertion or removal detection
     *
     */
    public class EventThread implements Runnable {
        SmartCardIOReader reader;

        public EventThread(SmartCardIOReader reader) {
            this.reader = reader;
        }

        public void run() {
            while (true) {
                try {
                    terminal.waitForCardPresent(0);
                    reader.notifyObservers(new ReaderEvent(reader, ReaderEvent.EventType.SE_INSERTED));
                    terminal.waitForCardAbsent(0);
                    reader.disconnect();
                    reader.notifyObservers(new ReaderEvent(reader, ReaderEvent.EventType.SE_REMOVAL));
                } catch (CardException e) {
                    reader.notifyObservers(new ReaderEvent(reader, ReaderEvent.EventType.IO_ERROR));
                } catch (IOReaderException e) {
                    reader.notifyObservers(new ReaderEvent(reader, ReaderEvent.EventType.IO_ERROR));
                }
            }
        }
    }

}
