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

package org.eclipse.keyple.seproxy.exception;

/**
 * Exception that do not print stack trace Useful when the exceptions are expected and managed.
 */
// TODO workaround for no stackstrace; should we keep it?
public class NoStackTraceThrowable extends Throwable {
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
