/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.calypso.util;

import java.io.ByteArrayOutputStream;
import org.eclipse.keyple.seproxy.ApduResponse;
import org.eclipse.keyple.util.ByteArrayUtils;

/**
 * @author f.razakarivony
 *
 */
public class TestsUtilsResponseTabByteGenerator {
    /**
     * Append the given byte arrays to one big array
     *
     * @param arrays The arrays to append
     * @return The complete array containing the appended data
     */
    private static byte[] append(final byte[]... arrays) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (arrays != null) {
            for (final byte[] array : arrays) {
                if (array != null) {
                    out.write(array, 0, array.length);
                }
            }
        }
        return out.toByteArray();
    }

    public byte[] generate4MultiRecordsTabByte() {
        return new byte[] {0x01, 0x01, 0x01, 0x01, 0x30, 0x01, 0x01, 0x30, 0x01, 0x01, 0x30, 0x01,
                0x01, 0x30};
    }

    private static byte[] generateResponseOkTabByteRev2_4() {
        return ByteArrayUtils.fromHex(
                "CC 11223344 00112233445566778899AABBCCDDEEFF 00112233445566778899AABBCC 9000");
    }

    private static byte[] generateDataOpenTabByte() {
        return new byte[] {0x7E, (byte) 0x8F, 0x05, 0x75, 0x01A, (byte) 0xFF, 0x01, 0x01, 0x00,
                0x30};
    }

    private static byte[] generateFciTabByte() {
        return new byte[] {(byte) 0x6F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54, 0x52, 0x2E, 0x49,
                0x43, 0x41, (byte) 0xA5, 0x16, (byte) 0xBF, 0x0C, 0x13, (byte) 0xC7, 0x08, 0x00,
                0x00, 0x00, 0x00, 0x27, 0x4A, (byte) 0x9A, (byte) 0xB7, 0x53, 0x07, 0x0A, 0x3C,
                0x11, 0x32, 0x14, 0x10, 0x01};
    }

    private static byte[] generateTerminalSessionSignatureTabByte() {
        return new byte[] {0x7E, (byte) 0x8F, 0x05, 0x75, 0x01A, (byte) 0xFF, 0x01, 0x01};
    }

    private static byte[] generateResponseOkTabByteRev3_1() {
        return new byte[] {0x6F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54, 0x1E, 0x2E, 0x49, 0x43,
                0x41, (byte) 0xA5, 0x16, (byte) 0xBF, 0x0C, 0x13, (byte) 0xC7, 0x08, 0x00, 0x00,
                0x00, 0x00, 0x27, 0x4A, (byte) 0x9A, (byte) 0xB9, 0x53, 0x07, 0x0A, 0x3C, 0x11,
                0x32, 0x14, 0x10, 0x01, (byte) 0x90, 0x00};
    }

    private static byte[] generateResponseOkTabByteRev3_2() {
        return new byte[] {0x6F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54, 0x52, 0x2E, 0x49, 0x1B,
                0x1A, (byte) 0xA5, 0x16, (byte) 0xBF, 0x0C, 0x13, (byte) 0xC7, 0x08, 0x00, 0x00,
                0x00, 0x00, 0x27, 0x4A, (byte) 0x9A, (byte) 0xB9, 0x53, 0x07, 0x0A, 0x3C, 0x11,
                0x32, 0x14, 0x10, 0x01, (byte) 0x90, 0x00};
    }

    public static ApduResponse generateApduResponseValidRev2_4() {
        return new ApduResponse(append(generateResponseOkTabByteRev2_4(),
                TestsUtilsStatusCodeGenerator.generateSuccessfulStatusCode()), null);
    }

    public static ApduResponse generateApduResponseValidRev3_1() {
        return new ApduResponse(append(generateResponseOkTabByteRev3_1(),
                TestsUtilsStatusCodeGenerator.generateSuccessfulStatusCode()), null);
    }

    public static ApduResponse generateApduResponseValidRev3_2() {
        return new ApduResponse(append(generateResponseOkTabByteRev3_2(),
                TestsUtilsStatusCodeGenerator.generateSuccessfulStatusCode()), null);
    }

    public static ApduResponse generateApduResponseOpenSessionCmd() {
        return new ApduResponse(append(generateDataOpenTabByte(),
                TestsUtilsStatusCodeGenerator.generateSuccessfulStatusCode()), null);
    }

    public static ApduResponse generateApduResponseOpenSessionCmdError() {
        return new ApduResponse(append(generateDataOpenTabByte(),
                TestsUtilsStatusCodeGenerator.generateAccessForbiddenStatusCode()), null);
    }

    public static ApduResponse generateApduResponseTerminalSessionSignatureCmd() {
        return new ApduResponse(append(generateTerminalSessionSignatureTabByte(),
                TestsUtilsStatusCodeGenerator.generateSuccessfulStatusCode()), null);
    }

    public static ApduResponse generateApduResponseTerminalSessionSignatureCmdError() {
        return new ApduResponse(append(generateTerminalSessionSignatureTabByte(),
                TestsUtilsStatusCodeGenerator.generateCommandForbiddenOnBinaryFilesStatusCode()),
                null);
    }

    public static ApduResponse generateApduResponseFciCmd() {
        return new ApduResponse(append(generateFciTabByte(),
                TestsUtilsStatusCodeGenerator.generateSuccessfulStatusCode()), null);
    }

    public static ApduResponse generateApduResponseFciCmdError() {
        return new ApduResponse(append(generateFciTabByte(),
                TestsUtilsStatusCodeGenerator.generateFileNotFoundStatusCode()), null);
    }
}
