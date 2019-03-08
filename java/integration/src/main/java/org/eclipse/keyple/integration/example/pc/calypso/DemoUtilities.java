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
package org.eclipse.keyple.integration.example.pc.calypso;

import java.util.regex.Pattern;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.SeReader;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;

public class DemoUtilities {

    public final static String PO_READER_NAME_REGEX = ".*(ASK|ACS).*";
    public final static String SAM_READER_NAME_REGEX =
            ".*(Cherry TC|SCM Microsystems|Identive|HID|Generic).*";

    /**
     * Get the terminals with names that match the expected pattern
     *
     * @param seProxyService SE Proxy service
     * @param pattern Pattern
     * @return SeReader
     * @throws KeypleReaderException Any error with the card communication
     */
    public static SeReader getReader(SeProxyService seProxyService, String pattern)
            throws KeypleReaderException {
        Pattern p = Pattern.compile(pattern);
        for (ReaderPlugin plugin : seProxyService.getPlugins()) {
            for (SeReader reader : plugin.getReaders()) {
                if (p.matcher(reader.getName()).matches()) {
                    return reader;
                }
            }
        }
        return null;
    }


}
