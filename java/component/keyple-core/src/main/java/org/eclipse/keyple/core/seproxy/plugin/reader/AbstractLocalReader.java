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
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderProtocolNotFoundException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderProtocolNotSupportedException;
import org.eclipse.keyple.core.seproxy.message.*;
import org.eclipse.keyple.core.seproxy.message.ChannelControl;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A local reader. <code>AbstractLocalReader</code> implements the methods defined by the {@link
 * org.eclipse.keyple.core.seproxy.SeReader} and {@link ProxyReader} interfaces for a local reader.
 * <br>
 * It also defines a set of abstract methods to be implemented by the reader plugins in order to
 * take into account the specific needs of the hardware.
 */
public abstract class AbstractLocalReader extends AbstractReader {

  /** logger */
  private static final Logger logger = LoggerFactory.getLogger(AbstractLocalReader.class);

  /** predefined "get response" byte array */
  private static final byte[] getResponseHackRequestBytes = ByteArrayUtil.fromHex("00C0000000");

  /** logical channel status flag */
  private boolean logicalChannelIsOpen = false;

  /**
   * Map associating reader and application protocol names
   *
   * <p>The reader's name is the key.<br>
   * The application's name is the value.
   */
  private final Map<String, String> protocolAssociations;

  /**
   * The current protocol is determined after opening the physical channel and removed when closing.
   */
  private String currentProtocol;

  /** Timestamp recorder */
  private long before;

  private boolean useDefaultProtocol;

  /**
   * Reader constructor
   *
   * <p>Defines the plugin and reader names.
   *
   * <p>Initializes the time measurement log at {@link ApduRequest} level. The first measurement
   * gives the time elapsed since the plugin was loaded.
   *
   * @param pluginName the name of the plugin that instantiated the reader
   * @param readerName the name of the reader
   */
  public AbstractLocalReader(String pluginName, String readerName) {

    super(pluginName, readerName);
    if (logger.isDebugEnabled()) {
      this.before = System.nanoTime();
    }
    protocolAssociations = new LinkedHashMap<String, String>();
  }
  /**
   * Check the presence of a card
   *
   * <p>This method is recommended for non-observable readers.
   *
   * <p>When the card is not present the logical and physical channels status may be refreshed
   * through a call to the processSeRemoved method.
   *
   * @return true if the card is present
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed
   */
  @Override
  public boolean isSePresent() {
    return checkSePresence();
  }

  /**
   * Wrapper for the native method of the plugin specific local reader to verify the presence of the
   * card.
   *
   * <p>This method must be implemented by the ProxyReader plugin (e.g. Pcsc reader plugin).
   *
   * <p>This method is invoked by isSePresent.
   *
   * @return true if the card is present
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed
   */
  protected abstract boolean checkSePresence();

  /** Close both logical and physical channels */
  protected void closeLogicalAndPhysicalChannels() {

    closeLogicalChannel();
    try {
      resetProtocolAndClosePhysicalChannel();
    } catch (KeypleReaderIOException e) {
      if (logger.isDebugEnabled()) {
        logger.debug(
            "[{}] Exception occurred in releaseSeChannel. Message: {}",
            this.getName(),
            e.getMessage());
      }
    }
  }

  /**
   * This abstract method must be implemented by the derived class in order to provide the
   * information retrieved when powering up the secure element.
   *
   * <p>In contact mode, ATR data is the data returned by the Secure Element.
   *
   * <p>In contactless mode, as the ATR is not provided by the secured element, it can vary from one
   * plugin to another
   *
   * @return A byte array (must be not null).
   */
  protected abstract byte[] getATR();

