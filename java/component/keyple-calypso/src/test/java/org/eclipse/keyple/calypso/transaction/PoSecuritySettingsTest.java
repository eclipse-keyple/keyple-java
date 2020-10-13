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
package org.eclipse.keyple.calypso.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.keyple.calypso.transaction.PoTransaction.PinTransmissionMode.ENCRYPTED;
import static org.eclipse.keyple.calypso.transaction.PoTransaction.PinTransmissionMode.PLAIN;
import static org.eclipse.keyple.calypso.transaction.PoTransaction.SessionSetting.*;
import static org.eclipse.keyple.calypso.transaction.PoTransaction.SessionSetting.ModificationMode.ATOMIC;
import static org.eclipse.keyple.calypso.transaction.PoTransaction.SessionSetting.ModificationMode.MULTIPLE;
import static org.eclipse.keyple.calypso.transaction.PoTransaction.SvSettings.NegativeBalance.AUTHORIZED;
import static org.eclipse.keyple.calypso.transaction.PoTransaction.SvSettings.NegativeBalance.FORBIDDEN;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.calypso.KeyReference;
import org.eclipse.keyple.core.selection.SeResource;
import org.eclipse.keyple.core.seproxy.Reader;
import org.eclipse.keyple.core.seproxy.message.AnswerToReset;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.message.SelectionStatus;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;

public class PoSecuritySettingsTest {
  public static String ATR1 = "3B001122805A0180D002030411223344829000";
  // The default KIF values for personalization, loading and debiting
  final byte DEFAULT_KIF_PERSO = (byte) 0x21;
  final byte DEFAULT_KIF_LOAD = (byte) 0x27;
  final byte DEFAULT_KIF_DEBIT = (byte) 0x30;
  final byte DEFAULT_KVC_PERSO = (byte) 0x11;
  final byte DEFAULT_KVC_LOAD = (byte) 0x22;
  final byte DEFAULT_KVC_DEBIT = (byte) 0x33;
  // The default key record number values for personalization, loading and debiting
  // The actual value should be adjusted.
  final byte DEFAULT_KEY_RECORD_NUMBER_PERSO = (byte) 0x01;
  final byte DEFAULT_KEY_RECORD_NUMBER_LOAD = (byte) 0x02;
  final byte DEFAULT_KEY_RECORD_NUMBER_DEBIT = (byte) 0x03;

