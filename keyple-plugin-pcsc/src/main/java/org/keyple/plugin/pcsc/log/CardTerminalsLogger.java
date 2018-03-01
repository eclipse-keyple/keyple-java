/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.plugin.pcsc.log;

import java.util.ArrayList;
import java.util.List;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;

public class CardTerminalsLogger extends CardTerminals {
    private final CardTerminals cardTerminals;

    public CardTerminalsLogger(CardTerminals cardTerminals) {
        this.cardTerminals = cardTerminals;
    }

    @Override
    public List<CardTerminal> list(State state) throws CardException {
        List<CardTerminal> list = new ArrayList<CardTerminal>();
        int i = 0;
        for (CardTerminal terminal : cardTerminals.list(state)) {
            list.add(new CardTerminalLogger(String.format("pcsc.%d", ++i), terminal));
        }
        return list;
    }

    @Override
    public boolean waitForChange(long l) throws CardException {
        return cardTerminals.waitForChange(l);
    }
}
