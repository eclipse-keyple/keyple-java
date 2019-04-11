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
package org.eclipse.keyple.integration.calypso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.parser.ReadDataStructure;
import org.eclipse.keyple.calypso.command.po.parser.ReadRecordsRespPars;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


public class CommandSetTestSuite {

    @BeforeAll
    public static void setUp() {
        try {
            TestEngine.configureReaders();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeypleBaseException e) {
            e.printStackTrace();
        }
    }

    private static int getCounterValueFromByteArray(byte[] inByteArray, int inCounterIndex) {
        int counterValue = 0;

        for (int i = 0; i < 3; i++) {
            counterValue <<= 8;
            counterValue |= (inByteArray[i + (3 * (inCounterIndex - 1))] & 0xFF);
        }

        return counterValue;
    }


    private static ReadRecordsRespPars readRecords(CalypsoPo calypsoPo, Byte fileSfi,
            Byte recordNumber, ReadDataStructure readDataStructureValue)
            throws KeypleReaderException {

        PoTransaction poTransaction =
                new PoTransaction(TestEngine.poReader, calypsoPo, TestEngine.samReader, null);

        int readRecordsRespParsIndex = poTransaction.prepareReadRecordsCmd(fileSfi,
                readDataStructureValue, (byte) recordNumber,
                String.format("SFI=%02X, recnbr=%d", fileSfi, recordNumber));

        poTransaction.processOpening(PoTransaction.ModificationMode.ATOMIC,
                PoTransaction.SessionAccessLevel.SESSION_LVL_DEBIT, (byte) 0x00, (byte) 0x00);

        poTransaction.processClosing(TransmissionMode.CONTACTLESS, ChannelState.KEEP_OPEN);
        /*
         * System.out.println("DataRead#: " +
         * ByteArrayUtils.toHex(dataReadInSession.getApduResponses().get(1).getDataOut()));
         * 
         * System.out.println("SW1SW2: " +
         * Integer.toHexString(dataReadInSession.getApduResponses().get(1).getStatusCode() &
         * 0xFFFF));
         */
        return (ReadRecordsRespPars) poTransaction.getResponseParser(readRecordsRespParsIndex);
    }


    private static void updateRecord(CalypsoPo calypsoPo, Byte sfi, Byte recordNumber,
            byte[] dataToWrite) throws KeypleReaderException {

        PoTransaction poTransaction =
                new PoTransaction(TestEngine.poReader, calypsoPo, TestEngine.samReader, null);

        poTransaction.processOpening(PoTransaction.ModificationMode.ATOMIC,
                PoTransaction.SessionAccessLevel.SESSION_LVL_LOAD, (byte) 0x00, (byte) 0x00);

        poTransaction.prepareUpdateRecordCmd(sfi, recordNumber, dataToWrite,
                String.format("SFI=%02X, recnbr=%d", sfi, recordNumber));
        poTransaction.processPoCommandsInSession();

        poTransaction.processClosing(TransmissionMode.CONTACTLESS, ChannelState.KEEP_OPEN);
    }


    private static void decreaseCounter(CalypsoPo calypsoPo, Byte countersSfi, Byte counterIndex,
            int valueToDecrement) throws KeypleReaderException {

        PoTransaction poTransaction =
                new PoTransaction(TestEngine.poReader, calypsoPo, TestEngine.samReader, null);

        poTransaction.processOpening(PoTransaction.ModificationMode.ATOMIC,
                PoTransaction.SessionAccessLevel.SESSION_LVL_DEBIT, (byte) 0x00, (byte) 0x00);

        poTransaction.prepareDecreaseCmd(countersSfi, counterIndex, valueToDecrement, String.format(
                "SFI=%02X, index=%d, decvalue=%d", countersSfi, counterIndex, valueToDecrement));

        poTransaction.processPoCommandsInSession();

        poTransaction.processClosing(TransmissionMode.CONTACTLESS, ChannelState.KEEP_OPEN);
    }


