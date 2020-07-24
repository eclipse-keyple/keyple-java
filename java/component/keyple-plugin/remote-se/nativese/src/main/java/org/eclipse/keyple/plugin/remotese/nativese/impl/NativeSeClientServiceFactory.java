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


import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.plugin.remotese.core.KeypleClientAsync;
import org.eclipse.keyple.plugin.remotese.core.KeypleClientReaderEventFilter;
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



    public static class Step implements NodeStep, ReaderStep, BuilderStep {

        private KeypleClientAsync asyncEndpoint;
        private KeypleClientSync syncEndpoint;
        private Boolean withReaderObservation;
        private KeypleClientReaderEventFilter eventFilter;

        Step() {}

        @Override
        public ReaderStep withAsyncNode(KeypleClientAsync endpoint) {
            this.asyncEndpoint = endpoint;
            return this;
        }

        @Override
        public ReaderStep withSyncNode(KeypleClientSync endpoint) {
            this.syncEndpoint = endpoint;
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
            Assert.getInstance().notNull(eventFilter, "eventFilter");

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
            if (asyncEndpoint != null) {
                logger.info(
                        "Create a new NativeSeClientServiceImpl with a async client and params withReaderObservation:{}",
                        withReaderObservation);
                service.bindClientAsyncNode(asyncEndpoint);
            } else {
                logger.info(
                        "Create a new NativeSeClientServiceImpl with a sync client and params withReaderObservation:{}",
                        withReaderObservation);
                service.bindClientSyncNode(syncEndpoint, null, null);
            }
            return service;
        }
    }
}
