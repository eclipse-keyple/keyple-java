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
import org.keyple.seproxy.ReaderEvent;
import org.keyple.util.Observable;


public abstract class AbstractLogicManager implements Runnable {
    private Thread thread;

    private final Observable observable = new Observable();

    protected String getName() {
        return getClass().getName();
    }

    public Observable<Event> getObservable() {
        return observable;
    }

    public void run() {
        // If we don't have any observer, we'll create a default one
        if (observable.countObservers() == 0) {
            observable.addObserver(new ConsoleEventReporter());
        }
    }

    public void start() {
        final AbstractLogicManager actualTask = this;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (observable.countObservers() == 0) {
                        observable.addObserver(new Observable.Observer<Event>() {
                            @Override
                            public void update(Observable<? extends Event> observable,
                                    Event event) {
                                System.out.println("Event: " + event);
                            }
                        });
                    }
                    notifyObservers("start");
                    actualTask.run();
                    notifyObservers("end");
                } catch (Exception ex) { // NOPMD
                    notifyObservers("error", "exception", ex);
                    System.err.println("Error: " + ex.getClass() + " : " + ex.getMessage());
                }
            }
        }, "thread-" + getName());
        thread.start();
    }

    private void notifyObservers(String name, Object... details) {
        observable.notifyObservers(new Event(name, details));
    }

    /**
     * General purpose event
     */
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

        public String getName() {
            return name;
        }

        public Map<String, Object> getDetails() {
            return details;
        }
    }

    /**
     * Console event reporter
     */
    private class ConsoleEventReporter implements Observable.Observer<ReaderEvent> {
        @Override
        public void update(Observable<? extends ReaderEvent> observable, ReaderEvent event) {
            System.out.println("Event: " + event);
        }
    }
}
