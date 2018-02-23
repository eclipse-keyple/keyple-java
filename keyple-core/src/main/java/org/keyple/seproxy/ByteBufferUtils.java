/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

import java.nio.ByteBuffer;
import java.util.regex.Pattern;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 * Utils around the {@link ByteBuffer}
 */
public class ByteBufferUtils {

    /**
     * Chars we will ignore when loading a sample HEX string. It allows to copy/paste the specs APDU
     */
    private static final Pattern HEX_IGNORED_CHARS = Pattern.compile(" |h");

    /**
     * Create a {@link ByteBuffer} from an hexa string. This method allows spaces and "h".
     *
     * @param hex Hexa string
     * @return ByteBuffer
     * @throws DecoderException If the buffer is not correctly formatted
     */
    public static ByteBuffer fromHex(String hex) throws DecoderException {
        return ByteBuffer.wrap(Hex.decodeHex(HEX_IGNORED_CHARS.matcher(hex).replaceAll("")));
    }

    /**
     * Represent the ByteBuffer. We only show the buffer from the array's offset to the limit.
     *
     * @param buffer Buffer to represent to hex
     * @return Hex representation of the buffer
     */
    public static String toHex(ByteBuffer buffer) {
        StringBuilder str = new StringBuilder((buffer.limit() - buffer.arrayOffset()) * 2);
        final byte[] array = buffer.array();
        for (int i = buffer.arrayOffset(), e = i + buffer.limit(); i < e; i++) {
            str.append(String.format("%02X", array[i]));
        }

        return str.toString();
    }

    /**
     * Convert the buffer to a byte array
     *
     * @param buffer Buffer to read from
     * @return Newly created byte array
     */
    public static byte[] toBytes(ByteBuffer buffer) {
        byte[] data = new byte[buffer.limit()];
        buffer.rewind();
        buffer.get(data);
        return data;
    }
}
