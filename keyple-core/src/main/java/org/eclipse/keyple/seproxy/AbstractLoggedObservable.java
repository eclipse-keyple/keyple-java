/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy;

import java.io.IOException;
import java.util.Map;
import org.eclipse.keyple.util.Observable;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

/**
 * Intermediate observable class to handle the logging of AbstractObservableReader and
 * AbstractObservablePlugin
 * 
 * @param <T>
 */
public class AbstractLoggedObservable<T> extends Observable<T> implements ConfigurableItem {
    private static final ILogger logger = SLoggerFactory.getLogger(AbstractLoggedObservable.class);
    private static final String ACTION_STR = "action"; // PMD rule AvoidDuplicateLiterals

    /**
     * Add an observer. This will allow to be notified about all readers or plugins events.
     * 
     * @param observer Observer to notify
     */

    public void addObserver(Observable.Observer<T> observer) {

        if (this instanceof AbstractObservableReader) {
            logger.info("AbstractObservableReader: Adding an observer", ACTION_STR,
                    "observable_reader.add_observer", "readerName",
                    ((AbstractObservableReader) this).getName());
        } else if (this instanceof AbstractObservablePlugin) {
            logger.info("AbstractObservablePlugin: Adding an observer", ACTION_STR,
                    "observable_plugin.add_observer", "pluginName",
                    ((AbstractObservablePlugin) this).getName());
        }

        super.addObserver(observer);
    }

    /**
     * Remove an observer.
     * 
     * @param observer Observer to stop notifying
     */

    public void removeObserver(Observable.Observer<T> observer) {

        if (this instanceof AbstractObservableReader) {
            logger.info("AbstractObservableReader: Deleting an observer", ACTION_STR,
                    "observable_reader.remove_observer", "readerName",
                    ((AbstractObservableReader) this).getName());
        } else if (this instanceof AbstractObservablePlugin) {
            logger.info("AbstractObservablePlugin: Deleting an observer", ACTION_STR,
                    "observable_plugin.remove_observer", "pluginName",
                    ((AbstractObservablePlugin) this).getName());
        }

        super.removeObserver(observer);
    }



    /**
     * This method shall be called only from a SE Proxy plugin or reader implementing
     * AbstractObservableReader or AbstractObservablePlugin. Push a ReaderEvent /
     * AbstractPluginEvent of the selected AbstractObservableReader / AbstractObservablePlugin to
     * its registered Observer.
     * 
     * @param event the event
     */

    public final void notifyObservers(T event) {

        if (this instanceof AbstractObservableReader) {
            logger.info("AbstractObservableReader: Notifying of an event", ACTION_STR,
                    "observable_reader.notify_observers", "event", event, "readerName",
                    ((AbstractObservableReader) this).getName());
        } else if (this instanceof AbstractObservablePlugin) {
            logger.info("AbstractObservablePlugin: Notifying of an event", ACTION_STR,
                    "observable_plugin.notify_observers", "event", event, "pluginName",
                    ((AbstractObservablePlugin) this).getName());
        }

        setChanged();

        super.notifyObservers(event);

    }

    @Override
    public Map<String, String> getParameters() {
        return null;
    }

    @Override
    public void setParameter(String key, String value) throws IOException {

    }

    @Override
    public void setParameters(Map<String, String> parameters) throws IOException {

    }
}
