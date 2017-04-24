package cna.sdk.plugin.smartcardio;

import java.util.ArrayList;
import java.util.List;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cna.sdk.seproxy.APDURequest;
import cna.sdk.seproxy.APDUResponse;
import cna.sdk.seproxy.ProxyReader;
import cna.sdk.seproxy.SERequest;
import cna.sdk.seproxy.SEResponse;

@SuppressWarnings("restriction")
public class SmartCardIOReader implements ProxyReader {

    static final Logger logger = LoggerFactory.getLogger(SmartCardIOReader.class);


    private String name;

    private CardTerminal terminal;

    public SmartCardIOReader(CardTerminal terminal, String name) {
        this.terminal = terminal;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SEResponse transmit(SERequest seApplicationRequest) {

        List<APDUResponse> apduResponseList = new ArrayList<>();
        APDUResponse fciResponse = null;

        CardChannel channel;
        try {
            channel = getChannel();
        } catch (CardException e) {
            logger.error("Error executing command", e);
            return null;
        }

        for (APDURequest apduRequest : seApplicationRequest.getApduRequests()) {
            logger.debug(getName() + " : Sending : " + DatatypeConverter.printHexBinary(apduRequest.getbytes()));
            if (isSEPresent()) {
                ResponseAPDU responseFciData;
                try {
                    responseFciData = channel.transmit(new CommandAPDU(apduRequest.getbytes()));
                    byte[] statusCode = new byte[] { (byte) responseFciData.getSW1(), (byte) responseFciData.getSW2() };
                    logger.debug(
                            getName() + " : Recept : " + DatatypeConverter.printHexBinary(responseFciData.getBytes()));
                    apduResponseList.add(new APDUResponse(responseFciData.getData(), true, statusCode));
                } catch (CardException e) {
                    logger.error(getName() + " : Error executing command", e);
                    apduResponseList.add(new APDUResponse(null, false, null));
                    break;
                }

            } else {
                return null;
            }

        }
        if (!seApplicationRequest.askKeepChannelOpen()) {
            this.disconnect();
        }
        if (seApplicationRequest.getAidToSelect() != null) {
            fciResponse = apduResponseList.remove(0);
        }
        return new SEResponse(false, fciResponse, apduResponseList);
    }

    @Override
    public boolean isSEPresent() {
        boolean sePresent = false;
        try {
            sePresent = terminal.isCardPresent();
        } catch (CardException e) {
            logger.error("Card not accessible", e);
        }
        return sePresent;
    }

    /**
     * method to get the channel to communicate with the card
     *
     * @return the default channel
     * @throws CardException
     */

    private CardChannel getChannel() throws CardException {
        // connection avec un terminal
        Card card = terminal.connect("*");
        return card.getBasicChannel();
    }

    /**
     * method to disconnect the card from the terminal
     *
     * @throws CardException
     *
     */
    private void disconnect() {
        // connection avec le terminal
        try {
            Card card = terminal.connect("*");
            card.disconnect(true);
        } catch (CardException e) {
            logger.error("Error while disconnecting", e);

        }

    }

}
