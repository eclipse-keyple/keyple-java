/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Before;
import org.junit.Test;

public class FileDataTest {

    private FileData file;
    private byte[] data1 = ByteArrayUtil.fromHex("11");
    private byte[] data2 = ByteArrayUtil.fromHex("2222");
    private byte[] data3 = ByteArrayUtil.fromHex("333333");
    private byte[] data4 = ByteArrayUtil.fromHex("44444444");

    @Before
    public void setUp() throws Exception {
        file = new FileData();
    }

    @Test
    public void getAllRecordsContent_whenNoContent_shouldReturnNotNullObject() {
        assertThat(file.getAllRecordsContent()).isNotNull();
    }

    @Test
    public void getAllRecordsContent_shouldReturnAReference() {
        file.setContent(1, data1);
        SortedMap<Integer, byte[]> copy1 = file.getAllRecordsContent();
        SortedMap<Integer, byte[]> copy2 = file.getAllRecordsContent();
        assertThat(copy1).isSameAs(copy2);
        assertThat(copy1.get(1)).isSameAs(copy2.get(1));
    }

    @Test(expected = NoSuchElementException.class)
    public void getContent_whenRecord1IsNotSet_shouldThrowNSEE() {
        file.getContent();
    }

    @Test
    public void getContent_shouldReturnAReference() {
        file.setContent(1, data1);
        byte[] copy = file.getContent();
        assertThat(copy).isSameAs(data1);
    }

    @Test
    public void getContent_shouldReturnRecord1() {
        file.setContent(1, data1);
        byte[] copy = file.getContent();
        assertThat(copy).isEqualTo(data1);
    }

    @Test(expected = NoSuchElementException.class)
    public void getContentP1_whenRecordIsNotSet_shouldThrowNSEE() {
        file.getContent(1);
    }

    @Test
    public void getContentP1_shouldReturnAReference() {
        file.setContent(1, data1);
        byte[] copy = file.getContent(1);
        assertThat(copy).isSameAs(data1);
    }

