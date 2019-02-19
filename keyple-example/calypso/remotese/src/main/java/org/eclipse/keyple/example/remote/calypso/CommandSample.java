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
package org.eclipse.keyple.example.remote.calypso;

import java.util.Arrays;
import java.util.List;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.plugin.remotese.pluginse.RemoteSePlugin;
import org.eclipse.keyple.plugin.remotese.pluginse.VirtualReader;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;

class CommandSample {



    static public void transmit(final Logger logger, final String remoteReaderName) {
        try {


            logger.info("--- PURE SYNC COMMAND RUNNING ---");

            // get the reader by its name
            final VirtualReader reader = (VirtualReader) ((RemoteSePlugin) SeProxyService
                    .getInstance().getPlugins().first()).getReaderByRemoteName(remoteReaderName);

            String poAid = "A000000291A000000191";

            // build 1st seRequestSet with keep channel open to true
            ReadRecordsCmdBuild poReadRecordCmd_T2Env = new ReadRecordsCmdBuild(PoRevision.REV3_1,
                    (byte) 0x14, (byte) 0x01, true, (byte) 0x20, "Hoplink EF T2Environment");

            List<ApduRequest> poApduRequestList;
            poApduRequestList = Arrays.asList(poReadRecordCmd_T2Env.getApduRequest());
            final SeRequest.Selector selector =
                    new SeRequest.AidSelector(ByteArrayUtils.fromHex(poAid));
            SeRequest seRequest = new SeRequest(selector, poApduRequestList, true,
                    ContactlessProtocols.PROTOCOL_ISO14443_4);

            // SYNC transmit seRequestSet to Reader With Callback function
            SeResponseSet seResponseSet = reader.transmitSet(new SeRequestSet(seRequest));

            logger.info("Received SYNCHRONOUSLY a 1rt SeResponseSet - isSuccessful : {}",
                    seResponseSet);

            // build 1st seRequestSet with keep channel open to true
            ReadRecordsCmdBuild poReadRecordCmd_T2Env2 = new ReadRecordsCmdBuild(PoRevision.REV3_1,
                    (byte) 0x14, (byte) 0x01, true, (byte) 0x20, "Hoplink EF T2Environment");

            List<ApduRequest> poApduRequestList2;
            poApduRequestList2 = Arrays.asList(poReadRecordCmd_T2Env2.getApduRequest());

            SeRequest seRequest2 = new SeRequest(selector, poApduRequestList2, false,
                    ContactlessProtocols.PROTOCOL_ISO14443_4);

            // SYNC transmit seRequestSet to Reader With Callback function
            SeResponseSet seResponseSet2 = reader.transmitSet(new SeRequestSet(seRequest2));

            logger.info("Received SYNCHRONOUSLY a 2nd SeResponseSet - isSuccessful : {}",
                    seResponseSet2);


        } catch (KeypleReaderNotFoundException e) {
            e.printStackTrace();
        } catch (KeypleReaderException e) {
            e.printStackTrace();
        }

    }
}
