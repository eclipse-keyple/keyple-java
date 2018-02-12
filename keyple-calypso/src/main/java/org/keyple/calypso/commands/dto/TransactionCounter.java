/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.dto;

/**
 * The Class TransactionCounter. For security purposes, a Calypso portable object shall maintain a
 * Transaction Counter (24 bit unsigned value) in its non-volatile memory, initialized with the
 * Calypso application, and which may only be decremented (reduced by 1) during the portable object
 * uses.
 */
public class TransactionCounter {

    /** The value. */
    private int value;

    /**
     * Instantiates a new TransactionCounter.
     *
     * @param value the value
     */
    public TransactionCounter(int value) {
        super();
        this.value = value;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public int getValue() {
        return value;
    }

}
