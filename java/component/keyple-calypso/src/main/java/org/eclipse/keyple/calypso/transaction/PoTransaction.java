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
package org.eclipse.keyple.calypso.transaction;

import static org.eclipse.keyple.calypso.command.po.CalypsoPoCommand.*;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.keyple.calypso.SelectFileControl;
import org.eclipse.keyple.calypso.command.po.AbstractPoCommandBuilder;
import org.eclipse.keyple.calypso.command.po.AbstractPoResponseParser;
import org.eclipse.keyple.calypso.command.po.CalypsoPoCommand;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.builder.AppendRecordCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.DecreaseCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.IncreaseCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.UpdateRecordCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.WriteRecordCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.security.AbstractOpenSessionCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.security.CloseSessionCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.security.InvalidateCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.security.PoGetChallengeCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.security.RatificationCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.security.RehabilitateCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.security.VerifyPinCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.storedvalue.SvDebitCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.storedvalue.SvGetCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.storedvalue.SvReloadCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.storedvalue.SvUndebitCmdBuild;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoCommandException;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoPinException;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoSecurityDataException;
import org.eclipse.keyple.calypso.command.po.parser.security.AbstractOpenSessionRespPars;
import org.eclipse.keyple.calypso.command.po.parser.security.CloseSessionRespPars;
import org.eclipse.keyple.calypso.command.sam.exception.CalypsoSamCommandException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoAtomicTransactionException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoAuthenticationNotVerifiedException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoDesynchronizedExchangesException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoPoCloseSecureSessionException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoPoIOException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoPoTransactionException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoPoTransactionIllegalStateException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoSamIOException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoSessionAuthenticationException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoUnauthorizedKvcException;
import org.eclipse.keyple.core.card.message.ApduRequest;
import org.eclipse.keyple.core.card.message.ApduResponse;
import org.eclipse.keyple.core.card.message.CardRequest;
import org.eclipse.keyple.core.card.message.CardResponse;
import org.eclipse.keyple.core.card.message.ChannelControl;
import org.eclipse.keyple.core.card.message.ProxyReader;
import org.eclipse.keyple.core.card.selection.CardResource;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service provides the high-level API to manage transactions with a Calypso PO.
 *
 * <p>Depending on the type of operations required, the presence of a SAM may be necessary.
 *
 * <p>The {@link CalypsoPo} object provided to the build is kept and updated at each step of using
 * the service. It is the main container of the data handled during the transaction and acts as a
 * card image.
 *
 * <p>There are two main steps in using the methods of this service:
 *
 * <ul>
 *   <li>A command preparation step during which the application invokes prefixed "prepare" methods
 *       that will add to an internal list of commands to be executed by the PO. The incoming data
 *       to the PO are placed in {@link CalypsoPo}.
 *   <li>A processing step corresponding to the prefixed "process" methods, which will carry out the
 *       communications with the PO and if necessary the SAM. The outgoing data from the PO are
 *       placed in {@link CalypsoPo}.
 * </ul>
 *
 * <p>Technical or data errors, security conditions, etc. are reported as exceptions.
 *
 * @since 0.9
 */
public class PoTransaction {

  /**
   * commands that modify the content of the PO in session have a cost on the session buffer equal
   * to the length of the outgoing data plus 6 bytes
   */
  private static final int SESSION_BUFFER_CMD_ADDITIONAL_COST = 6;

  private static final int APDU_HEADER_LENGTH = 5;

  private static final Logger logger = LoggerFactory.getLogger(PoTransaction.class);

  /** The reader for PO. */
  private final ProxyReader poReader;
  /** The PO security settings used to manage the secure session */
  private PoSecuritySettings poSecuritySettings;
  /** The SAM commands processor */
  private SamCommandProcessor samCommandProcessor;
  /** The current CalypsoPo */
  private final CalypsoPo calypsoPo;
  /** the type of the notified event. */
  private SessionState sessionState;
  /** The current secure session access level: PERSO, RELOAD, DEBIT */
  private SessionSetting.AccessLevel currentAccessLevel;
  /** modifications counter management */
  private int modificationsCounter;
  /** The object for managing PO commands */
  private final PoCommandManager poCommandManager;
  /** The current Store Value action */
  private SvSettings.Action svAction;
  /** The {@link ChannelControl} action */
  private ChannelControl channelControl;

  /**
   * Constructor.
   *
   * <p>Secure operations are enabled by the presence of {@link PoSecuritySettings}.
   *
   * @param poResource the PO resource (combination of {@link Reader} and {@link CalypsoPo})
   * @param poSecuritySettings a set of security settings ({@link PoSecuritySettings}) including a
   *     {@link CardResource} based on a {@link CalypsoSam}.
   * @since 0.9
   */
  public PoTransaction(CardResource<CalypsoPo> poResource, PoSecuritySettings poSecuritySettings) {

    this(poResource);

    this.poSecuritySettings = poSecuritySettings;

    samCommandProcessor = new SamCommandProcessor(poResource, poSecuritySettings);
  }

  /**
   * Constructor.
   *
   * @param poResource the PO resource (combination of {@link Reader} and {@link CalypsoPo})
   * @since 0.9
   */
  public PoTransaction(CardResource<CalypsoPo> poResource) {
    this.poReader = (ProxyReader) poResource.getReader();

    this.calypsoPo = poResource.getSmartCard();

    modificationsCounter = calypsoPo.getModificationsCounter();

    sessionState = SessionState.SESSION_UNINITIALIZED;

    poCommandManager = new PoCommandManager();

    channelControl = ChannelControl.KEEP_OPEN;
  }

  /**
   * Open a single Secure Session.
   *
   * @param accessLevel access level of the session (personalization, load or debit).
   * @param poCommands the po commands inside session
   * @throws CalypsoPoTransactionIllegalStateException if no {@link PoSecuritySettings} is available
   * @throws CalypsoPoTransactionException if a functional error occurs (including PO and SAM IO
   *     errors)
   * @throws CalypsoPoCommandException if a response from the PO was unexpected
   * @throws CalypsoSamCommandException if a response from the SAM was unexpected
   */
  private void processAtomicOpening(
      PoTransaction.SessionSetting.AccessLevel accessLevel,
      List<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> poCommands) {

    // This method should be called only if no session was previously open
    checkSessionIsNotOpen();

    if (poSecuritySettings == null) {
      throw new CalypsoPoTransactionIllegalStateException("No SAM resource is available");
    }

    // gets the terminal challenge
    byte[] sessionTerminalChallenge = samCommandProcessor.getSessionTerminalChallenge();

    // PO ApduRequest List to hold Open Secure Session and other optional commands
    List<ApduRequest> poApduRequests = new ArrayList<ApduRequest>();

    // The sfi and record number to be read when the open secure session command is executed.
    // The default value is 0 (no record to read) but we will optimize the exchanges if a read
    // record command has been prepared.
    int sfi = 0;
    int recordNumber = 0;

    // Let's check if we have a read record command at the top of the command list.
    //
    // If so, then the command is withdrawn in favour of its equivalent executed at the same
    // time as the open secure session command.
    if (poCommands != null && !poCommands.isEmpty()) {
      AbstractPoCommandBuilder<? extends AbstractPoResponseParser> poCommand = poCommands.get(0);
      if (poCommand.getCommandRef() == CalypsoPoCommand.READ_RECORDS
          && ((ReadRecordsCmdBuild) poCommand).getReadMode()
              == ReadRecordsCmdBuild.ReadMode.ONE_RECORD) {
        sfi = ((ReadRecordsCmdBuild) poCommand).getSfi();
        recordNumber = ((ReadRecordsCmdBuild) poCommand).getFirstRecordNumber();
        poCommands.remove(0);
      }
    }

    // Build the PO Open Secure Session command
    AbstractOpenSessionCmdBuild<AbstractOpenSessionRespPars> openSessionCmdBuild =
        AbstractOpenSessionCmdBuild.create(
            calypsoPo.getRevision(),
            accessLevel.getSessionKey(),
            sessionTerminalChallenge,
            sfi,
            recordNumber);

    // Add the resulting ApduRequest to the PO ApduRequest list
    poApduRequests.add(openSessionCmdBuild.getApduRequest());

    // Add all optional commands to the PO ApduRequest list
    if (poCommands != null) {
      poApduRequests.addAll(getApduRequests(poCommands));
    }

    // Create a CardRequest from the ApduRequest list, PO AID as Selector, keep channel open
    CardRequest poCardRequest = new CardRequest(poApduRequests);

    // Transmit the commands to the PO
    CardResponse poCardResponse = safePoTransmit(poCardRequest, ChannelControl.KEEP_OPEN);

    // Retrieve and check the ApduResponses
    List<ApduResponse> poApduResponses = poCardResponse.getApduResponses();

    // Do some basic checks
    checkCommandsResponsesSynchronization(poApduRequests.size(), poApduResponses.size());

    // Parse the response to Open Secure Session (the first item of poApduResponses)
    // The updateCalypsoPo method fills the CalypsoPo object with the command data and return
    // the parser used for an internal usage here.
    AbstractOpenSessionRespPars poOpenSessionPars =
        (AbstractOpenSessionRespPars)
            CalypsoPoUtils.updateCalypsoPo(calypsoPo, openSessionCmdBuild, poApduResponses.get(0));
    // Build the Digest Init command from PO Open Session
    // the session challenge is needed for the SAM digest computation
    byte[] sessionCardChallenge = poOpenSessionPars.getPoChallenge();

    // The PO KIF
    byte poKif = poOpenSessionPars.getSelectedKif();

    // The PO KVC, may be null for PO Rev 1.0
    Byte poKvc = poOpenSessionPars.getSelectedKvc();

    if (logger.isDebugEnabled()) {
      logger.debug(
          "processAtomicOpening => opening: CARDCHALLENGE = {}, POKIF = {}, POKVC = {}",
          ByteArrayUtil.toHex(sessionCardChallenge),
          String.format("%02X", poKif),
          String.format("%02X", poKvc));
    }

    if (!poSecuritySettings.isSessionKvcAuthorized(poKvc)) {
      throw new CalypsoUnauthorizedKvcException(String.format("PO KVC = %02X", poKvc));
    }

    // Initialize the digest processor. It will store all digest operations (Digest Init, Digest
    // Update) until the session closing. At this moment, all SAM Apdu will be processed at
    // once.
    samCommandProcessor.initializeDigester(
        accessLevel, false, false, poKif, poKvc, poApduResponses.get(0).getDataOut());

    // Add all commands data to the digest computation. The first command in the list is the
    // open secure session command. This command is not included in the digest computation, so
    // we skip it and start the loop at index 1.
    if ((poCommands != null) && !poCommands.isEmpty()) {
      // Add requests and responses to the digest processor
      samCommandProcessor.pushPoExchangeDataList(poApduRequests, poApduResponses, 1);
    }

    // Remove Open Secure Session response and create a new CardResponse
    poApduResponses.remove(0);

    // update CalypsoPo with the received data
    CalypsoPoUtils.updateCalypsoPo(calypsoPo, poCommands, poApduResponses);

    sessionState = SessionState.SESSION_OPEN;
  }

