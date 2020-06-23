package org.eclipse.keyple.plugin.remotese.nativese.impl;

import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.plugin.remotese.core.*;
import org.eclipse.keyple.plugin.remotese.nativese.NativeSeClientService;
import org.eclipse.keyple.plugin.remotese.nativese.NativeSeServerService;
import org.eclipse.keyple.plugin.remotese.nativese.RemoteServiceParameters;

/**
 * Singleton instance of the NatieSeClientService
 */
final class NativeSeClientServiceImpl extends AbstractNativeSeService implements ObservableReader.ReaderObserver, NativeSeClientService {

    Boolean withReaderObservation;
    static NativeSeClientServiceImpl uniqueInstance;

    private NativeSeClientServiceImpl(Boolean withReaderObservation){
        this.withReaderObservation = withReaderObservation;
    }

    static NativeSeClientServiceImpl createInstance(boolean withReaderObservation){
        if(uniqueInstance==null){
            uniqueInstance = new NativeSeClientServiceImpl(withReaderObservation);
        }
        return uniqueInstance;
    }

    static NativeSeClientServiceImpl getInstance(){
        return uniqueInstance;
    }

    @Override
    protected void onMessage(KeypleMessageDto msg) {
        //todo
    }

    /**
     * Propagate Reader Events to RemoteSePlugin
     * (internal use)
     * @param event : event to be propagated
     *
     */
    @Override
    public void update(ReaderEvent event) {
        //todo
    }

    @Override
    public <T extends KeypleUserData> T executeRemoteService(RemoteServiceParameters parameters, KeypleUserDataFactory<T> userOutputDataFactory) {
        //todo
        return null;
    }



}