  /**
   * This method is dedicated to the case where no FCI data is available in return for the select
   * command.
   *
   * <p>A specific APDU is sent to the card to retrieve the FCI data and returns it in an {@link
   * ApduResponse}.<br>
   * The provided AidSelector is used to check the response's status codes.
   *
   * @param aidSelector A {@link SeSelector.AidSelector} (must be not null).
   * @return A {@link ApduResponse} containing the FCI.
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed
   */
  private ApduResponse recoverSelectionFciData(SeSelector.AidSelector aidSelector) {

    ApduResponse fciResponse;
    // Get Data APDU: CLA, INS, P1: always 0, P2: 0x6F FCI for the current DF, LC: 0
    final byte[] getDataCommand = {(byte) 0x00, (byte) 0xCA, (byte) 0x00, (byte) 0x6F, (byte) 0x00};

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
   * Sends the select application command to the card and returns the requested data according to
   * AidSelector attributes (ISO7816-4 selection data) into an {@link ApduResponse}.
   *
   * @param aidSelector A not null {@link SeSelector.AidSelector}
   * @return A not null {@link ApduResponse}.
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed
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
     * build a get response command the actual length expected by the card in the get response
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
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed and
   *     the physical channel could not be open.
   * @since 0.9
   */
  protected abstract void openPhysicalChannel();

  /**
   * Opens the physical channel, determines and keep the current protocol.
   *
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed and
   *     the physical channel could not be open.
   * @since 1.0
   */
  private void openPhysicalChannelAndSetProtocol() {
    openPhysicalChannel();
    computeCurrentProtocol();
  }

  /**
   * Attempts to close the current physical channel.
   *
   * <p>This method must not return normally if the physical channel could not be closed.
   *
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed
   * @since 0.9
   */
  protected abstract void closePhysicalChannel();

  /**
   * Closes the physical channel and resets the current protocol info.
   *
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed
   * @since 1.0
   */
  private void resetProtocolAndClosePhysicalChannel() {
    currentProtocol = null;
    useDefaultProtocol = false;
    closePhysicalChannel();
  }

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
    if (this instanceof SmartSelectionReader) {
      /* SmartSelectionReaders have an explicit method for closing channels */
      ((SmartSelectionReader) this).closeLogicalChannel();
    }
    logicalChannelIsOpen = false;
  }

  /**
   * Tells if the current card communicates with the protocol provided as an argument.
   *
   * <p>The protocol identification string must match one of the protocols supported by this reader.
   *
   * <p>This method must be implemented by the plugin's reader, which is the only one able to
   * determine if the provided protocol matches the current protocol.
   *
   * <p>It returns true if the current protocol is the protocol provided as an argument, false if it
   * is not.
   *
   * @param readerProtocolName A not empty string.
   * @return True or false.
   * @throws KeypleReaderProtocolNotFoundException if it is not possible to determine the protocol.
   * @since 1.0
   */
  protected abstract boolean isCurrentProtocol(String readerProtocolName);

