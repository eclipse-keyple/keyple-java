/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.util;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Observable
 * 
 * @param <T>
 *
 *        See https://stackoverflow.com/a/48453679/847202 which is a generics-enabled version of
 *        https://docs.oracle.com/javase/7/docs/api/java/util/Observable.html
 */
public class Observable<T> {

    public interface Observer<U> {
        void update(Observable<? extends U> observable, U arg);
    }

    private boolean changed = false;
    protected final Collection<Observer<? super T>> observers;

    public Observable() {
        observers = new ArrayList<Observer<? super T>>();
    }

    public void addObserver(final Observer<? super T> observer) {
        synchronized (observers) {
            if (!observers.contains(observer)) {
                observers.add(observer);
            }
        }
    }

    public void removeObserver(final Observer<? super T> observer) {
        synchronized (observers) {
            observers.remove(observer);
        }
    }

    public void clearObservers() {
        synchronized (observers) {
            this.observers.clear();
        }
    }

    public void setChanged() {
        synchronized (observers) {
            this.changed = true;
        }
    }

    public void clearChanged() {
        synchronized (observers) {
            this.changed = false;
        }
    }

    public boolean hasChanged() {
        synchronized (observers) {
            return this.changed;
        }
    }

    public int countObservers() {
        synchronized (observers) {
            return observers.size();
        }
    }

    public void notifyObservers() {
        notifyObservers(null);
    }

    public void notifyObservers(final T value) {
        ArrayList<Observer<? super T>> toNotify = null;
        synchronized (observers) {
            if (!changed) {
                return;
            }
            toNotify = new ArrayList<Observer<? super T>>(observers);
            changed = false;
        }
        for (Observer<? super T> observer : toNotify) {
            observer.update(this, value);
        }
    }
}
