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
package org.eclipse.keyple.calypso.transaction;

import static org.junit.Assert.*;
import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.calypso.transaction.sam.SamSelector;
import org.junit.Test;

public class SamSelectorTest {

    @Test
    public void test_AidSelectorNull() {
        SamSelector samSelector = new SamSelector(SamRevision.S1D, null, "S1D");
        assertNull(samSelector.getAidSelector());
    }

    @Test
    public void test_SamRevision_S1D() {
        SamSelector samSelector = new SamSelector(SamRevision.S1D, null, "S1D");
        assertEquals("3B(.{6}|.{10})805A..80D?20.{4}.{8}829000",
                samSelector.getAtrFilter().getAtrRegex());
    }

    @Test
    public void test_SamRevision_S1E() {
        SamSelector samSelector = new SamSelector(SamRevision.S1E, null, "S1E");
        assertEquals("3B(.{6}|.{10})805A..80E120.{4}.{8}829000",
                samSelector.getAtrFilter().getAtrRegex());
    }

    @Test
    public void test_SamRevision_C1() {
        SamSelector samSelector = new SamSelector(SamRevision.C1, null, "C1");
        assertEquals("3B(.{6}|.{10})805A..80C120.{4}.{8}829000",
                samSelector.getAtrFilter().getAtrRegex());
    }

    @Test
    public void test_SamRevision_ANY() {
        SamSelector samSelector = new SamSelector(SamRevision.AUTO, null, "Any");
        assertEquals(".*", samSelector.getAtrFilter().getAtrRegex());
    }

    @Test
    public void test_SamSerialNumber() {
        SamSelector samSelector = new SamSelector(SamRevision.C1, "11223344", "Any");
        assertEquals("3B(.{6}|.{10})805A..80C120.{4}11223344829000",
                samSelector.getAtrFilter().getAtrRegex());
    }

    @Test
    public void test_ExtraInfo() {
        SamSelector samSelector = new SamSelector(SamRevision.AUTO, null, "Any");
        assertEquals("Any", samSelector.getExtraInfo());
    }
}
