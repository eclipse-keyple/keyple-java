package org.eclipse.keyple.core.seproxy.plugin;

public abstract class AbstractObservableState {

    /* Identifier of the state */
    private AbstractObservableLocalReader.MonitoringState state;

    /* Reference to Reader */
    private AbstractObservableLocalReader reader;

    /**
     * Create a new state with a state identifier
     * @param reader : observable reader this state is attached to
     * @param state : name of the state
     */
    AbstractObservableState(AbstractObservableLocalReader.MonitoringState state,AbstractObservableLocalReader reader){
        this.reader = reader;
        this.state = state;
    }

    /**
     * Get state identifier
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
     * Handle Se Removed Event
     * @return next state
     */
    abstract AbstractObservableState onSeRemoved();

}

