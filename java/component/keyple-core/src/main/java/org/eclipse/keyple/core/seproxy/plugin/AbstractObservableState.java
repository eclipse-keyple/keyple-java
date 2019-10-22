package org.eclipse.keyple.core.seproxy.plugin;

public abstract class AbstractObservableState {

    protected enum MonitoringState {
        WAIT_FOR_START_DETECTION, WAIT_FOR_SE_INSERTION, WAIT_FOR_SE_PROCESSING, WAIT_FOR_SE_REMOVAL
    }

    MonitoringState state;

    AbstractObservableState(MonitoringState state){
        this.state = state;
    }

    abstract AbstractObservableState onStartDetection();
    abstract AbstractObservableState onStopDetection();

    abstract AbstractObservableState onSeInserted();
    abstract AbstractObservableState onSeProcessed();
    abstract AbstractObservableState onSeRemoved();
}
