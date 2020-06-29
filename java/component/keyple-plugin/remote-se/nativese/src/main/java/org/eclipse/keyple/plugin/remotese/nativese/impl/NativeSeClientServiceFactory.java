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
import org.eclipse.keyple.plugin.remotese.core.impl.ServerPushEventStrategy;
import org.eclipse.keyple.plugin.remotese.nativese.NativeSeClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use this factory to create a NativeSeClientService
 *
 * @since 1.0
 */
public class NativeSeClientServiceFactory {

    private static final Logger logger =
            LoggerFactory.getLogger(NativeSeClientServiceFactory.class);

    /**
     * Init the builder
     * 
     * @return next configuration step
     */
    NodeStep builder() {
        return new Step();
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
        AsyncPluginStep withAsyncNode(KeypleClientAsync asyncClient);

        /**
         * Configure the service with a sync Client
         *
         * @param syncClient
         * @return next configuration step
         */
        SyncPluginStep withSyncNode(KeypleClientSync syncClient);
    }

    public interface AsyncPluginStep {
        /**
         * Configure the service to observe the local plugin
         *
         * @return next configuration step
         */
        AsyncReaderStep withPluginObservation();

        /**
         * Configure the service without observation
         *
         * @return next configuration step
         */
        AsyncReaderStep withoutPluginObservation();
    }

    public interface SyncPluginStep {
        /**
         * Configure the service to observe the local plugin
         * 
         * @param pluginObservationStrategy polling strategy for plugin observation (must not be
         *        null).
         * @return next configuration step
         */
        SyncReaderStep withPluginObservation(ServerPushEventStrategy pluginObservationStrategy);

        /**
         * Configure the service without observation
         *
         * @return next configuration step
         */
        SyncReaderStep withoutPluginObservation();
    }

    public interface AsyncReaderStep {
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

    public interface SyncReaderStep {
        /**
         * Configure the service to observe the local reader
         *
         * @param readerObservationStrategy polling strategy for reader observation (must not be
         *        null).
         * @return next configuration step
         */
        BuilderStep withReaderObservation(ServerPushEventStrategy readerObservationStrategy);

        /**
         * Configure the service without observation
         *
         * @return next configuration step
         */
        BuilderStep withoutReaderObservation();
    }

    public class Step implements NativeSeClientServiceFactory.NodeStep {

        protected KeypleClientAsync asyncClient;
        protected KeypleClientSync syncClient;

        @Override
        public AsyncPluginStep withAsyncNode(KeypleClientAsync asyncClient) {
            this.asyncClient = asyncClient;
            return new AsyncStep();
        }

        @Override
        public SyncPluginStep withSyncNode(KeypleClientSync syncClient) {
            this.syncClient = syncClient;
            return new SyncStep();
        }
    }



    /*
     * implementation of builder step
     */
    public class SyncStep extends Step implements NativeSeClientServiceFactory.BuilderStep,
            NativeSeClientServiceFactory.SyncReaderStep,
            NativeSeClientServiceFactory.SyncPluginStep {

        Boolean withReaderObservation;
        Boolean withPluginObservation;
        ServerPushEventStrategy pluginObservationStrategy;
        ServerPushEventStrategy readerObservationStrategy;

        @Override
        public NativeSeClientService getService() {
            // create the service
            NativeSeClientServiceImpl service = NativeSeClientServiceImpl
                    .createInstance(withPluginObservation, withReaderObservation);

            logger.info(
                    "Create a new NativeSeClientServiceImpl with a sync client and params "
                            + "withPluginObservation:{}, " + "pluginObservationStrategy:{}, "
                            + "withReaderObservation:{}, " + "readerObservationStrategy:{}.",
                    withPluginObservation,
                    pluginObservationStrategy != null ? readerObservationStrategy.getType()
                            : "null",
                    withReaderObservation,
                    readerObservationStrategy != null ? readerObservationStrategy.getType()
                            : "null");


            // bind the service to the node
            service.bindClientSyncNode(syncClient, pluginObservationStrategy,
                    readerObservationStrategy);

            return service;
        }

        @Override
        public BuilderStep withoutReaderObservation() {
            this.withReaderObservation = false;
            return this;
        }

        @Override
        public SyncReaderStep withPluginObservation(
                ServerPushEventStrategy pluginObservationStrategy) {
            if (pluginObservationStrategy == null) {
                throw new IllegalArgumentException("pluginObservationStrategy must be set");
            }
            this.withPluginObservation = true;
            this.pluginObservationStrategy = pluginObservationStrategy;
            return this;
        }

        @Override
        public SyncReaderStep withoutPluginObservation() {
            this.withPluginObservation = false;
            return this;
        }

        @Override
        public BuilderStep withReaderObservation(
                ServerPushEventStrategy readerObservationStrategy) {
            // check params nullity
            if (readerObservationStrategy == null) {
                throw new IllegalArgumentException("readerObservationStrategy must be set");
            }
            this.withReaderObservation = true;
            this.readerObservationStrategy = readerObservationStrategy;
            return this;
        }
    }


    /*
     * implementation of builder step
     */
    public class AsyncStep extends Step implements NativeSeClientServiceFactory.BuilderStep,
            NativeSeClientServiceFactory.AsyncReaderStep,
            NativeSeClientServiceFactory.AsyncPluginStep {

        Boolean withReaderObservation;
        Boolean withPluginObservation;

        @Override
        public NativeSeClientService getService() {
            // create the service
            NativeSeClientServiceImpl service = NativeSeClientServiceImpl
                    .createInstance(withPluginObservation, withReaderObservation);

            logger.info(
                    "Create a new NativeSeClientServiceImpl with a async client and params "
                            + "withPluginObservation:{}, " + "withReaderObservation:{}.",
                    withPluginObservation, withReaderObservation);

            // bind the service to the node
            service.bindClientAsyncNode(asyncClient);

            return service;
        }

        @Override
        public AsyncReaderStep withPluginObservation() {
            this.withPluginObservation = true;
            return this;
        }

        @Override
        public AsyncReaderStep withoutPluginObservation() {
            this.withPluginObservation = false;
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



}
