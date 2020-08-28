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
package org.eclipse.keyple.plugin.remotese.pluginse;

import static org.mockito.Mockito.doReturn;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.plugin.remotese.CoreBaseTest;
import org.eclipse.keyple.plugin.remotese.transport.DtoSender;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class RemoteSePluginImplTest extends CoreBaseTest {

  private static final Logger logger = LoggerFactory.getLogger(RemoteSePluginImplTest.class);

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
   * Test if createVirtualReader does not send ConcurrentModificationException
   * https://keyple.atlassian.net/browse/KEYP-203
   *
   * @throws Throwable
   */
  @Test
  public void createVirtualReaderMultiThread() throws InterruptedException, KeypleReaderException {

    DtoSender dtoSender = Mockito.mock(DtoSender.class);
    doReturn("masterNode1").when(dtoSender).getNodeId();

    RemoteSePluginImpl plugin =
        new RemoteSePluginImpl(
            new VirtualReaderSessionFactory(),
            dtoSender,
            10000,
            "pluginName",
            Executors.newCachedThreadPool());

    Map<String, SeReader> readers = plugin.getReaders();

    final CountDownLatch lock = new CountDownLatch(9);

    addReaderThread(plugin, dtoSender, 10, lock);
    removeReaderThread(readers, 10, lock);
    listReaders(readers, 10, lock);
    removeReaderThread(readers, 10, lock);
    listReaders(readers, 10, lock);
    addReaderThread(plugin, dtoSender, 10, lock);
    removeReaderThread(readers, 10, lock);
    listReaders(readers, 10, lock);
    removeReaderThread(readers, 10, lock);

    // wait for all thread to finish with timeout
    lock.await(10, TimeUnit.SECONDS);

    // if all thread finished correctly, lock count should be 0
    Assert.assertEquals(0, lock.getCount());
  }

  public static void listReaders(
      final Map<String, SeReader> readers, final int N, final CountDownLatch lock) {
    Thread thread =
        new Thread() {
          public void run() {
            for (int i = 0; i < N; i++) {
              Collection<SeReader> seReaders = readers.values();
              for (SeReader reader : seReaders) {
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
      final Map<String, SeReader> readers, final int N, final CountDownLatch lock) {
    Thread thread =
        new Thread() {
          public void run() {
            for (int i = 0; i < N; i++) {
              try {
                Map.Entry<String, SeReader> entry = readers.entrySet().iterator().next();
                if (entry != null) {
                  logger.debug("Removing reader {}", entry.getKey());
                  readers.remove(entry.getKey());
                } else {
                  // list is empty
                  logger.debug("readers: {}, list is empty", readers.size());
                }
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
      final RemoteSePluginImpl plugin,
      final DtoSender dtoSender,
      final int N,
      final CountDownLatch lock) {
    Thread thread =
        new Thread() {
          public void run() {
            boolean success = true;
            for (int i = 0; i < N; i++) {
              try {
                String readerName = "nativeReaderName-" + currentThread().getName() + "-" + i;
                logger.debug(
                    "create virtual reader: {}, add reader {}",
                    plugin.getReaders().size(),
                    readerName);
                plugin.createVirtualReader(
                    "slaveNodeId",
                    readerName,
                    dtoSender,
                    TransmissionMode.CONTACTS,
                    true,
                    new HashMap<String, String>());
              } catch (Exception e) {
                success = false;
                e.printStackTrace();
              }

              try {
                Thread.sleep(10);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            }
            // if no error, count down latch
            if (success) {
              lock.countDown();
            }
          }
        };
    thread.start();
  }
}
