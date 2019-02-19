/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
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

import java.lang.reflect.InvocationTargetException;
import org.simalliance.openmobileapi.SEService;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;



class SeServiceFactoryImpl implements ISeServiceFactory {

    static String TAG = SeServiceFactoryImpl.class.getSimpleName();

    @Override
    public SEService connectToSe(SEService.CallBack callBack) {
        if (isEnvironmentReady()) {
            Log.w(TAG, "Environment is ready for OMAPI, connecting to SeService");
            return new SEService(getApplicationContext(), callBack);
        } else {
            Log.w(TAG, "Environment is not ready for OMAPI");
            return null;
        }
    }

    static Boolean isEnvironmentReady() {
        return getOMAPIVersion(getApplicationContext()) != "";
    }

    /**
     * Retrieves Application Context automatically by a reflection invocation to method
     * android.app.ActivityThread#currentApplication
     * 
     * @return App context
     */
    static protected Application getApplicationContext() {
        Application app = null;

        Log.i(TAG, "Retrieving Application Context with reflection android.app.AppGlobals");

        try {

            app = (Application) Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication").invoke(null, (Object[]) null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            return app;
        }

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
}
