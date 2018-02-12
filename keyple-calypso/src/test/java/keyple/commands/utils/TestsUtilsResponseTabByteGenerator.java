/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package keyple.commands.utils;

import org.keyple.seproxy.ApduResponse;

/**
 * @author f.razakarivony
 *
 */
public class TestsUtilsResponseTabByteGenerator {

    public byte[] generate4MultiRecordsTabByte() {
        return new byte[] {0x01, 0x01, 0x01, 0x01, 0x30, 0x01, 0x01, 0x30, 0x01, 0x01, 0x30, 0x01,
                0x01, 0x30};
    }

    public static byte[] generateResponseOkTabByteRev2_4() {
        return new byte[] {0x6F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54, 0x52, 0x2E, 0x49, 0x43,
                0x41, (byte) 0xA5, 0x16, (byte) 0xBF, 0x0C, 0x13, (byte) 0xC7, 0x08, 0x00, 0x00,
                0x00, 0x00, 0x27, 0x4A, (byte) 0x9A, (byte) 0xB9, 0x53, 0x07, 0x0A, 0x3C, 0x11,
                0x32, 0x14, 0x10, 0x01, (byte) 0x90, 0x00};
    }

    public static byte[] generateDataOpenTabByte() {
        return new byte[] {0x7E, (byte) 0x8F, 0x05, 0x75, 0x01A, (byte) 0xFF, 0x01, 0x01, 0x00,
                0x30};
    }

    public static byte[] generateFciTabByte() {
        return new byte[] {(byte) 0x6F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54, 0x52, 0x2E, 0x49,
                0x43, 0x41, (byte) 0xA5, 0x16, (byte) 0xBF, 0x0C, 0x13, (byte) 0xC7, 0x08, 0x00,
                0x00, 0x00, 0x00, 0x27, 0x4A, (byte) 0x9A, (byte) 0xB7, 0x53, 0x07, 0x0A, 0x3C,
                0x11, 0x32, 0x14, 0x10, 0x01};
    }

    public static byte[] generateTerminalSessionSignatureTabByte() {
        return new byte[] {0x7E, (byte) 0x8F, 0x05, 0x75, 0x01A, (byte) 0xFF, 0x01, 0x01};
    }

    public static byte[] generateResponseOkTabByteRev3_1() {
        return new byte[] {0x6F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54, 0x1E, 0x2E, 0x49, 0x43,
                0x41, (byte) 0xA5, 0x16, (byte) 0xBF, 0x0C, 0x13, (byte) 0xC7, 0x08, 0x00, 0x00,
                0x00, 0x00, 0x27, 0x4A, (byte) 0x9A, (byte) 0xB9, 0x53, 0x07, 0x0A, 0x3C, 0x11,
                0x32, 0x14, 0x10, 0x01, (byte) 0x90, 0x00};
    }

    public static byte[] generateResponseOkTabByteRev3_2() {
        return new byte[] {0x6F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54, 0x52, 0x2E, 0x49, 0x1B,
                0x1A, (byte) 0xA5, 0x16, (byte) 0xBF, 0x0C, 0x13, (byte) 0xC7, 0x08, 0x00, 0x00,
                0x00, 0x00, 0x27, 0x4A, (byte) 0x9A, (byte) 0xB9, 0x53, 0x07, 0x0A, 0x3C, 0x11,
                0x32, 0x14, 0x10, 0x01, (byte) 0x90, 0x00};
    }

    public static ApduResponse generateApduResponseValideRev2_4() {
        return new ApduResponse(generateResponseOkTabByteRev2_4(), true,
                TestsUtilsStatusCodeGenerator.generateSuccessfulStatusCode());
    }

    public static ApduResponse generateApduResponseValideRev3_1() {
        return new ApduResponse(generateResponseOkTabByteRev3_1(), true,
                TestsUtilsStatusCodeGenerator.generateSuccessfulStatusCode());
    }

    public static ApduResponse generateApduResponseValideRev3_2() {
        return new ApduResponse(generateResponseOkTabByteRev3_2(), true,
                TestsUtilsStatusCodeGenerator.generateSuccessfulStatusCode());
    }

    public static ApduResponse generateApduResponseOpenSessionCmd() {
        return new ApduResponse(generateDataOpenTabByte(), true,
                TestsUtilsStatusCodeGenerator.generateSuccessfulStatusCode());
    }

    public static ApduResponse generateApduResponseOpenSessionCmdError() {
        return new ApduResponse(generateDataOpenTabByte(), true,
                TestsUtilsStatusCodeGenerator.generateAccessForbiddenStatusCode());
    }

    public static ApduResponse generateApduResponseTerminalSessionSignatureCmd() {
        return new ApduResponse(generateTerminalSessionSignatureTabByte(), true,
                TestsUtilsStatusCodeGenerator.generateSuccessfulStatusCode());
    }

    public static ApduResponse generateApduResponseTerminalSessionSignatureCmdError() {
        return new ApduResponse(generateTerminalSessionSignatureTabByte(), true,
                TestsUtilsStatusCodeGenerator.generateCommandForbiddenOnBinaryFilesStatusCode());
    }

    public static ApduResponse generateApduResponseFciCmd() {
        return new ApduResponse(generateFciTabByte(), true,
                TestsUtilsStatusCodeGenerator.generateSuccessfulStatusCode());
    }

    public static ApduResponse generateApduResponseFciCmdError() {
        return new ApduResponse(generateFciTabByte(), true,
                TestsUtilsStatusCodeGenerator.generateFileNotFoundStatusCode());
    }
}
