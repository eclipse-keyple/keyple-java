/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.remote_se.transport.json;


import java.util.*;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.seproxy.protocol.ContactsProtocols;
import org.eclipse.keyple.util.ByteArrayUtils;

public class SampleFactory {


    public static SeRequestSet getRequestIsoDepSetSample() {
        String poAid = "A000000291A000000191";

        // build 1st seRequestSet with keep channel open to true
        ReadRecordsCmdBuild poReadRecordCmd_T2Env = new ReadRecordsCmdBuild(PoRevision.REV3_1,
                (byte) 0x14, (byte) 0x01, true, (byte) 0x20, "Hoplink EF T2Environment");


        List<ApduRequest> poApduRequestList;

        poApduRequestList = Arrays.asList(poReadRecordCmd_T2Env.getApduRequest());

        SeRequest.Selector selector = new SeRequest.AidSelector(ByteArrayUtils.fromHex(poAid));

        SeRequest seRequest = new SeRequest(selector, poApduRequestList, false,
                ContactlessProtocols.PROTOCOL_ISO14443_4);

        return new SeRequestSet(seRequest);

    }

    public static SeRequestSet getCompleteRequestSet() {
        String poAid = "A000000291A000000191";

        // build 1st seRequestSet with keep channel open to true
        ReadRecordsCmdBuild poReadRecordCmd_T2Env = new ReadRecordsCmdBuild(PoRevision.REV3_1,
                (byte) 0x14, (byte) 0x01, true, (byte) 0x20, "Hoplink EF T2Environment");


        List<ApduRequest> poApduRequestList;

        poApduRequestList = Arrays.asList(poReadRecordCmd_T2Env.getApduRequest());

        SeRequest.Selector aIDselector = new SeRequest.AidSelector(ByteArrayUtils.fromHex(poAid));
        SeRequest.Selector aTRselector = new SeRequest.AtrSelector("/regex/");

        SeRequest seRequest = new SeRequest(aIDselector, poApduRequestList, false,
                ContactlessProtocols.PROTOCOL_ISO14443_4);

        SeRequest seRequest2 = new SeRequest(aTRselector, poApduRequestList, true,
                ContactsProtocols.PROTOCOL_ISO7816_3);

        Set<SeRequest> seRequests = new HashSet<SeRequest>();
        seRequests.add(seRequest);
        seRequests.add(seRequest2);

        SeRequestSet requestSet = new SeRequestSet(seRequests);

        return requestSet;


    }

    public static SeResponseSet getCompeleteResponseSet() {
        List<SeResponse> seResponses = new ArrayList<SeResponse>();
        seResponses.add(SeResponseTest.getASeResponse());
        seResponses.add(SeResponseTest.getASeResponse());
        return new SeResponseSet(seResponses);


    }


}
