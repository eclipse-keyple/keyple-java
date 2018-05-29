/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.android.omapi;

import org.simalliance.openmobileapi.SEService;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

public class AndroidOmapiFragment extends Fragment {

    private static final String TAG = AndroidOmapiFragment.class.getSimpleName();


    private SEService seService;

    public AndroidOmapiFragment() {
        // Required empty public constructor
    }

    public static AndroidOmapiFragment newInstance() {
        AndroidOmapiFragment fragment = new AndroidOmapiFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * At creation, fragment connects to OMAPI SE Service
     * 
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Log.i(TAG, "creating SEService object");
            seService = new SEService(getActivity(), AndroidOmapiPlugin.getInstance());
        } catch (SecurityException e) {
            Log.e(TAG,
                    "Binding not allowed, uses-permission org.simalliance.openmobileapi.SMARTCARD?");
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    /**
     * At destroy, fragment disconnects from OMAPI SE Service
     */
    @Override
    public void onDestroy() {
        if (seService != null && seService.isConnected()) {
            seService.shutdown();
        }
        super.onDestroy();
    }


}
