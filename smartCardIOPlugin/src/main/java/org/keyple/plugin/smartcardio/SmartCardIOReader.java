package org.keyple.plugin.smartcardio;

import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.List;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.xml.bind.DatatypeConverter;

import org.keyple.seproxy.APDURequest;
import org.keyple.seproxy.APDUResponse;
import org.keyple.seproxy.ProxyReader;
import org.keyple.seproxy.ReaderEvent;
import org.keyple.seproxy.ReaderObserver;
import org.keyple.seproxy.SERequest;
import org.keyple.seproxy.SEResponse;
import org.keyple.seproxy.exceptions.IOReaderException;
import org.keyple.seproxy.exceptions.POCardException;
import org.keyple.seproxy.exceptions.ReaderException;
import org.keyple.seproxy.exceptions.UnexpectedReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public SEResponse transmit(SERequest seApplicationRequest) throws ReaderException  {

        List<APDUResponse> apduResponseList = new ArrayList<>();
        APDUResponse fciResponse = null;

        CardChannel channel;
        try {
            channel = getChannel();
        } catch (CardException e) {
            logger.error("Error executing command", e);
//            return null;
            throw new UnexpectedReaderException("Error when getting channel", e.getCause());
        }

        for (APDURequest apduRequest : seApplicationRequest.getApduRequests()) {
            logger.info(getName() + " : Sending : " + DatatypeConverter.printHexBinary(apduRequest.getbytes()));
            boolean sEIsPresent = false;
            try {
            	sEIsPresent = isSEPresent();
            }catch (ReaderException e) {
            	throw new IOReaderException("Card not detected", e.getCause());
			}
            
            if (sEIsPresent) {
                ResponseAPDU responseFciData;
                try {
                    responseFciData = channel.transmit(new CommandAPDU(apduRequest.getbytes()));
                    byte[] statusCode = new byte[] { (byte) responseFciData.getSW1(), (byte) responseFciData.getSW2() };
                    logger.info(
                            getName() + " : Recept : " + DatatypeConverter.printHexBinary(responseFciData.getBytes()));
                    
//                    if (seApplicationRequest.getAidToSelect() != null ) {
//                    	if(apduRequest.getInstruction().equals()))
//                        fciResponse = apduResponseList.remove(0);
//                    }
                    apduResponseList.add(new APDUResponse(responseFciData.getData(), true, statusCode));
                } catch (CardException | NullPointerException e) {
                    logger.error(getName() + " : Error executing command", e);
                    apduResponseList.add(new APDUResponse(null, false, null));
                    break;
                }
            } 
        }
        if (!seApplicationRequest.askKeepChannelOpen()) {
            this.disconnect();
        }
       
        return new SEResponse(false, fciResponse, apduResponseList);
    }

    @Override
    public boolean isSEPresent() throws ReaderException{
        boolean sePresent = false;
        try {
            sePresent = terminal.isCardPresent();
        } catch (CardException e) {
            logger.error("Card not accessible", e);
            throw new ReaderException("Card not accessible", e.getCause());
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
