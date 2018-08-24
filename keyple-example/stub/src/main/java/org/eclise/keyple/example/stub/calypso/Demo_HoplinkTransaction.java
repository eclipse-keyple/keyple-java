package org.eclise.keyple.example.stub.calypso;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.eclipse.keyple.calypso.command.PoSendableInSession;
import org.eclipse.keyple.calypso.command.po.builder.AbstractOpenSessionCmdBuild;
import org.eclipse.keyple.calypso.transaction.PoSecureSession;
import org.eclipse.keyple.example.common.HoplinkInfoAndSampleCommands;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.plugin.stub.StubSecureElement;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.util.ByteBufferUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Pattern;

public class Demo_HoplinkTransaction implements ObservableReader.ReaderObserver  {

    private static final ILogger logger = SLoggerFactory.getLogger(Demo_HoplinkTransaction.class);

    private ProxyReader poReader, csmReader;

        public Demo_HoplinkTransaction() {}

        @Override
        public void update(ReaderEvent event) {
            switch (event.getEventType()) {
                case SE_INSERTED:
                    logger.info("SE INSERTED");
                    logger.info("\nStart processing of a Calypso PO");
                    operatePoTransactions();
                    break;
                case SE_REMOVAL:
                    logger.info("SE REMOVED");
                    logger.info("\nWait for Calypso PO");
                    break;
                default:
                    logger.error("IO Error");
            }
        }

        /**
         * Display SeRequest and SeResponse details in the console
         *
         * @param message user message
         * @param seRequest current SeRequest
         * @param seResponse current SeResponse (defined as public for purposes of javadoc)
         */
        public void printSelectAppResponseStatus(String message, SeRequest seRequest,
                                                 SeResponse seResponse) {
            int i;
            logger.info("===== " + message);
            logger.info("* Request:");
            logger.info("AID: " + ByteBufferUtils
                    .toHex(((SeRequest.AidSelector) seRequest.getSelector()).getAidToSelect()));
            List<ApduRequest> apduRequests = seRequest.getApduRequests();
            i = 0;
            if (apduRequests != null && apduRequests.size() > 0) {
                for (ApduRequest apduRequest : apduRequests) {
                    logger.info(
                            "COMMAND#" + i + ": " + ByteBufferUtils.toHex(apduRequest.getBytes()));
                    i++;
                }
            } else {
                logger.info("No APDU request");
            }
            logger.info("keepChannelOpen flag: " + seRequest.isKeepChannelOpen());
            logger.info("protocol flag: " + seRequest.getProtocolFlag());

            logger.info("* Response:");
            if (seResponse == null) {
                logger.info("SeResponse is null");
            } else {
                ApduResponse atr, fci;
                atr = seResponse.getAtr();
                fci = seResponse.getFci();
                List<ApduResponse> apduResponses = seResponse.getApduResponses();
                if (atr != null) {
                    logger.info("ATR: " + ByteBufferUtils.toHex(atr.getDataOut()));
                } else {
                    logger.info("ATR: null");
                }
                if (fci != null) {
                    logger.info("FCI: " + ByteBufferUtils.toHex(fci.getDataOut()));
                } else {
                    logger.info("FCI: null");
                }
                if (apduResponses.size() > 0) {
                    i = 0;
                    for (ApduResponse apduResponse : apduResponses) {
                        logger.info("RESPONSE#" + i + ": "
                                + ByteBufferUtils.toHex(apduResponse.getDataOut()) + ", SW1SW2: "
                                + Integer.toHexString(apduResponse.getStatusCode() & 0xFFFF));
                        i++;
                    }
                }
            }
            // new line
            logger.info("");
        }

