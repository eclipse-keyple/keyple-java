/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.plugin.pcsc.log;

import java.nio.ByteBuffer;
import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import org.keyple.seproxy.ByteBufferUtils;

public class CardLogger extends Card {

    private final String id;

    private final Card card;

    CardLogger(String id, Card card) {
        this.id = id;
        this.card = card;
    }

    @Override
    public ATR getATR() {
        ATR atr = card.getATR();
        Logging.LOG.info("Card: Get ATR", "action", "card.atr", "cardId", id, "atr",
                ByteBufferUtils.toHex(ByteBuffer.wrap(atr.getBytes())));
        return atr;
    }

    @Override
    public String getProtocol() {
        return card.getProtocol();
    }

    @Override
    public CardChannel getBasicChannel() {
        Logging.LOG.info("Card: Get basic channel", "action", "card.get_basic_channel", "cardId",
                id);
        return new CardChannelLogger(id + "B", card.getBasicChannel());
    }

    @Override
    public CardChannel openLogicalChannel() throws CardException {
        Logging.LOG.info("Card: Get logical channel", "action", "card.get_logical_channel",
                "cardId", id);
        return new CardChannelLogger(id + "L", card.openLogicalChannel());
    }

    @Override
    public void beginExclusive() throws CardException {
        Logging.LOG.info("Card: Begin exclusive", "action", "card.begin_exclusive", "cardId", id);
        card.beginExclusive();
    }

    @Override
    public void endExclusive() throws CardException {
        Logging.LOG.info("Card: End exclusive", "action", "card.end_exclusive", "cardId", id);
        card.endExclusive();
    }

    @Override
    public byte[] transmitControlCommand(int controlCommand, byte[] command) throws CardException {
        long before = System.nanoTime();
        Logging.LOG.info("Card: Sending control command", "action", "card.control_request",
                "cardId", id, "controlCommand", controlCommand, "command",
                ByteBufferUtils.toHex(ByteBuffer.wrap(command)));
        byte[] response = card.transmitControlCommand(controlCommand, command);
        double elapsedMs = (double) ((System.nanoTime() - before) / 100000) / 10;
        Logging.LOG.info("Card: Receiving control command", "action", "card.control_response",
                "cardId", id, "controlCommand", controlCommand, "response",
                ByteBufferUtils.toHex(ByteBuffer.wrap(response)), "elapsedMs", elapsedMs);
        return response;
    }

    @Override
    public void disconnect(boolean reset) throws CardException {
        Logging.LOG.info("Card: Disconnect", "action", "card.disconnect", "cardId", id, "reset",
                reset);
        card.disconnect(reset);
    }
}