  /**
   * Create an ApduRequest List from a AbstractPoCommandBuilder List.
   *
   * @param poCommands a list of PO commands
   * @return the ApduRequest list
   */
  private List<ApduRequest> getApduRequests(
      List<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> poCommands) {
    List<ApduRequest> apduRequests = new ArrayList<ApduRequest>();
    if (poCommands != null) {
      for (AbstractPoCommandBuilder<? extends AbstractPoResponseParser> commandBuilder :
          poCommands) {
        apduRequests.add(commandBuilder.getApduRequest());
      }
    }
    return apduRequests;
  }

  /**
   * Process PO commands in a Secure Session.
   *
   * <ul>
   *   <li>On the PO reader, generates a CardRequest with channelControl set to KEEP_OPEN, and
   *       ApduRequests with the PO commands.
   *   <li>In case the secure session is active, the "cache" of SAM commands is completed with the
   *       corresponding Digest Update commands.
   *   <li>If a session is open and channelControl is set to CLOSE_AFTER, the current PO session is
   *       aborted
   *   <li>Returns the corresponding PO CardResponse.
   * </ul>
   *
   * @param poCommands the po commands inside session
   * @param channelControl indicated if the card channel of the PO reader must be closed after the
   *     last command
   * @throws CalypsoPoTransactionException if a functional error occurs (including PO and SAM IO
   *     errors)
   * @throws CalypsoPoCommandException if a response from the PO was unexpected
   */
  private void processAtomicPoCommands(
      List<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> poCommands,
      ChannelControl channelControl) {

    // Get the PO ApduRequest List
    List<ApduRequest> poApduRequests = getApduRequests(poCommands);

    // Create a CardRequest from the ApduRequest list, PO AID as Selector, manage the logical
    // channel according to the channelControl enum
    CardRequest poCardRequest = new CardRequest(poApduRequests);

    // Transmit the commands to the PO
    CardResponse poCardResponse = safePoTransmit(poCardRequest, channelControl);

    // Retrieve and check the ApduResponses
    List<ApduResponse> poApduResponses = poCardResponse.getApduResponses();

    // Do some basic checks
    checkCommandsResponsesSynchronization(poApduRequests.size(), poApduResponses.size());

    // Add all commands data to the digest computation if this method is called within a Secure
    // Session.
    if (sessionState == SessionState.SESSION_OPEN) {
      samCommandProcessor.pushPoExchangeDataList(poApduRequests, poApduResponses, 0);
    }

    CalypsoPoUtils.updateCalypsoPo(calypsoPo, poCommands, poCardResponse.getApduResponses());
  }

  /**
   * Close the Secure Session.
   *
   * <ul>
   *   <li>The SAM cache is completed with the Digest Update commands related to the new PO commands
   *       to be sent and their anticipated responses. A Digest Close command is also added to the
   *       SAM command cache.
   *   <li>On the SAM session reader side, a CardRequest is transmitted with SAM commands from the
   *       command cache. The SAM command cache is emptied.
   *   <li>The SAM certificate is retrieved from the Digest Close response. The terminal signature
   *       is identified.
   *   <li>Then, on the PO reader, a CardRequest is transmitted with a {@link ChannelControl} set to
   *       CLOSE_AFTER or KEEP_OPEN depending on whether or not prepareReleasePoChannel was called,
   *       and apduRequests including the new PO commands to send in the session, a Close Session
   *       command (defined with the SAM certificate), and optionally a ratificationCommand.
   *       <ul>
   *         <li>The management of ratification is conditioned by the mode of communication.
   *             <ul>
   *               <li>If the communication mode is CONTACTLESS, a specific ratification command is
   *                   sent after the Close Session command. No ratification is requested in the
   *                   Close Session command.
   *               <li>If the communication mode is CONTACTS, no ratification command is sent after
   *                   the Close Session command. Ratification is requested in the Close Session
   *                   command.
   *             </ul>
   *         <li>Otherwise, the PO Close Secure Session command is defined to directly set the PO as
   *             ratified.
   *       </ul>
   *   <li>The PO responses of the poModificationCommands are compared with the
   *       poAnticipatedResponses. The PO signature is identified from the PO Close Session
   *       response.
   *   <li>The PO certificate is recovered from the Close Session response. The card signature is
   *       identified.
   *   <li>Finally, on the SAM session reader, a Digest Authenticate is automatically operated in
   *       order to verify the PO signature.
   *   <li>Returns the corresponding PO CardResponse.
   * </ul>
   *
   * The method is marked as deprecated because the advanced variant defined below must be used at
   * the application level.
   *
   * @param poModificationCommands a list of commands that can modify the PO memory content
   * @param poAnticipatedResponses a list of anticipated PO responses to the modification commands
   * @param ratificationMode the ratification mode tells if the session is closed ratified or not
   * @param channelControl indicates if the card channel of the PO reader must be closed after the
   *     last command
   * @throws CalypsoPoTransactionException if a functional error occurs (including PO and SAM IO
   *     errors)
   * @throws CalypsoPoCommandException if a response from the PO was unexpected
   * @throws CalypsoSamCommandException if a response from the SAM was unexpected
   */
  private void processAtomicClosing(
      List<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> poModificationCommands,
      List<ApduResponse> poAnticipatedResponses,
      SessionSetting.RatificationMode ratificationMode,
      ChannelControl channelControl) {

    checkSessionIsOpen();

    // Get the PO ApduRequest List - for the first PO exchange
    List<ApduRequest> poApduRequests = getApduRequests(poModificationCommands);

    // Compute "anticipated" Digest Update (for optional poModificationCommands)
    if ((poModificationCommands != null) && !poApduRequests.isEmpty()) {
      checkCommandsResponsesSynchronization(poApduRequests.size(), poAnticipatedResponses.size());
      // Add all commands data to the digest computation: commands and anticipated
      // responses.
      samCommandProcessor.pushPoExchangeDataList(poApduRequests, poAnticipatedResponses, 0);
    }

    // All SAM digest operations will now run at once.
    // Get Terminal Signature from the latest response
    byte[] sessionTerminalSignature = samCommandProcessor.getTerminalSignature();
    boolean ratificationCommandResponseReceived;

    // Build the PO Close Session command. The last one for this session
    CloseSessionCmdBuild closeSessionCmdBuild =
        new CloseSessionCmdBuild(
            calypsoPo.getPoClass(),
            SessionSetting.RatificationMode.CLOSE_RATIFIED.equals(ratificationMode),
            sessionTerminalSignature);

    poApduRequests.add(closeSessionCmdBuild.getApduRequest());

    // Keep the position of the Close Session command in request list
    int closeCommandIndex = poApduRequests.size() - 1;

    // Add the PO Ratification command if any
    boolean ratificationCommandAdded;
    if (SessionSetting.RatificationMode.CLOSE_RATIFIED.equals(ratificationMode)
        && poReader.isContactless()) {
      poApduRequests.add(RatificationCmdBuild.getApduRequest(calypsoPo.getPoClass()));
      ratificationCommandAdded = true;
    } else {
      ratificationCommandAdded = false;
    }

    // Transfer PO commands
    CardRequest poCardRequest = new CardRequest(poApduRequests);

    CardResponse poCardResponse;
    try {
      poCardResponse = poReader.transmitCardRequest(poCardRequest, channelControl);
      // if the ratification command was added and no error occured then the response has been
      // received
      ratificationCommandResponseReceived = ratificationCommandAdded;
    } catch (KeypleReaderIOException ex) {
      poCardResponse = ex.getCardResponse();
      // The current exception may have been caused by a communication issue with the PO
      // during the ratification command.
      //
      // In this case, we do not stop the process and consider the Secure Session close. We'll
      // check the signature.
      //
      // We should have one response less than requests.
      if (!ratificationCommandAdded
          || poCardResponse == null
          || poCardResponse.getApduResponses().size() != poApduRequests.size() - 1) {
        throw new CalypsoPoIOException("PO IO Exception while transmitting commands.", ex);
      }
      // we received all responses except the response to the ratification command
      ratificationCommandResponseReceived = false;
    }

    List<ApduResponse> poApduResponses = poCardResponse.getApduResponses();

    // Check the commands executed before closing the secure session (only responses to these
    // commands
    // will be taken into account)
    CalypsoPoUtils.updateCalypsoPo(calypsoPo, poModificationCommands, poApduResponses);

    // Check the PO's response to Close Secure Session
    CloseSessionRespPars poCloseSessionPars;
    try {
      poCloseSessionPars =
          (CloseSessionRespPars)
              CalypsoPoUtils.updateCalypsoPo(
                  calypsoPo, closeSessionCmdBuild, poApduResponses.get(closeCommandIndex));
    } catch (CalypsoPoSecurityDataException ex) {
      throw new CalypsoPoCloseSecureSessionException("Invalid PO session", ex);
    }

    // Check the PO signature
    try {
      samCommandProcessor.authenticatePoSignature(poCloseSessionPars.getSignatureLo());
    } catch (CalypsoSamIOException ex) {
      throw new CalypsoAuthenticationNotVerifiedException(ex.getMessage());
    } catch (CalypsoSamCommandException ex) {
      throw new CalypsoSessionAuthenticationException("PO authentication failed on SAM side.", ex);
    }

    // If necessary, we check the status of the SV after the session has been successfully
    // closed.
    if (poCommandManager.isSvOperationCompleteOneTime()) {
      samCommandProcessor.checkSvStatus(poCloseSessionPars.getPostponedData());
    }

    sessionState = SessionState.SESSION_CLOSED;

    if (ratificationCommandResponseReceived) { // NOSONAR: boolean change in catch
      // is not taken into account by
      // Sonar
      // Remove the ratification response
      poApduResponses.remove(poApduResponses.size() - 1);
    }

    // Remove Close Secure Session response and create a new CardResponse
    poApduResponses.remove(poApduResponses.size() - 1);
  }

