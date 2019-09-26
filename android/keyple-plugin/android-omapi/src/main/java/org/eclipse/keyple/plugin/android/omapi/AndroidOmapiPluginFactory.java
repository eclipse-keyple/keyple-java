/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.android.omapi;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import org.eclipse.keyple.core.seproxy.AbstractPluginFactory;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginInstanciationException;
import org.simalliance.openmobileapi.SEService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class AndroidOmapiPluginFactory extends AbstractPluginFactory  implements SEService.CallBack  {

    private static final Logger logger =
            LoggerFactory.getLogger(AndroidOmapiPluginFactory.class);

    private static Integer TIMEOUT = 20;
    private CountDownLatch lock = new CountDownLatch(1);
    private Context context;
    private SEService seService;
    public AndroidOmapiPluginFactory(Context context){
        this.context = context;
    }

    @Override
    public String getPluginName() {
        return AndroidOmapiPlugin.PLUGIN_NAME;
    }

    @Override
    protected ReaderPlugin getPluginInstance() throws KeyplePluginInstanciationException {
        logger.info("AndroidOmapiPluginFactory instanciate plugin");
        logger.info("Connect to SeService");
        connectToSe(this);

        try {
            logger.info("Wait {} seconds for SeService to connect", TIMEOUT);
            boolean success = lock.await(TIMEOUT, TimeUnit.SECONDS);
            if(success){
                return new AndroidOmapiPluginImpl(seService);
            }
            throw new KeyplePluginInstanciationException("Connect to OMAPI SeService has timeout");
        } catch (InterruptedException e) {
            throw new KeyplePluginInstanciationException("Connect to OMAPI SeService has timeout");
        }
    }


    /**
     * Called when OMAPI seService is connected
     * @param seService
     */
    @Override
    public void serviceConnected(SEService seService) {
        logger.debug("Service sucessfully connected");
        this.seService = seService;
        //release lock
        lock.countDown();
    }

    private void connectToSe(final SEService.CallBack callBack) {

        Thread thread = new Thread() {
            @Override
            public void run() {
                if (isEnvironmentReady()) {
                    logger.warn( "Environment is ready for OMAPI, connecting to SeService");
                    new SEService(context, callBack);
                } else {
                    logger.warn("Environment is not ready for OMAPI");

                }
            }
        };

        thread.start();

    }

    static private String getOMAPIVersion(Context context) {
        try {
            PackageInfo packageInfo =
                    context.getPackageManager().getPackageInfo("android.smartcard", 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e1) {
            try {
                PackageInfo packageInfo = context.getPackageManager()
                        .getPackageInfo("org.simalliance.openmobileapi.service", 0);
                return packageInfo.versionName;
            } catch (PackageManager.NameNotFoundException e2) {
                try {
                    PackageInfo packageInfo = context.getPackageManager()
                            .getPackageInfo("com.sonyericsson.smartcard", 0);
                    return packageInfo.versionName;
                } catch (PackageManager.NameNotFoundException e3) {
                    return "";
                }
            }
        }
    }

    Boolean isEnvironmentReady() {
        return getOMAPIVersion(context) != "";
    }
}
