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

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import org.eclipse.keyple.example.remote.transport.wspolling.WsPTransportDTO;
import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.plugin.remotese.transport.factory.ClientNode;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;
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
    private ClientNode.ConnectCallback connectCallback;

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
        this.poll = true;
        poll(nodeId);
    }

    private void poll(final String nodeId) {
        logger.debug("Polling from node {}", nodeId);
        final WsPRetrofitClientImpl thisClient = this;
        // if poll is activated
        if (this.poll) {
            Call<KeypleDto> call = getRetrofitClient(baseUrl).getPolling(nodeId);
            call.enqueue(new Callback<KeypleDto>() {
                @Override
                public void onResponse(Call<KeypleDto> call, Response<KeypleDto> response) {
                    if (thisClient.connectCallback != null) {
                        thisClient.connectCallback.onConnectSuccess();
                    }
                    int statusCode = response.code();

                    logger.trace("Polling for clientNodeId {} receive a httpResponse http code {}",
                            nodeId, statusCode);
                    if (statusCode == 200) {
                        processHttpResponseDTO(response);
                    } else {
                        // 204 : no response
                    }
                    poll(nodeId);// recursive call to restart polling
                }

                @Override
                public void onFailure(Call<KeypleDto> call, Throwable t) {
                    logger.trace("Receive exception : {} , {}", t.getMessage(), t.getClass());

                    // Log error here since request failed
                    if (t instanceof ConnectException) {
                        logger.error("Connection refused to server : {} , {}", t.getMessage(),
                                t.getCause());
                        thisClient.stopPollingWorker();
                        if (thisClient.connectCallback != null) {
                            thisClient.connectCallback.onConnectFailure();
                        }
                    } else if (t instanceof SocketTimeoutException) {
                        logger.trace("polling ends by timeout, keep polling, error : {}",
                                t.getMessage());
                        poll(nodeId);// recursive call to restart polling
                    } else {
                        logger.error("Unexpected error : {} , {}", t.getMessage(), t.getCause());
                        poll(nodeId);// recursive call to restart polling
                        if (thisClient.connectCallback != null) {
                            thisClient.connectCallback.onConnectFailure();
                        }
                    }
                }
            });
        } else {
            logger.warn("poll is not active, call startPollingWorker to activate again");
            // poll is not active, call startPollingWorker to activate again
        }
    }


    private void stopPollingWorker() {
        this.poll = false;
    }

    public Boolean isPolling() {
        return this.poll;
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
        logger.trace("Ws Client send DTO {}", KeypleDtoHelper.toJson(keypleDto));

        if (!KeypleDtoHelper.isNoResponse(transportDto.getKeypleDTO())) {

            Call<KeypleDto> call = getRetrofitClient(baseUrl).postDto(keypleDto);

            // post Keyple DTO
            call.enqueue(new Callback<KeypleDto>() {

                // process response
                @Override
                public void onResponse(Call<KeypleDto> call, Response<KeypleDto> response) {
                    int statusCode = response.code();
                    logger.trace("Receive response from sendDto {} {}", nodeId, statusCode);
                    processHttpResponseDTO(response);
                }

                // process failure
                @Override
                public void onFailure(Call<KeypleDto> call, Throwable t) {
                    // Log error here since request failed
                    logger.trace("Receive failure from sendDto {}", t.getCause());
                    // startPollingWorker(nodeId);
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

    /*
     * TransportNode
     */
    @Override
    public void setDtoHandler(DtoHandler dtoHandler) {
        this.dtoHandler = dtoHandler;
    }


    @Override
    public void connect(ConnectCallback connectCallback) {
        this.connectCallback = connectCallback;
        this.startPollingWorker(nodeId);
    }

    @Override
    public void disconnect() {
        this.stopPollingWorker();
    }



    static WsPRetrofitClient getRetrofitClient(String baseUrl) {

        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS).writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS).build();

        Retrofit retrofit = new Retrofit.Builder().client(okHttpClient).baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create()).build();

        return retrofit.create(WsPRetrofitClient.class);
    }

}
