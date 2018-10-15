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

package org.eclipse.keyple.integration.calypso;

import static org.eclipse.keyple.calypso.transaction.PoTransaction.CommunicationMode;
import static org.eclipse.keyple.integration.calypso.TestEngine.selectPO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.calypso.command.po.PoSendableInSession;
import org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.calypso.transaction.CalypsoPO;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.seproxy.SeResponse;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
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


    private static byte[] readRecords(PoTransaction poTransaction, Byte fileSfi, Byte recordNumber,
            boolean readOneRecordFlag) throws KeypleReaderException {

        ReadRecordsCmdBuild poReadRecordCmd = new ReadRecordsCmdBuild(poTransaction.getRevision(),
                fileSfi, (byte) recordNumber, readOneRecordFlag, (byte) 0x00,
                String.format("SFI=%02X, recnbr=%d", fileSfi, recordNumber));

        List<PoSendableInSession> filesToReadInSession = new ArrayList<PoSendableInSession>();
        filesToReadInSession.add(poReadRecordCmd);

        SeResponse dataReadInSession = poTransaction.processOpening(
                PoTransaction.ModificationMode.ATOMIC,
                PoTransaction.SessionAccessLevel.SESSION_LVL_DEBIT, (byte) 0x00, (byte) 0x00);

        poTransaction.processClosing(CommunicationMode.CONTACTLESS_MODE, false);
        /*
         * System.out.println("DataRead#: " +
         * ByteArrayUtils.toHex(dataReadInSession.getApduResponses().get(1).getDataOut()));
         * 
         * System.out.println("SW1SW2: " +
         * Integer.toHexString(dataReadInSession.getApduResponses().get(1).getStatusCode() &
         * 0xFFFF));
         */
        return dataReadInSession.getApduResponses().get(1).getDataOut();
    }


    private static void updateRecord(PoTransaction poTransaction, Byte sfi, Byte recordNumber,
            byte[] dataToWrite) throws KeypleReaderException {

        poTransaction.processOpening(PoTransaction.ModificationMode.ATOMIC,
                PoTransaction.SessionAccessLevel.SESSION_LVL_LOAD, (byte) 0x00, (byte) 0x00);

        poTransaction.prepareUpdateRecordCmd(sfi, recordNumber, dataToWrite,
                String.format("SFI=%02X, recnbr=%d", sfi, recordNumber));
        poTransaction.processPoCommands();

        poTransaction.processClosing(CommunicationMode.CONTACTLESS_MODE, false);
    }


    private static void decreaseCounter(PoTransaction poTransaction, Byte countersSfi,
            Byte counterIndex, int valueToDecrement) throws KeypleReaderException {

        poTransaction.processOpening(PoTransaction.ModificationMode.ATOMIC,
                PoTransaction.SessionAccessLevel.SESSION_LVL_DEBIT, (byte) 0x00, (byte) 0x00);

        poTransaction.prepareDecreaseCmd(countersSfi, counterIndex, valueToDecrement, String.format(
                "SFI=%02X, index=%d, decvalue=%d", countersSfi, counterIndex, valueToDecrement));

        poTransaction.processPoCommands();

        poTransaction.processClosing(CommunicationMode.CONTACTLESS_MODE, false);
    }


    private static void increaseCounter(PoTransaction poTransaction, Byte countersSfi,
            Byte counterIndex, int valueToIncrement) throws KeypleReaderException {

        poTransaction.processOpening(PoTransaction.ModificationMode.ATOMIC,
                PoTransaction.SessionAccessLevel.SESSION_LVL_LOAD, (byte) 0x00, (byte) 0x00);

        poTransaction.prepareIncreaseCmd(countersSfi, counterIndex, valueToIncrement, String.format(
                "SFI=%02X, index=%d, decvalue=%d", countersSfi, counterIndex, valueToIncrement));

        poTransaction.processPoCommands();

        poTransaction.processClosing(CommunicationMode.CONTACTLESS_MODE, false);
    }

    @Test
    public void testWriteCounter() {

        try {

            PoFileStructureInfo poData = selectPO();

            PoTransaction poTransaction = new PoTransaction(TestEngine.poReader,
                    new CalypsoPO(poData.getSelectionData()), TestEngine.csmReader, null);

            byte[] genericCounterData = new byte[] {0x00, 0x00, 0x0A, 0x00, 0x01, 0x00, 0x00, 0x0B,
                    0x00, 0x01, 0x00, 0x00, 0x0C, 0x00, 0x00, 0x00, 0x00, (byte) 0xB0, 0x00,
                    (byte) 0xC0, 0x00, 0x0F, 0x00, 0x00, 0x00, 0x00, 0x0D, 0x00, 0x00};

            byte[] counterData = new byte[poData.getCountersFileData().getRecSize()];

            System.arraycopy(genericCounterData, 0, counterData, 0, genericCounterData.length);

            updateRecord(poTransaction, poData.getCountersFileData().getSfi(), (byte) 0x01,
                    counterData);

            byte[] updatedCounterData = readRecords(poTransaction,
                    poData.getCountersFileData().getSfi(), (byte) 0x01, true);

            Assertions.assertArrayEquals(counterData, updatedCounterData);

            if (poData.getSimulatedCountersFileData().getRecNumb() > 0) {

                for (int i = 0; i < poData.getSimulatedCountersFileData().getRecNumb(); i++) {

                    byte[] updatedSingleCounterData = readRecords(poTransaction,
                            (byte) (poData.getSimulatedCountersFileData().getSfi() + i),
                            (byte) 0x01, true);

                    int updatedCounterValue =
                            getCounterValueFromByteArray(updatedSingleCounterData, 1);

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

            PoFileStructureInfo poData = selectPO();

            PoTransaction poTransaction = new PoTransaction(TestEngine.poReader,
                    new CalypsoPO(poData.getSelectionData()), TestEngine.csmReader, null);

            byte[] initialCounterData = readRecords(poTransaction,
                    poData.getCountersFileData().getSfi(), (byte) 0x01, true);

            for (int i = 0; i < (poData.getCountersFileData().getRecSize() / 3); i++) {

                int counterValue = getCounterValueFromByteArray(initialCounterData, i + 1);

                if (counterValue > 0) {

                    int valueToDecrement = counterValue / 2;
                    decreaseCounter(poTransaction, poData.getCountersFileData().getSfi(),
                            (byte) (i + 1), valueToDecrement);

                    byte[] updatedCounters = readRecords(poTransaction,
                            poData.getCountersFileData().getSfi(), (byte) 0x01, true);
                    int finalValue = getCounterValueFromByteArray(updatedCounters, i + 1);

                    Assertions.assertEquals(counterValue - valueToDecrement, finalValue);

                    if (poData.getSimulatedCountersFileData().getRecNumb() > 0) {

                        updatedCounters = readRecords(poTransaction,
                                (byte) (poData.getSimulatedCountersFileData().getSfi() + i),
                                (byte) 0x01, true);
                        finalValue = getCounterValueFromByteArray(updatedCounters, 1);

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

            PoFileStructureInfo poData = selectPO();

            PoTransaction poTransaction = new PoTransaction(TestEngine.poReader,
                    new CalypsoPO(poData.getSelectionData()), TestEngine.csmReader, null);

            byte[] initialCounterData = readRecords(poTransaction,
                    poData.getCountersFileData().getSfi(), (byte) 0x01, true);

            for (int i = 0; i < (poData.getCountersFileData().getRecSize() / 3); i++) {

                int counterValue = getCounterValueFromByteArray(initialCounterData, i + 1);
                int maxValue = 0xFFFFFF;

                if (counterValue < maxValue) {

                    int valueToIncrement = (maxValue - counterValue) / 2;
                    increaseCounter(poTransaction, poData.getCountersFileData().getSfi(),
                            (byte) (i + 1), valueToIncrement);

                    byte[] updatedCounters = readRecords(poTransaction,
                            poData.getCountersFileData().getSfi(), (byte) 0x01, true);
                    int finalValue = getCounterValueFromByteArray(updatedCounters, i + 1);

                    Assertions.assertEquals(counterValue + valueToIncrement, finalValue);

                    if (poData.getSimulatedCountersFileData().getRecNumb() > 0) {

                        updatedCounters = readRecords(poTransaction,
                                (byte) (poData.getSimulatedCountersFileData().getSfi() + i),
                                (byte) 0x01, true);
                        finalValue = getCounterValueFromByteArray(updatedCounters, 1);

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

            PoFileStructureInfo poData = selectPO();

            PoTransaction poTransaction = new PoTransaction(TestEngine.poReader,
                    new CalypsoPO(poData.getSelectionData()), TestEngine.csmReader, null);

            byte[] firstRecordData = new byte[] {0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
                    0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
                    0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
                    0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
                    0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
                    0x01, 0x01, 0x01, 0x01};

            byte[] secondRecordData = new byte[] {0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
                    0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
                    0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
                    0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
                    0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
                    0x02, 0x02, 0x02, 0x02};

            updateRecord(poTransaction, poData.getContractFileData().getSfi(), (byte) 0x01,
                    firstRecordData);
            updateRecord(poTransaction, poData.getContractFileData().getSfi(), (byte) 0x02,
                    secondRecordData);

            byte[] contractRecordsData = readRecords(poTransaction,
                    poData.getContractFileData().getSfi(), (byte) 0x01, false);

            Assertions.assertEquals(((64 + 2) * 2), contractRecordsData.length);

            Assertions.assertEquals(0x01, contractRecordsData[0]);
            Assertions.assertEquals(0x40, contractRecordsData[1]);


            byte[] firstRecordDataRead = new byte[64];
            System.arraycopy(contractRecordsData, 2, firstRecordDataRead, 0, 64);

            Assertions.assertArrayEquals(firstRecordData, firstRecordDataRead);

            Assertions.assertEquals(0x02, contractRecordsData[64 + 2]);
            Assertions.assertEquals(0x40, contractRecordsData[64 + 3]);

            byte[] secondRecordDataRead = new byte[64];
            System.arraycopy(contractRecordsData, 64 + 4, secondRecordDataRead, 0, 64);

            Assertions.assertArrayEquals(secondRecordData, secondRecordDataRead);


        } catch (Exception e) {

            Assertions.fail("Exception caught: " + e.getMessage());
            e.printStackTrace();
        }

    }

}
