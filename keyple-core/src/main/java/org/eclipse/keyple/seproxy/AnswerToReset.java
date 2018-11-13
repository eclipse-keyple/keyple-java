/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */
package org.eclipse.keyple.seproxy;

import java.util.Arrays;

/**
 * AnswerToReset bytes wrapper.
 * <p>
 * (May be enhanced to provide analysis methods)
 */
public class AnswerToReset {
    private byte[] atrBytes;

    public AnswerToReset(byte[] atrBytes) {
        this.atrBytes = atrBytes;
    }

    public byte[] getBytes() {
        return atrBytes;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof AnswerToReset)) {
            return false;
        }

        AnswerToReset atr = (AnswerToReset) o;
        return Arrays.equals(atr.getBytes(), this.atrBytes);
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 19 * hash + (atrBytes == null ? 0 : Arrays.hashCode(atrBytes));
        return hash;
    }
}
