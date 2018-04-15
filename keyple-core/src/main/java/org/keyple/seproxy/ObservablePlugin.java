package org.keyple.seproxy;

import org.keyple.util.event.Observable;

/**
 * Observable plugin.
 * These plugin can report when a reader is added or removed.
 */
public abstract class ObservablePlugin extends Observable<PluginEvent> implements ReadersPlugin {}
