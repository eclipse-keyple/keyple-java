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

import org.slf4j.Logger;

public class SamKeyData {

    private String index;

    private int indexDec;

    private String kif;

    private String kvc;

    private String alg;

    private String par1;

    private String par2;

    private String par3;

    private String par4;

    private String par5;

    private String par6;

    private String par7;

    private String par8;

    private String par9;

    private String par10;

    public SamKeyData(int keyIndex, byte[] keyParamData) {

        int paramIndex = 8;

        index = String.format("%02X", keyIndex);

        indexDec = keyIndex;

        kif = String.format("%02X", keyParamData[paramIndex++]);

        kvc = String.format("%02X", keyParamData[paramIndex++]);

        alg = String.format("%02X", keyParamData[paramIndex++]);

        par1 = String.format("%02X", keyParamData[paramIndex++]);

        par2 = String.format("%02X", keyParamData[paramIndex++]);

        par3 = String.format("%02X", keyParamData[paramIndex++]);

        par4 = String.format("%02X", keyParamData[paramIndex++]);

        par5 = String.format("%02X", keyParamData[paramIndex++]);

        par6 = String.format("%02X", keyParamData[paramIndex++]);

        par7 = String.format("%02X", keyParamData[paramIndex++]);

        par8 = String.format("%02X", keyParamData[paramIndex++]);

        par9 = String.format("%02X", keyParamData[paramIndex++]);

        par10 = String.format("%02X", keyParamData[paramIndex++]);
    }

    public void print(Logger logger) {

        logger.info("{}", String.format(
                "| %03d ($%2s) | $%s | $%s | $%s | $%s  | $%s  | $%s  | $%s  | $%s  | $%s  | $%s  | $%s  | $%s  |  $%s  |",
                this.getIndexDec(), this.getIndex(), this.getKif(), this.getKvc(), this.getAlg(),
                this.getPar1(), this.getPar2(), this.getPar3(), this.getPar4(), this.getPar5(),
                this.getPar6(), this.getPar7(), this.getPar8(), this.getPar9(), this.getPar10()));
    }

    public String getIndex() {
        return index;
    }

    public int getIndexDec() {
        return indexDec;
    }

    public String getKif() {
        return kif;
    }

    public String getKvc() {
        return kvc;
    }

    public String getAlg() {
        return alg;
    }

    public String getPar1() {
        return par1;
    }

    public String getPar2() {
        return par2;
    }

    public String getPar3() {
        return par3;
    }

    public String getPar4() {
        return par4;
    }

    public String getPar5() {
        return par5;
    }

    public String getPar6() {
        return par6;
    }

    public String getPar7() {
        return par7;
    }

    public String getPar8() {
        return par8;
    }

    public String getPar9() {
        return par9;
    }

    public String getPar10() {
        return par10;
    }
}
