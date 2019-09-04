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
package org.eclipse.keyple.integration.poData;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.eclipse.keyple.calypso.command.po.parser.SelectFileRespPars;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.integration.IntegrationUtils;
import org.slf4j.Logger;

public class PoApplicationData {

    public class Issuer {

        private String value;

        private String name;

        public Issuer(byte inValue) {

            value = String.format("%02X", inValue);

            name = new String(IntegrationUtils.getIssuerName(inValue));
        }

        public String getValue() {
            return value;
        }

        public String getName() {
            return name;
        }
    }

    private byte[] fci;

    private byte[] dfInfo;

    private String calypsoRevision;

    private byte[] aid;

    private byte[] csn;

    private long csnDec;

    private String sessionModif;

    private int sessionModifDec;

    private String platform;

    private String applicationType;

    private String applicationSubtype;

    private Issuer issuer;

    private String version;

    private String revision;

    private String transactionCounter;

    private long transactionCounterDec;

    private AccessConditions accessConditions;

    private String status;

    private String kif1;

    private String kif2;

    private String kif3;

    private String kvc1;

    private String kvc2;

    private String kvc3;

    private String lid;

    List<PoFileData> fileList = null;

    public PoApplicationData(SelectFileRespPars appFileInfo, CalypsoPo appData,
            long poTransactionCounter) {

        csn = Arrays.copyOf(appData.getApplicationSerialNumber(),
                appData.getApplicationSerialNumber().length);

        csnDec = IntegrationUtils.bytesToLong(appData.getApplicationSerialNumber(), 8);

        calypsoRevision = new String(appData.getRevision().toString());

        sessionModif = String.format("%04X", appData.getBufferSizeValue());

        sessionModifDec = appData.getBufferSizeValue();

        transactionCounter = String.format("%06X", poTransactionCounter);

        transactionCounterDec = poTransactionCounter;

        platform = String.format("%02X", appData.getPlatformByte());

        issuer = new Issuer(appData.getSoftwareIssuerByte());

        version = String.format("%02X", appData.getSoftwareVersionByte());

        revision = String.format("%02X", appData.getSoftwareRevisionByte());

        fci = Arrays.copyOf(appData.getSelectionStatus().getFci().getDataOut(),
                appData.getSelectionStatus().getFci().getDataOut().length);

        aid = Arrays.copyOf(appData.getDfName(), appData.getDfName().length);

        dfInfo = Arrays.copyOf(appFileInfo.getFileBinaryData(),
                appFileInfo.getFileBinaryData().length);

        lid = String.format("%04X", appFileInfo.getLid());

        kif1 = String.format("%02X", appFileInfo.getKifInfo()[0]);

        kif2 = String.format("%02X", appFileInfo.getKifInfo()[1]);

        kif3 = String.format("%02X", appFileInfo.getKifInfo()[2]);

        kvc1 = String.format("%02X", appFileInfo.getKvcInfo()[0]);

        kvc2 = String.format("%02X", appFileInfo.getKvcInfo()[1]);

        kvc3 = String.format("%02X", appFileInfo.getKvcInfo()[2]);

        status = String.format("%02X", appFileInfo.getDfStatus());

        accessConditions = new AccessConditions(appFileInfo.getAccessConditions(),
                appFileInfo.getKeyIndexes());

        applicationType = String.format("%02X", appData.getApplicationTypeByte());

        applicationSubtype = String.format("%02X", appData.getApplicationSubtypeByte());

        fileList = new ArrayList<PoFileData>();
    }


