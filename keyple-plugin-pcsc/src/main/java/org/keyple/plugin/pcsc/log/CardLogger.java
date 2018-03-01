/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.plugin.pcsc.log;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import org.apache.commons.codec.binary.Hex;

public class CardLogger extends Card {

    private final String name;

    private final Card card;

    CardLogger(String name, Card card) {
        this.name = name;
        this.card = card;
    }

    @Override
    public ATR getATR() {
        ATR atr = card.getATR();
        System.out.println(name + ".getATR(): " + atr);
        return atr;
    }

    @Override
    public String getProtocol() {
        return card.getProtocol();
    }

    @Override
    public CardChannel getBasicChannel() {
        System.out.println(name + ".getBasicChannel()");
        return new CardChannelLogger(name + "B", card.getBasicChannel());
    }

    @Override
    public CardChannel openLogicalChannel() throws CardException {
        System.out.println(name + ".openLogicalChannel()");
        return new CardChannelLogger(name + "L", card.openLogicalChannel());
    }

    @Override
    public void beginExclusive() throws CardException {
        System.out.println(name + ".beginExclusive()");
        card.beginExclusive();
    }

    @Override
    public void endExclusive() throws CardException {
        System.out.println(name + ".endExclusive()");
        card.endExclusive();
    }

    @Override
    public byte[] transmitControlCommand(int i, byte[] bytes) throws CardException {
        System.out.println(name + ".transmitControlCommand(" + i + ", "
                + Hex.encodeHexString(bytes, false) + ") ... ");
        byte[] data = card.transmitControlCommand(i, bytes);
        System.out.println(name + ".transmitControlCommand(): " + Hex.encodeHexString(data, false));
        return data;
    }

    @Override
    public void disconnect(boolean b) throws CardException {
        System.out.println(name + ".disconnect(" + b + ");");
        card.disconnect(b);
    }
}
