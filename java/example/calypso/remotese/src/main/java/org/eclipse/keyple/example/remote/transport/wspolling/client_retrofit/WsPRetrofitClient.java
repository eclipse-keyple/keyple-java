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
package org.eclipse.keyple.example.remote.transport.wspolling.client_retrofit;


import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

interface WsPRetrofitClient {

    @Headers({"Accept: application/json", "Content-Type: application/json; charset=UTF-8"})
    @GET("polling")
    Call<KeypleDto> getPolling(@Query("clientNodeId") String nodeId);

    @Headers({"Accept: application/json", "Content-Type: application/json; charset=UTF-8"})
    @POST("keypleDTO")
    Call<KeypleDto> postDto(@Body KeypleDto keypleDto);

}
