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
package org.eclipse.keyple.core.seproxy.message;

import java.util.List;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;

/**
 * A {@link ProxyReader} is an {@link SeReader} with methods for communicating with SEs.
 *
 * <p>Exchanges are made using {@link SeRequest} which in return result in {@link SeResponse}.<br>
 * The {@link SeRequest} optionally carries SE selection data and an APDU list.<br>
 * The {@link SeResponse} contains the result of the selection and the responses to the APDUs.
 *
 * <p>The {@link SeRequest} are transmitted individually ({@link #transmitSeRequest(SeRequest,
 * ChannelControl)} or by a list {@link #transmitSeRequests(List, MultiSeRequestProcessing,
 * ChannelControl)} allowing applications to provide several selection patterns with various
 * options.
 *
 * <p>{@link #releaseChannel()} is used to control the closing of logical and physical channels
 * taking into account the observation mechanisms potentially in progress.
 *
 * @since 0.9
 */
public interface ProxyReader extends SeReader {

  /**
   * Transmits the list of {@link SeRequest } and gets in return a list of {@link SeResponse}.
   *
   * <p>The actual processing of each {@link SeRequest} is similar to that performed by {@link
   * #transmitSeRequest(SeRequest, ChannelControl)} (see this method for further explanation of how
   * the process works).<br>
   * If the multiSeRequestProcessing parameter equals to {@link
   * MultiSeRequestProcessing#FIRST_MATCH}, the iteration over the {@link SeRequest} list is
   * interrupted at the first processing that leads to an open logical channel state. In this case,
   * the list of {@link SeResponse} may be shorter than the list of SeRequests provided as input.
   * <br>
   * If it equals to {@link MultiSeRequestProcessing#PROCESS_ALL}, all the {@link SeRequest} are
   * processed and the logical channel is closed after each process.<br>
   * The physical channel is managed by the ChannelControl parameter as in {@link
   * #transmitSeRequest(SeRequest, ChannelControl)}.
   *
   * @param seRequests A not empty SeRequest list.
   * @param multiSeRequestProcessing The multi se processing flag (must be not null).
   * @param channelControl indicates if the physical channel has to be closed at the end of the
   *     processing (must be not null).
   * @return A not null response list (can be empty).
   * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
   * @throws IllegalArgumentException if one of the arguments is null.
   * @since 0.9
   */
  List<SeResponse> transmitSeRequests(
      List<SeRequest> seRequests,
      MultiSeRequestProcessing multiSeRequestProcessing,
      ChannelControl channelControl);

  /**
   * Transmits a single {@link SeRequest} passed as an argument and returns a {@link SeResponse}.
   *
   * <p>The process includes the following steps:
   *
   * <ul>
   *   <li>Open the physical channel if it is not already open.
   *   <li>If the {@link SeRequest} contains a non null {@link SeSelector}. The 3 following
   *       operations are performed in this order:
   *       <ol>
   *         <li>If specified, check SE protocol (compare the specified protocol with the current
   *             protocol).
   *         <li>If specified, check the ATR (test the received ATR with the regular expression from
   *             the filter)
   *         <li>If specified, select the application by AID
   *       </ol>
   *       If one of the 3 operations fails, then an empty response containing the selection status
   *       is returned.<br>
   *       If all executed operations are successful then a selection status is created with the
   *       corresponding data (ATR and/or FCI) and the hasMatched flag true.
   *   <li>If the {@link SeRequest} contains a list of APDUs to send ({@link ApduRequest}) then each
   *       APDU is sent to the SE and its response ({@link ApduResponse} is added to a new list.
   *   <li>Closes the physical channel if the {@link ChannelControl} is {@link
   *       ChannelControl#CLOSE_AFTER}.
   *   <li>Returns a {@link SeResponse} containing:
   *       <ul>
   *         <li>a boolean giving the current logical channel status.
   *         <li>a boolean giving the previous logical channel status.
   *         <li>if a selection has been made ({@link SeSelector } not null) a {@link
   *             SelectionStatus} object containing the elements resulting from the selection.
   *         <li>if APDUs have been transmitted to the SE, the list of responses to these APDUs.
   *       </ul>
   * </ul>
   *
   * Note: in case of a communication error when sending an APDU an {@link KeypleReaderIOException}
   * exception is thrown. Responses to previous APDUs are attached to this exception.<br>
   * This allows the calling application to be tolerant to SE tearing and to recover a partial
   * response to the {@link SeRequest}.
   *
   * @param seRequest The {@link SeRequest} to be processed (must be not null).
   * @return seResponse A not null {@link SeResponse}.
   * @param channelControl indicates if the physical channel has to be closed at the end of the
   *     processing (must be not null).
   * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
   * @throws IllegalArgumentException if one of the arguments is null.
   * @since 0.9
   */
  SeResponse transmitSeRequest(SeRequest seRequest, ChannelControl channelControl);

  /**
   * Release the communication channel previously established with the SE.
   *
   * <p>If the ProxyReader is not observable the logical and physical channels must be instantly.
   * <br>
   * If the ProxyReader is observable, the closure of both channels must be the result of the
   * completion of a removal sequence.
   *
   * @since 1.0
   */
  void releaseChannel();
}