    private static void increaseCounter(CalypsoPo calypsoPo, Byte countersSfi, Byte counterIndex,
            int valueToIncrement) throws KeypleReaderException {

        PoTransaction poTransaction =
                new PoTransaction(TestEngine.poReader, calypsoPo, TestEngine.samReader, null);

        poTransaction.processOpening(PoTransaction.ModificationMode.ATOMIC,
                PoTransaction.SessionAccessLevel.SESSION_LVL_LOAD, (byte) 0x00, (byte) 0x00);

        poTransaction.prepareIncreaseCmd(countersSfi, counterIndex, valueToIncrement, String.format(
                "SFI=%02X, index=%d, decvalue=%d", countersSfi, counterIndex, valueToIncrement));

        poTransaction.processPoCommandsInSession();

        poTransaction.processClosing(TransmissionMode.CONTACTLESS, ChannelState.KEEP_OPEN);
    }


    private static void appendRecord(CalypsoPo calypsoPo, Byte sfi, byte[] dataToWrite)
            throws KeypleReaderException {

        PoTransaction poTransaction =
                new PoTransaction(TestEngine.poReader, calypsoPo, TestEngine.samReader, null);

        poTransaction.processOpening(PoTransaction.ModificationMode.ATOMIC,
                PoTransaction.SessionAccessLevel.SESSION_LVL_DEBIT, (byte) 0x00, (byte) 0x00);

        poTransaction.prepareAppendRecordCmd(sfi, dataToWrite, String.format("SFI=%02X", sfi));

        poTransaction.processPoCommandsInSession();

        poTransaction.processClosing(TransmissionMode.CONTACTLESS, ChannelState.KEEP_OPEN);
    }


    @Test
    public void testWriteCounter() {

        try {

            PoFileStructureInfo poData = TestEngine.selectPO();

            String genericCounterData =
                    "00000A 000100 000B00 010000 0C0000 0000B0 00C000 0F0000 00000D 0000";

            byte[] counterData = new byte[poData.getCountersFileData().getRecSize()];

            System.arraycopy(ByteArrayUtils.fromHex(genericCounterData), 0, counterData, 0,
                    counterData.length);

            updateRecord((CalypsoPo) poData.getMatchingSe(), poData.getCountersFileData().getSfi(),
                    (byte) 0x01, counterData);

            ReadRecordsRespPars readRecordsRespPars = readRecords(
                    (CalypsoPo) poData.getMatchingSe(), poData.getCountersFileData().getSfi(),
                    (byte) 0x01, ReadDataStructure.SINGLE_RECORD_DATA);
            byte[] updatedCounterData = readRecordsRespPars.getRecords().get(1);

            Assertions.assertArrayEquals(counterData, updatedCounterData);

            if (poData.getSimulatedCountersFileData().getRecNumb() > 0) {

                for (int i = 0; i < poData.getSimulatedCountersFileData().getRecNumb(); i++) {

                    readRecordsRespPars = readRecords((CalypsoPo) poData.getMatchingSe(),
                            (byte) (poData.getSimulatedCountersFileData().getSfi() + i),
                            (byte) 0x01, ReadDataStructure.SINGLE_COUNTER);

                    int updatedCounterValue = readRecordsRespPars.getCounters().get(0);

                    int expectedCounterValue = getCounterValueFromByteArray(counterData, i + 1);

                    Assertions.assertEquals(expectedCounterValue, updatedCounterValue);

                }
            }

        } catch (Exception e) {

            Assertions.fail("Exception caught: " + e.getMessage());
            e.printStackTrace();
        }

    }

