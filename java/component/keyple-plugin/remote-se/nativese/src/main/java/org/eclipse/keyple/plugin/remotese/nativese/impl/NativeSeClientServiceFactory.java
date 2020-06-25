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


import org.eclipse.keyple.plugin.remotese.core.KeypleClientAsync;
import org.eclipse.keyple.plugin.remotese.core.KeypleClientSync;
import org.eclipse.keyple.plugin.remotese.nativese.NativeSeClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use this factory to create a NativeSeClientService
 *
 * @since 1.0
 */
public class NativeSeClientServiceFactory {

    private static final Logger logger = LoggerFactory.getLogger(NativeSeClientServiceFactory.class);

    /**
     * First step to create the service
     * 
     * @return next configuration step
     */
    NodeStep builder() {
        return new Step();
    }


    /*
     * implementation of builder step
     */
    public class Step implements NativeSeClientServiceFactory.BuilderStep,
            NativeSeClientServiceFactory.NodeStep, NativeSeClientServiceFactory.ReaderStep {

        KeypleClientAsync asyncClient;
        KeypleClientSync syncClient;
        Boolean withReaderObservation;

        @Override
        public NativeSeClientService getService() {

            NativeSeClientServiceImpl service =
                    NativeSeClientServiceImpl.createInstance(withReaderObservation);
            if (asyncClient != null) {
                logger.info("Create a new NativeSeClientServiceImpl with an async client and params withReaderObservation:{}",withReaderObservation);
                service.bindClientAsyncNode(asyncClient);
            }
            if (syncClient != null) {
                logger.info("Create a new NativeSeClientServiceImpl with a sync client and params withReaderObservation:{}",withReaderObservation);
                service.bindClientSyncNode(syncClient, null, null);// todo
            }
            return service;
        }

        @Override
        public ReaderStep withAsyncNode(KeypleClientAsync client) {
            this.asyncClient = client;
            return this;
        }

        @Override
        public ReaderStep withSyncNode(KeypleClientSync client) {
            this.syncClient = client;
            return this;
        }

        @Override
        public BuilderStep withReaderObservation() {
            this.withReaderObservation = true;
            return this;
        }

        @Override
        public BuilderStep withoutReaderObservation() {
            this.withReaderObservation = false;
            return this;
        }
    }


    public interface BuilderStep {
        /**
         * Build the service
         * 
         * @return
         */
        NativeSeClientService getService();
    }

    public interface NodeStep {
        /**
         * Configure the service with an async Client
         * 
         * @param asyncClient
         * @return next configuration step
         */
        ReaderStep withAsyncNode(KeypleClientAsync asyncClient);

        /**
         * Configure the service with a sync Client
         * 
         * @param syncClient
         * @return next configuration step
         */
        ReaderStep withSyncNode(KeypleClientSync syncClient);
    }

    public interface ReaderStep {
        /**
         * Configure the service to observe the local reader
         * 
         * @return next configuration step
         */
        BuilderStep withReaderObservation();

        /**
         * Configure the service without observation
         * 
         * @return next configuration step
         */
        BuilderStep withoutReaderObservation();
    }

}
