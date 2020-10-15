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
import org.eclipse.keyple.core.seproxy.MultiSelectionProcessing;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.message.CardRequest;
import org.eclipse.keyple.core.seproxy.message.CardResponse;
import org.eclipse.keyple.core.seproxy.message.ChannelControl;
import org.eclipse.keyple.core.seproxy.plugin.reader.AbstractReader;
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
  protected final boolean isContactless;

  private static final Logger logger = LoggerFactory.getLogger(VirtualReaderImpl.class);

  private Map<String, String> parameters = new HashMap<String, String>();

  /**
   * Create a new Virtual Reader (only called by @{@link RemoteSePluginImpl})
   *
   * @param session : session associated to the reader
   * @param nativeReaderName : native reader name on slave terminal
   * @param rmTxEngine : processor for remote method
   * @param isContactless : transmission mode of the native reader on slave terminal
   */
  VirtualReaderImpl(
      VirtualReaderSession session,
      String nativeReaderName,
      RemoteMethodTxEngine rmTxEngine,
      String slaveNodeId,
      boolean isContactless,
      Map<String, String> options) {
    super(
        RemoteSePluginImpl.DEFAULT_PLUGIN_NAME,
        RemoteSePluginImpl.generateReaderName(nativeReaderName, slaveNodeId));
    this.session = session;
    this.nativeReaderName = nativeReaderName;
    this.rmTxEngine = rmTxEngine;
    this.slaveNodeId = slaveNodeId;
    this.isContactless = isContactless;
    this.parameters = options;
  }

  /** @return the current transmission mode */
  public boolean isContactless() {
    return isContactless;
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
   * Blocking TransmitCardRequests
   *
   * @param cardRequests : List of CardRequest to be transmitted to the card
   * @param multiSelectionProcessing the multi card processing mode
   * @param channelControl indicates if the channel has to be closed at the end of the processing
   * @return List of CardResponse from the card
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed
   */
  @Override
  protected List<CardResponse> processCardRequests(
      List<CardRequest> cardRequests,
      MultiSelectionProcessing multiSelectionProcessing,
      ChannelControl channelControl) {

    RmTransmitSetTx transmit =
        new RmTransmitSetTx(
            cardRequests,
            multiSelectionProcessing,
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
          "{} - processCardRequests encounters an exception while communicating with slave. "
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
   * @param cardRequest : CardRequest to be transmitted to the card
   * @param channelControl indicates if the channel has to be closed at the end of the processing
   * @return cardResponse : CardResponse from the card
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed
   */
  @Override
  protected CardResponse processCardRequest(
      CardRequest cardRequest, ChannelControl channelControl) {

    RmTransmitTx transmit =
        new RmTransmitTx(
            cardRequest,
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
          "{} - processCardRequest encounters an exception while communicating with slave. sessionId:{} error:{}",
          this.getName(),
          this.getSession().getSessionId(),
          e.getMessage());
      throw toKeypleReaderException(e);
    }
  }

  @Override
  public void activateProtocol(String readerProtocolName, String applicationProtocolName) {
    logger.warn("{} activateProtocol is not implemented yet in VirtualReader", this.getName());
  }

  @Override
  public void deactivateProtocol(String readerProtocolName) {
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
