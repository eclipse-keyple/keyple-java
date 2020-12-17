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
package org.eclipse.keyple.example.calypso.android.omapi.model

open class EventModel(val type: Int, val text: String) {
    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ACTION = 1
        const val TYPE_RESULT = 2
        const val TYPE_MULTICHOICE = 3
    }
}
