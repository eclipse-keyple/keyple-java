/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.util;


import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;


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

    private final Collection<Observer<T>> observers;

    public Observable() {
        observers = new CopyOnWriteArrayList<Observer<T>>();
    }

    public void addObserver(final Observer<T> observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(final Observer<T> observer) {
        observers.remove(observer);
    }

    public void clearObservers() {
        this.observers.clear();
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
        return observers.size();
    }

    public void notifyObservers() {
        notifyObservers(null);
    }

    public void notifyObservers(final T event) {
        for (Observer<T> observer : observers) {
            observer.update(event); // the Observable is already present in the event
        }
    }
}
