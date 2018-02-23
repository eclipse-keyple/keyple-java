package org.keyple.examples.pc;

import org.keyple.calypso.commands.po.PoRevision;
import org.keyple.calypso.commands.po.builder.ReadRecordsCmdBuild;
import org.keyple.calypso.commands.po.builder.UpdateRecordCmdBuild;
import org.keyple.plugin.pcsc.PcscPlugin;
import org.keyple.seproxy.*;

import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BasicCardAccess {
    public static void main(String[] args) throws Exception {
        SeProxyService seProxyService = SeProxyService.getInstance();
        System.out.println("SeProxyServ v"+seProxyService.getVersion());
        seProxyService.setPlugins(Collections.singletonList(PcscPlugin.getInstance()));
        for (ReadersPlugin rp : seProxyService.getPlugins()) {
            System.out.println("Reader plugin: "+rp.getName());
            for( ProxyReader pr : rp.getReaders() ) {
                System.out.println("Reader name: "+pr.getName()+", present: "+pr.isSEPresent());
                parseInfo(pr);
            }
        }
    }

    public static void parseInfo(ProxyReader poReader) throws Exception {
        String poAid = "A000000291A000000191";
        String t2UsageRecord1_dataFill = "0102030405060708090A0B0C0D0E0F10" +
                "1112131415161718191A1B1C1D1E1F20" +
                "2122232425262728292A2B2C2D2E2F30";

        ReadRecordsCmdBuild poReadRecordCmd_T2Env = new ReadRecordsCmdBuild(PoRevision.REV3_1, (byte) 0x01, true, (byte) 0x14, (byte) 0x20);
        ReadRecordsCmdBuild poReadRecordCmd_T2Usage = new ReadRecordsCmdBuild(PoRevision.REV3_1, (byte) 0x01, true, (byte) 0x1A, (byte) 0x30);
        UpdateRecordCmdBuild poUpdateRecordCmd_T2UsageFill = new UpdateRecordCmdBuild(PoRevision.REV3_1, (byte) 0x01, (byte) 0x1A, DatatypeConverter.parseHexBinary(t2UsageRecord1_dataFill));

// Get PO ApduRequest List
        List<ApduRequest> poApduRequestList = new ArrayList<ApduRequest>();
        poApduRequestList.add(poReadRecordCmd_T2Env.getApduRequest());
        poApduRequestList.add(poReadRecordCmd_T2Usage.getApduRequest());
        poApduRequestList.add(poUpdateRecordCmd_T2UsageFill.getApduRequest());

        SeRequest poRequest = new SeRequest(DatatypeConverter.parseHexBinary(poAid), poApduRequestList, false);
        SeResponse poResponse = poReader.transmit(poRequest);
        System.out.println("PoResponse: "+poResponse.getApduResponses());
    }
}
