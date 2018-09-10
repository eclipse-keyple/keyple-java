package org.eclipse.keyple.integration.calypso;

import org.eclipse.keyple.calypso.command.po.builder.DecreaseCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.IncreaseCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.calypso.transaction.PoSecureSession;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.util.ByteBufferUtils;
import org.eclipse.keyple.calypso.command.po.builder.UpdateRecordCmdBuild;
import org.eclipse.keyple.calypso.command.po.PoSendableInSession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.eclipse.keyple.integration.calypso.TestEngine.*;


public class CommandSetTestSuite {

    @BeforeAll
    public static void setUp() {
        try {
            TestEngine.configureReaders();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static int getCounterValueFromByteArray(byte[] inByteArray, int inCounterIndex) {
        int counterValue = 0;

        for (int i = 0; i < 3; i++) {
            counterValue <<= 8;
            counterValue |= (inByteArray[i + (3 * (inCounterIndex-1))] & 0xFF);
        }

        return counterValue;
    }


    private static byte[] readRecords(PoSecureSession poTransaction, ApduResponse fciData, Byte fileSfi, Byte recordNumber, boolean readOneRecordFlag) throws IOReaderException {

        ReadRecordsCmdBuild poReadRecordCmd =
                new ReadRecordsCmdBuild(poTransaction.getRevision(), fileSfi, (byte) recordNumber, readOneRecordFlag, (byte) 0x00);

        List<PoSendableInSession> filesToReadInSession = new ArrayList<PoSendableInSession>();
        filesToReadInSession.add(poReadRecordCmd);

        SeResponse dataReadInSession = poTransaction.processOpening(fciData, PoSecureSession.SessionAccessLevel.SESSION_LVL_DEBIT, (byte) 0x00, (byte) 0x00, filesToReadInSession);

        poTransaction.processClosing(null, null, null,false);
/*
        System.out.println("DataRead#: "
                + ByteBufferUtils.toHex(dataReadInSession.getApduResponses().get(1).getDataOut()));

        System.out.println("SW1SW2: "
                + Integer.toHexString(dataReadInSession.getApduResponses().get(1).getStatusCode() & 0xFFFF));
*/
        return ByteBufferUtils.toBytes(dataReadInSession.getApduResponses().get(1).getDataOut());
    }


    private static void updateRecord(PoSecureSession poTransaction, ApduResponse fciData, Byte sfi, Byte recordNumber, byte[] dataToWrite) throws IOReaderException {

        poTransaction.processOpening(fciData, PoSecureSession.SessionAccessLevel.SESSION_LVL_LOAD, (byte) 0x00, (byte) 0x00, null);

        UpdateRecordCmdBuild poUpdateRecordCmd = new UpdateRecordCmdBuild(poTransaction.getRevision(), sfi, recordNumber, ByteBuffer.wrap(dataToWrite));

        List<PoSendableInSession> filesToChangeInSession = new ArrayList<PoSendableInSession>();
        filesToChangeInSession.add(poUpdateRecordCmd);

        poTransaction.processPoCommands(filesToChangeInSession);

        poTransaction.processClosing(null, null, null, false);
    }


    private static void decreaseCounter(PoSecureSession poTransaction, ApduResponse fciData, Byte countersSfi, Byte counterIndex, int valueToDecrement) throws IOReaderException {

        poTransaction.processOpening(fciData, PoSecureSession.SessionAccessLevel.SESSION_LVL_DEBIT, (byte) 0x00, (byte) 0x00, null);

        DecreaseCmdBuild poDecreaseCmd_Counter = new DecreaseCmdBuild(poTransaction.getRevision(), countersSfi, counterIndex, valueToDecrement);

        List<PoSendableInSession> filesToChangeInSession = new ArrayList<PoSendableInSession>();
        filesToChangeInSession.add(poDecreaseCmd_Counter);

        poTransaction.processPoCommands(filesToChangeInSession);

        poTransaction.processClosing(null, null, null, false);
    }


    private static void increaseCounter(PoSecureSession poTransaction, ApduResponse fciData, Byte countersSfi, Byte counterIndex, int valueToIncrement) throws IOReaderException {

        poTransaction.processOpening(fciData, PoSecureSession.SessionAccessLevel.SESSION_LVL_LOAD, (byte) 0x00, (byte) 0x00, null);

        IncreaseCmdBuild poIncreaseCmd_Counter = new IncreaseCmdBuild(poTransaction.getRevision(), countersSfi, counterIndex, valueToIncrement);

        List<PoSendableInSession> filesToChangeInSession = new ArrayList<PoSendableInSession>();
        filesToChangeInSession.add(poIncreaseCmd_Counter);

        poTransaction.processPoCommands(filesToChangeInSession);

        poTransaction.processClosing(null, null, null, false);
    }

    @Test
    public void testWriteCounter() {

        try {

            List<SeResponse> seResponses = selectPO();

            PoSecureSession poTransaction = new PoSecureSession(TestEngine.poReader, TestEngine.csmReader, null);

            if (seResponses.get(0) != null) {

                ApduResponse fciData = seResponses.get(0).getFci();

                byte[] counterData = new byte[] {0x00, 0x00, 0x0A, 0x00, 0x01, 0x00, 0x00, 0x0B, 0x00, 0x01, 0x00, 0x00, 0x0C,
                        0x00, 0x00, 0x00, 0x00, (byte) 0xB0, 0x00, (byte) 0xC0, 0x00, 0x0F, 0x00, 0x00, 0x00, 0x00, 0x0D, 0x00, 0x00};

                updateRecord(poTransaction, fciData, (byte) 0x19, (byte) 0x01, counterData);

                byte[] updatedCounterData = readRecords(poTransaction, fciData, (byte) 0x19, (byte) 0x01, true);

                Assertions.assertArrayEquals(counterData, updatedCounterData);

            } else if (seResponses.get(1) != null) {

                ApduResponse fciData = seResponses.get(1).getFci();

                byte[] counterData = new byte[] {0x00, 0x00, 0x0A, 0x01, 0x00, 0x00};

                updateRecord(poTransaction, fciData, (byte) 0x1B, (byte) 0x01, counterData);

                byte[] updatedCounterData = readRecords(poTransaction, fciData, (byte) 0x1B, (byte) 0x01, true);

                Assertions.assertArrayEquals(counterData, updatedCounterData);

            } else if (seResponses.get(2) != null) {

                ApduResponse fciData = seResponses.get(2).getFci();

                byte[] counterData = new byte[] {0x00, 0x00, 0x0A, 0x00, 0x01, 0x00, 0x00, 0x0B, 0x00, 0x01, 0x00, 0x00, 0x0C,
                        0x00, 0x00, 0x00, 0x00, (byte) 0xB0, 0x00, (byte) 0xC0, 0x00, 0x0F, 0x00, 0x00, 0x00, 0x00, 0x0D, 0x00, 0x00};

                updateRecord(poTransaction, fciData, (byte) 0x19, (byte) 0x01, counterData);

                byte[] updatedCounterData = readRecords(poTransaction, fciData, (byte) 0x19, (byte) 0x01, true);

                Assertions.assertArrayEquals(counterData, updatedCounterData);

                for(int i = 0; i < 9; i++) {

                    byte[] updatedSingleCounterData = readRecords(poTransaction, fciData, (byte) (0x0A + i), (byte) 0x01, true);

                    int updatedCounterValue = getCounterValueFromByteArray(updatedSingleCounterData, 1);

                    int expectedCounterValue = getCounterValueFromByteArray(counterData, i+1);

                    Assertions.assertEquals(expectedCounterValue, updatedCounterValue);

                }

            } else {
                Assertions.fail("No recognizable PO detected.");
                System.out.println("No recognizable PO detected.");
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    @Test
    public void testDecreaseCounter() {

        try {

            List<SeResponse> seResponses = selectPO();

            PoSecureSession poTransaction = new PoSecureSession(TestEngine.poReader, TestEngine.csmReader, null);

            if (seResponses.get(0) != null) {

                ApduResponse fciData = seResponses.get(0).getFci();

                byte[] initialCounterData = readRecords(poTransaction, fciData, (byte) 0x19, (byte) 0x01, true);

                for(int i = 0; i < 9; i++) {

                    int counterValue = getCounterValueFromByteArray(initialCounterData, i+1);

                    if(counterValue > 0) {

                        int valueToDecrement = counterValue / 2;
                        decreaseCounter(poTransaction, fciData, (byte) 0x19, (byte) (i+1), valueToDecrement);

                        byte[] updatedCounters = readRecords(poTransaction, fciData, (byte) 0x19, (byte) 0x01, true);
                        int finalValue = getCounterValueFromByteArray(updatedCounters, i+1);

                        Assertions.assertEquals(counterValue - valueToDecrement, finalValue);
                    }
                }

            } else if (seResponses.get(1) != null) {

                ApduResponse fciData = seResponses.get(1).getFci();

                byte[] initialCounterData = readRecords(poTransaction, fciData, (byte) 0x1B, (byte) 0x01, true);

                for(int i = 0; i < 2; i++) {

                    int counterValue = getCounterValueFromByteArray(initialCounterData, i+1);

                    if(counterValue > 0) {

                        int valueToDecrement = counterValue / 2;
                        decreaseCounter(poTransaction, fciData, (byte) 0x1B, (byte) (i+1), valueToDecrement);

                        byte[] updatedCounters = readRecords(poTransaction, fciData, (byte) 0x1B, (byte) 0x01, true);
                        int finalValue = getCounterValueFromByteArray(updatedCounters, i+1);

                        Assertions.assertEquals(counterValue - valueToDecrement, finalValue);
                    }
                }

            } else if (seResponses.get(2) != null) {

                ApduResponse fciData = seResponses.get(2).getFci();

                byte[] initialCounterData = readRecords(poTransaction, fciData, (byte) 0x19, (byte) 0x01, true);

                for(int i = 0; i < 9; i++) {

                    int counterValue = getCounterValueFromByteArray(initialCounterData, i+1);

                    if(counterValue > 0) {

                        int valueToDecrement = counterValue / 2;
                        decreaseCounter(poTransaction, fciData, (byte) (0x0A + i), (byte) 0x01, valueToDecrement);

                        byte[] updatedCounters = readRecords(poTransaction, fciData, (byte) 0x19, (byte) 0x01, true);
                        int finalValue = getCounterValueFromByteArray(updatedCounters, i+1);

                        Assertions.assertEquals(counterValue - valueToDecrement, finalValue);
                    }
                }

            } else {
                Assertions.fail("No recognizable PO detected.");
                System.out.println("No recognizable PO detected.");
            }


        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @Test
    public void testIncreaseCounter() {

        try {

            List<SeResponse> seResponses = selectPO();

            PoSecureSession poTransaction = new PoSecureSession(TestEngine.poReader, TestEngine.csmReader, null);

            if (seResponses.get(0) != null) {

                ApduResponse fciData = seResponses.get(0).getFci();

                byte[] initialCounterData = readRecords(poTransaction, fciData, (byte) 0x19, (byte) 0x01, true);

                for(int i = 0; i < 9; i++) {

                    int counterValue = getCounterValueFromByteArray(initialCounterData, i+1);
                    int maxValue = 0xFFFFFF;

                    if(counterValue > 0) {

                        int valueToIncrement = (maxValue - counterValue) / 2;
                        increaseCounter(poTransaction, fciData, (byte) 0x19, (byte) (i+1), valueToIncrement);

                        byte[] updatedCounters = readRecords(poTransaction, fciData, (byte) 0x19, (byte) 0x01, true);
                        int finalValue = getCounterValueFromByteArray(updatedCounters, i+1);

                        Assertions.assertEquals(counterValue + valueToIncrement, finalValue);
                    }
                }

            } else if (seResponses.get(1) != null) {

                ApduResponse fciData = seResponses.get(1).getFci();

                byte[] initialCounterData = readRecords(poTransaction, fciData, (byte) 0x1B, (byte) 0x01, true);

                for(int i = 0; i < 2; i++) {

                    int counterValue = getCounterValueFromByteArray(initialCounterData, i+1);
                    int maxValue = 0xFFFFFF;

                    if(counterValue > 0) {

                        int valueToIncrement = (maxValue - counterValue) / 2;
                        increaseCounter(poTransaction, fciData, (byte) 0x1B, (byte) (i+1), valueToIncrement);

                        byte[] updatedCounters = readRecords(poTransaction, fciData, (byte) 0x1B, (byte) 0x01, true);
                        int finalValue = getCounterValueFromByteArray(updatedCounters, i+1);

                        Assertions.assertEquals(counterValue + valueToIncrement, finalValue);
                    }
                }

            } else if (seResponses.get(2) != null) {

                ApduResponse fciData = seResponses.get(2).getFci();

                byte[] initialCounterData = readRecords(poTransaction, fciData, (byte) 0x19, (byte) 0x01, true);

                for(int i = 0; i < 9; i++) {

                    int counterValue = getCounterValueFromByteArray(initialCounterData, i+1);
                    int maxValue = 0xFFFFFF;

                    if(counterValue > 0) {

                        int valueToIncrement = (maxValue - counterValue) / 2;
                        increaseCounter(poTransaction, fciData, (byte) (0x0A + i), (byte) (0x01), valueToIncrement);

                        byte[] updatedCounters = readRecords(poTransaction, fciData, (byte) 0x19, (byte) 0x01, true);
                        int finalValue = getCounterValueFromByteArray(updatedCounters, i+1);

                        Assertions.assertEquals(counterValue + valueToIncrement, finalValue);
                    }
                }

            } else {
                Assertions.fail("No recognizable PO detected.");
                System.out.println("No recognizable PO detected.");
            }


        } catch (Exception e) {

            e.printStackTrace();
        }

    }


    @Test
    public void testReadRecords() {

        try {

            List<SeResponse> seResponses = selectPO();

            PoSecureSession poTransaction = new PoSecureSession(TestEngine.poReader, TestEngine.csmReader, null);

            if (seResponses.get(0) != null) { // Rev3

                ApduResponse fciData = seResponses.get(0).getFci();

            } else if (seResponses.get(1) != null) { // CLAP

                ApduResponse fciData = seResponses.get(1).getFci();

                byte[] firstRecordData = new byte[] {0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
                        0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
                        0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
                        0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01};

                byte[] secondRecordData = new byte[] {0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
                        0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
                        0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
                        0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02};

                updateRecord(poTransaction, fciData, (byte) 0x15, (byte) 0x01, firstRecordData);
                updateRecord(poTransaction, fciData, (byte) 0x15, (byte) 0x02, secondRecordData);

                byte[] contractRecordsData = readRecords(poTransaction, fciData, (byte) 0x15, (byte) 0x01, false);

                Assertions.assertEquals(((64 + 2) * 2), contractRecordsData.length);

                Assertions.assertEquals(0x01, contractRecordsData[0]);
                Assertions.assertEquals(0x40, contractRecordsData[1]);


                byte[] firstRecordDataRead = new byte[64];
                System.arraycopy(contractRecordsData, 2, firstRecordDataRead, 0, 64);

                Assertions.assertArrayEquals(firstRecordData, firstRecordDataRead);

                Assertions.assertEquals(0x02, contractRecordsData[64+2]);
                Assertions.assertEquals(0x40, contractRecordsData[64+3]);

                byte[] secondRecordDataRead = new byte[64];
                System.arraycopy(contractRecordsData, 64+4, secondRecordDataRead, 0, 64);

                Assertions.assertArrayEquals(secondRecordData, secondRecordDataRead);

            } else if (seResponses.get(2) != null) { // Rev2.4

                ApduResponse fciData = seResponses.get(2).getFci();


            } else {
                Assertions.fail("No recognizable PO detected.");
                System.out.println("No recognizable PO detected.");
            }


        } catch (Exception e) {

            e.printStackTrace();
        }

    }
}
