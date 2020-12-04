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
package org.eclipse.keyple.plugin.remote;

import org.eclipse.keyple.core.service.event.ObservableReader;

/**
 * API of the <b>Observable Remote Reader Server</b> provided by the <b>Remote Plugin Server</b>.
 *
 * <p>This reader behaves like an {@link ObservableReader} but exposes additional services inherited
 * from {@link RemoteReaderServer}.
 *
 * @since 1.0
 */
public interface ObservableRemoteReaderServer extends RemoteReaderServer, ObservableReader {}