  /**
   * Advanced variant of processAtomicClosing in which the list of expected responses is determined
   * from previous reading operations.
   *
   * @param poCommands a list of commands that can modify the PO memory content
   * @param ratificationMode the ratification mode tells if the session is closed ratified or not
   * @param channelControl indicates if the card channel of the PO reader must be closed after the
   *     last command
   * @throws CalypsoPoTransactionException if a functional error occurs (including PO and SAM IO
   *     errors)
   * @throws CalypsoPoCommandException if a response from the PO was unexpected
   * @throws CalypsoSamCommandException if a response from the SAM was unexpected
   */
  private void processAtomicClosing(
      List<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> poCommands,
      SessionSetting.RatificationMode ratificationMode,
      ChannelControl channelControl) {
    List<ApduResponse> poAnticipatedResponses = getAnticipatedResponses(poCommands);
    processAtomicClosing(poCommands, poAnticipatedResponses, ratificationMode, channelControl);
  }

  /**
   * Contains the Calypso Secure Session set of parameters.
   *
   * @since 0.9
   */
  public static class SessionSetting {
    /**
     * The modification mode indicates whether the secure session can be closed and reopened to
     * manage the limitation of the PO buffer memory.
     *
     * @since 0.9
     */
    public enum ModificationMode {
      /**
       * The secure session is atomic. The consistency of the content of the resulting PO memory is
       * guaranteed.
       */
      ATOMIC,
      /**
       * Several secure sessions can be chained (to manage the writing of large amounts of data).
       * The resulting content of the PO's memory can be inconsistent if the PO is removed during
       * the process.
       */
      MULTIPLE
    }

    /**
     * The PO Transaction Access Level: personalization, loading or debiting.
     *
     * @since 0.9
     */
    public enum AccessLevel {

      /** Session Access Level used for personalization purposes. */
      SESSION_LVL_PERSO("perso", (byte) 0x01),
      /** Session Access Level used for reloading purposes. */
      SESSION_LVL_LOAD("load", (byte) 0x02),
      /** Session Access Level used for validating and debiting purposes. */
      SESSION_LVL_DEBIT("debit", (byte) 0x03);

      private final String name;
      private final byte sessionKey;

      AccessLevel(String name, byte sessionKey) {
        this.name = name;
        this.sessionKey = sessionKey;
      }

      public String getName() {
        return name;
      }

      public byte getSessionKey() {
        return sessionKey;
      }
    }

    /**
     * The ratification mode defines the behavior of processClosing regarding the ratification
     * process.
     *
     * @since 0.9
     */
    public enum RatificationMode {
      CLOSE_RATIFIED,
      CLOSE_NOT_RATIFIED
    }
  }

  /**
   * The PO Transaction State defined with the elements: ‘IOError’, ‘SEInserted’ and ‘SERemoval’.
   */
  private enum SessionState {
    /** Initial state of a PO transaction. The PO must have been previously selected. */
    SESSION_UNINITIALIZED,
    /** The secure session is active. */
    SESSION_OPEN,
    /** The secure session is closed. */
    SESSION_CLOSED
  }

  /**
   * Defines the PIN transmission modes: plain or encrypted.
   *
   * @since 0.9
   */
  public enum PinTransmissionMode {
    PLAIN,
    ENCRYPTED
  }

  /**
   * Defines the Stored Value transactions parameters
   *
   * @since 0.9
   */
  public static class SvSettings {
    /**
     * Defines the type of operation to be performed
     *
     * @since 0.9
     */
    public enum Operation {
      /** Increase the balance of the stored value */
      RELOAD,
      /** Decrease the balance of the stored value */
      DEBIT;
    }

    /**
     * Defines the type of action.
     *
     * @since 0.9
     */
    public enum Action {
      /**
       * In the case of a {@link Operation#RELOAD}, loads a positive amount; in the case of a {@link
       * Operation#DEBIT}, debits a positive amount
       */
      DO,
      /**
       * In the case of a {@link Operation#RELOAD}, loads a negative amount; in the case of a {@link
       * Operation#DEBIT}, cancels, totally or partially, a previous debit.
       */
      UNDO
    }

    /**
     * Defines the reading modes of the SV log.
     *
     * @since 0.9
     */
    public enum LogRead {
      /** Request only the RELOAD or DEBIT log according to the currently specified operation */
      SINGLE,
      /** Request both RELOAD and DEBIT logs */
      ALL
    }

    /**
     * Defines the acceptance modes for negative balances.
     *
     * @since 0.9
     */
    public enum NegativeBalance {
      /**
       * An SV exception will be raised if the attempted debit of the SV would result in a negative
       * balance.
       */
      FORBIDDEN,
      /** Negative balance is allowed */
      AUTHORIZED
    }
  }

  /**
   * Gets the value of the designated counter
   *
   * @param sfi the SFI of the EF containing the counter
   * @param counter the number of the counter
   * @return the value of the counter
   */
  private int getCounterValue(int sfi, int counter) {
    try {
      ElementaryFile ef = calypsoPo.getFileBySfi((byte) sfi);
      return ef.getData().getContentAsCounterValue(counter);
    } catch (NoSuchElementException e) {
      throw new CalypsoPoTransactionIllegalStateException(
          "Anticipated response. Unable to determine anticipated value of counter "
              + counter
              + " in EF sfi "
              + sfi);
    }
  }

  /**
   * Create an anticipated response to an Increase/Decrease command
   *
   * @param newCounterValue the anticipated counter value
   * @return an {@link ApduResponse} containing the expected bytes
   */
  private ApduResponse createIncreaseDecreaseResponse(int newCounterValue) {
    // response = NNNNNN9000
    byte[] response = new byte[5];
    response[0] = (byte) ((newCounterValue & 0x00FF0000) >> 16);
    response[1] = (byte) ((newCounterValue & 0x0000FF00) >> 8);
    response[2] = (byte) (newCounterValue & 0x000000FF);
    response[3] = (byte) 0x90;
    response[4] = (byte) 0x00;
    return new ApduResponse(response, null);
  }

  static final ApduResponse RESPONSE_OK =
      new ApduResponse(new byte[] {(byte) 0x90, (byte) 0x00}, null);
  static final ApduResponse RESPONSE_OK_POSTPONED =
      new ApduResponse(new byte[] {(byte) 0x62, (byte) 0x00}, null);

  /**
   * Get the anticipated response to the command sent in processClosing.<br>
   * These commands are supposed to be "modifying commands" i.e.
   * Increase/Decrease/UpdateRecord/WriteRecord ou AppendRecord.
   *
   * @param poCommands the list of PO commands sent
   * @return the list of the anticipated responses.
   * @throws CalypsoPoTransactionIllegalStateException if the anticipation process failed
   */
  private List<ApduResponse> getAnticipatedResponses(
      List<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> poCommands) {
    List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
    if (poCommands != null) {
      for (AbstractPoCommandBuilder<? extends AbstractPoResponseParser> commandBuilder :
          poCommands) {
        if (commandBuilder.getCommandRef() == DECREASE) {
          int sfi = ((DecreaseCmdBuild) commandBuilder).getSfi();
          int counter = ((DecreaseCmdBuild) commandBuilder).getCounterNumber();
          int newCounterValue =
              getCounterValue(sfi, counter) - ((DecreaseCmdBuild) commandBuilder).getDecValue();
          apduResponses.add(createIncreaseDecreaseResponse(newCounterValue));
        } else if (commandBuilder.getCommandRef() == INCREASE) {
          int sfi = ((IncreaseCmdBuild) commandBuilder).getSfi();
          int counter = ((IncreaseCmdBuild) commandBuilder).getCounterNumber();
          int newCounterValue =
              getCounterValue(sfi, counter) + ((IncreaseCmdBuild) commandBuilder).getIncValue();
          apduResponses.add(createIncreaseDecreaseResponse(newCounterValue));
        } else if (commandBuilder.getCommandRef() == SV_RELOAD
            || commandBuilder.getCommandRef() == SV_DEBIT
            || commandBuilder.getCommandRef() == SV_UNDEBIT) {
          apduResponses.add(RESPONSE_OK_POSTPONED);
        } else { // Append/Update/Write Record: response = 9000
          apduResponses.add(RESPONSE_OK);
        }
      }
    }
    return apduResponses;
  }

