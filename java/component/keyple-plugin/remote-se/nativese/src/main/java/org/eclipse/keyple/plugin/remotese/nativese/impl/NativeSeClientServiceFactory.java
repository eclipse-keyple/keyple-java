package org.eclipse.keyple.plugin.remotese.nativese.impl;


import org.eclipse.keyple.plugin.remotese.core.KeypleClientAsync;
import org.eclipse.keyple.plugin.remotese.core.KeypleClientSync;
import org.eclipse.keyple.plugin.remotese.nativese.NativeSeClientService;

/**
 * Use this factory to create a NativeSeClientService
 */
public class NativeSeClientServiceFactory {

    /**
     * First step to create the service
     * @return next configuration step
     */
    NodeStep builder(){
        return new Step();
    }


    /*
     * implementation of builder step
     */
    public class Step implements NativeSeClientServiceFactory.BuilderStep, NativeSeClientServiceFactory.NodeStep, NativeSeClientServiceFactory.ReaderStep{

        KeypleClientAsync asyncClient;
        KeypleClientSync syncClient;
        Boolean withReaderObservation;

        @Override
        public NativeSeClientService getService() {
            NativeSeClientServiceImpl service = NativeSeClientServiceImpl.createInstance(withReaderObservation);
            if(asyncClient !=null){
                service.bindClientAsyncNode(asyncClient);
            }
            if(syncClient !=null){
                service.bindClientSyncNode(syncClient,null,null);//todo
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
         * @return
         */
        NativeSeClientService getService();
    }

    public interface NodeStep {
        /**
         * Configure the service with an async Client
         * @param asyncClient
         * @return next configuration step
         */
        ReaderStep withAsyncNode (KeypleClientAsync asyncClient);

        /**
         * Configure the service with a sync Client
         * @param syncClient
         * @return next configuration step
         */
        ReaderStep withSyncNode (KeypleClientSync syncClient);
    }

    public interface ReaderStep {
        /**
         * Configure the service to observe the local reader
         * @return next configuration step
         */
        BuilderStep withReaderObservation();

        /**
         * Configure the service without observation
         * @return next configuration step
         */
        BuilderStep withoutReaderObservation();
    }

}
