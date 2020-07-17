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
package org.eclipse.keyple.plugin.remotese.nativese.impl;

import static org.assertj.core.api.Assertions.assertThat;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DefaultSelectionTest extends BaseNativeSeTest {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSelectionTest.class);

    KeypleMessageDto requestDto;

    @Before
    public void setUp() {
        this.init();
        requestDto = getSetDefaultSelectionDto();
    }

    @Test
    public void setDefaultSelection() {
        KeypleMessageDto responseDto =
                new DefaultSelectionExecutor(observableProxyReader).execute(requestDto);
        assertMetaDataMatches(requestDto, responseDto);
        assertThat(responseDto.getAction())
                .isEqualTo(KeypleMessageDto.Action.SET_DEFAULT_SELECTION.name());
        assertThat(responseDto.getErrorCode()).isNull();
        assertThat(responseDto.getErrorMessage()).isNull();
    }

}