  /**
   * Opens a Calypso Secure Session and then executes all previously prepared commands.
   *
   * <p>It is the starting point of the sequence:
   *
   * <ul>
   *   <li>{@link #processOpening(SessionSetting.AccessLevel)}
   *   <li>[{@link #processPoCommands()}]
   *   <li>[...]
   *   <li>[{@link #processPoCommands()}]
   *   <li>{@link #processClosing()}
   * </ul>
   *
   * <p>Each of the steps in this sequence may or may not be preceded by the preparation of one or
   * more commands and ends with an update of the {@link CalypsoPo} object provided when
   * PoTransaction was created.
   *
   * <p>As a prerequisite for calling this method, since the Calypso Secure Session involves the use
   * of a SAM, the PoTransaction must have been built in secure mode, i.e. the constructor used must
   * be the one expecting a reference to a valid {@link PoSecuritySettings} object, otherwise a
   * {@link CalypsoPoTransactionIllegalStateException} is raised.
   *
   * <p>The secure session is opened with the {@link SessionSetting.AccessLevel} passed as an
   * argument depending on whether it is a personalization, reload or debit transaction profile..
   *
   * <p>The possible overflow of the internal session buffer of the PO is managed in two ways
   * depending on the setting chosen in {@link PoSecuritySettings}.
   *
   * <ul>
   *   <li>If the session was opened with the {@link SessionSetting.ModificationMode#ATOMIC} mode
   *       and the previously prepared commands will cause the buffer to be exceeded, then an {@link
   *       CalypsoAtomicTransactionException} is raised and no transmission to the PO is made. <br>
   *   <li>If the session was opened with the {@link SessionSetting.ModificationMode#MULTIPLE} mode
   *       and the buffer is to be exceeded then a split into several secure sessions is performed
   *       automatically. However, regardless of the number of intermediate sessions performed, a
   *       secure session is opened at the end of the execution of this method.
   * </ul>
   *
   * <p>Be aware that in the "MULTIPLE" case we lose the benefit of the atomicity of the secure
   * session.
   *
   * <p><b>PO and SAM exchanges in detail</b>
   *
   * <p>When executing this method, communications with the PO and the SAM are (in that order) :
   *
   * <ul>
   *   <li>Sending the card diversifier (Calypso PO serial number) to the SAM and receiving the
   *       terminal challenge
   *   <li>Grouped sending to the PO (in a {@link CardRequest}) of
   *       <ul>
   *         <li>the open secure session command including the challenge terminal.
   *         <li>all previously prepared commands
   *       </ul>
   *   <li>Receiving grouped responses and updating {@link CalypsoPo} with the collected data.
   * </ul>
   *
   * For optimization purposes, if the first command prepared is the reading of a single record of a
   * PO file then this one is replaced by a setting of the session opening command allowing the
   * retrieval of this data in response to this command.
   *
   * <p><b>Other operations carried out</b>
   *
   * <ul>
   *   <li>The card KIF, KVC and card challenge received in response to the open secure session
   *       command are kept for a later initialization of the session's digest (see {@link
   *       #processClosing}).
   *   <li>All data received in response to the open secure session command and the responses to the
   *       prepared commands are also stored for later calculation of the digest.
   *   <li>If a list of authorized KVCs has been defined in {@link PoSecuritySettings} and the KVC
   *       of the card does not belong to this list then a {@link CalypsoUnauthorizedKvcException}
   *       is thrown.
   * </ul>
   *
   * <p>All unexpected results (communication errors, data or security errors, etc. are notified to
   * the calling application through dedicated exceptions.
   *
   * <p><i>Note: to understand in detail how the secure session works please refer to the PO
   * specification documents.</i>
   *
   * @param accessLevel An {@link SessionSetting.AccessLevel} enum entry.
   * @throws CalypsoPoTransactionIllegalStateException if no {@link PoSecuritySettings} is available
   * @throws CalypsoAtomicTransactionException if the PO session buffer were to overflow
   * @throws CalypsoUnauthorizedKvcException if the card KVC is not authorized
   * @throws CalypsoPoTransactionException if a functional error occurs (including PO and SAM IO
   *     errors)
   * @throws CalypsoPoCommandException if a response from the PO was unexpected
   * @throws CalypsoSamCommandException if a response from the SAM was unexpected
   * @since 0.9
   */
  public final void processOpening(PoTransaction.SessionSetting.AccessLevel accessLevel) {
    currentAccessLevel = accessLevel;

    // create a sublist of AbstractPoCommandBuilder to be sent atomically
    List<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> poAtomicCommands =
        new ArrayList<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>>();

    AtomicInteger neededSessionBufferSpace = new AtomicInteger();
    AtomicBoolean overflow = new AtomicBoolean();

    for (AbstractPoCommandBuilder<? extends AbstractPoResponseParser> commandBuilder :
        poCommandManager.getPoCommandBuilders()) {
      // check if the command is a modifying one and get it status (overflow yes/no,
      // neededSessionBufferSpace)
      // if the command overflows the session buffer in atomic modification mode, an exception
      // is raised.
      if (checkModifyingCommand(commandBuilder, overflow, neededSessionBufferSpace)) {
        if (overflow.get()) {
          // Open the session with the current commands
          processAtomicOpening(currentAccessLevel, poAtomicCommands);
          // Closes the session, resets the modifications buffer counters for the next
          // round (set the contact mode to avoid the transmission of the ratification)
          processAtomicClosing(
              null, SessionSetting.RatificationMode.CLOSE_RATIFIED, ChannelControl.KEEP_OPEN);
          resetModificationsBufferCounter();
          // Clear the list and add the command that did not fit in the PO modifications
          // buffer. We also update the usage counter without checking the result.
          poAtomicCommands.clear();
          poAtomicCommands.add(commandBuilder);
          // just update modifications buffer usage counter, ignore result (always false)
          isSessionBufferOverflowed(neededSessionBufferSpace.get());
        } else {
          // The command fits in the PO modifications buffer, just add it to the list
          poAtomicCommands.add(commandBuilder);
        }
      } else {
        // This command does not affect the PO modifications buffer
        poAtomicCommands.add(commandBuilder);
      }
    }

    processAtomicOpening(currentAccessLevel, poAtomicCommands);

    // sets the flag indicating that the commands have been executed
    poCommandManager.notifyCommandsProcessed();
  }

  /**
   * Process all prepared PO commands (outside a Secure Session).
   *
   * <p>Note: commands prepared prior to the invocation of this method shall not require the use of
   * a SAM.
   *
   * @param channelControl indicates if the card channel of the PO reader must be closed after the
   *     last command
   * @throws CalypsoPoTransactionException if a functional error occurs (including PO and SAM IO
   *     errors)
   * @throws CalypsoPoCommandException if a response from the PO was unexpected
   */
  private void processPoCommandsOutOfSession(ChannelControl channelControl) {

    // PO commands sent outside a Secure Session. No modifications buffer limitation.
    processAtomicPoCommands(poCommandManager.getPoCommandBuilders(), channelControl);

    // sets the flag indicating that the commands have been executed
    poCommandManager.notifyCommandsProcessed();

    // If an SV transaction was performed, we check the signature returned by the PO here
    if (poCommandManager.isSvOperationCompleteOneTime()) {
      samCommandProcessor.checkSvStatus(CalypsoPoUtils.getSvOperationSignature());
    }
  }

  /**
   * Process all prepared PO commands in a Secure Session.
   *
   * <p>The multiple session mode is handled according to the session settings.
   *
   * @throws CalypsoPoTransactionException if a functional error occurs (including PO and SAM IO
   *     errors)
   * @throws CalypsoPoCommandException if a response from the PO was unexpected
   * @throws CalypsoSamCommandException if a response from the SAM was unexpected
   */
  private void processPoCommandsInSession() {

    // A session is open, we have to care about the PO modifications buffer
    List<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> poAtomicBuilders =
        new ArrayList<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>>();

    AtomicInteger neededSessionBufferSpace = new AtomicInteger();
    AtomicBoolean overflow = new AtomicBoolean();

    for (AbstractPoCommandBuilder<? extends AbstractPoResponseParser> commandBuilder :
        poCommandManager.getPoCommandBuilders()) {
      // check if the command is a modifying one and get it status (overflow yes/no,
      // neededSessionBufferSpace)
      // if the command overflows the session buffer in atomic modification mode, an exception
      // is raised.
      if (checkModifyingCommand(commandBuilder, overflow, neededSessionBufferSpace)) {
        if (overflow.get()) {
          // The current command would overflow the modifications buffer in the PO. We
          // send the current commands and update the parsers. The parsers Iterator is
          // kept all along the process.
          processAtomicPoCommands(poAtomicBuilders, ChannelControl.KEEP_OPEN);
          // Close the session and reset the modifications buffer counters for the next
          // round (set the contact mode to avoid the transmission of the ratification)
          processAtomicClosing(
              null, SessionSetting.RatificationMode.CLOSE_RATIFIED, ChannelControl.KEEP_OPEN);
          resetModificationsBufferCounter();
          // We reopen a new session for the remaining commands to be sent
          processAtomicOpening(currentAccessLevel, null);
          // Clear the list and add the command that did not fit in the PO modifications
          // buffer. We also update the usage counter without checking the result.
          poAtomicBuilders.clear();
          poAtomicBuilders.add(commandBuilder);
          // just update modifications buffer usage counter, ignore result (always false)
          isSessionBufferOverflowed(neededSessionBufferSpace.get());
        } else {
          // The command fits in the PO modifications buffer, just add it to the list
          poAtomicBuilders.add(commandBuilder);
        }
      } else {
        // This command does not affect the PO modifications buffer
        poAtomicBuilders.add(commandBuilder);
      }
    }

    if (!poAtomicBuilders.isEmpty()) {
      processAtomicPoCommands(poAtomicBuilders, ChannelControl.KEEP_OPEN);
    }

    // sets the flag indicating that the commands have been executed
    poCommandManager.notifyCommandsProcessed();
  }

  /**
   * Process all previously prepared PO commands outside or inside a Secure Session.
   *
   * <ul>
   *   <li>All APDUs resulting from prepared commands are grouped in a {@link CardRequest} and sent
   *       to the PO.
   *   <li>The {@link CalypsoPo} object is updated with the result of the executed commands.
   *   <li>If a secure session is opened, except in the case where reloading or debit SV operations
   *       have been prepared, the invocation of this method does not generate communication with
   *       the SAM. The data necessary for the calculation of the terminal signature are kept to be
   *       sent to the SAM at the time of the call to {@link #processClosing()}.<br>
   *       The PO channel is kept open.
   *   <li>If no secure session is opened, the PO channel is closed depending on whether or not
   *       prepareReleasePoChannel has been called.
   *   <li>The PO session buffer overflows are managed in the same way as in {@link
   *       #processOpening(SessionSetting.AccessLevel)}. For example, when the {@link
   *       SessionSetting.ModificationMode#MULTIPLE} mode is chosen, the commands are separated in
   *       as many sessions as necessary to respect the capacity of the PO buffer.
   * </ul>
   *
   * @throws CalypsoPoTransactionException if a functional error occurs (including PO and SAM IO
   *     errors)
   * @throws CalypsoPoCommandException if a response from the PO was unexpected
   * @throws CalypsoSamCommandException if a response from the SAM was unexpected
   * @since 0.9
   */
  public final void processPoCommands() {
    if (sessionState == SessionState.SESSION_OPEN) {
      processPoCommandsInSession();
    } else {
      processPoCommandsOutOfSession(channelControl);
    }
  }

