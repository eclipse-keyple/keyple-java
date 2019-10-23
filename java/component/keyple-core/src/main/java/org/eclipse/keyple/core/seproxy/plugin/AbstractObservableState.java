package org.eclipse.keyple.core.seproxy.plugin;

public abstract class AbstractObservableState {


    private AbstractObservableLocalReader.MonitoringState state;//identifier of the state
    private AbstractObservableLocalReader reader; //reference to the reader

    AbstractObservableState(AbstractObservableLocalReader reader, AbstractObservableLocalReader.MonitoringState state){
        this.reader = reader;
        this.state = state;
    }

    public AbstractObservableLocalReader.MonitoringState getMonitoringState(){
        return state;
    }

    abstract AbstractObservableState onStartDetection();
    abstract AbstractObservableState onStopDetection();

    abstract AbstractObservableState onSeInserted();
    abstract AbstractObservableState onSeProcessed();
    abstract AbstractObservableState onSeRemoved();

}

