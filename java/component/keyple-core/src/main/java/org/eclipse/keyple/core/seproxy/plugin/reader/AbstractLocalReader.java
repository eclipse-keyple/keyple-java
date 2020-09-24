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
package org.eclipse.keyple.core.seproxy.plugin.reader;

import java.util.*;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.message.*;
import org.eclipse.keyple.core.seproxy.message.ChannelControl;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A local reader. <code>AbstractLocalReader</code> implements the algorithms of the methods defined
 * by the {@link org.eclipse.keyple.core.seproxy.SeReader} and {@link ProxyReader} interfaces for a
 * local reader.<br>
 * It also defines a set of abstract methods to be implemented by the reader plugins.
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.CyclomaticComplexity"})
public abstract class AbstractLocalReader extends AbstractReader {

  /** logger */
  private static final Logger logger = LoggerFactory.getLogger(AbstractLocalReader.class);

  /** predefined "get response" byte array */
  private static final byte[] getResponseHackRequestBytes = ByteArrayUtil.fromHex("00C0000000");

  /** logical channel status flag */
  private boolean logicalChannelIsOpen = false;

  /** Timestamp recorder */
  private long before;

  /** ==== Constructor =================================================== */

  /**
   * Reader constructor
   *
   * <p>Force the definition of a name through the use of super method.
   *
   * <p>Initialize the time measurement
   *
   * @param pluginName the name of the plugin that instantiated the reader
   * @param readerName the name of the reader
   */
  public AbstractLocalReader(String pluginName, String readerName) {
    super(pluginName, readerName);
    this.before = System.nanoTime(); /*
                                          * provides an initial value for measuring the
                                          * inter-exchange time. The first measurement gives the
                                          * time elapsed since the plugin was loaded.
                                          */
  }

  /** ==== Card presence management ====================================== */

  /**
   * Check the presence of a SE
   *
   * <p>This method is recommended for non-observable readers.
   *
   * <p>When the card is not present the logical and physical channels status may be refreshed
   * through a call to the processSeRemoved method.
   *
   * @return true if the SE is present
   * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
   */
  @Override
  public boolean isSePresent() {
    return checkSePresence();
  }

  /**
   * Wrapper for the native method of the plugin specific local reader to verify the presence of the
   * SE.
   *
   * <p>This method must be implemented by the ProxyReader plugin (e.g. Pcsc reader plugin).
   *
   * <p>This method is invoked by isSePresent.
   *
   * @return true if the SE is present
   * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
   */
  protected abstract boolean checkSePresence();

  /* ==== Physical and logical channels management ====================== */

  /** Close both logical and physical channels */
  protected void closeLogicalAndPhysicalChannels() {
    closeLogicalChannel();
    try {
      closePhysicalChannel();
    } catch (KeypleReaderIOException e) {
      if (logger.isDebugEnabled()) {
        logger.debug(
            "[{}] Exception occurred in closeLogicalAndPhysicalChannels. Message: {}",
            this.getName(),
            e.getMessage());
      }
    }
  }

  /**
   * This abstract method must be implemented by the derived class in order to provide the SE ATR
   * when available.
   *
   * <p>Gets the SE Answer to reset
   *
   * @return ATR returned by the SE or reconstructed by the reader (contactless)
   */
  protected abstract byte[] getATR();

  /* ==== Physical and logical channels management ====================== */
  /* Selection management */

  /**
   * This method is dedicated to the case where no FCI data is available in return for the select
   * command.
   *
   * <p>
   *
   * @param aidSelector used to retrieve the successful status codes from the main AidSelector
   * @return a ApduResponse containing the FCI
   * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
   */
  private ApduResponse recoverSelectionFciData(SeSelector.AidSelector aidSelector) {
    ApduResponse fciResponse;
    // Get Data APDU: CLA, INS, P1: always 0, P2: 0x6F FCI for the current DF, LC: 0
    byte[] getDataCommand = {(byte) 0x00, (byte) 0xCA, (byte) 0x00, (byte) 0x6F, (byte) 0x00};

    /*
     * The successful status codes list for this command is provided.
     */
    fciResponse =
        processApduRequest(
            new ApduRequest(
                "Internal Get Data",
                getDataCommand,
                false,
                aidSelector.getSuccessfulSelectionStatusCodes()));

    if (!fciResponse.isSuccessful() && logger.isDebugEnabled()) {
      logger.debug(
          "[{}] selectionGetData => Get data failed. SELECTOR = {}", this.getName(), aidSelector);
    }
    return fciResponse;
  }

