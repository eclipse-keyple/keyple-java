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
 * 
 * @deprecated This class should disappear
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
    private byte[] dataIn;

    /** The Le.(length of response data) */
    private byte le;

    private boolean forceLe;


    public CalypsoRequest(byte cla, CalypsoCommands ins, byte p1, byte p2, byte[] dataIn) {
        this.cla = cla;
        this.ins = ins;
        this.p1 = p1;
        this.p2 = p2;
        this.dataIn = (dataIn == null ?
                new byte[0] : dataIn.clone());
        if (dataIn == null) {
            this.lc = 0x00;
        } else {
            this.lc =
                    (byte) dataIn.length;
        }
        this.forceLe = false;
    }


    public CalypsoRequest(byte cla, CalypsoCommands ins, byte p1, byte p2, byte[] dataIn, byte
            le) {
        this.cla = cla;
        this.ins = ins;
        this.p1 = p1;
        this.p2 = p2;
        this.dataIn = (dataIn ==
                null ? new byte[0] : dataIn.clone());

        if (dataIn == null) {
            this.lc = 0x00;
        } else {
            this.lc = (byte) dataIn.length;
        }

        this.le = le;
        this.forceLe = true;
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

    public boolean isForceLe() {
        return forceLe;
    }

    public boolean isForceLc() {
        return false;
    }
}
