package org.eclipse.keyple.core.seproxy.plugin.state;

import org.eclipse.keyple.core.seproxy.plugin.AbstractObservableLocalReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultWaitForStartDetect extends AbstractObservableState {

    /** logger */
    private static final Logger logger =
            LoggerFactory.getLogger(DefaultWaitForStartDetect.class);

    public DefaultWaitForStartDetect(AbstractObservableLocalReader reader) {
        super(MonitoringState.WAIT_FOR_START_DETECTION, reader);
    }


    @Override
    public void onEvent(AbstractObservableLocalReader.InternalEvent event) {
        logger.trace("Event {} received on reader {} in currentState {}", event, reader.getName(), state);
        switch (event){
            case START_DETECT:
                reader.switchState(MonitoringState.WAIT_FOR_SE_INSERTION);
                break;

        }
    }

    @Override
    public void activate() {

    }

    @Override
    public void deActivate() {

    }
}