  /**
   * Executes the selection application command and returns the requested data according to
   * AidSelector attributes.
   *
   * @param aidSelector the selection parameters
   * @return the response to the select application command
   * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
   */
  private ApduResponse processExplicitAidSelection(SeSelector.AidSelector aidSelector) {
    ApduResponse fciResponse;
    final byte[] aid = aidSelector.getAidToSelect();
    if (aid == null) {
      throw new IllegalArgumentException("AID must not be null for an AidSelector.");
    }
    if (logger.isDebugEnabled()) {
      logger.debug(
          "[{}] openLogicalChannel => Select Application with AID = {}",
          this.getName(),
          ByteArrayUtil.toHex(aid));
    }
    /*
     * build a get response command the actual length expected by the SE in the get response
     * command is handled in transmitApdu
     */
    byte[] selectApplicationCommand = new byte[6 + aid.length];
    selectApplicationCommand[0] = (byte) 0x00; // CLA
    selectApplicationCommand[1] = (byte) 0xA4; // INS
    selectApplicationCommand[2] = (byte) 0x04; // P1: select by name
    // P2: b0,b1 define the File occurrence, b2,b3 define the File control information
    // we use the bitmask defined in the respective enums
    selectApplicationCommand[3] =
        (byte)
            (aidSelector.getFileOccurrence().getIsoBitMask()
                | aidSelector.getFileControlInformation().getIsoBitMask());
    selectApplicationCommand[4] = (byte) (aid.length); // Lc
    System.arraycopy(aid, 0, selectApplicationCommand, 5, aid.length); // data
    selectApplicationCommand[5 + aid.length] = (byte) 0x00; // Le

    /*
     * we use here processApduRequest to manage case 4 hack. The successful status codes list
     * for this command is provided.
     */
    fciResponse =
        processApduRequest(
            new ApduRequest(
                "Internal Select Application",
                selectApplicationCommand,
                true,
                aidSelector.getSuccessfulSelectionStatusCodes()));

    if (!fciResponse.isSuccessful() && logger.isDebugEnabled()) {
      logger.debug(
          "[{}] openLogicalChannel => Application Selection failed. SELECTOR = {}",
          this.getName(),
          aidSelector);
    }
    return fciResponse;
  }

  /**
   * Attempts to open the physical channel.
   *
   * <p>This method must not return normally if the physical channel could not be opened.
   *
   * @throws KeypleReaderIOException if the communication with the reader or the SE has failed and
   *     the physical channel could not be open.
   * @since 0.9
   */
  protected abstract void openPhysicalChannel();

  /**
   * Attempts to close the current physical channel.
   *
   * <p>This method must not return normally if the physical channel could not be closed.
   *
   * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
   * @since 0.9
   */
  protected abstract void closePhysicalChannel();

  /**
   * Tells if the physical channel is open or not.
   *
   * @return True is the physical channel is open, false if not.
   * @since 0.9
   */
  protected abstract boolean isPhysicalChannelOpen();

  /**
   * (package-private)<br>
   * Tells if a logical channel is open or not.
   *
   * @return True if the logical channel is open, false if not.
   * @since 0.9
   */
  final boolean isLogicalChannelOpen() {
    return logicalChannelIsOpen;
  }

  /**
   * Close the logical channel.<br>
   *
   * @since 0.9
   */
  private void closeLogicalChannel() {
    if (logger.isTraceEnabled()) {
      logger.trace("[{}] closeLogicalChannel => Closing of the logical channel.", this.getName());
    }
    logicalChannelIsOpen = false;
  }

  /* ==== Protocol management =========================================== */

