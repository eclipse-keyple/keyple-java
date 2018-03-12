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
    private final String id;
    private final CardTerminal terminal;

    private int count = 0;

    CardTerminalLogger(String id, CardTerminal terminal) {
        this.id = id;
        this.terminal = terminal;
    }

    @Override
    public String getName() {
        String name = terminal.getName();
        Logging.LOG.info("CardTerminal: Get name", "action", "card_terminal.get_name", "terminalId",
                id, "name", name);
        return name;
    }

    @Override
    public Card connect(String protocol) throws CardException {
        Logging.LOG.info("CardTerminal: Connect", "action", "card_terminal.connect", "terminalId",
                id, "protocol", protocol);
        return new CardLogger(String.format("%s.%d", id, ++count), terminal.connect(protocol));
    }

    @Override
    public boolean isCardPresent() throws CardException {
        boolean present = terminal.isCardPresent();
        Logging.LOG.info("CardTerminal: Is card present", "action", "card_terminal.is_card_present",
                "terminalId", id, "present", present);
        return present;
    }

    @Override
    public boolean waitForCardPresent(long timeout) throws CardException {
        Logging.LOG.info("CardTerminal: Wait for card present", "action",
                "card_terminal.wait_for_card_present_start", "terminalId", id, "timeout", timeout);
        boolean present = terminal.waitForCardPresent(timeout);
        Logging.LOG.info("CardTerminal: Finished waiting", "action",
                "card_terminal.wait_for_card_present_end", "terminalId", id, "present", present);
        return present;
    }

    @Override
    public boolean waitForCardAbsent(long timeout) throws CardException {
        Logging.LOG.info("CardTerminal: Wait for card absent", "action",
                "card_terminal.wait_for_card_absent_start", "terminalId", id, "timeout", timeout);
        boolean present = terminal.waitForCardAbsent(timeout);
        Logging.LOG.info("CardTerminal: Finished waiting", "action",
                "card_terminal.wait_for_card_absent_end", "terminalId", id, "present", present);
        return present;
    }
}
