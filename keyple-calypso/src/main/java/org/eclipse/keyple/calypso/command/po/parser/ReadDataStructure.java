/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

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
