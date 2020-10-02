/* **************************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.plugin.remotese.pluginse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.message.ChannelControl;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.plugin.reader.AbstractReader;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.plugin.remotese.exception.KeypleRemoteException;
import org.eclipse.keyple.plugin.remotese.pluginse.method.RmTransmitSetTx;
import org.eclipse.keyple.plugin.remotese.pluginse.method.RmTransmitTx;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodTxEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Virtual Reader is a proxy to a Native Reader on the slave terminal Use it like a local reader,
 * all API call will be transferred to the Native Reader with a RPC session
 */
class VirtualReaderImpl extends AbstractReader implements VirtualReader {

  protected final VirtualReaderSession session;
  protected final String nativeReaderName;
  protected final RemoteMethodTxEngine rmTxEngine;
  protected final String slaveNodeId;
  protected final TransmissionMode transmissionMode;

  private static final Logger logger = LoggerFactory.getLogger(VirtualReaderImpl.class);

  private Map<String, String> parameters = new HashMap<String, String>();

  /**
   * Create a new Virtual Reader (only called by @{@link RemoteSePluginImpl})
   *
   * @param session : session associated to the reader
   * @param nativeReaderName : native reader name on slave terminal
   * @param rmTxEngine : processor for remote method
   * @param transmissionMode : transmission mode of the native reader on slave terminal
   */
  VirtualReaderImpl(
      VirtualReaderSession session,
      String nativeReaderName,
      RemoteMethodTxEngine rmTxEngine,
      String slaveNodeId,
      TransmissionMode transmissionMode,
      Map<String, String> options) {
    super(
        RemoteSePluginImpl.DEFAULT_PLUGIN_NAME,
        RemoteSePluginImpl.generateReaderName(nativeReaderName, slaveNodeId));
    this.session = session;
    this.nativeReaderName = nativeReaderName;
    this.rmTxEngine = rmTxEngine;
    this.slaveNodeId = slaveNodeId;
    this.transmissionMode = transmissionMode;
    this.parameters = options;
  }

  /** @return the current transmission mode */
  public TransmissionMode getTransmissionMode() {
    return transmissionMode;
  }

  public String getNativeReaderName() {
    return nativeReaderName;
  }

  public VirtualReaderSession getSession() {
    return session;
  }

  RemoteMethodTxEngine getRmTxEngine() {
    return rmTxEngine;
  }

  @Override
  public boolean isSePresent() {
    logger.warn(
        "{} isSePresent is not implemented in VirtualReader, returns false", this.getName());
    return false; // not implemented
  }

  /**
   * Blocking TransmitSeRequests
   *
   * @param seRequests : List of SeRequest to be transmitted to SE
   * @param multiSeRequestProcessing the multi se processing mode
   * @param channelControl indicates if the channel has to be closed at the end of the processing
   * @return List of SeResponse from SE
   * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
   */
  @Override
  protected List<SeResponse> processSeRequests(
      List<SeRequest> seRequests,
      MultiSeRequestProcessing multiSeRequestProcessing,
      ChannelControl channelControl) {

    RmTransmitSetTx transmit =
        new RmTransmitSetTx(
            seRequests,
            multiSeRequestProcessing,
            channelControl,
            session.getSessionId(),
            this.getNativeReaderName(),
            this.getName(),
            session.getMasterNodeId(),
            session.getSlaveNodeId());
    try {
      // blocking call
      return transmit.execute(rmTxEngine);
    } catch (KeypleRemoteException e) {
      logger.error(
          "{} - processSeRequests encounters an exception while communicating with slave. "
              + "sessionId:{} error:{}",
          this.getName(),
          this.getSession().getSessionId(),
          e.getMessage());
      throw toKeypleReaderException(e);
    }
  }

  /**
   * Blocking Transmit
   *
   * @param seRequest : SeRequest to be transmitted to SE
   * @param channelControl indicates if the channel has to be closed at the end of the processing
   * @return seResponse : SeResponse from SE
   * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
   */
  @Override
  protected SeResponse processSeRequest(SeRequest seRequest, ChannelControl channelControl) {

    RmTransmitTx transmit =
        new RmTransmitTx(
            seRequest,
            channelControl,
            session.getSessionId(),
            this.getNativeReaderName(),
            this.getName(),
            session.getMasterNodeId(),
            session.getSlaveNodeId());
    try {
      // blocking call
      return transmit.execute(rmTxEngine);
    } catch (KeypleRemoteException e) {
      logger.error(
          "{} - processSeRequest encounters an exception while communicating with slave. sessionId:{} error:{}",
          this.getName(),
          this.getSession().getSessionId(),
          e.getMessage());
      throw toKeypleReaderException(e);
    }
  }

  @Override
  public void activateProtocol(SeProtocol seProtocol) {
    logger.warn("{} activateProtocol is not implemented yet in VirtualReader", this.getName());
  }

  @Override
  public void deactivateProtocol(SeProtocol seProtocol) {
    logger.warn("{} deactivateProtocol is not implemented yet in VirtualReader", this.getName());
  }

  /*
   * PRIVATE HELPERS
   */

  private KeypleReaderIOException toKeypleReaderException(KeypleRemoteException e) {
    if (e.getCause() != null) {
      if (e.getCause() instanceof KeypleReaderException) {
        // KeypleReaderIOException is inside the KeypleRemoteException
        return (KeypleReaderIOException) e.getCause();
      } else {
        return new KeypleReaderIOException(e.getMessage(), e);
      }
    } else {
      // create a new KeypleReaderIOException
      return new KeypleReaderIOException(e.getMessage());
    }
  }

  @Override
  public void releaseChannel() {}
}
