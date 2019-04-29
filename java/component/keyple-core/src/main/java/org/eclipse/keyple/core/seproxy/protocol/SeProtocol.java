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
package org.eclipse.keyple.core.seproxy.protocol;

public interface SeProtocol {
    /**
     * Type of protocol.
     * <p>
     * Standard protocols are defined as ISO standards.
     * <p>
     * Proprietary protocols are defined by the chip manufacturers.
     */
    public enum ProtocolType {
        STANDARD, PROPRIETARY
    }

    /**
     * NFC compatibility.
     * <p>
     * The NFC compatibility indicates whether the protocol is supported or not by NFC readers
     * (Android)
     */
    public enum NfcCompatibility {
        NFC, NOT_NFC
    }

    String getName();

    public ProtocolType getProtocolType();

    public TransmissionMode getTransmissionMode();

    public NfcCompatibility getNfcCompatibility();
}
