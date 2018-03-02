/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package ext.structlog4j;

import org.junit.Test;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

public class StructLog4JTest {
    @Test
    public void sample() {
        ILogger logger = SLoggerFactory.getLogger(StructLog4JTest.class);
        logger.info("My info", "name", "Florent Clairambault", "class", getClass(), "ex",
                new RuntimeException());
    }
}
