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
import org.apache.commons.codec.binary.Hex;
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
    public CardChannelLogger(String name, CardChannel cardChannel) {
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
        System.out.println(
                name + ".transmit(" + Hex.encodeHexString(commandAPDU.getBytes()) + ") ... ");
        ResponseAPDU response = cardChannel.transmit(commandAPDU);
        System.out.println(name + ".transmit(...): " + commandAPDU);
        return response;
    }

    @Override
    public int transmit(ByteBuffer in, ByteBuffer out) throws CardException {
        System.out.println(name + ".transmit(" + ByteBufferUtils.toHex(in) + ") ... ");
        int rc = cardChannel.transmit(in, out);
        System.out
                .println(name + ".transmit(...): rc=" + rc + ", out=" + ByteBufferUtils.toHex(out));
        return rc;
    }

    @Override
    public void close() throws CardException {
        cardChannel.close();
    }

    @Override
    public String toString() {
        return String.format("CardChannelLogger{name=%s,inner=%s}", name, cardChannel);
    }
}
