/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.core.seproxy;

/**
 * indicates the action to be operated on the physical channel at the end of the request
 * transmission
 * <ul>
 * <li>KEEP_OPEN: lefts the physical channel open</li>
 * <li>CLOSE_AND_CONTINUE: closes the physical channel and continues with SE detection</li>
 * <li>CLOSE_AND_STOP: closes the physical channel and stops the SE detection</li>
 * </ul>
 */
public enum ChannelState {
    KEEP_OPEN, CLOSE_AND_CONTINUE, CLOSE_AND_STOP
}