    @Test
    public void getContentP1_shouldReturnRecord() {
        file.setContent(1, data1);
        byte[] copy = file.getContent(1);
        assertThat(copy).isEqualTo(data1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getContentP3_whenOffsetLt0_shouldThrowIAE() {
        file.getContent(1, -1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getContentP3_whenLengthLt1_shouldThrowIAE() {
        file.getContent(1, 0, 0);
    }

    @Test(expected = NoSuchElementException.class)
    public void getContentP3_whenRecordIsNotSet_shouldThrowNSEE() {
        file.getContent(1, 0, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getContentP3_whenOffsetGeSize_shouldThrowIOOBE() {
        file.setContent(1, data1);
        file.getContent(1, 1, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getContentP3_whenOffsetLengthGtSize_shouldThrowIOOBE() {
        file.setContent(2, data2);
        file.getContent(2, 1, 2);
    }

    @Test
    public void getContentP3_shouldReturnACopy() {
        file.setContent(1, data1);
        byte[] copy = file.getContent(1, 0, 1);
        assertThat(copy).isNotSameAs(data1);
    }

    @Test
    public void getContentP3_shouldReturnASubset() {
        file.setContent(2, data2);
        byte[] copy = file.getContent(2, 1, 1);
        assertThat(copy).isEqualTo(ByteArrayUtil.fromHex("22"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getContentAsCounterValue_whenNumRecordLt1_shouldThrowIAE() {
        file.getContentAsCounterValue(0);
    }

    @Test(expected = NoSuchElementException.class)
    public void getContentAsCounterValue_whenRecordIsNotSet_shouldThrowNSEE() {
        file.getContentAsCounterValue(1);
    }

    @Test(expected = NoSuchElementException.class)
    public void getContentAsCounterValue_whenCounterIsNotSet_shouldThrowNSEE() {
        file.setContent(1, data3);
        file.getContentAsCounterValue(2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getContentAsCounterValue_whenCounterIsTruncated_shouldThrowIOOBE() {
        file.setContent(1, data4);
        file.getContentAsCounterValue(2);
    }

    @Test
    public void getContentAsCounterValue_shouldReturnCounterValue() {
        file.setContent(1, data3);
        int val = file.getContentAsCounterValue(1);
        assertThat(val).isEqualTo(0x333333);
    }

    @Test(expected = NoSuchElementException.class)
    public void getAllCountersValue_whenRecordIsNotSet_shouldThrowNSEE() {
        file.getAllCountersValue();
    }

    @Test
    public void getAllCountersValue_shouldReturnAllNonTruncatedCounters() {
        file.setContent(1, data4);
        SortedMap<Integer, Integer> counters = file.getAllCountersValue();
        assertThat(counters).containsExactly(entry(1, 0x444444));
    }

    @Test
    public void setContentP2_shouldPutAReference() {
        file.setContent(1, data1);
        byte[] copy = file.getContent(1);
        assertThat(copy).isSameAs(data1);
    }

    @Test
    public void setContentP2_shouldBeSuccess() {
        file.setContent(1, data1);
        byte[] val = file.getContent(1);
        assertThat(val).isEqualTo(data1);
    }

    @Test
    public void setContentP2_shouldReplaceExistingContent() {
        file.setContent(1, data1);
        file.setContent(1, data2);
        byte[] val = file.getContent(1);
        assertThat(val).isEqualTo(data2);
    }

    @Test
    public void setCounter_whenRecord1IsNotSet_shouldCreateRecord1() {
        file.setCounter(1, data3);
        byte[] val = file.getContent(1);
        assertThat(val).isNotNull();
    }

    @Test
    public void setCounter_shouldPutACopy() {
        file.setCounter(1, data3);
        byte[] copy = file.getContent(1);
        assertThat(copy).isNotSameAs(data3);
    }

    @Test
    public void setCounter_shouldSetOrReplaceCounterValue() {
        file.setContent(1, data4);
        file.setCounter(2, data3);
        byte[] val = file.getContent(1);
        assertThat(val).isEqualTo(ByteArrayUtil.fromHex("444444333333"));
    }

    @Test
    public void setContentP3_shouldPutACopy() {
        file.setContent(1, data1, 0);
        byte[] copy = file.getContent(1);
        assertThat(copy).isNotSameAs(data1);
    }

    @Test
    public void setContentP3_whenRecordIsNotSet_shouldPadWith0() {
        file.setContent(1, data1, 1);
        byte[] val = file.getContent(1);
        assertThat(val).isEqualTo(ByteArrayUtil.fromHex("0011"));
    }

    @Test
    public void setContentP3_whenOffsetGeSize_shouldPadWith0() {
        file.setContent(1, data1);
        file.setContent(1, data2, 2);
        byte[] val = file.getContent(1);
        assertThat(val).isEqualTo(ByteArrayUtil.fromHex("11002222"));
    }

    @Test
    public void setContentP3_shouldReplaceInRange() {
        file.setContent(1, data4);
        file.setContent(1, data2, 1);
        byte[] val = file.getContent(1);
        assertThat(val).isEqualTo(ByteArrayUtil.fromHex("44222244"));
    }

    @Test
    public void fillContent_whenRecordIsNotSet_shouldPutContent() {
        file.fillContent(1, data1);
        byte[] content = file.getContent(1);
        assertThat(content).isEqualTo(data1);
    }

    @Test
    public void fillContent_whenLengthGtActualSize_shouldApplyBinaryOperationAndRightPadWithContent() {
        file.setContent(1, data1);
        file.fillContent(1, data2);
        byte[] content = file.getContent(1);
        assertThat(content).isEqualTo(ByteArrayUtil.fromHex("3322"));
    }

    @Test
    public void fillContent_whenLengthLeActualSize_shouldApplyBinaryOperation() {
        file.setContent(1, data2);
        file.fillContent(1, data1);
        byte[] content = file.getContent(1);
        assertThat(content).isEqualTo(ByteArrayUtil.fromHex("3322"));
    }

    @Test
    public void addCyclicContent_whenNoContent_shouldSetContentToRecord1() {
        file.addCyclicContent(data1);
        byte[] val = file.getContent(1);
        assertThat(val).isEqualTo(data1);
    }

    @Test
    public void addCyclicContent_shouldShiftAllRecordsAndSetContentToRecord1() {
        file.setContent(1, data1);
        file.setContent(2, data2);
        file.addCyclicContent(data3);
        assertThat(file.getAllRecordsContent()).containsExactly(
                entry(1, ByteArrayUtil.fromHex("333333")), entry(2, ByteArrayUtil.fromHex("11")),
                entry(3, ByteArrayUtil.fromHex("2222")));
    }

    @Test
    public void clone_shouldReturnACopy() {
        file.setContent(1, data1);
        FileData clone = file.clone();
        assertThat(clone).isNotSameAs(file);
        assertThat(clone.getContent(1)).isNotSameAs(file.getContent(1));
    }
}
