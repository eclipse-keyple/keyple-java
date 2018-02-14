/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.dto;

import org.keyple.calypso.commands.CalypsoCommands;

/**
 * The Class CalypsoRequest.
 */
// TODO: Drop this. It's a temporary structure that serves no purpose.
public class CalypsoRequest {

    /** The CLA. (class byte) */
    private byte cla;

    /** The INS. (instruction byte) */
    private CalypsoCommands ins;

    /** The P1. (parameter 1) */
    private byte p1;

    /** The P2. (parameter 2) */
    private byte p2;

    /** The Lc. ((length of command data) */
    private byte lc;

    /** The data in. (data in the command) */
    private byte[] dataIn = new byte[0];

    /** The Le.(length of response data) */
    private byte le;

    private boolean forceLc = false;

    private boolean forceLe = true;

    /** The case 4. */
    private boolean case4;

    /**
     * Instantiates a new CalypsoRequest.
     *
     * @param cla the CLA
     * @param ins the INS
     * @param p1 the P1
     * @param p2 the P2
     * @param dataIn the data in
     */
    public CalypsoRequest(byte cla, CalypsoCommands ins, byte p1, byte p2, byte[] dataIn) {
        this.cla = cla;
        this.ins = ins;
        this.p1 = p1;
        this.p2 = p2;
        this.dataIn = (dataIn == null ? new byte[0] : dataIn.clone());
        if (dataIn == null) {

            this.lc = 0x00;
        } else {
            this.lc = (byte) dataIn.length;
        }
        this.forceLe = false;
    }

    /**
     * Instantiates a new CalypsoRequest.
     *
     * @param ins the INS
     * @param cla the CLA
     * @param p1 the P1
     * @param p2 the P2
     * @param dataIn the data in
     * @param le the Le
     */
    public CalypsoRequest(byte cla, CalypsoCommands ins, byte p1, byte p2, byte[] dataIn, byte le) {
        this.cla = cla;
        this.ins = ins;
        this.p1 = p1;
        this.p2 = p2;
        this.dataIn = (dataIn == null ? new byte[0] : dataIn.clone());

        if (dataIn == null) {
            this.lc = 0x00;
        } else {
            this.lc = (byte) dataIn.length;
        }

        this.le = le;
        this.forceLe = true;
    }

    /**
     * Set the lc
     *
     * @param lc length
     */
    public void setLc(byte lc) {
        this.lc = lc;
    }

    /**
     * Gets the cla.
     *
     * @return the cla
     */
    public byte getCla() {
        return cla;
    }

    /**
     * Gets the ins.
     *
     * @return the ins
     */
    public CalypsoCommands getIns() {
        return ins;
    }

    /**
     * Gets the p1.
     *
     * @return the p1
     */
    public byte getP1() {
        return p1;
    }

    /**
     * Gets the p2.
     *
     * @return the p2
     */
    public byte getP2() {
        return p2;
    }

    /**
     * Gets the lc.
     *
     * @return the lc
     */
    public byte getLc() {
        return lc;
    }

    /**
     * Gets the data in.
     *
     * @return the data in
     */
    public byte[] getDataIn() {
        return dataIn.clone();
    }

    /**
     * Gets the le.
     *
     * @return the le
     */
    public byte getLe() {
        return le;
    }

    /**
     * Checks if is case 4.
     *
     * @return the boolean
     */
    public boolean isCase4() {
        return case4;
    }

    public boolean isForceLe() {
        return forceLe;
    }

    public void setForceLe(boolean forceLe) {
        this.forceLe = forceLe;
    }

    public boolean isForceLc() {
        return forceLc;
    }

    public void setForceLc(boolean forceLc) {
        this.forceLc = forceLc;
    }

}
