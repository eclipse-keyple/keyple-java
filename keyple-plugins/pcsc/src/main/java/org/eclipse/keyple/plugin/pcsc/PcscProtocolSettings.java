/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.pcsc;

/**
 * These objects are used by the application to build the SeProtocolsMap
 */
public interface PcscProtocolSettings {
    public static String REGEX_PROTOCOL_B_PRIME = "3B8F8001805A0A0103200311........829000..";

    public static String REGEX_PROTOCOL_ISO14443_4 =
            "3B8880010000000000718100F9|3B8C800150........00000000007181..";

    public static String REGEX_PROTOCOL_MIFARE_UL = "3B8F8001804F0CA0000003060300030000000068";

    public static String REGEX_PROTOCOL_MIFARE_CLASSIC = "3B8F8001804F0CA000000306030001000000006A";

    public static String REGEX_PROTOCOL_DESFIRE = "3B8180018080";

    public static String REGEX_PROTOCOL_MEMORY_ST25 = "3B8F8001804F0CA000000306070007D0020C00B6";
}
