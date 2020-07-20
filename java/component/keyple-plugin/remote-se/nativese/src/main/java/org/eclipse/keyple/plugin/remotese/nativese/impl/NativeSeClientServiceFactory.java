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


import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.plugin.remotese.core.KeypleClientAsync;
import org.eclipse.keyple.plugin.remotese.core.KeypleClientSync;
import org.eclipse.keyple.plugin.remotese.core.KeypleUserData;
import org.eclipse.keyple.plugin.remotese.core.KeypleUserDataFactory;
import org.eclipse.keyple.plugin.remotese.core.exception.KeypleDoNotPropagateEventException;
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
    public NodeStep builder() {
        return new Step();
    }

    public interface BuilderStep {
        /**
         * Build the service
         *
         * @return singleton instance of the service
         */
        NativeSeClientService getService();
    }

    public interface NodeStep {
        /**
         * Configure the service with an async Client
         *
         * @param asyncClient non nullable instance of an async client
         * @return next configuration step
         */
        ReaderStep withAsyncNode(KeypleClientAsync asyncClient);

        /**
         * Configure the service with a sync Client
         *
         * @param syncClient non nullable instance of a sync client
         * @return next configuration step
         */
        ReaderStep withSyncNode(KeypleClientSync syncClient);
    }


    public interface ReaderStep {
        /**
         * Configure the service to observe the local reader
         *
         * @param eventFilter non-nullable event filter
         * @return next configuration step
         */
        BuilderStep withReaderObservation(KeypleClientReaderEventFilter eventFilter);

        /**
         * Configure the service without observation
         *
         * @return next configuration step
         */
        BuilderStep withoutReaderObservation();
    }


    public interface KeypleClientReaderEventFilter<T extends KeypleUserData> {
        /**
         * Configure the factory to retrieve the output
         * 
         * @return non nullable instance of the factory
         */
        KeypleUserDataFactory<T> getUserOutputDataFactory();

        /**
         * Execute any process before the event is sent to the server
         * 
         * @param event that will be propagated
         * @return nullable data that will be sent to the server.
         * @throws KeypleDoNotPropagateEventException if event should not be propagated to server
         */
        KeypleUserData beforePropagation(ReaderEvent event)
                throws KeypleDoNotPropagateEventException;

        /**
         * Retrieve the output from the event global processing
         * 
         * @param userOutputData nullable instance of the
         */
        void afterPropagation(T userOutputData);

    }

    public static class Step implements NodeStep, ReaderStep, BuilderStep {

        private KeypleClientAsync asyncClient;
        private KeypleClientSync syncClient;
        private Boolean withReaderObservation;
        private KeypleClientReaderEventFilter eventFilter;

        @Override
        public ReaderStep withAsyncNode(KeypleClientAsync asyncClient) {
            this.asyncClient = asyncClient;
            return this;
        }

        @Override
        public ReaderStep withSyncNode(KeypleClientSync syncClient) {
            this.syncClient = syncClient;
            return this;
        }

        @Override
        public BuilderStep withoutReaderObservation() {
            this.withReaderObservation = false;
            return this;
        }

        @Override
        public BuilderStep withReaderObservation(KeypleClientReaderEventFilter eventFilter) {
            // check params nullity
            if (eventFilter == null) {
                throw new IllegalArgumentException("Reader Event filter must be set");
            }
            this.withReaderObservation = true;
            this.eventFilter = eventFilter;
            return this;
        }

        @Override
        public NativeSeClientService getService() {
            // create the service
            NativeSeClientServiceImpl service =
                    NativeSeClientServiceImpl.createInstance(withReaderObservation, eventFilter);


            // bind the service to the node
            if (asyncClient != null) {
                logger.info(
                        "Create a new NativeSeClientServiceImpl with a async client and params withReaderObservation:{}",
                        withReaderObservation);
                service.bindClientAsyncNode(asyncClient);
            } else {
                logger.info(
                        "Create a new NativeSeClientServiceImpl with a sync client and params withReaderObservation:{}",
                        withReaderObservation);
                service.bindClientSyncNode(syncClient, null, null);
            }
            return service;
        }
    }
}
