/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.transaction;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import org.eclipse.keyple.calypso.command.po.PoModificationCommand;
import org.eclipse.keyple.calypso.command.po.PoSendableInSession;
import org.eclipse.keyple.calypso.command.po.builder.*;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.SeResponse;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;


public class PoTransaction extends PoSecureSession {

    List<PoSendableInSession> poCommandBuilderList = new ArrayList<PoSendableInSession>();

    /**
     * Instantiates a new po plain secure session.
     * <ul>
     * <li>Logical channels with PO &amp; CSM could already be established or not.</li>
     * <li>A list of CSM parameters is provided as en EnumMap.</li>
     * </ul>
     *
     * @param poReader the PO reader
     * @param csmReader the SAM reader
     * @param csmSetting a list of CSM related parameters. In the case this parameter is null,
     *        default parameters are applied. The available setting keys are defined in
     *        {@link CsmSettings}
     * @param calypsoPO the CalypsoPO object obtained at the end of the selection step
     */
    public PoTransaction(ProxyReader poReader, ProxyReader csmReader,
            EnumMap<CsmSettings, Byte> csmSetting, CalypsoPO calypsoPO) {
        super(poReader, csmReader, csmSetting, calypsoPO);
    }

    /**
     * Open a Secure Session.
     * <ul>
     * <li>The PO must have been previously selected, so a logical channel with the PO application
     * must be already active.</li>
     * <li>The PO serial &amp; revision are identified from FCI data.</li>
     * <li>A first request is sent to the CSM session reader.
     * <ul>
     * <li>In case not logical channel is active with the CSM, a channel is open.</li>
     * <li>Then a Select Diversifier (with the PO serial) &amp; a Get Challenge are automatically
     * operated. The CSM challenge is recovered.</li>
     * </ul>
     * </li>
     * <li>The PO Open Session command is built according to the PO revision, the CSM challenge, the
     * keyIndex, and openingSfiToSelect / openingRecordNumberToRead.</li>
     * <li>Next the PO reader is requested:
     * <ul>
     * <li>for the current selected PO AID, with keepChannelOpen set at true,</li>
     * <li>and some PO Apdu Requests including at least the Open Session command and all prepared PO
     * command to operate inside the session.</li>
     * </ul>
     * </li>
     * <li>The session PO keyset reference is identified from the PO Open Session response, the PO
     * challenge is recovered too.</li>
     * <li>According to the PO responses of Open Session and the PO commands sent inside the
     * session, a "cache" of CSM commands is filled with the corresponding Digest Init &amp; Digest
     * Update commands.</li>
     * <li>Returns the corresponding PO SeResponse (for all commands prepared before calling this
     * method).</li>
     * </ul>
     *
     * @param modificationMode the modification mode: ATOMIC or MULTIPLE (see
     *        {@link ModificationMode})
     * @param accessLevel access level of the session (personalization, load or debit).
     * @param openingSfiToSelect SFI of the file to select (0 means no file to select)
     * @param openingRecordNumberToRead number of the record to read
     * @return SeResponse response to all executed commands including the self generated "Open
     *         Secure Session" command
     * @throws KeypleReaderException the IO reader exception
     */
    public SeResponse processOpening(ModificationMode modificationMode,
            SessionAccessLevel accessLevel, byte openingSfiToSelect, byte openingRecordNumberToRead)
            throws KeypleReaderException {
        SeResponse seResponse = processOpening(modificationMode, accessLevel, openingSfiToSelect,
                openingRecordNumberToRead, poCommandBuilderList);
        poCommandBuilderList.clear();
        return seResponse;
    }

    /**
     * Process all prepared PO commands in a Secure Session.
     * <ul>
     * <li>On the PO reader, generates a SeRequest for the current selected AID, with
     * keepChannelOpen set at true, and ApduRequests with the PO commands.</li>
     * <li>In case the secure session is active, the "cache" of CSM commands is completed with the
     * corresponding Digest Update commands.</li>
     * <li>Returns the corresponding PO SeResponse.</li>
     * </ul>
     *
     * @return SeResponse all responses to the provided commands
     *
     * @throws KeypleReaderException IO Reader exception
     */
    public SeResponse processPoCommands() throws KeypleReaderException {
        SeResponse seResponse = processPoCommands(poCommandBuilderList);
        poCommandBuilderList.clear();
        return seResponse;
    }