  /**
   * Terminates the Secure Session sequence started with {@link
   * #processOpening(SessionSetting.AccessLevel)}.
   *
   * <p><b>Nominal case</b>
   *
   * <p>The previously prepared commands are integrated into the calculation of the session digest
   * by the SAM before execution by the PO by anticipating their responses.
   *
   * <p>Thus, the session closing command containing the terminal signature is integrated into the
   * same APDU group sent to the PO via a final {@link CardRequest}.
   *
   * <p>Upon reception of the {@link CardRequest} PO, the signature of the PO is verified with the
   * SAM.
   *
   * <p>If the method terminates normally, it means that the secure session closing and all related
   * security checks have been successful; conversely, if one of these operations fails, an
   * exception is raised.
   *
   * <p><b>Stored Value</b>
   *
   * <p>If the SV counter was debited or reloaded during the session, an additional verification
   * specific to the SV is performed by the SAM.
   *
   * <p><b>Ratification</b>
   *
   * <p>A ratification command is added after the close secure session command when the
   * communication is done in a contactless mode.
   *
   * <p>The logical channel is closed or left open depending on whether the {@link
   * #prepareReleasePoChannel()} method has been called before or not.
   *
   * <p><b>PO and SAM exchanges in detail</b>
   *
   * <ul>
   *   <li>All the data exchanged with the PO so far, to which are added the last prepared orders
   *       and their anticipated answers, are sent to the SAM for the calculation of the session
   *       digest. The terminal signature calculation request is also integrated in the same {@link
   *       CardRequest} SAM.
   *   <li>All previously prepared commands are sent to the PO along with the session closing
   *       command and possibly the ratification command within a single {@link CardRequest}.
   *   <li>The responses received from the PO are integrated into CalypsoPo. <br>
   *       Note: the reception of the answers of this final {@link CardRequest} PO is tolerant to
   *       the non-reception of the answer to the ratification order.
   *   <li>The data received from the PO in response to the logout (PO session signature and
   *       possibly SV signature) are sent to the SAM for verification.
   * </ul>
   *
   * @throws CalypsoPoTransactionException if a functional error occurs (including PO and SAM IO
   *     errors)
   * @throws CalypsoPoCommandException if a response from the PO was unexpected
   * @throws CalypsoSamCommandException if a response from the SAM was unexpected
   * @since 0.9
   */
  public final void processClosing() {
    checkSessionIsOpen();

    boolean atLeastOneReadCommand = false;
    boolean sessionPreviouslyClosed = false;

    AtomicInteger neededSessionBufferSpace = new AtomicInteger();
    AtomicBoolean overflow = new AtomicBoolean();

    List<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> poAtomicCommands =
        new ArrayList<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>>();
    for (AbstractPoCommandBuilder<? extends AbstractPoResponseParser> commandBuilder :
        poCommandManager.getPoCommandBuilders()) {
      // check if the command is a modifying one and get it status (overflow yes/no,
      // neededSessionBufferSpace)
      // if the command overflows the session buffer in atomic modification mode, an exception
      // is raised.
      if (checkModifyingCommand(commandBuilder, overflow, neededSessionBufferSpace)) {
        if (overflow.get()) {
          // Reopen a session with the same access level if it was previously closed in
          // this current processClosing
          if (sessionPreviouslyClosed) {
            processAtomicOpening(currentAccessLevel, null);
          }

          // If at least one non-modifying was prepared, we use processAtomicPoCommands
          // instead of processAtomicClosing to send the list
          if (atLeastOneReadCommand) {
            processAtomicPoCommands(poAtomicCommands, ChannelControl.KEEP_OPEN);
            // Clear the list of commands sent
            poAtomicCommands.clear();
            processAtomicClosing(
                poAtomicCommands,
                SessionSetting.RatificationMode.CLOSE_RATIFIED,
                ChannelControl.KEEP_OPEN);
            resetModificationsBufferCounter();
            sessionPreviouslyClosed = true;
            atLeastOneReadCommand = false;
          } else {
            // All commands in the list are 'modifying the PO'
            processAtomicClosing(
                poAtomicCommands,
                SessionSetting.RatificationMode.CLOSE_RATIFIED,
                ChannelControl.KEEP_OPEN);
            // Clear the list of commands sent
            poAtomicCommands.clear();
            resetModificationsBufferCounter();
            sessionPreviouslyClosed = true;
          }

          // Add the command that did not fit in the PO modifications
          // buffer. We also update the usage counter without checking the result.
          poAtomicCommands.add(commandBuilder);
          // just update modifications buffer usage counter, ignore result (always false)
          isSessionBufferOverflowed(neededSessionBufferSpace.get());
        } else {
          // The command fits in the PO modifications buffer, just add it to the list
          poAtomicCommands.add(commandBuilder);
        }
      } else {
        // This command does not affect the PO modifications buffer
        poAtomicCommands.add(commandBuilder);
        atLeastOneReadCommand = true;
      }
    }
    if (sessionPreviouslyClosed) {
      // Reopen if needed, to close the session with the requested conditions
      // (CommunicationMode and channelControl)
      processAtomicOpening(currentAccessLevel, null);
    }

    // Finally, close the session as requested
    processAtomicClosing(
        poAtomicCommands, poSecuritySettings.getRatificationMode(), channelControl);

    // sets the flag indicating that the commands have been executed
    poCommandManager.notifyCommandsProcessed();
  }

  /**
   * Aborts a Secure Session.
   *
   * <p>Send the appropriate command to the PO
   *
   * <p>Clean up internal data and status.
   *
   * @throws CalypsoPoTransactionException if a functional error occurs (including PO and SAM IO
   *     errors)
   * @throws CalypsoPoCommandException if a response from the PO was unexpected
   * @since 0.9
   */
  public final void processCancel() {
    // PO ApduRequest List to hold Close Secure Session command
    List<ApduRequest> poApduRequests = new ArrayList<ApduRequest>();

    // Build the PO Close Session command (in "abort" mode since no signature is provided).
    CloseSessionCmdBuild closeSessionCmdBuild = new CloseSessionCmdBuild(calypsoPo.getPoClass());

    poApduRequests.add(closeSessionCmdBuild.getApduRequest());

    // Transfer PO commands
    CardRequest poCardRequest = new CardRequest(poApduRequests);

    CardResponse poCardResponse = safePoTransmit(poCardRequest, channelControl);

    closeSessionCmdBuild
        .createResponseParser(poCardResponse.getApduResponses().get(0))
        .checkStatus();

    // sets the flag indicating that the commands have been executed
    poCommandManager.notifyCommandsProcessed();

    // session is now considered closed regardless the previous state or the result of the abort
    // session command sent to the PO.
    sessionState = SessionState.SESSION_CLOSED;
  }

  /**
   * Performs a PIN verification, in order to authenticate the card holder and/or unlock access to
   * certain PO files.
   *
   * <p>This command can be performed both in and out of a secure session.<br>
   * The PIN code can be transmitted in plain text or encrypted according to the parameter set in
   * PoSecuritySettings (by default the transmission is encrypted).<br>
   * If the execution is done out of session but an encrypted transmission is requested, then
   * PoTransaction must be constructed with {@link PoSecuritySettings}<br>
   * If PoTransaction is constructed without {@link PoSecuritySettings} the transmission in done in
   * plain.<br>
   * The PO channel is closed if prepareReleasePoChannel is called before this command.
   *
   * @param pin the PIN code value (4-byte long byte array)
   * @throws CalypsoPoTransactionException if a functional error occurs (including PO and SAM IO
   *     errors)
   * @throws CalypsoPoCommandException if a response from the PO was unexpected
   * @throws CalypsoPoPinException if the PIN presentation failed (the remaining attempt counter is
   *     update in Calypso). See {@link CalypsoPo#isPinBlocked} and {@link
   *     CalypsoPo#getPinAttemptRemaining} methods
   * @throws CalypsoPoTransactionIllegalStateException if the PIN feature is not available for this
   *     PO or if commands have been prepared before calling this process method.
   * @since 0.9
   */
  public final void processVerifyPin(byte[] pin) {
    Assert.getInstance()
        .notNull(pin, "pin")
        .isEqual(pin.length, CalypsoPoUtils.PIN_LENGTH, "PIN length");

    if (!calypsoPo.isPinFeatureAvailable()) {
      throw new CalypsoPoTransactionIllegalStateException("PIN is not available for this PO.");
    }

    if (poCommandManager.hasCommands()) {
      throw new CalypsoPoTransactionIllegalStateException(
          "No commands should have been prepared prior to a PIN submission.");
    }

    if (poSecuritySettings != null
        && PinTransmissionMode.ENCRYPTED.equals(poSecuritySettings.getPinTransmissionMode())) {
      poCommandManager.addRegularCommand(new PoGetChallengeCmdBuild(calypsoPo.getPoClass()));

      // transmit and receive data with the PO
      processAtomicPoCommands(poCommandManager.getPoCommandBuilders(), ChannelControl.KEEP_OPEN);

      // sets the flag indicating that the commands have been executed
      poCommandManager.notifyCommandsProcessed();

      // Get the encrypted PIN with the help of the SAM
      byte[] cipheredPin =
          samCommandProcessor.getCipheredPinData(CalypsoPoUtils.getPoChallenge(), pin, null);
      poCommandManager.addRegularCommand(
          new VerifyPinCmdBuild(
              calypsoPo.getPoClass(), PinTransmissionMode.ENCRYPTED, cipheredPin));
    } else {
      poCommandManager.addRegularCommand(
          new VerifyPinCmdBuild(calypsoPo.getPoClass(), PinTransmissionMode.PLAIN, pin));
    }

    // transmit and receive data with the PO
    processAtomicPoCommands(poCommandManager.getPoCommandBuilders(), channelControl);

    // sets the flag indicating that the commands have been executed
    poCommandManager.notifyCommandsProcessed();
  }

  /**
   * {@link #processVerifyPin(byte[])} variant with the PIN supplied as an ASCII string.
   *
   * <p>The provided String is converted into an array of bytes and processed with {@link
   * #processVerifyPin(byte[])}.
   *
   * <p>E.g. "1234" will be transmitted as { 0x31,032,0x33,0x34 }
   *
   * @param pin an ASCII string (4-character long)
   * @see #processVerifyPin(byte[])
   * @since 0.9
   */
  public final void processVerifyPin(String pin) {
    processVerifyPin(pin.getBytes());
  }

  private CardResponse safePoTransmit(CardRequest poCardRequest, ChannelControl channelControl) {
    try {
      return poReader.transmitCardRequest(poCardRequest, channelControl);
    } catch (KeypleReaderIOException e) {
      throw new CalypsoPoIOException("PO IO Exception while transmitting commands.", e);
    }
  }

  /**
   * Checks if a Secure Session is open, raises an exception if not
   *
   * @throws CalypsoPoTransactionIllegalStateException if no session is open
   */
  private void checkSessionIsOpen() {
    if (sessionState != SessionState.SESSION_OPEN) {
      throw new CalypsoPoTransactionIllegalStateException(
          "Bad session state. Current: "
              + sessionState
              + ", expected: "
              + SessionState.SESSION_OPEN);
    }
  }

  /**
   * Checks if a Secure Session is not open, raises an exception if not
   *
   * @throws CalypsoPoTransactionIllegalStateException if a session is open
   */
  private void checkSessionIsNotOpen() {
    if (sessionState == SessionState.SESSION_OPEN) {
      throw new CalypsoPoTransactionIllegalStateException(
          "Bad session state. Current: " + sessionState + ", expected: not open");
    }
  }

  /**
   * Checks if the number of responses matches the number of commands.<br>
   * Throw a {@link CalypsoDesynchronizedExchangesException} if not.
   *
   * @param commandsNumber the number of commands
   * @param responsesNumber the number of responses
   * @throws CalypsoDesynchronizedExchangesException if the test failed
   */
  private void checkCommandsResponsesSynchronization(int commandsNumber, int responsesNumber) {
    if (commandsNumber != responsesNumber) {
      throw new CalypsoDesynchronizedExchangesException(
          "The number of commands/responses does not match: cmd="
              + commandsNumber
              + ", resp="
              + responsesNumber);
    }
  }

