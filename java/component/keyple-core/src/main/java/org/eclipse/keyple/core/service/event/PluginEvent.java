/* **************************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.core.service.event;

import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This POJO is used to propagate a change of reader state in an {@link ObservablePlugin}.
 *
 * <p>A {@link EventType#READER_CONNECTED} event is fired if a new reader is detected and a {@link
 * EventType#READER_DISCONNECTED} event is fired when an already registered reader is removed from
 * the system.
 *
 * <p>The {@link #getReaderNames()} and {@link #getEventType()} methods allow the event recipient to
 * retrieve the names of the readers involved and the type of the event.
 *
 * <p>There are two types of events, the connection and disconnection of readers.
 *
 * <p>Since the event provides a list of reader names, a single event can be used to notify a change
 * for one or more readers.
 *
 * <p>However, only one type of event is notified at a time.
 *
 * @since 0.9
 */
public final class PluginEvent {

  private final String pluginName;
  private final SortedSet<String> readerNames;
  private final EventType eventType;

  /**
   * The two types of reader event
   *
   * @since 0.9
   */
  public enum EventType {

    /** A reader has been connected. */
    READER_CONNECTED,

    /** A reader has been disconnected. */
    READER_DISCONNECTED
  }

  /**
   * Create a PluginEvent for a single reader from the plugin and reader names and the type of
   * event.
   *
   * @param pluginName A string containing the name of the plugin (should be not null).
   * @param readerName A string containing the name of the reader (should be not null).
   * @param eventType An event type {@link EventType#READER_CONNECTED} or {@link
   *     EventType#READER_DISCONNECTED} (should be not null).
   * @since 0.9
   */
  public PluginEvent(String pluginName, String readerName, EventType eventType) {
    this.pluginName = pluginName;
    this.readerNames = new TreeSet<String>(Collections.singleton(readerName));
    this.eventType = eventType;
  }

  /**
   * Create a PluginEvent for multiple readers from the plugin name, multiple reader names and the
   * type of event.
   *
   * <p>Note: gathering several readers in the same event is always done for a same type of event
   * (e.g. simultaneous disconnection of 2 readers).
   *
   * @param pluginName A string containing the name of the plugin (must be not empty).
   * @param readerNames A set of string containing the reader names (must be not empty).
   * @param eventType An event type {@link EventType#READER_CONNECTED} or {@link
   *     EventType#READER_DISCONNECTED} (must be not null).
   * @since 0.9
   */
  public PluginEvent(String pluginName, Set<String> readerNames, EventType eventType) {
    this.pluginName = pluginName;
    this.readerNames = new TreeSet<String>(readerNames);
    this.eventType = eventType;
  }

  /**
   * Gets the name of the plugin to which the reader that generated the event belongs.
   *
   * @return A not empty string.
   * @since 0.9
   */
  public String getPluginName() {
    return pluginName;
  }

  /**
   * Gets the names of the readers related to the event in the form of a sorted set.
   *
   * @return A not empty sorted set of strings.
   * @since 0.9
   */
  public SortedSet<String> getReaderNames() {
    return readerNames;
  }

  /**
   * Gets the plugin event type.
   *
   * @return A not null value.
   * @since 0.9
   */
  public EventType getEventType() {
    return eventType;
  }
}
