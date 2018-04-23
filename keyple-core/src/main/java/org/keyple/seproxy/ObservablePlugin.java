/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

import org.keyple.util.event.Observable;

/**
 * Observable plugin. These plugin can report when a reader is added or removed.
 */
public abstract class ObservablePlugin extends Observable<PluginEvent> implements ReadersPlugin {
}
