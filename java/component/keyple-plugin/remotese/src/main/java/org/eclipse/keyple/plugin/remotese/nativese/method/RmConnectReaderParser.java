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
package org.eclipse.keyple.plugin.remotese.nativese.method;

import org.eclipse.keyple.plugin.remotese.exception.KeypleRemoteReaderException;
import org.eclipse.keyple.plugin.remotese.nativese.NativeReaderServiceImpl;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodParser;
import org.eclipse.keyple.plugin.remotese.transport.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.seproxy.message.ProxyReader;
import org.eclipse.keyple.seproxy.plugin.AbstractSelectionLocalReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class RmConnectReaderParser implements RemoteMethodParser<String> {

    private static final Logger logger = LoggerFactory.getLogger(RmConnectReaderParser.class);

    private final NativeReaderServiceImpl nativeReaderService;

    public RmConnectReaderParser(NativeReaderServiceImpl nativeReaderService) {
        this.nativeReaderService = nativeReaderService;
    }


    @Override
    public String parseResponse(KeypleDto keypleDto) throws KeypleRemoteReaderException {

        String nativeReaderName = keypleDto.getNativeReaderName();

        // reader connection was a success
        if (KeypleDtoHelper.containsException(keypleDto)) {
            Throwable ex = JsonParser.getGson().fromJson(keypleDto.getBody(), Throwable.class);
            throw new KeypleRemoteReaderException(
                    "An exception occurs while calling the remote method Connect Reader", ex);
        } else {
            try {
                // observe reader to propagate reader events
                ProxyReader localReader = nativeReaderService.findLocalReader(nativeReaderName);
                if (localReader instanceof AbstractSelectionLocalReader) {
                    logger.debug("Add NativeReaderServiceImpl as an observer for native reader {}",
                            localReader.getName());
                    ((AbstractSelectionLocalReader) localReader).addObserver(nativeReaderService);
                }


                // todo sessionId should be stored here

            } catch (KeypleReaderNotFoundException e) {
                logger.warn(
                        "While receiving a confirmation of Rse connection, local reader was not found");
            }
            return keypleDto.getSessionId();
        }
    }
}
