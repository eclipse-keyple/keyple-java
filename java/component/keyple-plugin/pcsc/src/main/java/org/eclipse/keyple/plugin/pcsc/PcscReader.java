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
package org.eclipse.keyple.plugin.pcsc;

import org.eclipse.keyple.core.seproxy.event.ObservableReader;

/**
 * The PcscReader interface provides the public elements used to manage a PC/SC reader.
 */
public interface PcscReader extends ObservableReader {
    String SETTING_KEY_TRANSMISSION_MODE = "transmission_mode";
    String SETTING_TRANSMISSION_MODE_CONTACTS = "contacts";
    String SETTING_TRANSMISSION_MODE_CONTACTLESS = "contactless";
    String SETTING_KEY_PROTOCOL = "protocol";
    String SETTING_PROTOCOL_T0 = "T0";
    String SETTING_PROTOCOL_T1 = "T1";
    String SETTING_PROTOCOL_T_CL = "TCL";
    String SETTING_PROTOCOL_TX = "Tx";
    String SETTING_KEY_MODE = "mode";
    String SETTING_MODE_EXCLUSIVE = "exclusive";
    String SETTING_MODE_SHARED = "shared";
    String SETTING_KEY_DISCONNECT = "disconnect";
    String SETTING_DISCONNECT_RESET = "reset";
    String SETTING_DISCONNECT_UNPOWER = "unpower";
    String SETTING_DISCONNECT_LEAVE = "leave";
    String SETTING_DISCONNECT_EJECT = "eject";
    String SETTING_KEY_LOGGING = "logging";
}
