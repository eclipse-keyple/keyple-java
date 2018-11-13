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

import org.junit.Assert;
import org.junit.Test;

public class ObservableTest {
    static class Event {
        private final String name;

        public Event(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Event{" + name + "}";
        }
    }

    static class EventPlus extends Event {

        public EventPlus(String name) {
            super(name);
        }

        @Override
        public String toString() {
            return "EventPlus{" + getName() + "}";
        }
    }

    static class Observer implements Observable.Observer<Event> {
        private int nbCalls;

        public int getNbCalls() {
            return nbCalls;
        }

        @Override
        public void update(Event arg) {
            // System.out.println(name + " received" + event);
            nbCalls += 1;
        }
    }

    @Test
    public void sample() {
        Observable<Event> pub = new Observable<Event>();
        Observer sub1 = new Observer();
        Observer sub2 = new Observer();
        pub.addObserver(sub1);
        pub.notifyObservers(new Event("ev1"));
        pub.addObserver(sub2);
        pub.notifyObservers(new EventPlus("ev2"));
        Assert.assertEquals(2, sub1.getNbCalls());
        Assert.assertEquals(1, sub2.getNbCalls());
    }
}
