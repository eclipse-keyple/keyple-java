/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.plugin.pcsc.log;

import java.nio.ByteBuffer;
import javax.smartcardio.*;
import org.keyple.seproxy.ByteBufferUtils;

/**
 * Card channel logging helper. This should allow to properly monitor any exchange being done with
 * the card. The behavior of the {@link CardChannel} is not transformed by this class.
 */
public class CardChannelLogger extends CardChannel {

    private final String name;
    private final CardChannel cardChannel;

    /**
     * Creator of the new card channel logger
     *
     * @param name Name of the logger
     * @param cardChannel Inner card channel being used
     */
    CardChannelLogger(String name, CardChannel cardChannel) {
        this.name = name;
        this.cardChannel = cardChannel;
    }

    @Override
    public Card getCard() {
        return cardChannel.getCard();
    }

    @Override
    public int getChannelNumber() {
        return cardChannel.getChannelNumber();
    }

    @Override
    public ResponseAPDU transmit(CommandAPDU commandAPDU) throws CardException {
        Logging.LOG.info("CardChannel: Request", "action", "card_channel.request", "cardChannelId",
                name, "apdu", ByteBufferUtils.toHex(ByteBuffer.wrap(commandAPDU.getBytes())));
        long before = System.nanoTime();
        ResponseAPDU response = cardChannel.transmit(commandAPDU);
        double elapsedMs = (double) ((System.nanoTime() - before) / 100000) / 10;
        Logging.LOG.info("CardChannel: Response", "action", "card_channel.response",
                "cardChannelId", name, "apdu",
                ByteBufferUtils.toHex(ByteBuffer.wrap(response.getBytes())), "elapsedMs",
                elapsedMs);
        return response;
    }

    @Override
    public int transmit(ByteBuffer in, ByteBuffer out) throws CardException {
        Logging.LOG.info("CardChannel: Request", "action", "card_channel.request", "cardChannelId",
                name, "apdu", ByteBufferUtils.toHex(in));
        long before = System.nanoTime();
        int rc = cardChannel.transmit(in, out);
        double elapsedMs = (double) ((System.nanoTime() - before) / 100000) / 10;
        Logging.LOG.info("CardChannel: Response", "action", "card_channel.response",
                "cardChannelId", name, "apdu", ByteBufferUtils.toHex(out), "rc", rc, "elapsedMs",
                elapsedMs);
        return rc;
    }

    @Override
    public void close() throws CardException {
        Logging.LOG.info("CardChannel: Close", "action", "card_channel.close", "cardChannelId",
                name);
        cardChannel.close();
    }

    @Override
    public String toString() {
        return String.format("CardChannelLogger{id=%s,inner=%s}", name, cardChannel);
    }
}
