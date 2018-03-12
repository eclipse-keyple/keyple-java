/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.examples.pc;

import java.nio.ByteBuffer;
import javax.smartcardio.*;
import org.keyple.seproxy.ByteBufferUtils;

public class PCSCTests {
    public static void main(String[] args) throws CardException, InterruptedException {
        TerminalFactory termFactory = TerminalFactory.getDefault();
        while (true) {
            try {
                for (CardTerminal cardTerm : termFactory.terminals().list()) {
                    System.out.println("Card terminal: " + cardTerm.getName() + " / "
                            + cardTerm.isCardPresent());
                    if (cardTerm.isCardPresent()) {
                        Card card = cardTerm.connect("T=0");
                        ATR atr = card.getATR();
                        System.out.println(
                                "  ATR: " + ByteBufferUtils.toHex(ByteBuffer.wrap(atr.getBytes())));

                        CardChannel channel = card.getBasicChannel();
                        byte[] aid =
                                {(byte) 0xA0, 0x00, 0x00, 0x00, 0x62, 0x03, 0x01, 0x0C, 0x06, 0x01};

                        ResponseAPDU response =
                                channel.transmit(new CommandAPDU(0x00, 0xA4, 0x04, 0x00, aid));
                        System.out.println("  Response: " + response);

                        card.disconnect(false);
                    }
                }
            } catch (Exception ex) {
                System.out.println("Ex: " + ex.getClass() + " : " + ex.getMessage());
            }
            try {
                termFactory.terminals().waitForChange();
            } catch (IllegalStateException ex) {
                System.out.println("Please connect a terminal (" + ex.getMessage() + ")");
                Thread.sleep(5000);
                continue;
            }
            System.out.println("---");
        }
    }
}
