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
package org.eclipse.keyple.calypso.command.po.parser;

/**
 * Indicates whether the current ReadRecords operation operates on:
 * <ul>
 * <li>Single data record (even P2 value)</li>
 * <li>Multiple data records (odd P2 value)</li>
 * <li>Single counter record (depending on PO structure)</li>
 * <li>Multiple counter records</li>
 * </ul>
 */
public enum ReadDataStructure {
    SINGLE_RECORD_DATA, MULTIPLE_RECORD_DATA, SINGLE_COUNTER, MULTIPLE_COUNTER
}