  /**
   * Checks the provided command from the session buffer overflow management perspective<br>
   * A exception is raised if the session buffer is overflowed in ATOMIC modification mode.<br>
   * Returns false if the command does not affect the session buffer.<br>
   * Sets the overflow flag and the neededSessionBufferSpace value according to the characteristics
   * of the command in other cases.
   *
   * @param builder the command builder
   * @param overflow flag set to true if the command overflowed the buffer
   * @param neededSessionBufferSpace updated with the size of the buffer consumed by the command
   * @return true if the command modifies the content of the PO, false if not
   * @throws CalypsoAtomicTransactionException if the command overflows the buffer in ATOMIC
   *     modification mode
   */
  private boolean checkModifyingCommand(
      AbstractPoCommandBuilder<? extends AbstractPoResponseParser> builder,
      AtomicBoolean overflow,
      AtomicInteger neededSessionBufferSpace) {
    if (builder.isSessionBufferUsed()) {
      // This command affects the PO modifications buffer
      neededSessionBufferSpace.set(
          builder.getApduRequest().getBytes().length
              + SESSION_BUFFER_CMD_ADDITIONAL_COST
              - APDU_HEADER_LENGTH);
      if (isSessionBufferOverflowed(neededSessionBufferSpace.get())) {
        // raise an exception if in atomic mode
        if (poSecuritySettings.getSessionModificationMode()
            == SessionSetting.ModificationMode.ATOMIC) {
          throw new CalypsoAtomicTransactionException(
              "ATOMIC mode error! This command would overflow the PO modifications buffer: "
                  + builder.getName());
        }
        overflow.set(true);
      } else {
        overflow.set(false);
      }
      return true;
    } else return false;
  }

  /**
   * Checks whether the requirement for the modifications buffer of the command provided in argument
   * is compatible with the current usage level of the buffer.
   *
   * <p>If it is compatible, the requirement is subtracted from the current level and the method
   * returns false. If this is not the case, the method returns true and the current level is left
   * unchanged.
   *
   * @param sessionBufferSizeConsumed session buffer requirement
   * @return true or false
   */
  private boolean isSessionBufferOverflowed(int sessionBufferSizeConsumed) {
    boolean isSessionBufferFull = false;
    if (calypsoPo.isModificationsCounterInBytes()) {
      if (modificationsCounter - sessionBufferSizeConsumed >= 0) {
        modificationsCounter = modificationsCounter - sessionBufferSizeConsumed;
      } else {
        if (logger.isDebugEnabled()) {
          logger.debug(
              "Modifications buffer overflow! BYTESMODE, CURRENTCOUNTER = {}, REQUIREMENT = {}",
              modificationsCounter,
              sessionBufferSizeConsumed);
        }
        isSessionBufferFull = true;
      }
    } else {
      if (modificationsCounter > 0) {
        modificationsCounter--;
      } else {
        if (logger.isDebugEnabled()) {
          logger.debug(
              "Modifications buffer overflow! COMMANDSMODE, CURRENTCOUNTER = {}, REQUIREMENT = {}",
              modificationsCounter,
              1);
        }
        isSessionBufferFull = true;
      }
    }
    return isSessionBufferFull;
  }

  /** Initialized the modifications buffer counter to its maximum value for the current PO */
  private void resetModificationsBufferCounter() {
    if (logger.isTraceEnabled()) {
      logger.trace(
          "Modifications buffer counter reset: PREVIOUSVALUE = {}, NEWVALUE = {}",
          modificationsCounter,
          calypsoPo.getModificationsCounter());
    }
    modificationsCounter = calypsoPo.getModificationsCounter();
  }

  /**
   * Requests the closing of the PO channel.
   *
   * <p>If this command is called before a "process" command (except for processOpening) then the
   * last transmission to the PO will be associated with the indication CLOSE_AFTER in order to
   * close the PO channel.<br>
   * Important: this command must imperatively be called at the end of any transaction, whether it
   * ended normally or not.<br>
   * In case the transaction was interrupted (exception), an additional call to processPoCommands
   * must be made to effectively close the channel.
   *
   * @since 0.9
   */
  public final void prepareReleasePoChannel() {
    channelControl = ChannelControl.CLOSE_AFTER;
  }

  /**
   * Schedules the execution of a <b>Select File</b> command based on the file's LID.
   *
   * <p>Once this command is processed, the result is available in {@link CalypsoPo} through the
   * {@link CalypsoPo#getFileBySfi(byte)} and {@link ElementaryFile#getHeader()} methods.
   *
   * @param lid the LID of the EF to select
   * @since 0.9
   */
  public final void prepareSelectFile(byte[] lid) {
    // create the builder and add it to the list of commands
    poCommandManager.addRegularCommand(
        CalypsoPoUtils.prepareSelectFile(calypsoPo.getPoClass(), lid));
  }

  /**
   * Schedules the execution of a <b>Select File</b> command using a navigation control defined by
   * the ISO standard.
   *
   * <p>Once this command is processed, the result is available in {@link CalypsoPo} through the
   * {@link CalypsoPo#getFileBySfi(byte)} and {@link ElementaryFile#getHeader()} methods.
   *
   * @param control A {@link SelectFileControl} enum entry
   * @since 0.9
   */
  public final void prepareSelectFile(SelectFileControl control) {
    // create the builder and add it to the list of commands
    poCommandManager.addRegularCommand(
        CalypsoPoUtils.prepareSelectFile(calypsoPo.getPoClass(), control));
  }

  /**
   * Schedules the execution of a <b>Read Records</b> command to read a single record from the
   * indicated EF.
   *
   * <p>Once this command is processed, the result is available in {@link CalypsoPo}.<br>
   * See the method {@link CalypsoPo#getFileBySfi(byte)}, the objects {@link ElementaryFile}, {@link
   * FileData} and their specialized methods according to the type of expected data: e.g. {@link
   * FileData#getContent(int)}.
   *
   * @param sfi the SFI of the EF to read
   * @param recordNumber the record number to read
   * @throws IllegalArgumentException if one of the provided argument is out of range
   * @since 0.9
   */
  public final void prepareReadRecordFile(byte sfi, int recordNumber) {
    // create the builder and add it to the list of commands
    poCommandManager.addRegularCommand(
        CalypsoPoUtils.prepareReadRecordFile(calypsoPo.getPoClass(), sfi, recordNumber));
  }

  /**
   * Schedules the execution of a <b>Read Records</b> command to read one or more records from the
   * indicated EF.
   *
   * <p>Once this command is processed, the result is available in {@link CalypsoPo}.<br>
   * See the method {@link CalypsoPo#getFileBySfi(byte)}, the objects {@link ElementaryFile}, {@link
   * FileData} and their specialized methods according to the type of expected data: e.g. {@link
   * FileData#getContent()}.
   *
   * @param sfi the SFI of the EF
   * @param firstRecordNumber the record number to read (or first record to read in case of several
   *     records)
   * @param numberOfRecords the number of records expected
   * @param recordSize the record length
   * @throws IllegalArgumentException if one of the provided argument is out of range
   * @since 0.9
   */
  public final void prepareReadRecordFile(
      byte sfi, int firstRecordNumber, int numberOfRecords, int recordSize) {

    Assert.getInstance() //
        .isInRange((int) sfi, CalypsoPoUtils.SFI_MIN, CalypsoPoUtils.SFI_MAX, "sfi") //
        .isInRange(
            firstRecordNumber,
            CalypsoPoUtils.NB_REC_MIN,
            CalypsoPoUtils.NB_REC_MAX,
            "firstRecordNumber") //
        .isInRange(
            numberOfRecords,
            CalypsoPoUtils.NB_REC_MIN,
            CalypsoPoUtils.NB_REC_MAX - firstRecordNumber,
            "numberOfRecords");

    if (numberOfRecords == 1) {
      // create the builder and add it to the list of commands
      poCommandManager.addRegularCommand(
          new ReadRecordsCmdBuild(
              calypsoPo.getPoClass(),
              sfi,
              firstRecordNumber,
              ReadRecordsCmdBuild.ReadMode.ONE_RECORD,
              recordSize));
    } else {
      // Manages the reading of multiple records taking into account the transmission capacity
      // of the PO and the response format (2 extra bytes)
      // Multiple APDUs can be generated depending on record size and transmission capacity.
      int recordsPerApdu = calypsoPo.getPayloadCapacity() / (recordSize + 2);
      int maxSizeDataPerApdu = recordsPerApdu * (recordSize + 2);
      int remainingRecords = numberOfRecords;
      int startRecordNumber = firstRecordNumber;
      while (remainingRecords > 0) {
        int expectedLength;
        if (remainingRecords > recordsPerApdu) {
          expectedLength = maxSizeDataPerApdu;
          remainingRecords = remainingRecords - recordsPerApdu;
          startRecordNumber = startRecordNumber + recordsPerApdu;
        } else {
          expectedLength = remainingRecords * (recordSize + 2);
          remainingRecords = 0;
        }
        // create the builder and add it to the list of commands
        poCommandManager.addRegularCommand(
            new ReadRecordsCmdBuild(
                calypsoPo.getPoClass(),
                sfi,
                startRecordNumber,
                ReadRecordsCmdBuild.ReadMode.MULTIPLE_RECORD,
                expectedLength));
      }
    }
  }

  /**
   * Schedules the execution of a <b>Read Records</b> command to reads a record of the indicated EF,
   * which should be a counter file.
   *
   * <p>The record will be read up to the counter location indicated in parameter.<br>
   * Thus all previous counters will also be read.
   *
   * <p>Once this command is processed, the result is available in {@link CalypsoPo}.<br>
   * See the method {@link CalypsoPo#getFileBySfi(byte)}, the objects {@link ElementaryFile}, {@link
   * FileData} and their specialized methods according to the type of expected data: e.g. {@link
   * FileData#getAllCountersValue()} (int)}.
   *
   * @param sfi the SFI of the EF
   * @param countersNumber the number of the last counter to be read
   * @throws IllegalArgumentException if one of the provided argument is out of range
   * @since 0.9
   */
  public final void prepareReadCounterFile(byte sfi, int countersNumber) {
    prepareReadRecordFile(sfi, 1, 1, countersNumber * 3);
  }

  /**
   * Schedules the execution of a <b>Append Record</b> command to adds the data provided in the
   * indicated cyclic file.
   *
   * <p>A new record is added, the oldest record is deleted.
   *
   * <p>Note: {@link CalypsoPo} is filled with the provided input data.
   *
   * @param sfi the sfi to select
   * @param recordData the new record data to write
   * @throws IllegalArgumentException if the command is inconsistent
   * @since 0.9
   */
  public final void prepareAppendRecord(byte sfi, byte[] recordData) {
    Assert.getInstance() //
        .isInRange((int) sfi, CalypsoPoUtils.SFI_MIN, CalypsoPoUtils.SFI_MAX, "sfi");

    // create the builder and add it to the list of commands
    poCommandManager.addRegularCommand(
        new AppendRecordCmdBuild(calypsoPo.getPoClass(), sfi, recordData));
  }

