/* **************************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.core.seproxy.plugin;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.seproxy.Plugin;
import org.eclipse.keyple.core.seproxy.Reader;
import org.eclipse.keyple.core.seproxy.plugin.reader.BlankAbstractReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class AbstractPluginTest extends CoreBaseTest {

  private static final Logger logger = LoggerFactory.getLogger(AbstractPluginTest.class);

  static final Integer X_TIMES = 50; // run tests multiple times

  @Parameterized.Parameters
  public static Object[][] data() {
    return new Object[X_TIMES][0];
  }

  @Before
  public void setUp() {
    logger.info("------------------------------");
    logger.info("Test {}", name.getMethodName() + "");
    logger.info("------------------------------");
  }

  /**
   * Test if readers list does not send ConcurrentModificationException
   * https://keyple.atlassian.net/browse/KEYP-195
   *
   * @throws Throwable
   */
  @Test
  public void addRemoveReadersMultiThreaded() throws Exception {
    Plugin plugin = new BlankAbstractPlugin("addRemoveReadersMultiThreaded");
    Map<String, Reader> readers = plugin.getReaders();
    final CountDownLatch lock = new CountDownLatch(10);

    addReaderThread(readers, 10, lock);
    addReaderThread(readers, 10, lock);
    removeReaderThread(readers, 10, lock);
    listReaders(readers, 10, lock);
    addReaderThread(readers, 10, lock);
    removeReaderThread(readers, 10, lock);
    listReaders(readers, 10, lock);
    removeReaderThread(readers, 10, lock);
    listReaders(readers, 10, lock);
    removeReaderThread(readers, 10, lock);

    // wait for all thread to finish with timeout
    lock.await(2, TimeUnit.SECONDS);

    // if all thread finished correctly, lock count should be 0
    Assert.assertEquals(0, lock.getCount());
  }

  public static void listReaders(
      final Map<String, Reader> readers, final int N, final CountDownLatch lock) {
    Thread thread =
        new Thread() {
          public void run() {
            for (int i = 0; i < N; i++) {
              final Collection<Reader> readerCollection = readers.values();
              for (Reader reader : readerCollection) {
                logger.debug("list, readers: {}, reader {}", readers.size(), reader.getName());
              }
              try {
                Thread.sleep(10);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            }
            // if no error, count down latch
            lock.countDown();
          }
        };
    thread.start();
  }

  public static void removeReaderThread(
      final Map<String, Reader> readers, final int N, final CountDownLatch lock) {
    Thread thread =
        new Thread() {
          public void run() {
            for (int i = 0; i < N; i++) {
              try {
                if (readers.size() > 0) {
                  Map.Entry<String, Reader> reader = readers.entrySet().iterator().next();
                  readers.remove(reader.getKey());
                } else {
                  throw new NoSuchElementException("Empty reader list");
                }
                logger.debug("readers: {}, remove first reader", readers.size());
              } catch (NoSuchElementException e) {
                // list is empty
                logger.debug("readers: {}, list is empty", readers.size());
              }
              try {
                Thread.sleep(10);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            }
            // if no error, count down latch
            lock.countDown();
          }
        };
    thread.start();
  }

  public static void addReaderThread(
      final Map<String, Reader> readers, final int N, final CountDownLatch lock) {
    Thread thread =
        new Thread() {
          public void run() {
            for (int i = 0; i < N; i++) {
              Reader reader = new BlankAbstractReader("pluginName", UUID.randomUUID().toString());
              readers.put(reader.getName(), reader);
              logger.debug("readers: {}, add reader {}", readers.size(), reader.getName());

              try {
                Thread.sleep(10);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            }
            // if no error, count down latch
            lock.countDown();
          }
        };
    thread.start();
  }
}
