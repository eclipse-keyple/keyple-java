/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.android.omapi;

import org.eclipse.keyple.core.seproxy.SeReader;

/**
 * Communicates with Android readers throught the Open Mobile API see org.simalliance.openmobileapi.Reader
 *
 * Instances of this class represent SE readers supported by this device. These readers can be physical devices
 * or virtual devices. They can be removable or not. They can contain one SE that can or cannot be
 * removed.
 */
public interface AndroidOmapiReader extends SeReader {
}