  /**
   * Schedules the execution of a <b>Update Record</b> command to overwrites the target file's
   * record contents with the provided data.
   *
   * <p>If the input data is shorter than the record size, only the first bytes will be overwritten.
   *
   * <p>Note: {@link CalypsoPo} is filled with the provided input data.
   *
   * @param sfi the sfi to select
   * @param recordNumber the record number to update
   * @param recordData the new record data. If length {@code <} RecSize, bytes beyond length are
   *     left unchanged.
   * @throws IllegalArgumentException if record number is {@code <} 1
   * @throws IllegalArgumentException if the request is inconsistent
   * @since 0.9
   */
  public final void prepareUpdateRecord(byte sfi, int recordNumber, byte[] recordData) {
    Assert.getInstance() //
        .isInRange((int) sfi, CalypsoPoUtils.SFI_MIN, CalypsoPoUtils.SFI_MAX, "sfi") //
        .isInRange(
            recordNumber, CalypsoPoUtils.NB_REC_MIN, CalypsoPoUtils.NB_REC_MAX, "recordNumber");

    // create the builder and add it to the list of commands
    poCommandManager.addRegularCommand(
        new UpdateRecordCmdBuild(calypsoPo.getPoClass(), sfi, recordNumber, recordData));
  }

  /**
   * Schedules the execution of a <b>Write Record</b> command to updates the target file's record
   * contents with the result of a binary OR between the existing data and the provided data.
   *
   * <p>If the input data is shorter than the record size, only the first bytes will be overwritten.
   *
   * <p>Note: {@link CalypsoPo} is filled with the provided input data.
   *
   * @param sfi the sfi to select
   * @param recordNumber the record number to write
   * @param recordData the data to overwrite in the record. If length {@code <} RecSize, bytes
   *     beyond length are left unchanged.
   * @throws IllegalArgumentException if record number is {@code <} 1
   * @throws IllegalArgumentException if the request is inconsistent
   * @since 0.9
   */
  public final void prepareWriteRecord(byte sfi, int recordNumber, byte[] recordData) {
    Assert.getInstance() //
        .isInRange((int) sfi, CalypsoPoUtils.SFI_MIN, CalypsoPoUtils.SFI_MAX, "sfi") //
        .isInRange(
            recordNumber, CalypsoPoUtils.NB_REC_MIN, CalypsoPoUtils.NB_REC_MAX, "recordNumber");

    // create the builder and add it to the list of commands
    poCommandManager.addRegularCommand(
        new WriteRecordCmdBuild(calypsoPo.getPoClass(), sfi, recordNumber, recordData));
  }

  /**
   * Schedules the execution of a <b>Increase command</b> command to increase the target counter.
   *
   * <p>Note: {@link CalypsoPo} is filled with the provided input data.
   *
   * @param counterNumber {@code >=} 01h: Counters file, number of the counter. 00h: Simulated
   *     Counter file.
   * @param sfi SFI of the file to select or 00h for current EF
   * @param incValue Value to add to the counter (defined as a positive int {@code <=} 16777215
   *     [FFFFFFh])
   * @throws IllegalArgumentException if the decrement value is out of range
   * @throws IllegalArgumentException if the command is inconsistent
   * @since 0.9
   */
  public final void prepareIncreaseCounter(byte sfi, int counterNumber, int incValue) {
    Assert.getInstance() //
        .isInRange((int) sfi, CalypsoPoUtils.SFI_MIN, CalypsoPoUtils.SFI_MAX, "sfi") //
        .isInRange(
            counterNumber,
            CalypsoPoUtils.NB_CNT_MIN,
            CalypsoPoUtils.NB_CNT_MAX,
            "counterNumber") //
        .isInRange(
            incValue, CalypsoPoUtils.CNT_VALUE_MIN, CalypsoPoUtils.CNT_VALUE_MAX, "incValue");

    // create the builder and add it to the list of commands
    poCommandManager.addRegularCommand(
        new IncreaseCmdBuild(calypsoPo.getPoClass(), sfi, counterNumber, incValue));
  }

  /**
   * Schedules the execution of a <b>Decrease command</b> command to decrease the target counter.
   *
   * <p>Note: {@link CalypsoPo} is filled with the provided input data.
   *
   * @param counterNumber {@code >=} 01h: Counters file, number of the counter. 00h: Simulated
   *     Counter file.
   * @param sfi SFI of the file to select or 00h for current EF
   * @param decValue Value to subtract to the counter (defined as a positive int {@code <=} 16777215
   *     [FFFFFFh])
   * @throws IllegalArgumentException if the decrement value is out of range
   * @throws IllegalArgumentException if the command is inconsistent
   * @since 0.9
   */
  public final void prepareDecreaseCounter(byte sfi, int counterNumber, int decValue) {
    Assert.getInstance() //
        .isInRange((int) sfi, CalypsoPoUtils.SFI_MIN, CalypsoPoUtils.SFI_MAX, "sfi") //
        .isInRange(
            counterNumber,
            CalypsoPoUtils.NB_CNT_MIN,
            CalypsoPoUtils.NB_CNT_MAX,
            "counterNumber") //
        .isInRange(
            decValue, CalypsoPoUtils.CNT_VALUE_MIN, CalypsoPoUtils.CNT_VALUE_MAX, "decValue");

    // create the builder and add it to the list of commands
    poCommandManager.addRegularCommand(
        new DecreaseCmdBuild(calypsoPo.getPoClass(), sfi, counterNumber, decValue));
  }

  /**
   * Schedules the execution of a command to set the value of the target counter.
   *
   * <p>It builds an Increase or Decrease command and add it to the list of commands to be sent with
   * the next <b>process</b> command in order to set the target counter to the specified value.<br>
   * The operation (Increase or Decrease) is selected according to whether the difference between
   * the current value and the desired value is negative (Increase) or positive (Decrease).
   *
   * <p>Note: it is assumed here that:<br>
   *
   * <ul>
   *   <li>the counter value has been read before,
   *   <li>the type of session (and associated access rights) is consistent with the requested
   *       operation.
   * </ul>
   *
   * (reload session if the counter is incremented, debit if it is decremented). No control is
   * performed on this point by this method; it is the closing of the session that will determine
   * the success of the operation..
   *
   * @param counterNumber {@code >=} 01h: Counters file, number of the counter. 00h: Simulated
   *     Counter file.
   * @param sfi SFI of the file to select or 00h for current EF
   * @param newValue the desired value for the counter (defined as a positive int {@code <=}
   *     16777215 [FFFFFFh])
   * @throws IllegalArgumentException if the desired value is out of range or if the command is
   *     inconsistent
   * @throws CalypsoPoTransactionIllegalStateException if the current counter value is unknown.
   * @since 0.9
   */
  public final void prepareSetCounter(byte sfi, int counterNumber, int newValue) {
    int delta = 0;
    try {
      delta =
          newValue - calypsoPo.getFileBySfi(sfi).getData().getContentAsCounterValue(counterNumber);
    } catch (NoSuchElementException ex) {
      throw new CalypsoPoTransactionIllegalStateException(
          "The value for counter " + counterNumber + " in file " + sfi + " is not available");
    }
    if (delta > 0) {
      if (logger.isTraceEnabled()) {
        logger.trace(
            "Increment counter {} (file {}) from {} to {}",
            counterNumber,
            sfi,
            newValue - delta,
            newValue);
      }
      prepareIncreaseCounter(sfi, counterNumber, delta);
    } else if (delta < 0) {
      if (logger.isTraceEnabled()) {
        logger.trace(
            "Decrement counter {} (file {}) from {} to {}",
            counterNumber,
            sfi,
            newValue - delta,
            newValue);
      }
      prepareDecreaseCounter(sfi, counterNumber, -delta);
    } else {
      logger.info(
          "The counter {} (SFI {}) is already set to the desired value {}.",
          counterNumber,
          sfi,
          newValue);
    }
  }

  /**
   * Schedules the execution of a VerifyPin command without PIN presentation in order to get the
   * attempt counter.
   *
   * <p>The PIN status will made available in CalypsoPo after the execution of process command.<br>
   * Adds it to the list of commands to be sent with the next process command.
   *
   * <p>See {@link CalypsoPo#isPinBlocked} and {@link CalypsoPo#getPinAttemptRemaining} methods.
   *
   * @throws CalypsoPoTransactionIllegalStateException if the PIN feature is not available for this
   *     PO.
   * @since 0.9
   */
  public final void prepareCheckPinStatus() {
    if (!calypsoPo.isPinFeatureAvailable()) {
      throw new CalypsoPoTransactionIllegalStateException("PIN is not available for this PO.");
    }
    // create the builder and add it to the list of commands
    poCommandManager.addRegularCommand(new VerifyPinCmdBuild(calypsoPo.getPoClass()));
  }

  /**
   * Schedules the execution of a <b>SV Get</b> command to prepare an SV operation or simply
   * retrieves the current SV status.
   *
   * <p>Once this command is processed, the result is available in {@link CalypsoPo}.<br>
   * See the methods {@link CalypsoPo#getSvBalance()}, {@link CalypsoPo#getSvLoadLogRecord()} ()},
   * {@link CalypsoPo#getSvDebitLogLastRecord()}, {@link CalypsoPo#getSvDebitLogAllRecords()}.
   *
   * @param svOperation informs about the nature of the intended operation: debit or reload
   * @param svAction the type of action: DO a debit or a positive reload, UNDO an undebit or a
   *     negative reload
   * @throws CalypsoPoTransactionIllegalStateException if the SV feature is not available for this
   *     PO.
   * @since 0.9
   */
  public final void prepareSvGet(SvSettings.Operation svOperation, SvSettings.Action svAction) {
    if (!calypsoPo.isSvFeatureAvailable()) {
      throw new CalypsoPoTransactionIllegalStateException(
          "Stored Value is not available for this PO.");
    }
    if (SvSettings.LogRead.ALL.equals(poSecuritySettings.getSvGetLogReadMode())
        && (calypsoPo.getRevision() != PoRevision.REV3_2)) {
      // @see Calypso Layer ID 8.09/8.10 (200108): both reload and debit logs are requested
      // for a non rev3.2 PO add two SvGet commands (for RELOAD then for DEBIT).
      SvSettings.Operation operation1 =
          SvSettings.Operation.RELOAD.equals(svOperation)
              ? SvSettings.Operation.DEBIT
              : SvSettings.Operation.RELOAD;
      poCommandManager.addStoredValueCommand(
          new SvGetCmdBuild(calypsoPo.getPoClass(), calypsoPo.getRevision(), operation1),
          operation1);
    }
    poCommandManager.addStoredValueCommand(
        new SvGetCmdBuild(calypsoPo.getPoClass(), calypsoPo.getRevision(), svOperation),
        svOperation);
    this.svAction = svAction;
  }

