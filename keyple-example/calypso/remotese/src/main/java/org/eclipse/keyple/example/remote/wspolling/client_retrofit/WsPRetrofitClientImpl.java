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

import java.util.concurrent.TimeUnit;
import org.eclipse.keyple.example.remote.transport.ClientNode;
import org.eclipse.keyple.example.remote.wspolling.WsPTransportDTO;
import org.eclipse.keyple.plugin.remotese.transport.DtoHandler;
import org.eclipse.keyple.plugin.remotese.transport.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.TransportDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Rest client, polls server, based on client_retrofit and callbacks
 */
public class WsPRetrofitClientImpl implements ClientNode {


    private static final Logger logger = LoggerFactory.getLogger(WsPRetrofitClientImpl.class);

    final private String baseUrl;
    final private String nodeId;
    private Boolean poll;

    private DtoHandler dtoHandler;

    public WsPRetrofitClientImpl(String baseUrl, String nodeId) {
        this.baseUrl = baseUrl;
        this.nodeId = nodeId;
    }


    /**
     * recursive polling method based on client_retrofit callbacks
     * 
     * @param nodeId : terminal node Id (ie : androidDevice1)
     */
    private void startPollingWorker(final String nodeId) {

        logger.trace("Polling clientNodeId {}", nodeId);
        this.poll = true;
        poll(nodeId);
    }

    private void poll(final String nodeId) {
        // if poll is activated
        if (this.poll) {
            Call<KeypleDto> call = getRseAPIClient(baseUrl).getPolling(nodeId);
            call.enqueue(new Callback<KeypleDto>() {
                @Override
                public void onResponse(Call<KeypleDto> call, Response<KeypleDto> response) {
                    int statusCode = response.code();
                    logger.trace("Polling for clientNodeId {} receive a httpResponse http code {}",
                            nodeId, statusCode);
                    processHttpResponseDTO(response);
                    poll(nodeId);// recursive call to restart polling
                }

                @Override
                public void onFailure(Call<KeypleDto> call, Throwable t) {
                    // Log error here since request failed
                    logger.debug("polling ends, start it over, error : " + t.getMessage());
                    poll(nodeId);// recursive call to restart polling
                }
            });
        } else {
            logger.debug("poll is not active, call startPollingWorker to activate again");
            // poll is not active, call startPollingWorker to activate again
        }
    }


    private void stopPollingWorker() {
        this.poll = false;
    }


    private void processHttpResponseDTO(Response<KeypleDto> response) {

        KeypleDto responseDTO = response.body();

        if (!KeypleDtoHelper.isNoResponse(responseDTO)) {
            TransportDto transportDto = new WsPTransportDTO(responseDTO, this);
            // connection
            final TransportDto sendback = this.dtoHandler.onDTO(transportDto);

            // if sendBack is not a noresponse (can be a keyple request or keyple response)
            if (!KeypleDtoHelper.isNoResponse(sendback.getKeypleDTO())) {
                // send the keyple object in a new thread to avoid blocking the polling

                sendDTO(sendback);

            }
        }


    }


    @Override
    public void sendDTO(TransportDto transportDto) {
        KeypleDto keypleDto = transportDto.getKeypleDTO();
        logger.debug("Ws Client send DTO {}", KeypleDtoHelper.toJson(keypleDto));

        if (!KeypleDtoHelper.isNoResponse(transportDto.getKeypleDTO())) {

            Call<KeypleDto> call = getRseAPIClient(baseUrl).postDto(keypleDto);
            call.enqueue(new Callback<KeypleDto>() {
                @Override
                public void onResponse(Call<KeypleDto> call, Response<KeypleDto> response) {
                    int statusCode = response.code();
                    logger.trace("Polling for clientNodeId {} receive a httpResponse http code {}",
                            nodeId, statusCode);
                    processHttpResponseDTO(response);
                }

                @Override
                public void onFailure(Call<KeypleDto> call, Throwable t) {
                    // Log error here since request failed
                    logger.debug("polling ends, start it over" + t.getMessage());
                    startPollingWorker(nodeId);
                }
            });

        }
    }

    @Override
    public void sendDTO(KeypleDto message) {
        sendDTO(new WsPTransportDTO(message, null));
    }

    @Override
    public String getNodeId() {
        return this.nodeId;
    }

    @Override
    public void update(KeypleDto event) {
        this.sendDTO(event);
    }


    /*
     * TransportNode
     */
    @Override
    public void setDtoHandler(DtoHandler dtoHandler) {
        this.dtoHandler = dtoHandler;
    }


    @Override
    public void connect() {
        this.startPollingWorker(nodeId);
    }

    @Override
    public void disconnect() {
        this.stopPollingWorker();
    }



    static WsPRetrofitClient getRseAPIClient(String baseUrl) {

        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS).writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS).build();

        Retrofit retrofit = new Retrofit.Builder().client(okHttpClient).baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create()).build();

        return retrofit.create(WsPRetrofitClient.class);
    }

}
