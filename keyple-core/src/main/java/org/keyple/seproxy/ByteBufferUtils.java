/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;


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
     */
    public static ByteBuffer fromHex(String hex) {
        hex = HEX_IGNORED_CHARS.matcher(hex).replaceAll("").toUpperCase();

        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Odd numbered hex array");
        }

        ByteBuffer buffer = ByteBuffer.allocate(hex.length() / 2);

        for (int i = 0, e = hex.length(); i < e; i += 2) {
            buffer.put((byte) (Integer.parseInt(hex.substring(i, i + 2), 16) & 0xFF));
        }

        buffer.position(0);

        return buffer;
    }

    /**
     * Represent the ByteBuffer. We only show the buffer from the array's offset to the limit.
     *
     * @param buffer Buffer to represent to hex
     * @return Hex representation of the buffer
     */
    public static String toHex(ByteBuffer buffer) {
        if (buffer == null) {
            return "";
        }
        ByteBuffer buf = buffer.duplicate();
        buf.position(0);
        StringBuilder str = new StringBuilder(buf.remaining() * 2);
        while (buf.hasRemaining()) {
            str.append(String.format("%02X", buf.get()));
        }
        return str.toString();
    }

    private static String toHex(List<ByteBuffer> buffers) {
        StringBuilder str = new StringBuilder();

        for (ByteBuffer buf : buffers) {
            if (str.length() > 0) {
                str.append(' ');
            }
            str.append(toHex(buf));
        }

        return str.toString();
    }

    /**
     * Convenience method to represent a list of buffers separated by separating them by a space
     * 
     * @param buffers List of buffers
     * @return String representation
     */
    public static String toHex(ByteBuffer... buffers) {
        return toHex(Arrays.asList(buffers));
    }

    /**
     * Convenience method to represent a buffer cut with a space by indice
     * 
     * @param buffer Buffer to represent
     * @param cutLength Indices used to cut it
     * @return String representation
     */
    public static String toHexCutLen(ByteBuffer buffer, int... cutLength) {
        List<ByteBuffer> buffers = new ArrayList<ByteBuffer>(cutLength.length + 1);
        int lastIndex = 0;
        for (int length : cutLength) {
            buffers.add(ByteBufferUtils.subIndex(buffer, lastIndex, lastIndex + length));
            lastIndex += length;
        }
        if (buffer.limit() > lastIndex) {
            buffers.add(ByteBufferUtils.subIndex(buffer, lastIndex, buffer.limit()));
        }
        return toHex(buffers);
    }

    /**
     * Convert the buffer to a byte array
     *
     * @param buffer Buffer to read from
     * @return Newly created byte array
     */
    public static byte[] toBytes(ByteBuffer buffer) {
        byte[] data = new byte[buffer.limit()];
        int p = buffer.position();
        buffer.get(data);
        buffer.position(p);
        return data;
    }

    public static ByteBuffer subIndex(ByteBuffer buf, int start, int end) {
        buf = buf.duplicate();
        buf.position(start).limit(end);
        return buf.slice();
    }

    public static ByteBuffer subLen(ByteBuffer buf, int offset, int length) {
        buf = buf.duplicate();
        buf.position(offset).limit(offset + length);
        return buf.slice();
    }

    /**
     * Temporary conversion method. Every time this method is called it should be replaced by
     * something else.
     *
     * @param array Array to convert to {@link ByteBuffer}
     * @return {@link ByteBuffer} or null
     * @deprecated This should be replaced by some proper {@link ByteBuffer} handling
     */
    public static ByteBuffer wrap(byte[] array) {
        return array != null ? ByteBuffer.wrap(array) : null;
    }

    public static ByteBuffer concat(ByteBuffer buf1, ByteBuffer buf2) {
        ByteBuffer result = ByteBuffer.allocate(buf1.remaining() + buf2.remaining());
        result.put(buf1.asReadOnlyBuffer());
        result.put(buf2.asReadOnlyBuffer());
        result.position(0);
        return result;
    }
}
