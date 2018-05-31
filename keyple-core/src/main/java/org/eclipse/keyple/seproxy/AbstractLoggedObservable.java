/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy;

import org.eclipse.keyple.util.Observable;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

/**
 * Intermediate observable class to handle the logging of AbstractReader and AbstractPlugin
 * 
 * @param <T>
 */
public class AbstractLoggedObservable<T> extends Observable<T> {
    private static final ILogger logger = SLoggerFactory.getLogger(AbstractLoggedObservable.class);
    private static final String ACTION_STR = "action"; // PMD rule AvoidDuplicateLiterals

    /**
     * Add an observer. This will allow to be notified about all readers or plugins events.
     * 
     * @param observer Observer to notify
     */

    public void addObserver(Observable.Observer<T> observer) {

        if (this instanceof AbstractReader) {
            logger.info("AbstractReader: Adding an observer", ACTION_STR,
                    "observable_reader.add_observer", "readerName",
                    ((AbstractReader) this).getName());
        } else if (this instanceof AbstractPlugin) {
            logger.info("AbstractPlugin: Adding an observer", ACTION_STR,
                    "observable_plugin.add_observer", "pluginName",
                    ((AbstractPlugin) this).getName());
        }

        super.addObserver(observer);
    }

    /**
     * Remove an observer.
     * 
     * @param observer Observer to stop notifying
     */

    public void removeObserver(Observable.Observer<T> observer) {

        if (this instanceof AbstractReader) {
            logger.info("AbstractReader: Deleting an observer", ACTION_STR,
                    "observable_reader.remove_observer", "readerName",
                    ((AbstractReader) this).getName());
        } else if (this instanceof AbstractPlugin) {
            logger.info("AbstractPlugin: Deleting an observer", ACTION_STR,
                    "observable_plugin.remove_observer", "pluginName",
                    ((AbstractPlugin) this).getName());
        }

        super.removeObserver(observer);
    }



    /**
     * This method shall be called only from a SE Proxy plugin or reader implementing AbstractReader
     * or AbstractPlugin. Push a ReaderEvent / PluginEvent of the selected AbstractReader /
     * AbstractPlugin to its registered Observer.
     * 
     * @param event the event
     */

    public final void notifyObservers(T event) {

        if (this instanceof AbstractReader) {
            logger.info("AbstractReader: Notifying of an event", ACTION_STR,
                    "observable_reader.notify_observers", "event", event, "readerName",
                    ((AbstractReader) this).getName());
        } else if (this instanceof AbstractPlugin) {
            logger.info("AbstractPlugin: Notifying of an event", ACTION_STR,
                    "observable_plugin.notify_observers", "event", event, "pluginName",
                    ((AbstractPlugin) this).getName());
        }

        setChanged();

        super.notifyObservers(event);

    }
}