    public void print(Logger logger) {


        logger.info(
                "========================================================================================================");
        logger.info(
                "| AID                             | LID  | KVC1 | KVC2 | KVC3 | KIF1 | KIF2 | KIF3 | G0 | G1 | G2 | G3 |");
        logger.info("{}", String.format(
                "|%32s | %4s |  %2s  |  %2s  |  %2s  |  %2s  |  %2s  |  %2s  | %s | %s | %s | %s |",
                ByteArrayUtil.toHex(this.getAid()), this.getLid(), this.getKvc1(), this.getKvc2(),
                this.getKvc3(), this.getKif1(), this.getKif2(), this.getKif3(),
                IntegrationUtils.getAcName(
                        this.getAccessConditions().getGroup0().getAccessCondition(),
                        this.getAccessConditions().getGroup0().getKeyLevel(), false),
                IntegrationUtils.getAcName(
                        this.getAccessConditions().getGroup1().getAccessCondition(),
                        this.getAccessConditions().getGroup1().getKeyLevel(), false),
                IntegrationUtils.getAcName(
                        this.getAccessConditions().getGroup2().getAccessCondition(),
                        this.getAccessConditions().getGroup2().getKeyLevel(), false),
                IntegrationUtils.getAcName(
                        this.getAccessConditions().getGroup3().getAccessCondition(),
                        this.getAccessConditions().getGroup3().getKeyLevel(), false)));
        logger.info(
                "========================================================================================================");

        logger.info("{}", String.format("= FCI:: %s", ByteArrayUtil.toHex(this.getFci())));
        logger.info("{}", String.format("= Serial Number:: %s (%d)",
                ByteArrayUtil.toHex(this.getCsn()), this.getCsnDec()));
        logger.info("{}",
                String.format("= Transaction Counter:: %d", this.getTransactionCounterDec()));
        logger.info("{}", String.format("= Revision:: %s", this.getCalypsoRevision()));
        logger.info("{}",
                String.format("= Session Buffer Size:: %d bytes", this.getSessionModifDec()));
        logger.info("{}", String.format("= Platform (Chip Type):: %s", this.getPlatform()));
        logger.info("{}", String.format("= Issuer:: %s(%s)", this.getIssuerInfo().getName(),
                this.getIssuerInfo().getValue()));
        logger.info("{}",
                String.format("= Software Version:: %s.%s", this.getVersion(), this.getRevision()));
        logger.info("{}", String.format("= Application Type:: %s", this.getApplicationType()));
        logger.info("{}",
                String.format("= Application Subtype:: %s", this.getApplicationSubtype()));
        logger.info("{}", String.format("= DF Status:: %2s", this.getStatus()));
        logger.info(
                "========================================================================================================");

        logger.info("| LID  | Type | SID | #R | Size | G0 | G1 | G2 | G3 | DRef |");
        logger.info("----------------------------------------------------------");

        List<PoFileData> fileList = this.getFileList();
        Iterator fileIter = fileList.iterator();

        while (fileIter.hasNext()) {

            PoFileData fileData = (PoFileData) fileIter.next();

            fileData.print(logger);
        }
    }

    public byte[] getFci() {
        return fci;
    }

    public byte[] getCsn() {
        return csn;
    }

    public long getCsnDec() {
        return csnDec;
    }

    public String getCalypsoRevision() {
        return calypsoRevision;
    }

    public String getSessionModif() {
        return sessionModif;
    }

    public int getSessionModifDec() {
        return sessionModifDec;
    }

    public String getTransactionCounter() {
        return transactionCounter;
    }

    public long getTransactionCounterDec() {
        return transactionCounterDec;
    }

    public String getPlatform() {
        return platform;
    }

    public Issuer getIssuerInfo() {
        return issuer;
    }

    public Issuer getIssuer() {
        return issuer;
    }

    public String getVersion() {
        return version;
    }

    public String getRevision() {
        return revision;
    }

    public byte[] getAid() {
        return aid;
    }

    public byte[] getDfInfo() {
        return dfInfo;
    }

    public String getLid() {
        return lid;
    }

    public String getKif1() {
        return kif1;
    }

    public String getKif2() {
        return kif2;
    }

    public String getKif3() {
        return kif3;
    }

    public String getKvc1() {
        return kvc1;
    }

    public String getKvc2() {
        return kvc2;
    }

    public String getKvc3() {
        return kvc3;
    }

    public AccessConditions getAccessConditions() {
        return accessConditions;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public String getApplicationSubtype() {
        return applicationSubtype;
    }

    public String getStatus() {
        return status;
    }

    public List<PoFileData> getFileList() {
        return fileList;
    }
}
