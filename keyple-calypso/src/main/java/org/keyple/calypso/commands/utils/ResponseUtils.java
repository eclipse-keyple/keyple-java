/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.utils;



/**
 * This class eases the parse of APDUResponses into objects.
 *
 * @author Ixxi
 */
public class ResponseUtils {

    /**
     * Instantiates a new ResponseUtils.
     */
    private ResponseUtils() {

    }

    /**
     * Method to get the KVC from the response in revision 2 mode.
     *
     * @param apduResponse the apdu response
     * @return a KVC byte
     */
    public static byte toKVCRev2(byte[] apduResponse) {
        // TODO: Check that part: I replaced a (null) KVC by a 0x00
        return apduResponse.length > 4 ? apduResponse[0] : 0x00;
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

    /**
     * Create a sub-array from an array
     *
     * @param source Source array
     * @param indexStart Start index
     * @param indexEnd End index
     * @return
     */
    public static byte[] subArray(byte[] source, int indexStart, int indexEnd) {
        byte[] res = new byte[indexEnd - indexStart];
        System.arraycopy(source, indexStart, res, 0, indexEnd - indexStart);
        return res;
    }
}
