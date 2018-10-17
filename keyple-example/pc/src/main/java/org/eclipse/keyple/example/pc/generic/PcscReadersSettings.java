/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */
package org.eclipse.keyple.example.pc.generic;

public interface PcscReadersSettings {
    // This is where you should add patterns of readers you want to use for tests
    String PO_READER_NAME_REGEX = ".*(ASK|ACS).*";
    String SAM_READER_NAME_REGEX = ".*(Cherry TC|SCM Microsystems|Identive|HID|Generic).*";
}
