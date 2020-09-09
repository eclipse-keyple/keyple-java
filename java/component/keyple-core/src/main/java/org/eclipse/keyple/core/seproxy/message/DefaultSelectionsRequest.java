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
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;

/**
 * Class containing the Set of {@link SeRequest} used to make a default selection at the {@link
 * ObservableReader} level.<br>
 * It implements the {@link AbstractDefaultSelectionsRequest} class by providing it with a public
 * constructor.
 */
public final class DefaultSelectionsRequest extends AbstractDefaultSelectionsRequest {
  public DefaultSelectionsRequest(
      List<SeRequest> selectionSeRequests,
      MultiSeRequestProcessing multiSeRequestProcessing,
      ChannelControl channelControl) {
    super(selectionSeRequests, multiSeRequestProcessing, channelControl);
  }
}
