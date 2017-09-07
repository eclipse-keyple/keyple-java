package org.keyple.calypso.utils;

import javax.xml.bind.DatatypeConverter;

/**
 * The Class LogUtils.
 */
public class LogUtils {

	private LogUtils(){}
    /**
     * Hexa to string.
     *
     * @param value
     *            the value
     * @return the string
     */
    public static String hexaToString(byte[] value) {
        if (value == null) {
            return null;
        }
        return DatatypeConverter.printHexBinary(value);
    }

    /**
     * Hexa to string.
     *
     * @param value
     *            the value
     * @return the string
     */
    public static String hexaToString(byte value) {

        return DatatypeConverter.printHexBinary(new byte[] { value });
    }
}