  /**
   * PO selection map associating seProtocols and selection strings.
   *
   * <p>The String associated with a particular protocol can be anything that is relevant to be
   * interpreted by reader plugins implementing protocolFlagMatches (e.g. ATR regex for Pcsc
   * plugins, technology name for Nfc plugins, etc).
   */
  private final Map<SeProtocol, String> protocolsMap = new HashMap<SeProtocol, String>();

  /**
   * Defines the protocol setting Map to allow SE to be differentiated according to their
   * communication protocol.
   *
   * @param seProtocol the protocol key identifier to be added to the plugin internal list
   * @param protocolRule a string use to define how to identify the protocol
   * @since 0.9
   */
  @Override
  public void addSeProtocolSetting(SeProtocol seProtocol, String protocolRule) {
    this.protocolsMap.put(seProtocol, protocolRule);
  }

  /**
   * Complete the current setting map with the provided map
   *
   * @param protocolSetting the protocol setting map
   */
  @Override
  public void setSeProtocolSetting(Map<SeProtocol, String> protocolSetting) {
    this.protocolsMap.putAll(protocolSetting);
  }

  /**
   * @return the Map containing the protocol definitions set by addSeProtocolSetting and
   *     setSeProtocolSetting
   * @since 0.9
   */
  protected final Map<SeProtocol, String> getProtocolsMap() {
    return protocolsMap;
  }

  /**
   * Test if the current protocol matches the provided protocol flag.
   *
   * <p>The method must be implemented by the ProxyReader plugin.
   *
   * <p>The protocol flag is used to retrieve from the protocolsMap the String used to differentiate
   * this particular protocol. (e.g. in PC/SC the only way to identify the SE protocol is to analyse
   * the ATR returned by the reader [ISO SE and memory card SE have specific ATR], in Android Nfc
   * the SE protocol can be deduced with the TagTechnology interface).
   *
   * @param protocolFlag the protocol flag
   * @return true if the current protocol matches the provided protocol flag
   * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
   * @since 0.9
   */
  protected abstract boolean protocolFlagMatches(SeProtocol protocolFlag);

  /**
   * Executes the ChannelControl instruction.
   *
   * <p>Does nothing if channelControl is {@link ChannelControl#KEEP_OPEN}<br>
   * Otherwise, closes the logical channel and initiates the closure of the physical channel.
   * Instantly if the reader is not observed or by the removal sequence if it is.
   *
   * @param channelControl The channel control.
   */
  private void processChannelControl(ChannelControl channelControl) {

    if (channelControl != ChannelControl.KEEP_OPEN) {
      // close logical channel unconditionally
      closeLogicalChannel();
      if (!(this instanceof ObservableReader)
          || (((ObservableReader) this).countObservers() == 0)) {
        /* Not observable/observed: close immediately the physical channel if requested */
        closePhysicalChannel();
      }
      if (this instanceof AbstractObservableLocalReader) {
        /*
         * request the removal sequence when the reader is monitored by a thread
         */
        this.terminateSeCommunication();
      }
    }
  }

  /**
   * Local implementation of {@link AbstractReader#processSeRequests(List, MultiSeRequestProcessing, ChannelControl)}
   *
   * <p>{@inheritDoc}<br>
   *
   * @since 0.9
   */
  @Override
  protected final List<SeResponse> processSeRequests(
      List<SeRequest> seRequests,
      MultiSeRequestProcessing multiSeRequestProcessing,
      ChannelControl channelControl) {

    // check seRequests, multiSeRequestProcessing and channelControl
    Assert.getInstance()
        .notNull(seRequests, "seRequests")
        .notNull(multiSeRequestProcessing, "multiSeRequestProcessing")
        .notNull(channelControl, "channelControl");

    List<SeResponse> seResponses = new ArrayList<SeResponse>();

    /* Open the physical channel if needed */
    if (!isPhysicalChannelOpen()) {
      openPhysicalChannel();
    }

    /* loop over all SeRequest provided in the list */
    for (SeRequest request : seRequests) {
      /* process the SeRequest and append the SeResponse list */
      seResponses.add(processSeRequestLogical(request));
      if (multiSeRequestProcessing == MultiSeRequestProcessing.PROCESS_ALL) {
        /* multi SeRequest case: just close the logical channel and go on with the next selection. */
        closeLogicalChannel();
      } else {
        if (logicalChannelIsOpen) {
          /* the logical channel being open, we stop here */
          break; // exit for loop
        }
      }
    }

    /* handle the communication closing according to the provided ChannelControl parameter */
    processChannelControl(channelControl);

    return seResponses;
  }

