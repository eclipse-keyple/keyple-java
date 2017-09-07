package org.keyple.seproxy;

import static org.junit.Assert.*;

import org.junit.Test;
import org.keyple.seproxy.exceptions.ReaderException;

public class ReaderExceptionTest {

    @Test
    public void test() {
        try {
            throw new ReaderException("readerException", null);
        } catch (Exception e) {
            assertEquals("readerException", e.getMessage());
        }
    }

}
