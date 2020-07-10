/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.transaction;

import static org.assertj.core.api.Assertions.*;
import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;

public class SamSelectorTest {

    @Test
    public void test_AidSelectorNull() {
        SamSelector samSelector = SamSelector.builder()
                .samIdentifier(SamIdentifier.builder().samRevision(SamRevision.S1D).build())
                .build();
        assertThat(samSelector.getAidSelector()).isNull();
    }

    @Test
    public void test_SamRevision_S1D() {
        SamSelector samSelector = SamSelector.builder()
                .samIdentifier(SamIdentifier.builder().samRevision(SamRevision.S1D).build())
                .build();
        assertThat(samSelector.getAtrFilter().getAtrRegex())
                .isEqualTo("3B(.{6}|.{10})805A..80D?20.{4}.{8}829000");
    }

    @Test
    public void test_SamRevision_S1E() {
        SamSelector samSelector = SamSelector.builder()
                .samIdentifier(SamIdentifier.builder().samRevision(SamRevision.S1E).build())
                .build();
        assertThat(samSelector.getAtrFilter().getAtrRegex())
                .isEqualTo("3B(.{6}|.{10})805A..80E120.{4}.{8}829000");
    }

    @Test
    public void test_SamRevision_C1() {
        SamSelector samSelector = SamSelector.builder()
                .samIdentifier(SamIdentifier.builder().samRevision(SamRevision.C1).build()).build();
        assertThat(samSelector.getAtrFilter().getAtrRegex())
                .isEqualTo("3B(.{6}|.{10})805A..80C120.{4}.{8}829000");
    }

    @Test
    public void test_SamRevision_ANY() {
        SamSelector samSelector = SamSelector.builder()
                .samIdentifier(SamIdentifier.builder().samRevision(SamRevision.AUTO).build())
                .build();
        assertThat(samSelector.getAtrFilter().getAtrRegex()).isEqualTo(".*");
    }

    @Test
    public void test_SamSerialNumber() {
        SamSelector samSelector =
                SamSelector.builder()
                        .samIdentifier(new SamIdentifier.SamIdentifierBuilder()
                                .samRevision(SamRevision.C1).serialNumber("11223344").build())
                        .build();
        assertThat(samSelector.getAtrFilter().getAtrRegex())
                .isEqualTo("3B(.{6}|.{10})805A..80C120.{4}11223344829000");
    }

    @Test(expected = IllegalStateException.class)
    public void test_UnlockData_notSet() {

        SamSelector samSelector = SamSelector.builder()
                .samIdentifier(new SamIdentifier.SamIdentifierBuilder()//
                        .samRevision(SamRevision.C1)//
                        .serialNumber("11223344")//
                        .build())//
                .build();
    }

    @Test
    public void test_UnlockData_ok() {
        final String UNLOCK_DATA = "00112233445566778899AABBCCDDEEFF";

        SamSelector samSelector = SamSelector.builder()
                .samIdentifier(new SamIdentifier.SamIdentifierBuilder()//
                        .samRevision(SamRevision.C1)//
                        .serialNumber("11223344")//
                        .build())//
                .unlockData(ByteArrayUtil.fromHex(UNLOCK_DATA))//
                .build();
        assertThat(samSelector.getUnlockData()).isEqualTo(ByteArrayUtil.fromHex(UNLOCK_DATA));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_UnlockData_badLength() {
        final String UNLOCK_DATA_BAD_LENGTH = "00112233445566778899AABBCCDDEEFF00";

        SamSelector samSelector = SamSelector.builder()
                .samIdentifier(new SamIdentifier.SamIdentifierBuilder()//
                        .samRevision(SamRevision.C1)//
                        .serialNumber("11223344")//
                        .build())//
                .unlockData(ByteArrayUtil.fromHex(UNLOCK_DATA_BAD_LENGTH))//
                .build();
    }
}
