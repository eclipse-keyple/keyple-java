package org.eclipse.keyple.core.seproxy.plugin.state;

import org.eclipse.keyple.core.seproxy.plugin.AbstractObservableLocalReader;
import org.eclipse.keyple.core.seproxy.plugin.AbstractObservableState;
import org.eclipse.keyple.core.seproxy.plugin.AbstractThreadedObservableLocalReader;
import org.eclipse.keyple.core.seproxy.plugin.SmartPresenceReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class BasicWaitForSeRemoval extends AbstractObservableState {

    /** logger */
    private static final Logger logger =
            LoggerFactory.getLogger(BasicWaitForSeRemoval.class);

    public BasicWaitForSeRemoval(AbstractObservableLocalReader reader) {
        super(MonitoringState.WAIT_FOR_SE_REMOVAL, reader);
    }

    @Override
    protected void onEvent(AbstractObservableLocalReader.StateEvent event) {
        logger.trace("Event {} received on reader {} in currentState {}", event, reader.getName(), state);
        switch (event){
            case SE_REMOVED:
                logger.debug("Se Removed event received for reader {}", reader.getName());
                reader.switchState(MonitoringState.WAIT_FOR_SE_INSERTION);
                break;


        }
    }

    @Override
    protected void activate() { }

    @Override
    protected void deActivate() { }

}