        /**
         * Do an 2-step Hoplink transaction:
         * <ul>
         * <li>Process identification</li>
         * <li>Process Opening and Closing</li>
         * </ul>
         * <p>
         * File with SFI 1A is read at session opening.
         * <p>
         * T2 Environment and T2 Usage are read in session.
         *
         * @param poTransaction PoSecureSession object
         * @param fciData FCI data from the selection step
         * @param closeSeChannel flag to ask or not the channel closing at the end of the transaction
         * @throws IOReaderException reader exception (defined as public for purposes of javadoc)
         */
        public void doHoplinkTwoStepAuthentication(PoSecureSession poTransaction, ApduResponse fciData,
                                                   boolean closeSeChannel) throws IOReaderException {
            List<PoSendableInSession> filesToReadInSession = new ArrayList<PoSendableInSession>();
            filesToReadInSession.add(HoplinkInfoAndSampleCommands.poReadRecordCmd_T2Env);
            filesToReadInSession.add(HoplinkInfoAndSampleCommands.poReadRecordCmd_T2Usage);

            // Step 1
            logger.info(
                    "\n\n========= PO Hoplink 2-step transaction ======= Identification =====================");
            poTransaction.processIdentification(fciData);

            // Step 2A
            logger.info(
                    "========= PO Hoplink 2-step transaction ======= Opening + Closing ====================");

            byte debitKeyIndex = 0x03;
            // Open Session for the debit key #3 - with reading of the first record of the cyclic EF of
            // SFI 1Ah
            AbstractOpenSessionCmdBuild poOpenSession =
                    AbstractOpenSessionCmdBuild.create(poTransaction.getRevision(), debitKeyIndex,
                            poTransaction.sessionTerminalChallenge, (byte) 0x1A, (byte) 0x01);

            poTransaction.processOpeningClosing(poOpenSession, filesToReadInSession, null,
                    closeSeChannel);

            if (poTransaction.isSuccessful()) {
                logger.info(
                        "========= PO Hoplink 2-step transaction ======= SUCCESS !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            } else {
                logger.warn(
                        "========= PO Hoplink 2-step transaction ======= ERROR !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
        }

        /**
         * Do an 3-step Hoplink transaction:
         * <ul>
         * <li>Process identification</li>
         * <li>Process Opening and Closing</li>
         * </ul>
         * <p>
         * File with SFI 1A is read at session opening.
         * <p>
         * T2 Environment and T2 Usage are read in session.
         *
         * @param poTransaction PoSecureSession object
         * @param fciData FCI data from the selection step
         * @param closeSeChannel flag to ask or not the channel closing at the end of the transaction
         * @throws IOReaderException reader exception (defined as public for purposes of javadoc)
         */
        public void doHoplinkThreeStepReadWriteTransaction(PoSecureSession poTransaction,
                                                           ApduResponse fciData, boolean closeSeChannel) throws IOReaderException {


            List<PoSendableInSession> filesToReadInSession = new ArrayList<PoSendableInSession>();
            filesToReadInSession.add(HoplinkInfoAndSampleCommands.poReadRecordCmd_T2Env);
            filesToReadInSession.add(HoplinkInfoAndSampleCommands.poReadRecordCmd_T2Usage);
            // filesToReadInSession.add(HoplinkInfoAndSampleCommands.poUpdateRecordCmd_T2UsageFill);

            // Step 1
            logger.info(
                    "\n\n========= PO Hoplink 3-step session ======= Identification =====================");
            poTransaction.processIdentification(fciData);

            // Step 2
            logger.info(
                    "========= PO Hoplink 3-step session ======= Opening ============================");
            byte debitKeyIndex = 0x03;
            // Open Session for the debit key #3 - with reading of the first record of the cyclic EF of
            // SFI 0Ah
            AbstractOpenSessionCmdBuild poOpenSession =
                    AbstractOpenSessionCmdBuild.create(poTransaction.getRevision(), debitKeyIndex,
                            poTransaction.sessionTerminalChallenge, (byte) 0x1A, (byte) 0x01);
            poTransaction.processOpening(poOpenSession, filesToReadInSession);

            // Step 3
            logger.info(
                    "========= PO Hoplink 3-step session ======= Proceed =======================");
            poTransaction.processProceeding(filesToReadInSession);

            // Step 4
            logger.info(
                    "========= PO Hoplink 3-step session ======= Closing ============================");
            poTransaction.processClosing(null, null, HoplinkInfoAndSampleCommands.poRatificationCommand,
                    false);

            if (poTransaction.isSuccessful()) {
                logger.info(
                        "========= PO Hoplink 3-step session ======= SUCCESS !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            } else {
                logger.warn(
                        "========= PO Hoplink 3-step session ======= ERROR !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
        }

        /**
         * Chain 3 Hoplink transactions: 2-step, 3-step, 2-step (see @link
         * doHoplinkTwoStepAuthentication and @link doHoplinkThreeStepReadWriteTransaction)
         * <p>
         * To illustrate the the logical channel management, it is kept open after the 1st transaction.
         * <p>
         * Closed after the end of the 2nd transaction and reopened before the 3rd transaction.
         * <p>
         * Finally the logical channel is closed at the end of the 3rd transaction.
         *
         * @param poTransaction PoSecureSession object
         * @param fciData FCI data from the selection step
         * @throws IOReaderException reader exception (defined as public for purposes of javadoc)
         */
        public void operateMultipleHoplinkTransactions(PoSecureSession poTransaction,
                                                       ApduResponse fciData) throws IOReaderException {
            // execute a two-step Calypso session: processIdentification, processOpeningClosing
            // keep the logical channel opened
            doHoplinkTwoStepAuthentication(poTransaction, fciData, false);

            // execute a three-step Calypso session: processIdentification, processOpening,
            // processClosing
            // close the logical channel opened
            doHoplinkThreeStepReadWriteTransaction(poTransaction, fciData, true);

            // redo the Hoplink PO selection after logical channel closing (may be not needed with some
            // PO
            // for which the application is selected by default)
            SeRequestSet selectionRequest =
                    new SeRequestSet(new SeRequest(
                            new SeRequest.AidSelector(
                                    ByteBufferUtils.fromHex(HoplinkInfoAndSampleCommands.AID)),
                            null, true));
            fciData = poReader.transmit(selectionRequest).getSingleResponse().getFci();

            // execute a two-step Calypso session: processIdentification, processOpeningClosing
            // close the logical channel opened
            doHoplinkTwoStepAuthentication(poTransaction, fciData, true);
        }

        /**
         * Do the PO selection and possibly go on with Hoplink transactions.
         */
        public void operatePoTransactions() {
            try {
                // operate PO multiselection
                String poFakeAid = "AABBCCDDEE"; //
                String poNavigoAid = "A0000004040125090101"; // Navigo AID
                String poHoplinkAid = HoplinkInfoAndSampleCommands.AID; // commands before session, keep
                // true
                String csmC1ATRregex = HoplinkInfoAndSampleCommands.CSM_C1_ATR_REGEX; // csm identifier

                // check the availability of the CSM, open its physical and logical channels and keep it
                // open
                SeRequest csmCheckRequest =
                        new SeRequest(new SeRequest.AtrSelector(csmC1ATRregex), null, true);
                SeResponse csmCheckResponse =
                        csmReader.transmit(new SeRequestSet(csmCheckRequest)).getSingleResponse();

                if (csmCheckResponse == null) {
                    logger.info("Unable to open a logical channel for CSM!");
                    throw new IllegalStateException("CSM channel opening failure");
                }

                // prepare the PO selection SeRequestSet
                // Create a SeRequest list
                Set<SeRequest> selectionRequests = new LinkedHashSet<SeRequest>();

                // fake application seRequest preparation, addition to the list
                SeRequest seRequest = new SeRequest(
                        new SeRequest.AidSelector(ByteBufferUtils.fromHex(poFakeAid)), null, false);
                selectionRequests.add(seRequest);

                // Navigo application seRequest preparation, addition to the list
                seRequest = new SeRequest(
                        new SeRequest.AidSelector(ByteBufferUtils.fromHex(poNavigoAid)), null, false);
                selectionRequests.add(seRequest);

                // Hoplink application seRequest preparation, addition to the list
                // read commands before session
                List<ApduRequest> requestToExecuteBeforeSession = new ArrayList<ApduRequest>();
                requestToExecuteBeforeSession
                        .add(HoplinkInfoAndSampleCommands.poReadRecordCmd_T2Env.getApduRequest());

                // AID based selection
                seRequest =
                        new SeRequest(new SeRequest.AidSelector(ByteBufferUtils.fromHex(poHoplinkAid)),
                                requestToExecuteBeforeSession, false,
                                HoplinkInfoAndSampleCommands.selectApplicationSuccessfulStatusCodes);

                selectionRequests.add(seRequest);

                List<SeResponse> seResponses =
                        poReader.transmit(new SeRequestSet(selectionRequests)).getResponses();

                Iterator<SeRequest> seReqIterator = selectionRequests.iterator();
                Iterator<SeResponse> seRespIterator = seResponses.iterator();

                // we expect 3 responses
                printSelectAppResponseStatus("Case #1: fake AID", seReqIterator.next(),
                        seRespIterator.next());
                printSelectAppResponseStatus("Case #2: Navigo AID", seReqIterator.next(),
                        seRespIterator.next());
                printSelectAppResponseStatus("Case #3: Hoplink AID", seReqIterator.next(),
                        seRespIterator.next());

                PoSecureSession poTransaction = new PoSecureSession(poReader, csmReader, (byte) 0x00);

                // test if the Hoplink selection succeeded
                if (seResponses.get(2) != null) {
                    ApduResponse fciData = seResponses.get(2).getFci();
                    operateMultipleHoplinkTransactions(poTransaction, fciData);
                } else {
                    logger.info(
                            "No Hoplink transaction. SeResponse to Hoplink selection was null.");
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        /**
         * Get the terminal which names match the expected pattern
         *
         * @param seProxyService SE Proxy service
         * @param pattern regex pattern to select a reader
         * @return ProxyReader
         * @throws IOReaderException Any error with the card communication (defined as public for
         *         purposes of javadoc)
         */
        public ProxyReader getReader(SeProxyService seProxyService, String pattern)
                throws IOReaderException {
            Pattern p = Pattern.compile(pattern);
            for (ReaderPlugin plugin : seProxyService.getPlugins()) {
                for (ProxyReader reader : plugin.getReaders()) {
                    if (p.matcher(reader.getName()).matches()) {
                        return reader;
                    }
                }
            }
            return null;
        }

        /**
         * This object is used to freeze the main thread while card operations are handle through the
         * observers callbacks. A call to the notify() method would end the program (not demonstrated
         * here).
         */
        private static final Object waitForEnd = new Object();

        /**
         * main program entry
         *
         * @param args the program arguments
         * @throws IOException setParameter exception
         * @throws IOReaderException reader exception
         * @throws InterruptedException thread exception
         */
        public static void main(String[] args)
                throws IOException, IOReaderException, InterruptedException {

            SeProxyService seProxyService = SeProxyService.getInstance();
            SortedSet<ReaderPlugin> pluginsSet = new ConcurrentSkipListSet<ReaderPlugin>();
            pluginsSet.add(StubPlugin.getInstance());
            seProxyService.setPlugins(pluginsSet);

            // Setting up ourself as an observer
            Demo_HoplinkTransaction observer = new Demo_HoplinkTransaction();

            StubReader poReader = StubPlugin.getInstance().plugStubReader("poReader");
            StubReader csmReader = StubPlugin.getInstance().plugStubReader("csmReader");


            logger.info("PO Reader  : " + poReader.getName());
            logger.info("CSM Reader : " + csmReader.getName());


            observer.poReader = poReader;
            observer.csmReader = csmReader;

            // Set terminal as Observer of the first reader
            ((ObservableReader) poReader).addObserver(observer);


            StubSecureElement hoplinkSE = new HoplinkStubSE();
            StubSecureElement csmSE = new CSMStubSE();

            csmReader.insertSe(csmSE);
            poReader.insertSe(hoplinkSE);
            poReader.removeSe();


        }
    }

