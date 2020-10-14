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
import org.eclipse.keyple.core.seproxy.CardSelector;
import org.eclipse.keyple.core.seproxy.MultiSelectionProcessing;
import org.eclipse.keyple.core.seproxy.Reader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;

/**
 * A {@link ProxyReader} is an {@link Reader} with methods for communicating with SEs.
 *
 * <p>Exchanges are made using {@link CardRequest} which in return result in {@link CardResponse}.
 * <br>
 * The {@link CardRequest} optionally carries the card selection data and an APDU list.<br>
 * The {@link CardResponse} contains the result of the selection and the responses to the APDUs.
 *
 * <p>The {@link CardRequest} are transmitted individually ({@link #transmitSeRequest(CardRequest,
 * ChannelControl)} or by a list {@link #transmitSeRequests(List, MultiSelectionProcessing,
 * ChannelControl)} allowing applications to provide several selection patterns with various
 * options.
 *
 * <p>{@link #releaseChannel()} is used to control the closing of logical and physical channels
 * taking into account the observation mechanisms potentially in progress.
 *
 * @since 0.9
 */
public interface ProxyReader extends Reader {

  /**
   * Transmits the list of {@link CardRequest } and gets in return a list of {@link CardResponse}.
   *
   * <p>The actual processing of each {@link CardRequest} is similar to that performed by {@link
   * #transmitSeRequest(CardRequest, ChannelControl)} (see this method for further explanation of
   * how the process works).
   *
   * <p>If the multiSelectionProcessing parameter equals to {@link
   * MultiSelectionProcessing#FIRST_MATCH}, the iteration over the {@link CardRequest} list is
   * interrupted at the first processing that leads to an open logical channel state. In this case,
   * the list of {@link CardResponse} may be shorter than the list of SeRequests provided as input.
   *
   * <p>If it equals to {@link MultiSelectionProcessing#PROCESS_ALL}, all the {@link CardRequest}
   * are processed and the logical channel is closed after each process.<br>
   * The physical channel is managed by the ChannelControl parameter as in {@link
   * #transmitSeRequest(CardRequest, ChannelControl)}.
   *
   * <p>In the case of a selection specifying a card protocol, it is imperative to activate it
   * previously with the method {@link Reader#activateProtocol(String, String)}. An
   * IllegalStateException exception will be thrown in case of inconsistency.
   *
   * @param cardRequests A not empty CardRequest list.
   * @param multiSelectionProcessing The multi card processing flag (must be not null).
   * @param channelControl indicates if the physical channel has to be closed at the end of the
   *     processing (must be not null).
   * @return A not null response list (can be empty).
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed
   * @throws IllegalArgumentException if one of the arguments is null.
   * @throws IllegalStateException in case of configuration inconsistency.
   * @since 0.9
   */
  List<CardResponse> transmitSeRequests(
      List<CardRequest> cardRequests,
      MultiSelectionProcessing multiSelectionProcessing,
      ChannelControl channelControl);

  /**
   * Transmits a single {@link CardRequest} passed as an argument and returns a {@link
   * CardResponse}.
   *
   * <p>The process includes the following steps:
   *
   * <ul>
   *   <li>Open the physical channel if it is not already open.
   *   <li>If the {@link CardRequest} contains a non null {@link CardSelector}. The 3 following
   *       operations are performed in this order:
   *       <ol>
   *         <li>If specified, check the card protocol (compare the specified protocol with the
   *             current protocol).
   *         <li>If specified, check the ATR (test the received ATR with the regular expression from
   *             the filter)
   *         <li>If specified, select the application by AID
   *       </ol>
   *       If one of the 3 operations fails, then an empty response containing the selection status
   *       is returned.<br>
   *       If all executed operations are successful then a selection status is created with the
   *       corresponding data (ATR and/or FCI) and the hasMatched flag true.
   *   <li>If the {@link CardRequest} contains a list of APDUs to send ({@link ApduRequest}) then
   *       each APDU is sent to the card and its response ({@link ApduResponse} is added to a new
   *       list.
   *   <li>Closes the physical channel if the {@link ChannelControl} is {@link
   *       ChannelControl#CLOSE_AFTER}.
   *   <li>Returns a {@link CardResponse} containing:
   *       <ul>
   *         <li>a boolean giving the current logical channel status.
   *         <li>a boolean giving the previous logical channel status.
   *         <li>if a selection has been made ({@link CardSelector } not null) a {@link
   *             SelectionStatus} object containing the elements resulting from the selection.
   *         <li>if APDUs have been transmitted to the card, the list of responses to these APDUs.
   *       </ul>
   * </ul>
   *
   * Note: in case of a communication error when sending an APDU an {@link KeypleReaderIOException}
   * exception is thrown. Responses to previous APDUs are attached to this exception.<br>
   * This allows the calling application to be tolerant to the card tearing and to recover a partial
   * response to the {@link CardRequest}.
   *
   * @param cardRequest The {@link CardRequest} to be processed (must be not null).
   * @return cardResponse A not null {@link CardResponse}.
   * @param channelControl indicates if the physical channel has to be closed at the end of the
   *     processing (must be not null).
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed
   * @throws IllegalArgumentException if one of the arguments is null.
   * @since 0.9
   */
  CardResponse transmitSeRequest(CardRequest cardRequest, ChannelControl channelControl);

  /**
   * Release the communication channel previously established with the card.
   *
   * <p>If the ProxyReader is not observable the logical and physical channels must be closed
   * instantly. <br>
   * If the ProxyReader is observable, the closure of both channels must be the result of the
   * completion of a removal sequence. TODO check how to make this conditional on the
   * WAIT_FOR_SE_PROCESSING state.
   *
   * @since 1.0
   */
  void releaseChannel();
}
