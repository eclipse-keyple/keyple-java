/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Topic<T> {
    public interface Subscriber<T> {
        void update(T event);
    }

    private final List<Subscriber<T>> subscribers = new CopyOnWriteArrayList<Subscriber<T>>();

    public void addSubscriber(final Subscriber<T> subscriber) {
        if (!subscribers.contains(subscriber)) {
            subscribers.add(subscriber);
        }
    }

    public void removeSubscriber(final Subscriber<T> subscriber) {
        subscribers.remove(subscriber);
    }

    public void clearSubscribers() {
        subscribers.clear();
    }

    public int countSubscribers() {
        return subscribers.size();
    }

    public void post(final T value) {
        for (Subscriber<T> subscriber : subscribers) {
            subscriber.update(value);
        }
    }
}
