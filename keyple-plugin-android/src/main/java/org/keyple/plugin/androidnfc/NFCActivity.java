/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.plugin.androidnfc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by ixxi on 18/01/2018.
 */

public class NFCActivity extends Activity {


    public static final String TAG = "NFCActivity";
    private AndroidNFCPlugin mAndroidNFCPlugin;
    Button bouton;
    TextView txtView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_nfc);

        txtView = (TextView) findViewById(R.id.textView_explanationNFC);

        bouton = (Button) findViewById(R.id.start_button);
        bouton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    // Start Reader thread
                    AndroidApp.getInstance().StartThread();
                    AndroidApp.getInstance().myThread.start();
                } catch (Exception reException) {
                    Tools.ToastErr(NFCActivity.this, reException.getMessage());
                }
            }
        });

        // Start Reader
        try {
            mAndroidNFCPlugin = new AndroidNFCPlugin(this, this.getApplicationContext());

            // Determine if a NFC reader is present
            if (mAndroidNFCPlugin.getTypeReader() == AndroidNFCPlugin.ReaderNFC) {
                // Start new NFC Reader thread
                AndroidApp.getInstance().CreateThread(txtView, this, mAndroidNFCPlugin);
            } else {
                Tools.ToastErr(this, "No reader available");
                onDestroy();
            }
        } catch (Exception e) {
            Tools.ToastErr(this, e.getMessage());
            onDestroy();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        AndroidNFCPlugin.Resume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        AndroidNFCReader.getInstance().NewIntent(intent);
    }

    @Override
    // Called when the system is about to start resuming another activity.
    public void onPause() {
        super.onPause();
        AndroidNFCPlugin.Pause();
    }

    @Override
    // Called before the activity is destroyed.
    public void onDestroy() {
        super.onDestroy();
        try {
            AndroidApp.getInstance().myThread.EndThread();

            Tools.sleepThread(250);
        } catch (Exception e) {
        } ;
        System.exit(0);
    }

}