  /**
   * Schedules the execution of a <b>SV Reload</b> command to increase the current SV balance and
   * using the provided additional data.
   *
   * <p>Note #1: a communication with the SAM is done here.
   *
   * <p>Note #2: the key used is the reload key.
   *
   * @param amount the value to be reloaded, positive or negative integer in the range
   *     -8388608..8388607
   * @param date 2-byte free value
   * @param time 2-byte free value
   * @param free 2-byte free value
   * @throws CalypsoPoTransactionIllegalStateException if the SV feature is not available for this
   *     PO.
   * @since 0.9
   */
  public final void prepareSvReload(int amount, byte[] date, byte[] time, byte[] free) {
    // create the initial builder with the application data
    SvReloadCmdBuild svReloadCmdBuild =
        new SvReloadCmdBuild(
            calypsoPo.getPoClass(),
            calypsoPo.getRevision(),
            amount,
            CalypsoPoUtils.getSvKvc(),
            date,
            time,
            free);

    // get the security data from the SAM
    byte[] svReloadComplementaryData =
        samCommandProcessor.getSvReloadComplementaryData(
            svReloadCmdBuild, CalypsoPoUtils.getSvGetHeader(), CalypsoPoUtils.getSvGetData());

    // finalize the SvReload command builder with the data provided by the SAM
    svReloadCmdBuild.finalizeBuilder(svReloadComplementaryData);

    // create and keep the PoCommand
    poCommandManager.addStoredValueCommand(svReloadCmdBuild, SvSettings.Operation.RELOAD);
  }

  /**
   * Schedules the execution of a <b>SV Reload</b> command to increase the current SV balance.
   *
   * <p>Note #1: the optional SV additional data are set to zero.
   *
   * <p>Note #2: a communication with the SAM is done here.
   *
   * <p>Note #3: the key used is the reload key.
   *
   * @param amount the value to be reloaded, positive integer in the range 0..8388607 for a DO
   *     action, in the range 0..8388608 for an UNDO action.
   * @throws CalypsoPoTransactionIllegalStateException if the SV feature is not available for this
   *     PO.
   * @since 0.9
   */
  public final void prepareSvReload(int amount) {
    final byte[] zero = {0x00, 0x00};
    prepareSvReload(amount, zero, zero, zero);
  }

  /**
   * Schedules the execution of a <b>SV Debit</b> command to decrease the current SV balance.
   *
   * <p>It consists in decreasing the current balance of the SV by a certain amount.
   *
   * <p>Note: the key used is the debit key
   *
   * @param amount the amount to be subtracted, positive integer in the range 0..32767
   * @param date 2-byte free value
   * @param time 2-byte free value
   */
  private void prepareSvDebitPriv(int amount, byte[] date, byte[] time) {

    if (SvSettings.NegativeBalance.FORBIDDEN.equals(poSecuritySettings.getSvNegativeBalance())
        && (calypsoPo.getSvBalance() - amount) < 0) {
      throw new CalypsoPoTransactionIllegalStateException("Negative balances not allowed.");
    }

    // create the initial builder with the application data
    SvDebitCmdBuild svDebitCmdBuild =
        new SvDebitCmdBuild(
            calypsoPo.getPoClass(),
            calypsoPo.getRevision(),
            amount,
            CalypsoPoUtils.getSvKvc(),
            date,
            time);

    // get the security data from the SAM
    byte[] svDebitComplementaryData =
        samCommandProcessor.getSvDebitComplementaryData(
            svDebitCmdBuild, CalypsoPoUtils.getSvGetHeader(), CalypsoPoUtils.getSvGetData());

    // finalize the SvDebit command builder with the data provided by the SAM
    svDebitCmdBuild.finalizeBuilder(svDebitComplementaryData);

    // create and keep the PoCommand
    poCommandManager.addStoredValueCommand(svDebitCmdBuild, SvSettings.Operation.DEBIT);
  }

  /**
   * Prepares an SV Undebit (partially or totally cancels the last SV debit command).
   *
   * <p>It consists in canceling a previous debit. <br>
   * Note: the key used is the debit key
   *
   * @param amount the amount to be subtracted, positive integer in the range 0..32767
   * @param date 2-byte free value
   * @param time 2-byte free value
   */
  private void prepareSvUndebitPriv(int amount, byte[] date, byte[] time) {

    // create the initial builder with the application data
    SvUndebitCmdBuild svUndebitCmdBuild =
        new SvUndebitCmdBuild(
            calypsoPo.getPoClass(),
            calypsoPo.getRevision(),
            amount,
            CalypsoPoUtils.getSvKvc(),
            date,
            time);

    // get the security data from the SAM
    byte[] svDebitComplementaryData =
        samCommandProcessor.getSvUndebitComplementaryData(
            svUndebitCmdBuild, CalypsoPoUtils.getSvGetHeader(), CalypsoPoUtils.getSvGetData());

    // finalize the SvUndebit command builder with the data provided by the SAM
    svUndebitCmdBuild.finalizeBuilder(svDebitComplementaryData);

    // create and keep the PoCommand
    poCommandManager.addStoredValueCommand(svUndebitCmdBuild, SvSettings.Operation.DEBIT);
  }

  /**
   * Schedules the execution of a <b>SV Debit</b> or <b>SV Undebit</b> command to increase the
   * current SV balance or to partially or totally cancels the last SV debit command and using the
   * provided additional data.
   *
   * <p>It consists in decreasing the current balance of the SV by a certain amount or canceling a
   * previous debit according to the type operation chosen in when invoking the previous SV Get
   * command.
   *
   * <p>Note #1: a communication with the SAM is done here.
   *
   * <p>Note #2: the key used is the reload key.
   *
   * @param amount the amount to be subtracted or added, positive integer in the range 0..32767 when
   *     subtracted and 0..32768 when added.
   * @param date 2-byte free value
   * @param time 2-byte free value
   * @since 0.9
   */
  public final void prepareSvDebit(int amount, byte[] date, byte[] time) {
    if (SvSettings.Action.DO.equals(svAction)) {
      prepareSvDebitPriv(amount, date, time);
    } else {
      prepareSvUndebitPriv(amount, date, time);
    }
  }

  /**
   * Schedules the execution of a <b>SV Debit</b> or <b>SV Undebit</b> command to increase the
   * current SV balance or to partially or totally cancels the last SV debit command.
   *
   * <p>It consists in decreasing the current balance of the SV by a certain amount or canceling a
   * previous debit.
   *
   * <p>Note #1: the optional SV additional data are set to zero.
   *
   * <p>Note #2: a communication with the SAM is done here.
   *
   * <p>Note #3: the key used is the reload key.The information fields such as date and time are set
   * to 0. The extraInfo field propagated in Logs are automatically generated with the type of
   * transaction and amount.<br>
   * Operations that would result in a negative balance are forbidden (SV Exception raised). <br>
   * Note: the key used is the debit key
   *
   * @param amount the amount to be subtracted or added, positive integer in the range 0..32767 when
   *     subtracted and 0..32768 when added.
   * @since 0.9
   */
  public final void prepareSvDebit(int amount) {
    final byte[] zero = {0x00, 0x00};
    prepareSvDebit(amount, zero, zero);
  }

  /**
   * Schedules the execution of <b>Read Records</b> commands to read all SV logs.
   *
   * <p>The SV transaction logs are contained in two files with fixed identifiers.<br>
   * The file whose SFI is 0x14 contains 1 record containing the unique reload log.<br>
   * The file whose SFI is 0x15 contains 3 records containing the last three debit logs.<br>
   * At the end of this reading operation, the data will be accessible in CalypsoPo in raw format
   * via the standard commands for accessing read files or in the form of dedicated objects (see
   * {@link CalypsoPo#getSvLoadLogRecord()} and {@link CalypsoPo#getSvDebitLogAllRecords()})
   *
   * <p>Once this command is processed, the result is available in {@link CalypsoPo}.<br>
   * See the methods {@link CalypsoPo#getSvBalance()}, {@link CalypsoPo#getSvLoadLogRecord()} ()},
   * {@link CalypsoPo#getSvDebitLogLastRecord()}, {@link CalypsoPo#getSvDebitLogAllRecords()}. *
   *
   * @since 0.9
   */
  public final void prepareSvReadAllLogs() {
    if (calypsoPo.getApplicationSubtype() != CalypsoPoUtils.STORED_VALUE_FILE_STRUCTURE_ID) {
      throw new CalypsoPoTransactionIllegalStateException(
          "The currently selected application is not an SV application.");
    }
    // reset SV data in CalypsoPo if any
    calypsoPo.setSvData(0, 0, null, null);
    prepareReadRecordFile(
        CalypsoPoUtils.SV_RELOAD_LOG_FILE_SFI, CalypsoPoUtils.SV_RELOAD_LOG_FILE_NB_REC);
    prepareReadRecordFile(
        CalypsoPoUtils.SV_DEBIT_LOG_FILE_SFI,
        1,
        CalypsoPoUtils.SV_DEBIT_LOG_FILE_NB_REC,
        CalypsoPoUtils.SV_LOG_FILE_REC_LENGTH);
  }

  /**
   * Schedules the execution of a <b>Invalidate</b> command.
   *
   * <p>This command is usually executed within a secure session with the SESSION_LVL_DEBIT key
   * (depends on the access rights given to this command in the file structure of the PO).
   *
   * @throws CalypsoPoTransactionIllegalStateException if the PO is already invalidated
   * @since 0.9
   */
  public final void prepareInvalidate() {
    if (calypsoPo.isDfInvalidated()) {
      throw new CalypsoPoTransactionIllegalStateException("This PO is already invalidated.");
    }
    poCommandManager.addRegularCommand(new InvalidateCmdBuild(calypsoPo.getPoClass()));
  }

  /**
   * Schedules the execution of a <b>Rehabilitate</b> command.
   *
   * <p>This command is usually executed within a secure session with the SESSION_LVL_PERSO key
   * (depends on the access rights given to this command in the file structure of the PO).
   *
   * @throws CalypsoPoTransactionIllegalStateException if the PO is not invalidated
   * @since 0.9
   */
  public final void prepareRehabilitate() {
    if (!calypsoPo.isDfInvalidated()) {
      throw new CalypsoPoTransactionIllegalStateException("This PO is not invalidated.");
    }
    poCommandManager.addRegularCommand(new RehabilitateCmdBuild(calypsoPo.getPoClass()));
  }
}
