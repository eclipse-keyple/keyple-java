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



public class TransactionSettingsTest {

    // @Test
    // public void setGetDefaultKif() {
    // TransactionSettings transactionSettings = new TransactionSettings();
    // Assert.assertEquals(transactionSettings
    // .getDefaultKif(PoTransaction.SessionAccessLevel.SESSION_LVL_PERSO), (byte) 0x21);
    // Assert.assertEquals(transactionSettings
    // .getDefaultKif(PoTransaction.SessionAccessLevel.SESSION_LVL_LOAD), (byte) 0x27);
    // Assert.assertEquals(transactionSettings
    // .getDefaultKif(PoTransaction.SessionAccessLevel.SESSION_LVL_DEBIT), (byte) 0x30);
    // transactionSettings.setDefaultKif(PoTransaction.SessionAccessLevel.SESSION_LVL_PERSO,
    // (byte) 0x22);
    // transactionSettings.setDefaultKif(PoTransaction.SessionAccessLevel.SESSION_LVL_LOAD,
    // (byte) 0x28);
    // transactionSettings.setDefaultKif(PoTransaction.SessionAccessLevel.SESSION_LVL_DEBIT,
    // (byte) 0x31);
    // Assert.assertEquals(transactionSettings
    // .getDefaultKif(PoTransaction.SessionAccessLevel.SESSION_LVL_PERSO), (byte) 0x22);
    // Assert.assertEquals(transactionSettings
    // .getDefaultKif(PoTransaction.SessionAccessLevel.SESSION_LVL_LOAD), (byte) 0x28);
    // Assert.assertEquals(transactionSettings
    // .getDefaultKif(PoTransaction.SessionAccessLevel.SESSION_LVL_DEBIT), (byte) 0x31);
    // }
    //
    // @Test
    // public void setGetDefaultKvc() {
    // TransactionSettings transactionSettings = new TransactionSettings();
    // Assert.assertEquals(transactionSettings
    // .getDefaultKvc(PoTransaction.SessionAccessLevel.SESSION_LVL_PERSO), (byte) 0x00);
    // Assert.assertEquals(transactionSettings
    // .getDefaultKvc(PoTransaction.SessionAccessLevel.SESSION_LVL_LOAD), (byte) 0x01);
    // Assert.assertEquals(transactionSettings
    // .getDefaultKvc(PoTransaction.SessionAccessLevel.SESSION_LVL_DEBIT), (byte) 0x02);
    // transactionSettings.setDefaultKvc(PoTransaction.SessionAccessLevel.SESSION_LVL_PERSO,
    // (byte) 0x10);
    // transactionSettings.setDefaultKvc(PoTransaction.SessionAccessLevel.SESSION_LVL_LOAD,
    // (byte) 0x11);
    // transactionSettings.setDefaultKvc(PoTransaction.SessionAccessLevel.SESSION_LVL_DEBIT,
    // (byte) 0x12);
    // Assert.assertEquals(transactionSettings
    // .getDefaultKvc(PoTransaction.SessionAccessLevel.SESSION_LVL_PERSO), (byte) 0x10);
    // Assert.assertEquals(transactionSettings
    // .getDefaultKvc(PoTransaction.SessionAccessLevel.SESSION_LVL_LOAD), (byte) 0x11);
    // Assert.assertEquals(transactionSettings
    // .getDefaultKvc(PoTransaction.SessionAccessLevel.SESSION_LVL_DEBIT), (byte) 0x12);
    // }
    //
    // @Test
    // public void setGetDefaultKeyRecordNumber() {
    // TransactionSettings transactionSettings = new TransactionSettings();
    // Assert.assertEquals(transactionSettings.getDefaultKeyRecordNumber(
    // PoTransaction.SessionAccessLevel.SESSION_LVL_PERSO), (byte) 0x00);
    // transactionSettings.setDefaultKeyRecordNumber(
    // PoTransaction.SessionAccessLevel.SESSION_LVL_PERSO, (byte) 0x10);
    // Assert.assertEquals(transactionSettings.getDefaultKeyRecordNumber(
    // PoTransaction.SessionAccessLevel.SESSION_LVL_PERSO), (byte) 0x10);
    // }
    //
    // @Test
    // public void setTestAuthorizedKifList() {
    // TransactionSettings transactionSettings = new TransactionSettings();
    // // empty list => always authorized
    // Assert.assertTrue(transactionSettings.isAuthorizedKif((byte) 0x10));
    // List<Byte> byteList = new ArrayList<Byte>();
    // byteList.add((byte) 0x15);
    // byteList.add((byte) 0x25);
    // byteList.add((byte) 0x35);
    // transactionSettings.setAuthorizedKifList(byteList);
    // Assert.assertFalse(transactionSettings.isAuthorizedKif((byte) 0x10));
    // Assert.assertTrue(transactionSettings.isAuthorizedKif((byte) 0x25));
    // }
    //
    // @Test
    // public void setTestAuthorizedKvcList() {
    // TransactionSettings transactionSettings = new TransactionSettings();
    // // empty list => always authorized
    // Assert.assertTrue(transactionSettings.isAuthorizedKvc((byte) 0x10));
    // List<Byte> byteList = new ArrayList<Byte>();
    // byteList.add((byte) 0x15);
    // byteList.add((byte) 0x25);
    // byteList.add((byte) 0x35);
    // transactionSettings.setAuthorizedKvcList(byteList);
    // Assert.assertFalse(transactionSettings.isAuthorizedKvc((byte) 0x10));
    // Assert.assertTrue(transactionSettings.isAuthorizedKvc((byte) 0x25));
    // }
    //
    // @Test
    // public void setGetPoSerialNumberFilter() {
    // TransactionSettings transactionSettings = new TransactionSettings();
    // Assert.assertNull(transactionSettings.getPoSerialNumberFilter());
    // transactionSettings.setPoSerialNumberFilter("30.*");
    // Assert.assertEquals(transactionSettings.getPoSerialNumberFilter(), "30.*");
    // }
    //
    // @Test
    // public void getSamResource() {
    // SamResource samResource = new SamResource(null, null);
    // TransactionSettings transactionSettings = new
    // TransactionSettings(CalypsoClassicInfo.calypsoRev31Ticketing,samResource);
    // Assert.assertEquals(transactionSettings.getSamResource(), samResource);
    // }
}
