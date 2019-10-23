package org.eclipse.keyple.core.seproxy.plugin;

public abstract class AbstractObservableState {


    private AbstractObservableLocalReader.MonitoringState state;//identifier of the state

    /* Reference to Reader */
    private AbstractObservableLocalReader reader; //reference to the reader

    /**
     * Create a new state to
     * @param reader : observable reader this state is attached to
     * @param state : name of the state
     */
    AbstractObservableState(AbstractObservableLocalReader reader, AbstractObservableLocalReader.MonitoringState state){
        this.reader = reader;
        this.state = state;
    }

    /**
     * Get state name
     * @return name state
     */
    public AbstractObservableLocalReader.MonitoringState getMonitoringState(){
        return state;
    }

    /**
     * Handle Start Detection Event
     * @return next state
     */
    abstract AbstractObservableState onStartDetection();

    /**
     * Handle Stop Detection Event
     * @return next state
     */
    abstract AbstractObservableState onStopDetection();

    /**
     * Handle Se Inserted Event
     * @return next state
     */
    abstract AbstractObservableState onSeInserted();

    /**
     * Handle Se Processed Event
     * @return next state
     */
    abstract AbstractObservableState onSeProcessed();

    /**
     * HandleSe Removed Event
     * @return next state
     */
    abstract AbstractObservableState onSeRemoved();

}