  /**
   * Local implementation of {@link AbstractReader#processSeRequests(List, MultiSeRequestProcessing,
   * ChannelControl)}
   *
   * <p>{@inheritDoc}<br>
   *
   * @since 0.9
   */
  @Override
  protected final SeResponse processSeRequest(SeRequest seRequest, ChannelControl channelControl) {

    // check channelControl, seRequest can be null
    Assert.getInstance().notNull(channelControl, "channelControl");

    /* Open the physical channel if needed */
    if (!isPhysicalChannelOpen()) {
      openPhysicalChannel();
    }

    SeResponse seResponse;
    if (seRequest != null) {
      /* process the SeRequest and keep the SeResponse */
       seResponse = processSeRequestLogical(seRequest);
      /* handle the communication closing according to the provided ChannelControl parameter */
      processChannelControl(channelControl);
    } else {
      /* seRequest is null, force the channel closing whatever the channel control */
      processChannelControl(ChannelControl.CLOSE_AFTER);
      /* returns an empty response with channel status */
      seResponse = new SeResponse(logicalChannelIsOpen, true, null, new ArrayList<ApduResponse>());
    }

    return seResponse;
  }

  /**
   * Checks the provided ATR with the AtrFilter.
   *
   * <p>Returns true if the ATR is accepted by the filter or if no filter is set.
   *
   * @param atr A byte array.
   * @param atrFilter A not null {@link org.eclipse.keyple.core.seproxy.SeSelector.AtrFilter}
   * @return True or false.
   * @throws IllegalStateException if no ATR is available and the AtrFilter is set.
   * @see #processSelection(SeSelector)
   */
  private boolean checkAtr(byte[] atr, SeSelector.AtrFilter atrFilter) {

    if (atrFilter == null) {
      // no filter
      return true;
    }
    if (atr == null) {
      // no ATR
      throw new IllegalStateException("No ATR available while an AtrFilter is set.");
    }
    if (logger.isDebugEnabled()) {
      logger.debug("[{}] openLogicalChannel => ATR = {}", this.getName(), ByteArrayUtil.toHex(atr));
    }

    // check the ATR
    if (!atrFilter.atrMatches(atr)) {
      if (logger.isInfoEnabled()) {
        logger.info(
            "[{}] openLogicalChannel => ATR didn't match. ATR = {}, regex filter = {}",
            this.getName(),
            ByteArrayUtil.toHex(atr),
            atrFilter.getAtrRegex());
      }
      // the ATR has been rejected
      return false;
    } else {
      // the ATR has been accepted
      return true;
    }
  }

  /**
   * Selects the SE with the provided AID and gets the FCI response in return.
   *
   * @param aidSelector A {@link org.eclipse.keyple.core.seproxy.SeSelector.AidSelector} must be not
   *     null.
   * @return An not null {@link ApduResponse} containing the FCI.
   * @throws KeypleReaderIOException if the communication with the reader or the SE has failed.
   * @see #processSelection(SeSelector)
   */
  private ApduResponse selectByAid(SeSelector.AidSelector aidSelector) {

    ApduResponse fciResponse;

    if (this instanceof SmartSelectionReader) {
      fciResponse = ((SmartSelectionReader) this).openChannelForAid(aidSelector);
    } else {
      fciResponse = processExplicitAidSelection(aidSelector);
    }

    if (fciResponse.isSuccessful() && fciResponse.getDataOut().length == 0) {
      /*
       * The selection didn't provide data (e.g. OMAPI), we get the FCI using a Get Data
       * command.
       *
       * The AID selector is provided to handle successful status word in the Get Data
       * command.
       */
      fciResponse = recoverSelectionFciData(aidSelector);
    }
    return fciResponse;
  }