  /**
   * Determines the current protocol used by the card.
   *
   * <p>The Map {@link #protocolAssociations} containing the protocol names (reader and application)
   * is iterated and the reader protocol (key of the Map) is checked with the reader.
   *
   * <p>If the Map is not empty:
   *
   * <ul>
   *   <li>The boolean {@link #useDefaultProtocol} is set to false.
   *   <li>If the test is positive (the protocol presented is the one used by the current card) then
   *       the field {@link #currentProtocol} is set with the name of the protocol known to the
   *       application.
   *   <li>If none of the protocols present in the Map matches then the method returns null.
   * </ul>
   *
   * <br>
   * If the Map is empty, no other check is done, the String field {@link #currentProtocol} is set
   * to null and the boolean field {@link #useDefaultProtocol} is set to true.
   */
  private void computeCurrentProtocol() {

    /* Determine the current protocol */
    currentProtocol = null;
    if (protocolAssociations.size() == 0) {
      useDefaultProtocol = true;
    } else {
      useDefaultProtocol = false;
      for (Map.Entry<String, String> entry : protocolAssociations.entrySet()) {
        if (isCurrentProtocol(entry.getKey())) {
          currentProtocol = entry.getValue();
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void releaseChannel() {

    // close logical channel unconditionally
    closeLogicalChannel();
    if (this instanceof ObservableReader) {
      if ((((ObservableReader) this).countObservers() != 0)) {
        /*
         * request the removal sequence
         */
        this.terminateSeCommunication();
      } else {
        /* Not observed: close immediately the physical channel if requested */
        resetProtocolAndClosePhysicalChannel();
      }
    } else {
      /* Not observable: close immediately the physical channel if requested */
      resetProtocolAndClosePhysicalChannel();
    }
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
  protected final List<SeResponse> processSeRequests(
      List<SeRequest> seRequests,
      MultiSeRequestProcessing multiSeRequestProcessing,
      ChannelControl channelControl) {

    List<SeResponse> seResponses = new ArrayList<SeResponse>();

    /* Open the physical channel if needed, determine the current protocol */
    if (!isPhysicalChannelOpen()) {
      openPhysicalChannelAndSetProtocol();
    }

    /* loop over all SeRequest provided in the list */
    for (SeRequest seRequest : seRequests) {
      /* process the SeRequest and append the SeResponse list */
      SeResponse seResponse;
      try {
        seResponse = processSeRequestLogical(seRequest);
      } catch (KeypleReaderIOException ex) {
        /*
         * The process has been interrupted. We launch a KeypleReaderException with
         * the responses collected so far.
         * Add the latest (and partial) SeResponse to the current list.
         */
        seResponses.add(ex.getSeResponse());
        /* Build a List of SeResponse with the available data. */
        ex.setSeResponses(seResponses);
        if (logger.isDebugEnabled()) {
          logger.debug(
              "[{}] processSeRequests => transmit : process interrupted, collect previous responses {}",
              this.getName(),
              seResponses);
        }
        throw ex;
      }
      seResponses.add(seResponse);
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

    /* close the channel if requested */
    if (channelControl == ChannelControl.CLOSE_AFTER) {
      releaseChannel();
    }

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

    /* Open the physical channel if needed, determine the current protocol */
    if (!isPhysicalChannelOpen()) {
      openPhysicalChannelAndSetProtocol();
    }

    SeResponse seResponse;

    /* process the SeRequest and keep the SeResponse */
    seResponse = processSeRequestLogical(seRequest);

    /* close the channel if requested */
    if (channelControl == ChannelControl.CLOSE_AFTER) {
      releaseChannel();
    }

    return seResponse;
  }

  /**
   * Checks the provided ATR with the AtrFilter.
   *
   * <p>Returns true if the ATR is accepted by the filter.
   *
   * @param atr A byte array.
   * @param atrFilter A not null {@link org.eclipse.keyple.core.seproxy.SeSelector.AtrFilter}
   * @return True or false.
   * @throws IllegalStateException if no ATR is available and the AtrFilter is set.
   * @see #processSelection(SeSelector)
   */
  private boolean checkAtr(byte[] atr, SeSelector.AtrFilter atrFilter) {

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
   * Selects the card with the provided AID and gets the FCI response in return.
   *
   * @param aidSelector A {@link org.eclipse.keyple.core.seproxy.SeSelector.AidSelector} must be not
   *     null.
   * @return An not null {@link ApduResponse} containing the FCI.
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed.
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
   * Select the card according to the {@link SeSelector}.
   *
   * <p>The selection status is returned.<br>
   * 3 levels of filtering/selection are applied successively if they are enabled: protocol, ATR and
   * AID.<br>
   * As soon as one of these operations fails, the method returns with a failed selection status.
   *
   * <p>Conversely, the selection is considered successful if none of the filters have rejected the
   * card, even if none of the filters are active.
   *
   * @param seSelector A not null {@link SeSelector}.
   * @return A not null {@link SelectionStatus}.
   * @throws IllegalStateException in case of configuration inconsistency.
   * @see #processSeRequestLogical(SeRequest)
   */
  private SelectionStatus processSelection(SeSelector seSelector) {

    AnswerToReset answerToReset;
    ApduResponse fciResponse;
    boolean hasMatched = true;

    if (seSelector.getSeProtocol() != null && useDefaultProtocol) {
      throw new IllegalStateException(
          "Protocol " + seSelector.getSeProtocol() + " not associated to a reader protocol.");
    }

    // check protocol if enabled
    if (seSelector.getSeProtocol() == null
        || useDefaultProtocol
        || seSelector.getSeProtocol().equals(currentProtocol)) {
      // protocol check succeeded, check ATR if enabled
      byte[] atr = getATR();
      answerToReset = new AnswerToReset(atr);
      SeSelector.AtrFilter atrFilter = seSelector.getAtrFilter();
      if (atrFilter == null || checkAtr(atr, atrFilter)) {
        // no ATR filter or ATR check succeeded, select by AID if enabled.
        SeSelector.AidSelector aidSelector = seSelector.getAidSelector();
        if (aidSelector != null) {
          fciResponse = selectByAid(aidSelector);
          hasMatched = fciResponse.isSuccessful();
        } else {
          fciResponse = null;
        }
      } else {
        // check failed
        hasMatched = false;
        fciResponse = null;
      }
    } else {
      // protocol failed
      answerToReset = null;
      fciResponse = null;
      hasMatched = false;
    }
    return new SelectionStatus(answerToReset, fciResponse, hasMatched);
  }

  /**
   * Processes the {@link SeRequest} passed as an argument and returns a {@link SeResponse}.
   *
   * <p>The complete description of the process of transmitting an {@link SeRequest} is described in
   * {{@link ProxyReader#transmitSeRequest(SeRequest, ChannelControl)}}
   *
   * @param seRequest The {@link SeRequest} to be processed (must be not null).
   * @return seResponse A not null {@link SeResponse}.
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed
   * @throws IllegalStateException in case of configuration inconsistency.
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
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed
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
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed
   * @since 0.9
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
   * Transmits a single APDU and receives its response. Both are in the form of an array of bytes.
   *
   * <p>This abstract method must be implemented by the ProxyReader plugin (e.g. Pcsc, Nfc).
   *
   * <p><b>Caution: the implementation must handle the case where the card response is 61xy and
   * execute the appropriate get response command.</b>
   *
   * @param apduIn byte buffer containing the ingoing data (should be not null).
   * @return apduResponse byte buffer containing the outgoing data.
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed
   * @since 0.9
   */
  protected abstract byte[] transmitApdu(byte[] apduIn);

  /**
   * Method to be implemented by child classes in order to handle the needed actions when
   * terminating the communication with a card (closing of the physical channel, initiating a
   * removal sequence, etc.)
   *
   * @since 0.9
   */
  abstract void terminateSeCommunication();

  /**
   * Activates the protocol provided from the reader's implementation point of view.
   *
   * <p>The argument is a reader specific String identifying the protocol.
   *
   * <p>Must implemented by the plugin.
   *
   * @param readerProtocolName A not empty String.
   * @throws KeypleReaderProtocolNotSupportedException if the protocol is not supported.
   * @since 1.0
   */
  protected abstract void activateReaderProtocol(String readerProtocolName);

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public final void activateProtocol(String readerProtocolName, String applicationProtocolName) {

    Assert.getInstance()
        .notEmpty(readerProtocolName, "readerProtocolName")
        .notEmpty(applicationProtocolName, "applicationProtocolName");

    activateReaderProtocol(readerProtocolName);
    protocolAssociations.put(readerProtocolName, applicationProtocolName);
  }

  /**
   * Deactivates the protocol provided from the reader's implementation point of view.
   *
   * <p>The argument is a reader specific String identifying the protocol.
   *
   * @param readerProtocolName A not empty String.
   * @throws KeypleReaderProtocolNotSupportedException if the protocol is not supported.
   * @since 1.0
   */
  protected abstract void deactivateReaderProtocol(String readerProtocolName);

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  public final void deactivateProtocol(String readerProtocolName) {

    Assert.getInstance().notEmpty(readerProtocolName, "readerProtocolName");

    protocolAssociations.remove(readerProtocolName);
    deactivateReaderProtocol(readerProtocolName);
  }
}
