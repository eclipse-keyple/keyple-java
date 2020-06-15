/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.remotese.virtualse;

import org.eclipse.keyple.core.seproxy.event.ObservableReader;

/**
 * <b>Remote SE Server Observable Reader</b> API.
 * <p>
 * This reader must be used in the use case of the <b>Remote SE Server Plugin</b>.
 * <p>
 * This reader behaves like an {@link ObservableReader} but exposes additional services inherited
 * from {@link RemoteSeServerReader}.
 *
 * @since 1.0
 */
public interface RemoteSeServerObservableReader extends RemoteSeServerReader, ObservableReader {
}