  /**
   * Select the SE according to the {@link SeSelector}.
   *
   * <p>The selection status is returned.<br>
   * 3 levels of filtering/selection are applied successively if they are enabled: protocol, ATR and
   * AID.<br>
   * As soon as one of these operations fails, the method returns with a failed selection status.
   *
   * @param seSelector A not null {@link SeSelector}.
   * @return A not null {@link SelectionStatus}.
   * @see #processSeRequestLogical(SeRequest)
   */
  private SelectionStatus processSelection(SeSelector seSelector) {

    AnswerToReset answerToReset;
    ApduResponse fciResponse;
    boolean hasMatched = true;

    // check protocol if enabled
    if (seSelector.getSeProtocol() != null && !protocolFlagMatches(seSelector.getSeProtocol())) {
      answerToReset = null;
      fciResponse = null;
      hasMatched = false;
    } else {
      // protocol check succeeded, check ATR if enabled
      SeSelector.AtrFilter atrFilter = seSelector.getAtrFilter();
      byte[] atr = getATR();
      if (atr != null) {
        answerToReset = new AnswerToReset(atr);
      } else {
        answerToReset = null;
      }
      if (!checkAtr(atr, atrFilter)) {
        // check failed
        hasMatched = false;
        fciResponse = null;
      } else {
        // ATR check succeeded, select by AID if enabled.
        SeSelector.AidSelector aidSelector = seSelector.getAidSelector();
        if (aidSelector != null) {
          fciResponse = selectByAid(aidSelector);
          hasMatched = fciResponse.isSuccessful();
        } else {
          fciResponse = null;
        }
      }
    }
    return new SelectionStatus(answerToReset, fciResponse, hasMatched);
  }

  /**
   * Processes the {@link SeRequest} passed as an argument and returns a {@link SeResponse}.
   *
   * <p>The complete description of the process of transmitting an {@link SeRequest} is described in
   * {{@link ProxyReader#transmitSeRequest(SeRequest, ChannelControl)}}
   *
   * @param seRequest The {@link SeRequest} to be processed (may be null).
   * @return seResponse A not null {@link SeResponse}.
   * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
   * @throws IllegalArgumentException if seRequest is null
   * @see #processSeRequests(List, MultiSeRequestProcessing, ChannelControl)
   * @see #processSeRequest(SeRequest, ChannelControl)
   * @since 0.9
   */
  private SeResponse processSeRequestLogical(SeRequest seRequest) {

    SelectionStatus selectionStatus = null;
    SeSelector seSelector = seRequest.getSeSelector();
    boolean previouslyOpen = logicalChannelIsOpen;

    if (seSelector != null) {
      selectionStatus = processSelection(seSelector);
      if (!selectionStatus.hasMatched()) {
        // the selection failed, return an empty response having the selection status
        return new SeResponse(
            false, previouslyOpen, selectionStatus, new ArrayList<ApduResponse>());
      }

      logicalChannelIsOpen = true;
    }

    List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();

    /* The ApduRequests are optional, check if null */
    if (seRequest.getApduRequests() != null) {
      /* Proceeds with the APDU requests present in the SeRequest if any */
      for (ApduRequest apduRequest : seRequest.getApduRequests()) {
        try {
          apduResponses.add(processApduRequest(apduRequest));
        } catch (KeypleReaderIOException ex) {
          /*
           * The process has been interrupted. We close the logical channel and launch a
           * KeypleReaderException with the Apdu responses collected so far.
           */
          if (logger.isDebugEnabled()) {
            logger.debug(
                "The process has been interrupted, collect Apdu responses collected so far");
          }

          closeLogicalAndPhysicalChannels();
          ex.setSeResponse(new SeResponse(false, previouslyOpen, selectionStatus, apduResponses));
          throw ex;
        }
      }
    }

    return new SeResponse(logicalChannelIsOpen, previouslyOpen, selectionStatus, apduResponses);
  }

