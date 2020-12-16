/* **************************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.distributed;

import org.eclipse.keyple.distributed.impl.PoolLocalServiceServerFactory;

/**
 * API of the <b>Pool Local Service Server</b> associated to the <b>Pool Remote Plugin Client</b>.
 *
 * <p>This service must be started by the application installed on a <b>Server</b> having local
 * access to the smart card reader but wishes to delegate all or part of the ticketing processing to
 * a remote application :
 *
 * <ul>
 *   <li>To <b>start</b> the service, use the factory {@link PoolLocalServiceServerFactory}.
 *   <li>To <b>access</b> the service, there is no method because this service is only used
 *       internally by Keyple.
 *   <li>To <b>stop</b> the service, there is nothing special to do because the service is a
 *       standard java singleton instance.
 * </ul>
 *
 * @since 1.0
 */
public interface PoolLocalServiceServer {}
