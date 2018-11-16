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
package org.eclipse.keyple.plugin.remotese.nativese;

import org.eclipse.keyple.plugin.remotese.transport.DtoSender;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.junit.Before;
import org.mockito.Mock;

// @RunWith(MockitoJUnitRunner.class)
public class NativeReaderServiceImplTest {

    @Mock
    SeProxyService seProxyService;

    @Mock
    DtoSender dtoSender;

    private NativeReaderServiceImpl nse;

    @Before
    public void Setup() {

        nse = new NativeReaderServiceImpl(dtoSender);

    }



}