  /**
   * Transmits an ApduRequest and receives the ApduResponse
   *
   * <p>The time measurement is carried out and logged with the detailed information of the
   * exchanges (TRACE level).
   *
   * @param apduRequest APDU request
   * @return APDU response
   * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
   * @since 0.9
   */
  private ApduResponse processApduRequest(ApduRequest apduRequest) {

    ApduResponse apduResponse;
    if (logger.isDebugEnabled()) {
      long timeStamp = System.nanoTime();
      long elapsed10ms = (timeStamp - before) / 100000;
      this.before = timeStamp;
      logger.debug(
          "[{}] processApduRequest => {}, elapsed {} ms.",
          this.getName(),
          apduRequest,
          elapsed10ms / 10.0);
    }

    byte[] buffer = apduRequest.getBytes();
    apduResponse = new ApduResponse(transmitApdu(buffer), apduRequest.getSuccessfulStatusCodes());

    if (apduRequest.isCase4()
        && apduResponse.getDataOut().length == 0
        && apduResponse.isSuccessful()) {
      // do the get response command but keep the original status code
      apduResponse = case4HackGetResponse(apduResponse.getStatusCode());
    }

    if (logger.isDebugEnabled()) {
      long timeStamp = System.nanoTime();
      long elapsed10ms = (timeStamp - before) / 100000;
      this.before = timeStamp;
      logger.debug(
          "[{}] processApduRequest => {}, elapsed {} ms.",
          this.getName(),
          apduResponse,
          elapsed10ms / 10.0);
    }
    return apduResponse;
  }

  /**
   * Execute a get response command in order to get outgoing data from specific cards answering 9000
   * with no data although the command has outgoing data. Note that this method relies on the right
   * get response management by transmitApdu
   *
   * @param originalStatusCode the status code of the command that didn't returned data
   * @return ApduResponse the response to the get response command
   * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
   */
  private ApduResponse case4HackGetResponse(int originalStatusCode) {

    if (logger.isDebugEnabled()) {
      long timeStamp = System.nanoTime();
      long elapsed10ms = (timeStamp - before) / 100000;
      this.before = timeStamp;
      logger.debug(
          "[{}] case4HackGetResponse => ApduRequest: NAME = \"Internal Get Response\", RAWDATA = {}, elapsed = {}",
          this.getName(),
          ByteArrayUtil.toHex(getResponseHackRequestBytes),
          elapsed10ms / 10.0);
    }

    byte[] getResponseHackResponseBytes = transmitApdu(getResponseHackRequestBytes);

    /* we expect here a 0x9000 status code */
    ApduResponse getResponseHackResponse = new ApduResponse(getResponseHackResponseBytes, null);

    if (logger.isDebugEnabled()) {
      long timeStamp = System.nanoTime();
      long elapsed10ms = (timeStamp - before) / 100000;
      this.before = timeStamp;
      logger.debug(
          "[{}] case4HackGetResponse => Internal {}, elapsed {} ms.",
          this.getName(),
          getResponseHackResponseBytes,
          elapsed10ms / 10.0);
    }

    if (getResponseHackResponse.isSuccessful()) {
      // replace the two last status word bytes by the original status word
      getResponseHackResponseBytes[getResponseHackResponseBytes.length - 2] =
          (byte) (originalStatusCode >> 8);
      getResponseHackResponseBytes[getResponseHackResponseBytes.length - 1] =
          (byte) (originalStatusCode & 0xFF);
    }
    return getResponseHackResponse;
  }

  /**
   * Transmits a single APDU and receives its response.
   *
   * <p>This abstract method must be implemented by the ProxyReader plugin (e.g. Pcsc, Nfc). The
   * implementation must handle the case where the SE response is 61xy and execute the appropriate
   * get response command.
   *
   * @param apduIn byte buffer containing the ingoing data
   * @return apduResponse byte buffer containing the outgoing data.
   * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
   */
  protected abstract byte[] transmitApdu(byte[] apduIn);

  /**
   * Method to be implemented by child classes in order to handle the needed actions when
   * terminating the communication with a SE (closing of the physical channel, initiating a removal
   * sequence, etc.)
   */
  abstract void terminateSeCommunication();
}
