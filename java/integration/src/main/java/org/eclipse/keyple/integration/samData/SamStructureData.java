/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.integration.samData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.keyple.calypso.transaction.CalypsoSam;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.integration.IntegrationUtils;
import org.slf4j.Logger;

public class SamStructureData {

    private String serialNumber;

    private long serialNumberDec;

    private String samRevision;

    private String platform;

    private String applicationType;

    private String applicationSubType;

    private String softwareIssuer;

    private String softwareVersion;

    private String softwareRevision;

    private List<SamKeyData> systemKeyList = null;

    private List<SamKeyData> workKeyList = null;

    private List<SamCounterData> counterDataList = null;

    public SamStructureData(CalypsoSam samData) {

        serialNumber = ByteArrayUtil.toHex(samData.getSerialNumber());

        serialNumberDec = IntegrationUtils.bytesToLong(samData.getSerialNumber(), 4) & 0xFFFFFFFF;

        samRevision = samData.getSamRevision().getName();

        platform = String.format("%02X", samData.getPlatform());

        applicationType = String.format("%02X", samData.getApplicationType());

        applicationSubType = String.format("%02X", samData.getApplicationSubType());

        softwareIssuer = String.format("%02X", samData.getSoftwareIssuer());

        softwareVersion = String.format("%02X", samData.getSoftwareVersion());

        softwareRevision = String.format("%02X", samData.getSoftwareRevision());

        systemKeyList = new ArrayList<SamKeyData>();

        workKeyList = new ArrayList<SamKeyData>();

        counterDataList = new ArrayList<SamCounterData>();
    }

    public void print(Logger logger) {

        logger.info(
                "========================================================================================================");
        logger.info("{}", String.format("= Serial Number:: %d ($%s)", this.getSerialNumberDec(),
                this.getSerialNumber()));
        logger.info("{}", String.format("= Sam Revision:: $%s", this.getSamRevision()));
        logger.info("{}", String.format("= Platform:: $%s", this.getPlatform()));
        logger.info("{}", String.format("= Application Type:: $%s", this.getApplicationType()));
        logger.info("{}",
                String.format("= Application SubType:: $%s", this.getApplicationSubType()));
        logger.info("{}", String.format("= Software Issuer:: $%s", this.getSoftwareIssuer()));
        logger.info("{}", String.format("= Software Version:: $%s", this.getSoftwareVersion()));
        logger.info("{}", String.format("= Software Revision:: $%s", this.getSoftwareRevision()));

        logger.info(
                "--------------------------------------------------------------------------------------------------------");
        logger.info("= SYSTEM KEYS");
        logger.info(
                "--------------------------------------------------------------------------------------------------------");

        logger.info(
                "|   Index   | KIF | KVC | ALG | PAR1 | PAR2 | PAR3 | PAR4 | PAR5 | PAR6 | PAR7 | PAR8 | PAR9 | PAR10 |");

        List<SamKeyData> systemKeyList = this.getSystemKeyList();
        Iterator sysIter = systemKeyList.iterator();

        while (sysIter.hasNext()) {
            SamKeyData sysKeyData = (SamKeyData) sysIter.next();

            sysKeyData.print(logger);
        }

        logger.info(
                "--------------------------------------------------------------------------------------------------------");
        logger.info("= WORK KEYS");
        logger.info(
                "--------------------------------------------------------------------------------------------------------");

        logger.info(
                "|   Index   | KIF | KVC | ALG | PAR1 | PAR2 | PAR3 | PAR4 | PAR5 | PAR6 | PAR7 | PAR8 | PAR9 | PAR10 |");

        List<SamKeyData> workKeyList = this.getWorkKeyList();
        Iterator wrkIter = workKeyList.iterator();

        while (wrkIter.hasNext()) {
            SamKeyData wrkKeyData = (SamKeyData) wrkIter.next();

            wrkKeyData.print(logger);
        }
        logger.info(
                "--------------------------------------------------------------------------------------------------------");
        logger.info("= COUNTERS AND CEILINGS");
        logger.info(
                "--------------------------------------------------------------------------------------------------------");
        logger.info("|   Index   |       Counter      |       Ceiling      |");

        List<SamCounterData> counterList = this.getCounterDataList();
        Iterator ctrIter = counterList.iterator();

        while (ctrIter.hasNext()) {
            SamCounterData ctrData = (SamCounterData) ctrIter.next();

            ctrData.print(logger);
        }

        logger.info(
                "========================================================================================================");
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public long getSerialNumberDec() {
        return serialNumberDec;
    }

    public String getSamRevision() {
        return samRevision;
    }

    public String getPlatform() {
        return platform;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public String getApplicationSubType() {
        return applicationSubType;
    }

    public String getSoftwareIssuer() {
        return softwareIssuer;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public String getSoftwareRevision() {
        return softwareRevision;
    }

    public List<SamKeyData> getSystemKeyList() {
        return systemKeyList;
    }

    public List<SamKeyData> getWorkKeyList() {
        return workKeyList;
    }

    public List<SamCounterData> getCounterDataList() {
        return counterDataList;
    }
}
