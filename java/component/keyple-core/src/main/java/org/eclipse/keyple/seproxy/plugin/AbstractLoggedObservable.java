/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.seproxy.plugin;


import java.util.Map;
import org.eclipse.keyple.seproxy.event.PluginEvent;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.util.Configurable;
import org.eclipse.keyple.util.Nameable;
import org.eclipse.keyple.util.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Intermediate observable class to handle the logging of AbstractObservableReader and
 * AbstractObservablePlugin
 * 
 */
abstract class AbstractLoggedObservable<T> extends Observable<T> implements Nameable, Configurable {
    private static final Logger logger = LoggerFactory.getLogger(AbstractLoggedObservable.class);

    /**
     * The item name (must be unique)
     */
    protected final String name;

    /**
     * Item constructor Force the definition of a name through the use of super method.
     *
     * @param name name of the observed object
     */
    AbstractLoggedObservable(String name) {
        this.name = name;
    }

    /**
     * Gets the reader name
     *
     * @return the reader name string
     */
    public final String getName() {
        return name;
    }

    /**
     * Add an observer. This will allow to be notified about all readers or plugins events.
     * 
     * @param observer Observer to notify
     */

    public void addObserver(final AbstractLoggedObservable.Observer<T> observer) {

        logger.trace("[{}][{}] addObserver => Adding an observer.", this.getClass(),
                this.getName());

        super.addObserver(observer);
    }

    /**
     * Remove an observer.
     * 
     * @param observer Observer to stop notifying
     */

    public void removeObserver(final AbstractLoggedObservable.Observer<T> observer) {

        if (this instanceof AbstractObservableReader) {
            logger.trace("[{}] removeObserver => Deleting a reader observer", this.getName());
        } else if (this instanceof AbstractObservablePlugin) {
            logger.trace("[{}] removeObserver => Deleting a plugin observer", this.getName());
        }

        super.removeObserver(observer);
    }



    /**
     * This method shall be called only from a SE Proxy plugin or reader implementing
     * AbstractObservableReader or AbstractObservablePlugin. Push a ReaderEvent / PluginEvent of the
     * selected AbstractObservableReader / AbstractObservablePlugin to its registered Observer.
     * 
     * @param event the event
     */

    public final void notifyObservers(final T event) {

        if (this instanceof AbstractObservableReader) {
            logger.trace(
                    "[{}] AbstractObservableReader => Notifying a reader event to {} observers. EVENTNAME = {}",
                    this.getName(), this.countObservers(),
                    ((ReaderEvent) event).getEventType().getName());
        } else if (this instanceof AbstractObservablePlugin) {
            logger.trace(
                    "[{}] AbstractObservableReader => Notifying a plugin event to {} observers. EVENTNAME = {} ",
                    this.getName(), this.countObservers(),
                    ((PluginEvent) event).getEventType().getName());
        }

        setChanged();

        super.notifyObservers(event);
    }

    /**
     * Set a list of parameters on a reader.
     * <p>
     * See {@link #setParameter(String, String)} for more details
     *
     * @param parameters the new parameters
     * @throws KeypleBaseException This method can fail when disabling the exclusive mode as it's
     *         executed instantly
     */
    public final void setParameters(Map<String, String> parameters)
            throws IllegalArgumentException, KeypleBaseException {
        for (Map.Entry<String, String> en : parameters.entrySet()) {
            setParameter(en.getKey(), en.getValue());
        }
    }
}
