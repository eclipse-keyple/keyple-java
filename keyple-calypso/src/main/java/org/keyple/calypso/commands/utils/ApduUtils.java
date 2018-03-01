/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.utils;

public class ApduUtils {

    /**
     * Instantiates a new ResponseUtils.
     */
    private ApduUtils() {

    }

    /**
     * Get the value of the bit
     *
     * @param b Input byte
     * @param p Bit position in the byte
     * @return true if the bit is set
     */
    public static boolean isBitSet(byte b, int p) {
        return (1 == ((b >> p) & 1));
    }
}