  @Test
  public void poSecuritySettings_nominal() {
    Reader samReader = null;
    CalypsoSam calypsoSam = createCalypsoSam();
    SeResource<CalypsoSam> samResource = new SeResource<CalypsoSam>(samReader, calypsoSam);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)
            .sessionDefaultKif(AccessLevel.SESSION_LVL_PERSO, DEFAULT_KIF_PERSO)
            .sessionDefaultKif(AccessLevel.SESSION_LVL_LOAD, DEFAULT_KIF_LOAD)
            .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT)
            .sessionDefaultKvc(AccessLevel.SESSION_LVL_PERSO, DEFAULT_KVC_PERSO)
            .sessionDefaultKvc(AccessLevel.SESSION_LVL_LOAD, DEFAULT_KVC_LOAD)
            .sessionDefaultKvc(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KVC_DEBIT)
            .sessionDefaultKeyRecordNumber(
                AccessLevel.SESSION_LVL_PERSO, DEFAULT_KEY_RECORD_NUMBER_PERSO)
            .sessionDefaultKeyRecordNumber(
                AccessLevel.SESSION_LVL_LOAD, DEFAULT_KEY_RECORD_NUMBER_LOAD)
            .sessionDefaultKeyRecordNumber(
                AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KEY_RECORD_NUMBER_DEBIT)
            .build();
    assertThat(poSecuritySettings.getSessionDefaultKif(AccessLevel.SESSION_LVL_PERSO))
        .isEqualTo(DEFAULT_KIF_PERSO);
    assertThat(poSecuritySettings.getSessionDefaultKif(AccessLevel.SESSION_LVL_LOAD))
        .isEqualTo(DEFAULT_KIF_LOAD);
    assertThat(poSecuritySettings.getSessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT))
        .isEqualTo(DEFAULT_KIF_DEBIT);
    assertThat(poSecuritySettings.getSessionDefaultKvc(AccessLevel.SESSION_LVL_PERSO))
        .isEqualTo(DEFAULT_KVC_PERSO);
    assertThat(poSecuritySettings.getSessionDefaultKvc(AccessLevel.SESSION_LVL_LOAD))
        .isEqualTo(DEFAULT_KVC_LOAD);
    assertThat(poSecuritySettings.getSessionDefaultKvc(AccessLevel.SESSION_LVL_DEBIT))
        .isEqualTo(DEFAULT_KVC_DEBIT);
    assertThat(poSecuritySettings.getSessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_PERSO))
        .isEqualTo(DEFAULT_KEY_RECORD_NUMBER_PERSO);
    assertThat(poSecuritySettings.getSessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_LOAD))
        .isEqualTo(DEFAULT_KEY_RECORD_NUMBER_LOAD);
    assertThat(poSecuritySettings.getSessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_DEBIT))
        .isEqualTo(DEFAULT_KEY_RECORD_NUMBER_DEBIT);
    assertThat(poSecuritySettings.getSamResource()).isEqualTo(samResource);
  }

  @Test
  public void poSecuritySettings_modificationMode_default() {
    Reader samReader = null;
    CalypsoSam calypsoSam = createCalypsoSam();
    SeResource<CalypsoSam> samResource = new SeResource<CalypsoSam>(samReader, calypsoSam);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource).build();
    assertThat(poSecuritySettings.getSessionModificationMode()).isEqualTo(ATOMIC);
  }

  @Test
  public void poSecuritySettings_modificationMode_Atomic() {
    Reader samReader = null;
    CalypsoSam calypsoSam = createCalypsoSam();
    SeResource<CalypsoSam> samResource = new SeResource<CalypsoSam>(samReader, calypsoSam);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)
            .sessionModificationMode(ATOMIC)
            .build();
    assertThat(poSecuritySettings.getSessionModificationMode()).isEqualTo(ATOMIC);
  }

  @Test
  public void poSecuritySettings_modificationMode_Multiple() {
    Reader samReader = null;
    CalypsoSam calypsoSam = createCalypsoSam();
    SeResource<CalypsoSam> samResource = new SeResource<CalypsoSam>(samReader, calypsoSam);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)
            .sessionModificationMode(MULTIPLE)
            .build();
    assertThat(poSecuritySettings.getSessionModificationMode()).isEqualTo(MULTIPLE);
  }

  @Test
  public void poSecuritySettings_ratificationMode_default() {
    Reader samReader = null;
    CalypsoSam calypsoSam = createCalypsoSam();
    SeResource<CalypsoSam> samResource = new SeResource<CalypsoSam>(samReader, calypsoSam);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource).build();
    assertThat(poSecuritySettings.getRatificationMode()).isEqualTo(RatificationMode.CLOSE_RATIFIED);
  }

  @Test
  public void poSecuritySettings_ratificationMode_CloseRatified() {
    Reader samReader = null;
    CalypsoSam calypsoSam = createCalypsoSam();
    SeResource<CalypsoSam> samResource = new SeResource<CalypsoSam>(samReader, calypsoSam);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)
            .ratificationMode(RatificationMode.CLOSE_RATIFIED)
            .build();
    assertThat(poSecuritySettings.getRatificationMode()).isEqualTo(RatificationMode.CLOSE_RATIFIED);
  }

  @Test
  public void poSecuritySettings_ratificationMode_CloseNotRatified() {
    Reader samReader = null;
    CalypsoSam calypsoSam = createCalypsoSam();
    SeResource<CalypsoSam> samResource = new SeResource<CalypsoSam>(samReader, calypsoSam);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)
            .ratificationMode(RatificationMode.CLOSE_NOT_RATIFIED)
            .build();
    assertThat(poSecuritySettings.getRatificationMode())
        .isEqualTo(RatificationMode.CLOSE_NOT_RATIFIED);
  }

  @Test
  public void poSecuritySettings_pinTransmissionMode_default() {
    Reader samReader = null;
    CalypsoSam calypsoSam = createCalypsoSam();
    SeResource<CalypsoSam> samResource = new SeResource<CalypsoSam>(samReader, calypsoSam);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource).build();
    assertThat(poSecuritySettings.getPinTransmissionMode()).isEqualTo(ENCRYPTED);
  }

  @Test
  public void poSecuritySettings_pinTransmissionMode_plain() {
    Reader samReader = null;
    CalypsoSam calypsoSam = createCalypsoSam();
    SeResource<CalypsoSam> samResource = new SeResource<CalypsoSam>(samReader, calypsoSam);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)
            .pinTransmissionMode(PLAIN)
            .build();
    assertThat(poSecuritySettings.getPinTransmissionMode()).isEqualTo(PLAIN);
  }

  @Test
  public void poSecuritySettings_pinTransmissionMode_encrypted() {
    Reader samReader = null;
    CalypsoSam calypsoSam = createCalypsoSam();
    SeResource<CalypsoSam> samResource = new SeResource<CalypsoSam>(samReader, calypsoSam);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)
            .pinTransmissionMode(ENCRYPTED)
            .build();
    assertThat(poSecuritySettings.getPinTransmissionMode()).isEqualTo(ENCRYPTED);
  }

  @Test
  public void poSecuritySettings_authorizedKvcList() {
    Reader samReader = null;
    CalypsoSam calypsoSam = createCalypsoSam();
    SeResource<CalypsoSam> samResource = new SeResource<CalypsoSam>(samReader, calypsoSam);
    List<Byte> authorizedKvcs = new ArrayList<Byte>();
    authorizedKvcs.add((byte) 0x12);
    authorizedKvcs.add((byte) 0x34);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)
            .sessionAuthorizedKvcList(authorizedKvcs)
            .build();
    assertThat(poSecuritySettings.isSessionKvcAuthorized((byte) 0x12)).isTrue();
    assertThat(poSecuritySettings.isSessionKvcAuthorized((byte) 0x34)).isTrue();
    assertThat(poSecuritySettings.isSessionKvcAuthorized((byte) 0x56)).isFalse();
  }

  @Test
  public void poSecuritySettings_defaultPinCipheringKey() {
    Reader samReader = null;
    CalypsoSam calypsoSam = createCalypsoSam();
    SeResource<CalypsoSam> samResource = new SeResource<CalypsoSam>(samReader, calypsoSam);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)
            .pinCipheringKey((byte) 0x11, (byte) 0x22)
            .build();
    KeyReference keyReference = poSecuritySettings.getDefaultPinCipheringKey();
    assertThat(keyReference.getKif()).isEqualTo(((byte) 0x11));
    assertThat(keyReference.getKvc()).isEqualTo((byte) 0x22);
  }

  private CalypsoSam createCalypsoSam() {

    SelectionStatus selectionStatus =
        new SelectionStatus(new AnswerToReset(ByteArrayUtil.fromHex(ATR1)), null, true);
    return new CalypsoSam(new SeResponse(true, true, selectionStatus, null));
  }

  @Test
  public void poSecuritySettings_negativeSvBalance() {
    Reader samReader = null;
    CalypsoSam calypsoSam = createCalypsoSam();
    SeResource<CalypsoSam> samResource = new SeResource<CalypsoSam>(samReader, calypsoSam);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource).build();
    assertThat(poSecuritySettings.getSvNegativeBalance()).isEqualTo(FORBIDDEN);
    poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)
            .svNegativeBalance(AUTHORIZED)
            .build();
    assertThat(poSecuritySettings.getSvNegativeBalance()).isEqualTo(AUTHORIZED);
  }
}
