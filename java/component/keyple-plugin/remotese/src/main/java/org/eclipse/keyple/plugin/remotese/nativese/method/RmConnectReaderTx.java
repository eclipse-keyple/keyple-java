/* **************************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.plugin.remotese.nativese.method;

import com.google.gson.JsonObject;
import java.util.Map;
import org.eclipse.keyple.core.reader.Reader;
import org.eclipse.keyple.core.reader.event.ObservableReader;
import org.eclipse.keyple.core.reader.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.reader.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.plugin.remotese.exception.KeypleRemoteException;
import org.eclipse.keyple.plugin.remotese.nativese.INativeReaderService;
import org.eclipse.keyple.plugin.remotese.rm.AbstractRemoteMethodTx;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodName;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Handle the Connect Reader keypleDTO serialization and deserialization */
public class RmConnectReaderTx extends AbstractRemoteMethodTx<String> {

  private final Reader localReader;
  private final INativeReaderService slaveAPI;
  private final Map<String, String> options;

  @Override
  public RemoteMethodName getMethodName() {
    return RemoteMethodName.READER_CONNECT;
  }

  public RmConnectReaderTx(
      String sessionId,
      String nativeReaderName,
      String virtualReaderName,
      String masterNodeId,
      Reader localReader,
      String slaveNodeId,
      INativeReaderService slaveAPI,
      Map<String, String> options) {
    super(sessionId, nativeReaderName, virtualReaderName, masterNodeId, slaveNodeId);
    this.localReader = localReader;
    this.slaveAPI = slaveAPI;
    this.options = options;
  }

  private static final Logger logger = LoggerFactory.getLogger(RmConnectReaderTx.class);

  @Override
  public String parseResponse(KeypleDto keypleDto) {
    String nativeReaderName = keypleDto.getNativeReaderName();

    // if reader connection thrown an exception
    if (KeypleDtoHelper.containsException(keypleDto)) {
      logger.trace("KeypleDto contains an exception: {}", keypleDto);
      KeypleReaderIOException ex =
          JsonParser.getGson().fromJson(keypleDto.getError(), KeypleReaderIOException.class);
      throw new KeypleRemoteException(
          "An exception occurs while calling the remote method connectReader", ex);
    } else {
      // if dto does not contain an exception
      try {
        /*
         * configure slaveAPI to propagate reader events if the reader is observable
         */

        // find the local reader by name
        Reader localReader = slaveAPI.findLocalReader(nativeReaderName);

        if (localReader instanceof ObservableReader) {
          logger.trace(
              "Register SlaveAPI as an observer for native reader {}", localReader.getName());
          ((ObservableReader) localReader).addObserver((ObservableReader.ReaderObserver) slaveAPI);
        } else {
          logger.trace(
              "Connected reader is not observable, do not register observer capabilities to virtual reader");
        }

        // retrieve sessionId from keypleDto
        JsonObject body = JsonParser.getGson().fromJson(keypleDto.getBody(), JsonObject.class);

        // sessionId is returned here
        return body.get("sessionId").getAsString();

      } catch (KeypleReaderNotFoundException e) {
        logger.warn("While receiving a confirmation of Rse connection, local reader was not found");
        throw new KeypleRemoteException(
            "While receiving a confirmation of Rse connection, local reader was not found");
      }
    }
  }

  @Override
  public KeypleDto dto() {

    // create response
    JsonObject body = new JsonObject();
    body.addProperty("isContactless", localReader.isContactless());
    body.addProperty("isObservable", localReader instanceof ObservableReader);
    body.addProperty("options", JsonParser.getGson().toJson(options));

    return KeypleDtoHelper.buildRequest(
        getMethodName().getName(),
        body.toString(),
        null, // no
        // session
        // yet
        localReader.getName(),
        null, // no virtualreader yet
        requesterNodeId,
        targetNodeId,
        id);
  }
}
