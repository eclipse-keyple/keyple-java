/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.util;

class TestsUtilsStatusCodeGenerator {

    public static byte[] generateSuccessfulStatusCode() {
        return new byte[] {(byte) 0x90, 0x00};
    }

    public static byte[] generateCommandForbiddenOnBinaryFilesStatusCode() {
        return new byte[] {(byte) 0x69, (byte) 0x81};
    }

    public static byte[] generateFileNotFoundStatusCode() {
        return new byte[] {(byte) 0x69, (byte) 0x82};
    }

    public static byte[] generateRecordNotFoundStatusCode() {
        return new byte[] {(byte) 0x6A, (byte) 0x83};
    }

    public static byte[] generateP2ValueNotSupportedStatusCode() {
        return new byte[] {(byte) 0x6B, 0x00};
    }

    public static byte[] generateLeValueIncorrectStatusCode() {
        return new byte[] {(byte) 0x6C, (byte) 0xFF};
    }

    public static byte[] generateAccessForbiddenStatusCode() {
        return new byte[] {(byte) 0x69, (byte) 0x85};
    }
}