    @Test
    public void testDecreaseCounter() {

        try {

            PoFileStructureInfo poData = TestEngine.selectPO();

            ReadRecordsRespPars readRecordsRespPars = readRecords(
                    (CalypsoPo) poData.getMatchingSe(), poData.getCountersFileData().getSfi(),
                    (byte) 0x01, ReadDataStructure.MULTIPLE_COUNTER);

            SortedMap<Integer, Integer> initialCounterData = readRecordsRespPars.getCounters();

            for (int i = 0; i < (poData.getCountersFileData().getRecSize() / 3); i++) {

                int counterValue = initialCounterData.get(i);

                if (counterValue > 0) {

                    int valueToDecrement = counterValue / 2;
                    decreaseCounter((CalypsoPo) poData.getMatchingSe(),
                            poData.getCountersFileData().getSfi(), (byte) (i + 1),
                            valueToDecrement);

                    readRecordsRespPars = readRecords((CalypsoPo) poData.getMatchingSe(),
                            poData.getCountersFileData().getSfi(), (byte) 0x01,
                            ReadDataStructure.MULTIPLE_COUNTER);

                    SortedMap<Integer, Integer> updatedCounters = readRecordsRespPars.getCounters();
                    int finalValue = updatedCounters.get(i);

                    Assertions.assertEquals(counterValue - valueToDecrement, finalValue);

                    if (poData.getSimulatedCountersFileData().getRecNumb() > 0) {

                        readRecordsRespPars = readRecords((CalypsoPo) poData.getMatchingSe(),
                                (byte) (poData.getSimulatedCountersFileData().getSfi() + i),
                                (byte) 0x01, ReadDataStructure.SINGLE_COUNTER);

                        finalValue = readRecordsRespPars.getCounters().get(0);

                        Assertions.assertEquals(counterValue - valueToDecrement, finalValue);
                    }
                }
            }

        } catch (Exception e) {

            Assertions.fail("Exception caught: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Test
    public void testIncreaseCounter() {

        try {

            PoFileStructureInfo poData = TestEngine.selectPO();

            ReadRecordsRespPars readRecordsRespPars = readRecords(
                    (CalypsoPo) poData.getMatchingSe(), poData.getCountersFileData().getSfi(),
                    (byte) 0x01, ReadDataStructure.MULTIPLE_COUNTER);

            SortedMap<Integer, Integer> initialCounterData = readRecordsRespPars.getCounters();

            for (int i = 0; i < (poData.getCountersFileData().getRecSize() / 3); i++) {

                int counterValue = initialCounterData.get(i);

                int maxValue = 0xFFFFFF;

                if (counterValue < maxValue) {

                    int valueToIncrement = (maxValue - counterValue) / 2;
                    increaseCounter((CalypsoPo) poData.getMatchingSe(),
                            poData.getCountersFileData().getSfi(), (byte) (i + 1),
                            valueToIncrement);

                    readRecordsRespPars = readRecords((CalypsoPo) poData.getMatchingSe(),
                            poData.getCountersFileData().getSfi(), (byte) 0x01,
                            ReadDataStructure.MULTIPLE_COUNTER);

                    SortedMap<Integer, Integer> updatedCounters = readRecordsRespPars.getCounters();

                    int finalValue = updatedCounters.get(i);

                    Assertions.assertEquals(counterValue + valueToIncrement, finalValue);

                    if (poData.getSimulatedCountersFileData().getRecNumb() > 0) {

                        readRecordsRespPars = readRecords((CalypsoPo) poData.getMatchingSe(),
                                (byte) (poData.getSimulatedCountersFileData().getSfi() + i),
                                (byte) 0x01, ReadDataStructure.SINGLE_COUNTER);

                        finalValue = readRecordsRespPars.getCounters().get(0);

                        Assertions.assertEquals(counterValue + valueToIncrement, finalValue);
                    }
                }
            }

        } catch (Exception e) {

            Assertions.fail("Exception caught: " + e.getMessage());
            e.printStackTrace();
        }

    }


    @Test
    public void testReadRecords() {

        try {

            PoFileStructureInfo poData = TestEngine.selectPO();
            ReadDataStructure readDataStructureValue = ReadDataStructure.MULTIPLE_RECORD_DATA;

            PoRevision poRevision = (new PoTransaction(TestEngine.poReader,
                    (CalypsoPo) poData.getMatchingSe(), TestEngine.samReader, null)).getRevision();


            List<byte[]> recordsData = new ArrayList<byte[]>();

            for (int i = 0; i < poData.getContractFileData().getRecNumb(); i++) {

                byte[] recordData = new byte[poData.getContractFileData().getRecSize()];
                Arrays.fill(recordData, (byte) (i + 1));
                recordsData.add(recordData);

                updateRecord((CalypsoPo) poData.getMatchingSe(),
                        poData.getContractFileData().getSfi(), (byte) (i + 1), recordData);
            }

            if (poRevision == PoRevision.REV2_4) {
                readDataStructureValue = ReadDataStructure.SINGLE_RECORD_DATA;
            }

            ReadRecordsRespPars readRecordsRespPars = readRecords(
                    (CalypsoPo) poData.getMatchingSe(), poData.getContractFileData().getSfi(),
                    (byte) 0x01, readDataStructureValue);

            if (poRevision == PoRevision.REV2_4) {

                // Rev2.4 doesn't read multiple contracts. It will only read the first.
                Assertions.assertEquals(poData.getContractFileData().getRecSize(),
                        readRecordsRespPars.getRecords().get(1).length);
                Assertions.assertArrayEquals(recordsData.get(0),
                        readRecordsRespPars.getRecords().get(1));

            } else {

                for (int i = 0; i < poData.getContractFileData().getRecNumb(); i++) {

                    Assertions.assertArrayEquals(recordsData.get(i),
                            readRecordsRespPars.getRecords().get(i + 1));
                }

            }

        } catch (Exception e) {

            Assertions.fail("Exception caught: " + e.getMessage());
            e.printStackTrace();
        }

    }

    @Test
    public void testAppendRecord() {

        try {

            PoFileStructureInfo poData = TestEngine.selectPO();

            byte[] recordData = new byte[poData.getEventFileData().getRecSize()];
            Arrays.fill(recordData, (byte) (0x01));

            updateRecord((CalypsoPo) poData.getMatchingSe(), poData.getEventFileData().getSfi(),
                    (byte) 0x01, recordData);

            byte[] recordDataToAppend = new byte[poData.getEventFileData().getRecSize()];
            Arrays.fill(recordDataToAppend, (byte) (0x11));

            appendRecord((CalypsoPo) poData.getMatchingSe(), poData.getEventFileData().getSfi(),
                    recordDataToAppend);

            ReadRecordsRespPars readRecordsRespPars = readRecords(
                    (CalypsoPo) poData.getMatchingSe(), poData.getEventFileData().getSfi(),
                    (byte) 0x01, ReadDataStructure.SINGLE_RECORD_DATA);

            byte[] firstEventRecord = readRecordsRespPars.getRecords().get(1);

            Assertions.assertArrayEquals(recordDataToAppend, firstEventRecord);

            readRecordsRespPars = readRecords((CalypsoPo) poData.getMatchingSe(),
                    poData.getEventFileData().getSfi(), (byte) 0x02,
                    ReadDataStructure.SINGLE_RECORD_DATA);

            byte[] secondEventRecord = readRecordsRespPars.getRecords().get(2);

            Assertions.assertArrayEquals(recordData, secondEventRecord);

        } catch (Exception e) {

            Assertions.fail("Exception caught: " + e.getMessage());
            e.printStackTrace();
        }

    }

    @Test
    public void testUpdateAndDecreaseCounterSingleSession() {

        try {

            PoFileStructureInfo poData = TestEngine.selectPO();

            PoTransaction poTransaction = new PoTransaction(TestEngine.poReader,
                    (CalypsoPo) poData.getMatchingSe(), TestEngine.samReader, null);

            String genericCounterData =
                    "00000A 000100 000B00 010000 0C0000 0000B0 00C000 0F0000 00000D 0000";

            byte[] counterData = new byte[poData.getCountersFileData().getRecSize()];

            System.arraycopy(ByteArrayUtils.fromHex(genericCounterData), 0, counterData, 0,
                    counterData.length);

            poTransaction.processOpening(PoTransaction.ModificationMode.ATOMIC,
                    PoTransaction.SessionAccessLevel.SESSION_LVL_LOAD, (byte) 0x00, (byte) 0x00);

            poTransaction.prepareUpdateRecordCmd(poData.getCountersFileData().getSfi(), (byte) 0x01,
                    counterData,
                    String.format("SFI=%02X, recnbr=1", poData.getCountersFileData().getSfi()));

            poTransaction.prepareDecreaseCmd(poData.getCountersFileData().getSfi(), (byte) 0x01,
                    0x01, String.format("SFI=%02X, index=1, decvalue=1",
                            poData.getCountersFileData().getSfi()));

            poTransaction.processClosing(TransmissionMode.CONTACTLESS, ChannelState.KEEP_OPEN);

            counterData[2] = 0x09;

            ReadRecordsRespPars readRecordsRespPars = readRecords(
                    (CalypsoPo) poData.getMatchingSe(), poData.getCountersFileData().getSfi(),
                    (byte) 0x01, ReadDataStructure.SINGLE_RECORD_DATA);

            byte[] updatedCounterData = readRecordsRespPars.getRecords().get(1);

            Assertions.assertArrayEquals(counterData, updatedCounterData);

        } catch (Exception e) {

            Assertions.fail("Exception caught: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testUpdateAndIncreaseCounterSingleSession() {

        try {

            PoFileStructureInfo poData = TestEngine.selectPO();

            PoTransaction poTransaction = new PoTransaction(TestEngine.poReader,
                    (CalypsoPo) poData.getMatchingSe(), TestEngine.samReader, null);

            String genericCounterData =
                    "0A0000 000100 000B00 010000 0C0000 0000B0 00C000 0F0000 00000D 0000";

            byte[] counterData = new byte[poData.getCountersFileData().getRecSize()];

            System.arraycopy(ByteArrayUtils.fromHex(genericCounterData), 0, counterData, 0,
                    counterData.length);

            poTransaction.processOpening(PoTransaction.ModificationMode.ATOMIC,
                    PoTransaction.SessionAccessLevel.SESSION_LVL_LOAD, (byte) 0x00, (byte) 0x00);

            poTransaction.prepareUpdateRecordCmd(poData.getCountersFileData().getSfi(), (byte) 0x01,
                    counterData,
                    String.format("SFI=%02X, recnbr=1", poData.getCountersFileData().getSfi()));

            poTransaction.prepareIncreaseCmd(poData.getCountersFileData().getSfi(), (byte) 0x01,
                    0xFF, String.format("SFI=%02X, index=1, decvalue=255",
                            poData.getCountersFileData().getSfi()));

            poTransaction.processClosing(TransmissionMode.CONTACTLESS, ChannelState.KEEP_OPEN);

            counterData[2] = (byte) 0xFF;

            ReadRecordsRespPars readRecordsRespPars = readRecords(
                    (CalypsoPo) poData.getMatchingSe(), poData.getCountersFileData().getSfi(),
                    (byte) 0x01, ReadDataStructure.SINGLE_RECORD_DATA);

            byte[] updatedCounterData = readRecordsRespPars.getRecords().get(1);

            Assertions.assertArrayEquals(counterData, updatedCounterData);

        } catch (Exception e) {

            Assertions.fail("Exception caught: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testUpdateAndAppendSingleSession() {

        try {

            PoFileStructureInfo poData = TestEngine.selectPO();

            PoTransaction poTransaction = new PoTransaction(TestEngine.poReader,
                    (CalypsoPo) poData.getMatchingSe(), TestEngine.samReader, null);

            byte[] recordData = new byte[poData.getEventFileData().getRecSize()];
            Arrays.fill(recordData, (byte) (0x01));

            byte[] recordDataToAppend = new byte[poData.getEventFileData().getRecSize()];
            Arrays.fill(recordDataToAppend, (byte) (0x11));

            poTransaction.processOpening(PoTransaction.ModificationMode.ATOMIC,
                    PoTransaction.SessionAccessLevel.SESSION_LVL_LOAD, (byte) 0x00, (byte) 0x00);

            poTransaction.prepareUpdateRecordCmd(poData.getEventFileData().getSfi(), (byte) 0x01,
                    recordData,
                    String.format("SFI=%02X, recnbr=1", poData.getEventFileData().getSfi()));

            poTransaction.prepareAppendRecordCmd(poData.getEventFileData().getSfi(),
                    recordDataToAppend,
                    String.format("SFI=%02X", poData.getEventFileData().getSfi()));

            poTransaction.processClosing(TransmissionMode.CONTACTLESS, ChannelState.KEEP_OPEN);

            ReadRecordsRespPars readRecordsRespPars = readRecords(
                    (CalypsoPo) poData.getMatchingSe(), poData.getEventFileData().getSfi(),
                    (byte) 0x01, ReadDataStructure.SINGLE_RECORD_DATA);

            byte[] firstEventRecord = readRecordsRespPars.getRecords().get(1);

            Assertions.assertArrayEquals(recordDataToAppend, firstEventRecord);

            readRecordsRespPars = readRecords((CalypsoPo) poData.getMatchingSe(),
                    poData.getEventFileData().getSfi(), (byte) 0x02,
                    ReadDataStructure.SINGLE_RECORD_DATA);

            byte[] secondEventRecord = readRecordsRespPars.getRecords().get(2);

            Assertions.assertArrayEquals(recordData, secondEventRecord);

        } catch (Exception e) {

            Assertions.fail("Exception caught: " + e.getMessage());
            e.printStackTrace();
        }

    }


    @Test
    public void testReadDataInOpenSession() {

        try {

            PoFileStructureInfo poData = TestEngine.selectPO();

            PoTransaction poTransaction = new PoTransaction(TestEngine.poReader,
                    (CalypsoPo) poData.getMatchingSe(), TestEngine.samReader, null);

            byte[] recordData = new byte[poData.getEventFileData().getRecSize()];
            Arrays.fill(recordData, (byte) (0xA5));

            poTransaction.processOpening(PoTransaction.ModificationMode.ATOMIC,
                    PoTransaction.SessionAccessLevel.SESSION_LVL_LOAD, (byte) 0x00, (byte) 0x00);

            poTransaction.prepareUpdateRecordCmd(poData.getContractFileData().getSfi(),
                    (byte) poData.getContractFileData().getRecNumb(), recordData,
                    String.format("SFI=%02X, recnbr=%02X", poData.getContractFileData().getSfi(),
                            poData.getContractFileData().getRecNumb()));

            poTransaction.processPoCommandsInSession();

            poTransaction.processClosing(TransmissionMode.CONTACTLESS, ChannelState.KEEP_OPEN);

            poTransaction = new PoTransaction(TestEngine.poReader,
                    (CalypsoPo) poData.getMatchingSe(), TestEngine.samReader, null);

            poTransaction.processOpening(PoTransaction.ModificationMode.ATOMIC,
                    PoTransaction.SessionAccessLevel.SESSION_LVL_DEBIT,
                    poData.getContractFileData().getSfi(),
                    (byte) poData.getContractFileData().getRecNumb());

            poTransaction.processClosing(TransmissionMode.CONTACTLESS, ChannelState.KEEP_OPEN);

            byte[] dataReadInSession = poTransaction.getOpenRecordDataRead();

            Assertions.assertArrayEquals(recordData, dataReadInSession);

        } catch (Exception e) {

            Assertions.fail("Exception caught: " + e.getMessage());
            e.printStackTrace();
        }

    }


    @Test
    public void testWriteDataBeforeProcessOpening() {

        try {

            PoFileStructureInfo poData = TestEngine.selectPO();

            PoTransaction poTransaction = new PoTransaction(TestEngine.poReader,
                    (CalypsoPo) poData.getMatchingSe(), TestEngine.samReader, null);

            byte[] recordData = new byte[poData.getEventFileData().getRecSize()];
            Arrays.fill(recordData, (byte) (0xA9));

            poTransaction.prepareUpdateRecordCmd(poData.getContractFileData().getSfi(),
                    (byte) poData.getContractFileData().getRecNumb(), recordData,
                    String.format("SFI=%02X, recnbr=%02X", poData.getContractFileData().getSfi(),
                            poData.getContractFileData().getRecNumb()));

            poTransaction.processOpening(PoTransaction.ModificationMode.ATOMIC,
                    PoTransaction.SessionAccessLevel.SESSION_LVL_LOAD, (byte) 0x00, (byte) 0x00);

            poTransaction.processClosing(TransmissionMode.CONTACTLESS, ChannelState.KEEP_OPEN);

            poTransaction = new PoTransaction(TestEngine.poReader,
                    (CalypsoPo) poData.getMatchingSe(), TestEngine.samReader, null);

            poTransaction.processOpening(PoTransaction.ModificationMode.ATOMIC,
                    PoTransaction.SessionAccessLevel.SESSION_LVL_DEBIT,
                    poData.getContractFileData().getSfi(),
                    (byte) poData.getContractFileData().getRecNumb());

            poTransaction.processClosing(TransmissionMode.CONTACTLESS, ChannelState.KEEP_OPEN);

            byte[] dataRead = poTransaction.getOpenRecordDataRead();

            Assertions.assertArrayEquals(recordData, dataRead);

        } catch (Exception e) {

            Assertions.fail("Exception caught: " + e.getMessage());
            e.printStackTrace();
        }

    }
}
