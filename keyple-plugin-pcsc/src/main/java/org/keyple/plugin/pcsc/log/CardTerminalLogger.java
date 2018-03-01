/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.plugin.pcsc.log;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

public class CardTerminalLogger extends CardTerminal {
    private final String name;
    private final CardTerminal terminal;

    private int count = 0;

    public CardTerminalLogger(String name, CardTerminal terminal) {
        this.name = name;
        this.terminal = terminal;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Card connect(String s) throws CardException {
        System.out.println(name + ".connect()");
        return new CardLogger(String.format("%s.%d", name, ++count), terminal.connect(s));
    }

    @Override
    public boolean isCardPresent() throws CardException {
        boolean present = terminal.isCardPresent();
        System.out.println(name + ".isCardPresent(): " + present);
        return present;
    }

    @Override
    public boolean waitForCardPresent(long l) throws CardException {
        System.out.println(name + ".waitForCardPresent( " + l + ")... ");
        boolean present = terminal.waitForCardPresent(l);
        System.out.println(name + ".waitForCardPresent(): " + present);
        return present;
    }

    @Override
    public boolean waitForCardAbsent(long l) throws CardException {
        System.out.println(name + ".waitForCardAbsent(" + l + ")... ");
        boolean present = terminal.waitForCardAbsent(l);
        System.out.println(name + ".waitForCardAbsent(): " + present);
        return present;
    }
}
