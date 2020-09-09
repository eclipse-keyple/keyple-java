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
package org.eclipse.keyple.plugin.android.omapi

/**
 * Se Service Factory provide a framework to implement the connection the SE using an OMAPI
 * interface.
 */
internal interface SeServiceFactory<T, V> {
    /**
     * Allow usage of connectToSe regardless of OMAPI package
     * @param callBack: Callback or Listener provided by OMAPI
     * @return reader: Object of type Reader of OMAPI
     */
    fun connectToSe(callBack: V): T
}
