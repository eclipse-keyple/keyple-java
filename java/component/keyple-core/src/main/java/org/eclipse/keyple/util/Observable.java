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
package org.eclipse.keyple.util;


import java.util.HashSet;
import java.util.Set;


/**
 * Generic Observable class
 *
 * @param <T> Generic event
 *
 */

public class Observable<T> {
    public interface Observer<T> {
        void update(T event);
    }

    private boolean changed = false;

    /*
     * this object will be used to synchronize the access to the observers list in order to be
     * thread safe
     */
    private final Object SYNC = new Object();

    private Set<Observer<T>> observers;

    public void addObserver(final Observer<T> observer) {
        if (observer == null) {
            return;
        }

        synchronized (SYNC) {
            if (observers == null) {
                observers = new HashSet<Observer<T>>(1);
            }
            observers.add(observer);
        }
    }

    public void removeObserver(final Observer<T> observer) {
        if (observer == null) {
            return;
        }

        synchronized (SYNC) {
            if (observers != null) {
                observers.remove(observer);
            }
        }
    }

    public void clearObservers() {
        if (observers != null) {
            this.observers.clear();
        }
    }

    public void setChanged() {
        this.changed = true;
    }

    public void clearChanged() {
        this.changed = false;
    }

    public boolean hasChanged() {
        return this.changed;
    }

    public int countObservers() {
        return observers == null ? 0 : observers.size();
    }

    public void notifyObservers() {
        notifyObservers(null);
    }

    public void notifyObservers(final T event) {
        Set<Observer> observersCopy;

        synchronized (SYNC) {
            if (observers == null) {
                return;
            }
            observersCopy = new HashSet<Observer>(observers);
        }

        for (Observer observer : observersCopy) {
            observer.update(event);
        }
    }
}