    /**
     * processClosing in which the list of prepared commands is sent
     *
     * The list of expected responses is determined from previous reading operations.
     *
     * @param communicationMode the communication mode. If the communication mode is
     *        CONTACTLESS_MODE, a ratification command will be generated and sent to the PO after
     *        the Close Session command; the ratification will not be requested in the Close Session
     *        command. On the contrary, if the communication mode is CONTACTS_MODE, no ratification
     *        command will be sent to the PO and ratification will be requested in the Close Session
     *        command
     * @param closeSeChannel if true the SE channel of the PO reader must be closed after the last
     *        command
     * @return SeResponse close session response
     * @throws KeypleReaderException the IO reader exception This method is deprecated.
     *         <ul>
     *         <li>The argument of the ratification command is replaced by an indication of the PO
     *         communication mode.</li>
     *         </ul>
     */
    public SeResponse processClosing(CommunicationMode communicationMode, boolean closeSeChannel)
            throws KeypleReaderException {
        List<PoModificationCommand> poModificationCommandList =
                new ArrayList<PoModificationCommand>();
        for (PoSendableInSession command : poCommandBuilderList) {
            poModificationCommandList.add((PoModificationCommand) command);
        }
        SeResponse seResponse =
                processClosing(poModificationCommandList, communicationMode, closeSeChannel);
        poCommandBuilderList.clear();
        return null;
    }

    /**
     * Build a ReadRecords command and add it to the list of commands to be sent with the next
     * process command
     * 
     * @param sfi the sfi top select
     * @param firstRecordNumber the record number to read (or first record to read in case of
     *        several records)
     * @param readJustOneRecord the read just one record
     * @param expectedLength the expected length of the record(s)
     * @param extraInfo extra information included in the logs (can be null or empty)
     * @throws java.lang.IllegalArgumentException - if record number &lt; 1
     * @throws java.lang.IllegalArgumentException - if the request is inconsistent
     */
    public void prepareReadRecordsCmd(byte sfi, byte firstRecordNumber, boolean readJustOneRecord,
            byte expectedLength, String extraInfo) {
        poCommandBuilderList.add(new ReadRecordsCmdBuild(calypsoPo.getRevision(), sfi,
                firstRecordNumber, readJustOneRecord, expectedLength, extraInfo));
    }

    /**
     * Build an AppendRecord command and add it to the list of commands to be sent with the next
     * process command
     *
     * @param sfi the sfi to select
     * @param newRecordData the new record data to write
     * @param extraInfo extra information included in the logs (can be null or empty)
     * @throws java.lang.IllegalArgumentException - if the command is inconsistent
     */
    public void prepareAppendRecordCmd(byte sfi, byte[] newRecordData, String extraInfo) {
        poCommandBuilderList.add(
                new AppendRecordCmdBuild(calypsoPo.getRevision(), sfi, newRecordData, extraInfo));
    }

    /**
     * Build an UpdateRecord command and add it to the list of commands to be sent with the next
     * process command
     *
     * @param sfi the sfi to select
     * @param recordNumber the record number to update
     * @param newRecordData the new record data to write
     * @param extraInfo extra information included in the logs (can be null or empty)
     * @throws java.lang.IllegalArgumentException - if record number is &lt; 1
     * @throws java.lang.IllegalArgumentException - if the request is inconsistent
     */
    public void prepareUpdateRecordCmd(byte sfi, byte recordNumber, byte[] newRecordData,
            String extraInfo) {
        poCommandBuilderList.add(new UpdateRecordCmdBuild(calypsoPo.getRevision(), sfi,
                recordNumber, newRecordData, extraInfo));
    }

    /**
     * Build a Increase command and add it to the list of commands to be sent with the next process
     * command
     *
     * @param counterNumber &gt;= 01h: Counters file, number of the counter. 00h: Simulated Counter
     *        file.
     * @param sfi SFI of the file to select or 00h for current EF
     * @param incValue Value to add to the counter (defined as a positive int &lt;= 16777215
     *        [FFFFFFh])
     * @param extraInfo extra information included in the logs (can be null or empty)
     * @throws java.lang.IllegalArgumentException - if the decrement value is out of range
     * @throws java.lang.IllegalArgumentException - if the command is inconsistent
     */
    public void prepareIncreaseCmd(byte sfi, byte counterNumber, int incValue, String extraInfo) {
        poCommandBuilderList.add(new IncreaseCmdBuild(calypsoPo.getRevision(), sfi, counterNumber,
                incValue, extraInfo));
    }

    /**
     * Build a Decrease command and add it to the list of commands to be sent with the next process
     * command
     *
     * @param counterNumber &gt;= 01h: Counters file, number of the counter. 00h: Simulated Counter
     *        file.
     * @param sfi SFI of the file to select or 00h for current EF
     * @param decValue Value to subtract to the counter (defined as a positive int &lt;= 16777215
     *        [FFFFFFh])
     * @param extraInfo extra information included in the logs (can be null or empty)
     * @throws java.lang.IllegalArgumentException - if the decrement value is out of range
     * @throws java.lang.IllegalArgumentException - if the command is inconsistent
     */
    public void prepareDecreaseCmd(byte sfi, byte counterNumber, int decValue, String extraInfo) {
        poCommandBuilderList.add(new DecreaseCmdBuild(calypsoPo.getRevision(), sfi, counterNumber,
                decValue, extraInfo));
    }
}
