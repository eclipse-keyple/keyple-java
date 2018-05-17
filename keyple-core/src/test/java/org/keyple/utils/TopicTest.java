/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.utils;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.util.Topic;

public class TopicTest {
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

    static class Subscriber implements Topic.Subscriber<Event> {
        private int nbCalls;

        @Override
        public void update(Event event) {
            // System.out.println(name + " received" + event);
            nbCalls += 1;
        }

        public int getNbCalls() {
            return nbCalls;
        }
    }

    @Test
    public void sample() {
        Topic<Event> pub = new Topic<Event>();
        Subscriber sub1 = new Subscriber();
        Subscriber sub2 = new Subscriber();
        pub.addSubscriber(sub1);
        pub.post(new Event("ev1"));
        pub.addSubscriber(sub2);
        pub.post(new EventPlus("ev2"));
        Assert.assertEquals(2, sub1.getNbCalls());
        Assert.assertEquals(1, sub2.getNbCalls());
    }
}
