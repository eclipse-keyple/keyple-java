package org.eclipse.keyple.integration.calypso;

import org.eclipse.keyple.calypso.command.po.parser.GetDataFciRespPars;
import org.eclipse.keyple.seproxy.ApduResponse;
import org.eclipse.keyple.util.ByteBufferUtils;

import java.nio.ByteBuffer;

public class PoFileStructureInfo {

    public final static String poAuditC0Aid = "315449432E4943414C54"; // AID of the Rev3 PO with Audit C0 profile
    public final static String clapAid = "315449432E494341D62010029101"; // AID of the CLAP product being tested
    public final static String cdLightAid = "315449432E494341"; // AID of the Rev2.4 PO emulating CDLight

    private EfData environmentFileData;

    private EfData eventFileData;

    private EfData contractListFileData;

    private EfData contractFileData;

    private EfData countersFileData;

    private EfData simulatedCountersFileData;

    private ApduResponse fciData;

    private void setFileStructureForAuditC0() {

        environmentFileData = new EfData(1, (byte) 0x07, 29);
        eventFileData = new EfData(3, (byte) 0x08, 29);
        contractListFileData = new EfData(20, (byte) 0x1E, 10);
        contractFileData = new EfData(4, (byte) 0x09, 29);
        countersFileData = new EfData(1, (byte) 0x19, 29);
        simulatedCountersFileData = new EfData(0, (byte) 0x00, 0);

    }


    private void setFileStructureForClap() {

        environmentFileData = new EfData(1, (byte) 0x14, 32);
        eventFileData = new EfData(2, (byte) 0x08, 64);
        contractListFileData = new EfData(2, (byte) 0x1A, 48);
        contractFileData = new EfData(2, (byte) 0x15, 64);
        countersFileData = new EfData(1, (byte) 0x1B, 6);
        simulatedCountersFileData = new EfData(0, (byte) 0x00, 0);

    }


    private void setFileStructureForCdLight() {

        environmentFileData = new EfData(1, (byte) 0x07, 29);
        eventFileData = new EfData(3, (byte) 0x08, 29);
        contractListFileData = new EfData(1, (byte) 0x1E, 29);
        contractFileData = new EfData(4, (byte) 0x09, 29);
        countersFileData = new EfData(1, (byte) 0x19, 29);
        simulatedCountersFileData = new EfData(9, (byte) 0x0A, 3);

    }

    public PoFileStructureInfo(ApduResponse poFciData) {

        GetDataFciRespPars poFciRespPars = new GetDataFciRespPars(poFciData);
        ByteBuffer poCalypsoInstanceAid = poFciRespPars.getDfName();

        if(poCalypsoInstanceAid.equals((ByteBufferUtils.fromHex(poAuditC0Aid)))) {

            setFileStructureForAuditC0();

        } else if(poCalypsoInstanceAid.equals((ByteBufferUtils.fromHex(clapAid)))) {

            setFileStructureForClap();

        } else if(poCalypsoInstanceAid.equals((ByteBufferUtils.fromHex(cdLightAid)))) {

            setFileStructureForCdLight();

        } else {
            throw new IllegalArgumentException("The file structure for AID " + ByteBufferUtils.toHex(poCalypsoInstanceAid) + " is not registered for testing.");
        }

        fciData = poFciData;
    }

    public EfData getEnvironmentFileData() {
        return environmentFileData;
    }

    public EfData getEventFileData() {
        return eventFileData;
    }

    public EfData getContractListFileData() {
        return contractListFileData;
    }

    public EfData getContractFileData() {
        return contractFileData;
    }

    public EfData getCountersFileData() {
        return countersFileData;
    }

    public EfData getSimulatedCountersFileData() {
        return simulatedCountersFileData;
    }

    public ApduResponse getFciData() {
        return fciData;
    }
}
