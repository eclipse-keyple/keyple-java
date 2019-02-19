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
package org.eclipse.keyple.example.remote.wspolling.client_retrofit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import java.io.IOException;
import org.eclipse.keyple.plugin.remotese.transport.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.KeypleDtoHelper;
import org.junit.Before;
import retrofit2.Response;

public class VirtualReaderClientClientTest {

    final private String BASE_URL = "http://localhost:8081/";

    @Before
    public void seTup() {}

    /**
     * Polling should failed after a timeout is raised on server
     *
     */
    // @Test(expected = IOException.class)
    public void testPolling() throws IOException {
        WsPRetrofitClient rseClient = WsPRetrofitClientImpl.getRseAPIClient(BASE_URL);
        Response<KeypleDto> kdto = rseClient.getPolling("clientNodeId").execute();

    }

    /**
     * Send a valid READER_CONNECT dto
     */
    // @Test
    public void testPostDto() throws IOException {

        KeypleDto dtoConnect = new KeypleDto(KeypleDtoHelper.READER_CONNECT,
                "{nativeReaderName:test, clientNodeId:testnode1}", true);

        WsPRetrofitClient rseClient = WsPRetrofitClientImpl.getRseAPIClient(BASE_URL);
        Response<KeypleDto> resp = rseClient.postDto(dtoConnect).execute();


        assertEquals(200, resp.code());
        assertEquals(KeypleDtoHelper.READER_CONNECT, resp.body().getAction());
        assertFalse(resp.body().isRequest());

    }

}
