/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.plugin.androidnfc;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by ixxi on 12/01/2018.
 */

public class Tools {

    public static final String TAG = "Tools";

    /**
     * method sleepThread
     *
     * @param time_ms time
     */
    public static void sleepThread(long time_ms) {
        long timer;

        timer = System.nanoTime();
        try {
            while ((System.nanoTime() - timer) < (time_ms * 1000000))
                Thread.sleep(10);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } ;
    }

    /**
     * notify the error
     *
     * @param myContext context
     * @param msg message to display
     */
    public static void ToastErr(Context myContext, CharSequence msg) {
        Toast.makeText(myContext, msg, Toast.LENGTH_LONG).show();
    }


    /**
     * method to convert the byte array to the String
     *
     * @param data byte array
     * @return data String
     */
    public static String byteArrayToSHex(byte[] data) {
        if (data == null)
            return "";
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (int i = 0; i < data.length; i++) {
            sb.append(String.format("%02X ", data[i]));
        }
        return sb.toString().trim();
    }

    /**
     * method to convert the short to byte array
     *
     * @param s short
     * @return byte array
     */
    public static byte[] shortToBytes(short s) {
        //
        return new byte[] {(byte) ((s & 0xFF00) >> 8), (byte) (s & 0x00FF)};
    }



    static public byte[] parseHexBinary(String s) {
        final int len = s.length();

        // "111" is not a valid hex encoding.
        if (len % 2 != 0)
            throw new IllegalArgumentException("hexBinary needs to be even-length: " + s);

        byte[] out = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            int h = hexToBin(s.charAt(i));
            int l = hexToBin(s.charAt(i + 1));
            if (h == -1 || l == -1)
                throw new IllegalArgumentException(
                        "contains illegal character for hexBinary: " + s);

            out[i / 2] = (byte) (h * 16 + l);
        }

        return out;
    }

    private static int hexToBin(char ch) {
        if ('0' <= ch && ch <= '9')
            return ch - '0';
        if ('A' <= ch && ch <= 'F')
            return ch - 'A' + 10;
        if ('a' <= ch && ch <= 'f')
            return ch - 'a' + 10;
        return -1;
    }


}
