/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.example.common;

import java.util.HashMap;
import java.util.Map;
import org.keyple.util.event.Topic;


public abstract class AbstractLogicManager implements Runnable {
    private Thread thread;

    private final Topic<Event> topic = new Topic<Event>();

    protected String getName() {
        return getClass().getName();
    }

    public Topic<Event> getTopic() {
        return topic;
    }

    public void start() {
        final AbstractLogicManager actualTask = this;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (topic.countSubscribers() == 0) {
                        topic.addSubscriber(new Topic.Subscriber<Event>() {
                            @Override
                            public void update(Event event) {
                                System.out.println("Event: " + event);
                            }
                        });
                    }
                    post("start");
                    actualTask.run();
                    post("end");
                } catch (Exception ex) { // NOPMD
                    post("error", "exception", ex);
                    System.err.println("Error: " + ex.getClass() + " : " + ex.getMessage());
                }
            }
        }, "thread-" + getName());
        thread.start();
    }

    protected void post(String name, Object... details) {
        topic.post(new Event(name, details));
    }

    public static class Event {
        private final String name;
        private final Map<String, Object> details;

        Event(String name, Object... details) {
            this.name = name;
            this.details = new HashMap<String, Object>();

            int count = 1;
            String key = null;
            for (Object d : details) {
                if (count % 2 == 1) {
                    if (d instanceof String) {
                        key = (String) d;
                    } else {
                        throw new IllegalArgumentException("Argument " + count
                                + " should be a string instead of " + d.getClass());
                    }
                } else {
                    this.details.put(key, d);
                }

                count += 1;
            }
        }

        @Override
        public String toString() {
            StringBuilder output = new StringBuilder();
            output.append(this.name);
            if (this.details != null) {
                output.append('{');
                for (Map.Entry<String, Object> en : details.entrySet()) {
                    output.append(en.getKey()).append("=\"").append(en.getValue()).append('\"');
                }
                output.append('}');
            }
            return output.toString();
        }
    }
}
