/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.example.remote.common;

import java.util.Arrays;
import java.util.List;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.plugin.remote_se.rse.ISeResponseSetCallback;
import org.eclipse.keyple.plugin.remote_se.rse.RsePlugin;
import org.eclipse.keyple.plugin.remote_se.rse.RseReader;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.util.ByteBufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandSample {




    static public void transmit(final Logger logger, final String remoteReaderName) {
        try {


            logger.info("--- PURE SYNC COMMAND RUNNING ---");

            // get the reader by its name
            final RseReader reader = (RseReader) ((RsePlugin) SeProxyService.getInstance()
                    .getPlugins().first()).getReaderByRemoteName(remoteReaderName);

            String poAid = "A000000291A000000191";

            // build 1st seRequestSet with keep channel open to true
            final ReadRecordsCmdBuild poReadRecordCmd_T2Env = new ReadRecordsCmdBuild(
                    PoRevision.REV3_1, (byte) 0x14, (byte) 0x01, true, (byte) 0x20);



            List<ApduRequest> poApduRequestList;
            poApduRequestList = Arrays.asList(poReadRecordCmd_T2Env.getApduRequest());
            final SeRequest.Selector selector =
                    new SeRequest.AidSelector(ByteBufferUtils.fromHex(poAid));
            SeRequest seRequest = new SeRequest(selector, poApduRequestList, true);

            // SYNC transmit seRequestSet to Reader With Callback function
            SeResponseSet seResponseSet = reader.transmitNewThread(new SeRequestSet(seRequest));

            logger.info(
                    "Received SYNCHRONOUSLY a 1rt SeResponseSet - isSuccessful : {}",seResponseSet.getSingleResponse().getApduResponses().iterator().next().isSuccessful());

            // build 1st seRequestSet with keep channel open to true
            final ReadRecordsCmdBuild poReadRecordCmd_T2Env2 = new ReadRecordsCmdBuild(
                    PoRevision.REV3_1, (byte) 0x14, (byte) 0x01, true, (byte) 0x20);



            List<ApduRequest> poApduRequestList2;
            poApduRequestList2 = Arrays.asList(poReadRecordCmd_T2Env.getApduRequest());

            SeRequest seRequest2 = new SeRequest(selector, poApduRequestList2, false);

            // SYNC transmit seRequestSet to Reader With Callback function
            SeResponseSet seResponseSet2 = reader.transmitNewThread(new SeRequestSet(seRequest2));

            logger.info(
                    "Received SYNCHRONOUSLY a 2nd SeResponseSet - isSuccessful : {}",seResponseSet2.getSingleResponse().getApduResponses().iterator().next().isSuccessful());


        } catch (KeypleReaderNotFoundException e) {
            e.printStackTrace();
        } catch (KeypleReaderException e) {
            e.printStackTrace();
        }



    }

    static public void asyncTransmit(final Logger logger, final String remoteReaderName) {
        try {

            // get the reader by its name
            final RseReader reader =
                    (RseReader) ((RsePlugin) SeProxyService.getInstance().getPlugins().first())
                            .getReaderByRemoteName(remoteReaderName);

            logger.info("--- ASYNC COMMAND RUNNING ---");


            String poAid = "A000000291A000000191";

            // build 1st seRequestSet with keep channel open to true
            final ReadRecordsCmdBuild poReadRecordCmd_T2Env = new ReadRecordsCmdBuild(
                    PoRevision.REV3_1, (byte) 0x14, (byte) 0x01, true, (byte) 0x20);



            List<ApduRequest> poApduRequestList;
            poApduRequestList = Arrays.asList(poReadRecordCmd_T2Env.getApduRequest());
            final SeRequest.Selector selector =
                    new SeRequest.AidSelector(ByteBufferUtils.fromHex(poAid));
            SeRequest seRequest = new SeRequest(selector, poApduRequestList, true);

            // ASYNC transmit seRequestSet to Reader With Callback function
            reader.asyncTransmit(new SeRequestSet(seRequest), new ISeResponseSetCallback() {
                @Override
                public void getResponseSet(SeResponseSet seResponseSet) {
                    logger.info(
                            "Received asynchronously a SeResponseSet with Webservice RemoteSE {}",
                            seResponseSet);

                    List<ApduRequest> poApduRequestList2;

                    final ReadRecordsCmdBuild poReadRecordCmd_T2Usage = new ReadRecordsCmdBuild(
                            PoRevision.REV3_1, (byte) 0x1A, (byte) 0x01, true, (byte) 0x30);
                    poApduRequestList2 = Arrays.asList(poReadRecordCmd_T2Usage.getApduRequest(),
                            poReadRecordCmd_T2Usage.getApduRequest());

                    SeRequest seRequest2 = new SeRequest(selector, poApduRequestList2, false);

                    // ASYNC transmit seRequestSet to Reader
                    try {
                        ((RseReader) reader).asyncTransmit(new SeRequestSet(seRequest2),
                                new ISeResponseSetCallback() {
                                    @Override
                                    public void getResponseSet(SeResponseSet seResponseSet) {
                                        logger.info(
                                                "Received asynchronously a SeResponseSet with Webservice RemoteSE : {}",
                                                seResponseSet);

                                        //continue here

                                    }
                                });
                    } catch (KeypleReaderException e) {
                        e.printStackTrace();
                    }
                }
            });



        } catch (KeypleReaderNotFoundException e) {
            e.printStackTrace();
        } catch (KeypleReaderException e) {
            e.printStackTrace();
        }

    }
    @Deprecated
    static public void transmitSyncCommand(final Logger logger, final String remoteReaderName) {

        Thread thread = new Thread() {
            public void run() {
                try {


                    logger.info("--- NEW THREAD FOR SYNC COMMAND RUNNING ---");

                    // get the reader by its name
                    final RseReader reader = (RseReader) ((RsePlugin) SeProxyService.getInstance()
                            .getPlugins().first()).getReaderByRemoteName(remoteReaderName);

                    String poAid = "A000000291A000000191";

                    // build 1st seRequestSet with keep channel open to true
                    final ReadRecordsCmdBuild poReadRecordCmd_T2Env = new ReadRecordsCmdBuild(
                            PoRevision.REV3_1, (byte) 0x14, (byte) 0x01, true, (byte) 0x20);



                    List<ApduRequest> poApduRequestList;
                    poApduRequestList = Arrays.asList(poReadRecordCmd_T2Env.getApduRequest());
                    final SeRequest.Selector selector =
                            new SeRequest.AidSelector(ByteBufferUtils.fromHex(poAid));
                    SeRequest seRequest = new SeRequest(selector, poApduRequestList, true);

                    // SYNC transmit seRequestSet to Reader With Callback function
                    SeResponseSet seResponseSet = reader.transmit(new SeRequestSet(seRequest));

                    logger.info(
                            "Received SYNCHRONOUSLY a 1rt SeResponseSet with keep channel open {}, details : {}",
                            seResponseSet.getResponses().get(0).wasChannelPreviouslyOpen(), seResponseSet);

                    // build 1st seRequestSet with keep channel open to true
                    final ReadRecordsCmdBuild poReadRecordCmd_T2Env2 = new ReadRecordsCmdBuild(
                            PoRevision.REV3_1, (byte) 0x14, (byte) 0x01, true, (byte) 0x20);



                    List<ApduRequest> poApduRequestList2;
                    poApduRequestList2 = Arrays.asList(poReadRecordCmd_T2Env.getApduRequest());

                    SeRequest seRequest2 = new SeRequest(selector, poApduRequestList2, false);

                    // SYNC transmit seRequestSet to Reader With Callback function
                    SeResponseSet seResponseSet2 = reader.transmit(new SeRequestSet(seRequest2));

                    logger.info(
                            "Received SYNCHRONOUSLY a 2nd SeResponseSet with keep channel open {}, details : {}",
                            seResponseSet2.getResponses().get(0).wasChannelPreviouslyOpen(), seResponseSet2);


                } catch (KeypleReaderNotFoundException e) {
                    e.printStackTrace();
                } catch (KeypleReaderException e) {
                    e.printStackTrace();
                }


            }
        };

        thread.start();
    }



}
