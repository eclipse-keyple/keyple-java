package org.eclipse.keyple.core.seproxy.plugin.state;

import org.eclipse.keyple.core.seproxy.plugin.AbstractObservableLocalReader;
import org.eclipse.keyple.core.seproxy.plugin.AbstractObservableState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicWaitForStartDetect extends AbstractObservableState {

    /** logger */
    private static final Logger logger =
            LoggerFactory.getLogger(BasicWaitForStartDetect.class);

    public BasicWaitForStartDetect(AbstractObservableLocalReader reader) {
        super(MonitoringState.WAIT_FOR_START_DETECTION, reader);
    }


    @Override
    protected void onEvent(AbstractObservableLocalReader.StateEvent event) {
        logger.trace("Event {} received on reader {} in currentState {}", event, reader.getName(), state);
        switch (event){
            case START_DETECT:
                reader.switchState(MonitoringState.WAIT_FOR_SE_INSERTION);
                break;

        }
    }

    @Override
    protected void activate() {

    }

    @Override
    protected void deActivate() {

    }
}
