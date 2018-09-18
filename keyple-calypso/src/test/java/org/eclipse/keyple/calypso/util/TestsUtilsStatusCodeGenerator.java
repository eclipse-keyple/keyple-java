/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

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
