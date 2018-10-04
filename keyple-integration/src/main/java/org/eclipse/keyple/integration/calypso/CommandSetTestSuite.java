/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.integration.calypso;

import static org.eclipse.keyple.calypso.transaction.PoSecureSession.CommunicationMode;
import static org.eclipse.keyple.integration.calypso.TestEngine.selectPO;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.calypso.command.po.PoSendableInSession;
import org.eclipse.keyple.calypso.command.po.builder.DecreaseCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.IncreaseCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.UpdateRecordCmdBuild;
import org.eclipse.keyple.calypso.transaction.PoSecureSession;
import org.eclipse.keyple.seproxy.ApduResponse;
import org.eclipse.keyple.seproxy.SeResponse;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.util.ByteBufferUtils;
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


    private static byte[] readRecords(PoSecureSession poTransaction, ApduResponse fciData,
            Byte fileSfi, Byte recordNumber, boolean readOneRecordFlag)
            throws KeypleReaderException {

        ReadRecordsCmdBuild poReadRecordCmd = new ReadRecordsCmdBuild(poTransaction.getRevision(),
                fileSfi, (byte) recordNumber, readOneRecordFlag, (byte) 0x00);

        List<PoSendableInSession> filesToReadInSession = new ArrayList<PoSendableInSession>();
        filesToReadInSession.add(poReadRecordCmd);

        SeResponse dataReadInSession = poTransaction.processOpening(fciData,
                PoSecureSession.SessionAccessLevel.SESSION_LVL_DEBIT, (byte) 0x00, (byte) 0x00,
                filesToReadInSession);

        poTransaction.processClosing(null, null, CommunicationMode.CONTACTLESS_MODE, false);
        /*
         * System.out.println("DataRead#: " +
         * ByteBufferUtils.toHex(dataReadInSession.getApduResponses().get(1).getDataOut()));
         * 
         * System.out.println("SW1SW2: " +
         * Integer.toHexString(dataReadInSession.getApduResponses().get(1).getStatusCode() &
         * 0xFFFF));
         */
        return ByteBufferUtils.toBytes(dataReadInSession.getApduResponses().get(1).getDataOut());
    }


    private static void updateRecord(PoSecureSession poTransaction, ApduResponse fciData, Byte sfi,
            Byte recordNumber, byte[] dataToWrite) throws KeypleReaderException {

        poTransaction.processOpening(fciData, PoSecureSession.SessionAccessLevel.SESSION_LVL_LOAD,
                (byte) 0x00, (byte) 0x00, null);

        UpdateRecordCmdBuild poUpdateRecordCmd = new UpdateRecordCmdBuild(
                poTransaction.getRevision(), sfi, recordNumber, ByteBuffer.wrap(dataToWrite));

        List<PoSendableInSession> filesToChangeInSession = new ArrayList<PoSendableInSession>();
        filesToChangeInSession.add(poUpdateRecordCmd);

        poTransaction.processPoCommands(filesToChangeInSession);

        poTransaction.processClosing(null, null, CommunicationMode.CONTACTLESS_MODE, false);
    }


    private static void decreaseCounter(PoSecureSession poTransaction, ApduResponse fciData,
            Byte countersSfi, Byte counterIndex, int valueToDecrement)
            throws KeypleReaderException {

        poTransaction.processOpening(fciData, PoSecureSession.SessionAccessLevel.SESSION_LVL_DEBIT,
                (byte) 0x00, (byte) 0x00, null);

        DecreaseCmdBuild poDecreaseCmd_Counter = new DecreaseCmdBuild(poTransaction.getRevision(),
                countersSfi, counterIndex, valueToDecrement);

        List<PoSendableInSession> filesToChangeInSession = new ArrayList<PoSendableInSession>();
        filesToChangeInSession.add(poDecreaseCmd_Counter);

        poTransaction.processPoCommands(filesToChangeInSession);

        poTransaction.processClosing(null, null, CommunicationMode.CONTACTLESS_MODE, false);
    }


    private static void increaseCounter(PoSecureSession poTransaction, ApduResponse fciData,
            Byte countersSfi, Byte counterIndex, int valueToIncrement)
            throws KeypleReaderException {

        poTransaction.processOpening(fciData, PoSecureSession.SessionAccessLevel.SESSION_LVL_LOAD,
                (byte) 0x00, (byte) 0x00, null);

        IncreaseCmdBuild poIncreaseCmd_Counter = new IncreaseCmdBuild(poTransaction.getRevision(),
                countersSfi, counterIndex, valueToIncrement);

        List<PoSendableInSession> filesToChangeInSession = new ArrayList<PoSendableInSession>();
        filesToChangeInSession.add(poIncreaseCmd_Counter);

        poTransaction.processPoCommands(filesToChangeInSession);

        poTransaction.processClosing(null, null, CommunicationMode.CONTACTLESS_MODE, false);
    }

    @Test
    public void testWriteCounter() {

        try {

            PoFileStructureInfo poData = selectPO();

            PoSecureSession poTransaction =
                    new PoSecureSession(TestEngine.poReader, TestEngine.csmReader, null);

            byte[] genericCounterData = new byte[] {0x00, 0x00, 0x0A, 0x00, 0x01, 0x00, 0x00, 0x0B,
                    0x00, 0x01, 0x00, 0x00, 0x0C, 0x00, 0x00, 0x00, 0x00, (byte) 0xB0, 0x00,
                    (byte) 0xC0, 0x00, 0x0F, 0x00, 0x00, 0x00, 0x00, 0x0D, 0x00, 0x00};

            byte[] counterData = new byte[poData.getCountersFileData().getRecSize()];

            System.arraycopy(genericCounterData, 0, counterData, 0, counterData.length);

            updateRecord(poTransaction, poData.getFciData(), poData.getCountersFileData().getSfi(),
                    (byte) 0x01, counterData);

            byte[] updatedCounterData = readRecords(poTransaction, poData.getFciData(),
                    poData.getCountersFileData().getSfi(), (byte) 0x01, true);

            Assertions.assertArrayEquals(counterData, updatedCounterData);

            if (poData.getSimulatedCountersFileData().getRecNumb() > 0) {

                for (int i = 0; i < poData.getSimulatedCountersFileData().getRecNumb(); i++) {

                    byte[] updatedSingleCounterData =
                            readRecords(poTransaction, poData.getFciData(),
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

            PoSecureSession poTransaction =
                    new PoSecureSession(TestEngine.poReader, TestEngine.csmReader, null);

            byte[] initialCounterData = readRecords(poTransaction, poData.getFciData(),
                    poData.getCountersFileData().getSfi(), (byte) 0x01, true);

            for (int i = 0; i < (poData.getCountersFileData().getRecSize() / 3); i++) {

                int counterValue = getCounterValueFromByteArray(initialCounterData, i + 1);

                if (counterValue > 0) {

                    int valueToDecrement = counterValue / 2;
                    decreaseCounter(poTransaction, poData.getFciData(),
                            poData.getCountersFileData().getSfi(), (byte) (i + 1),
                            valueToDecrement);

                    byte[] updatedCounters = readRecords(poTransaction, poData.getFciData(),
                            poData.getCountersFileData().getSfi(), (byte) 0x01, true);
                    int finalValue = getCounterValueFromByteArray(updatedCounters, i + 1);

                    Assertions.assertEquals(counterValue - valueToDecrement, finalValue);

                    if (poData.getSimulatedCountersFileData().getRecNumb() > 0) {

                        updatedCounters = readRecords(poTransaction, poData.getFciData(),
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

            PoSecureSession poTransaction =
                    new PoSecureSession(TestEngine.poReader, TestEngine.csmReader, null);

            byte[] initialCounterData = readRecords(poTransaction, poData.getFciData(),
                    poData.getCountersFileData().getSfi(), (byte) 0x01, true);

            for (int i = 0; i < (poData.getCountersFileData().getRecSize() / 3); i++) {

                int counterValue = getCounterValueFromByteArray(initialCounterData, i + 1);
                int maxValue = 0xFFFFFF;

                if (counterValue < maxValue) {

                    int valueToIncrement = (maxValue - counterValue) / 2;
                    increaseCounter(poTransaction, poData.getFciData(),
                            poData.getCountersFileData().getSfi(), (byte) (i + 1),
                            valueToIncrement);

                    byte[] updatedCounters = readRecords(poTransaction, poData.getFciData(),
                            poData.getCountersFileData().getSfi(), (byte) 0x01, true);
                    int finalValue = getCounterValueFromByteArray(updatedCounters, i + 1);

                    Assertions.assertEquals(counterValue + valueToIncrement, finalValue);

                    if (poData.getSimulatedCountersFileData().getRecNumb() > 0) {

                        updatedCounters = readRecords(poTransaction, poData.getFciData(),
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

            PoSecureSession poTransaction =
                    new PoSecureSession(TestEngine.poReader, TestEngine.csmReader, null);

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

            updateRecord(poTransaction, poData.getFciData(), poData.getContractFileData().getSfi(),
                    (byte) 0x01, firstRecordData);
            updateRecord(poTransaction, poData.getFciData(), poData.getContractFileData().getSfi(),
                    (byte) 0x02, secondRecordData);

            byte[] contractRecordsData = readRecords(poTransaction, poData.getFciData(),
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
