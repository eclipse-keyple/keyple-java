/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.examples.androidnfc;

import java.util.ArrayList;
import java.util.List;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.example.common.AbstractLogicManager;
import org.keyple.example.common.AbstractLogicManager.Event;
import org.keyple.example.common.BasicCardAccessManager;
import org.keyple.example.common.KeepOpenCardAccessManager;
import org.keyple.plugin.androidnfc.AndroidNfcFragment;
import org.keyple.plugin.androidnfc.AndroidNfcPlugin;
import org.keyple.seproxy.ObservableReader;
import org.keyple.seproxy.ProxyReader;
import org.keyple.seproxy.ReaderEvent;
import org.keyple.seproxy.ReaderObserver;
import org.keyple.seproxy.ReadersPlugin;
import org.keyple.seproxy.SeProxyService;
import org.keyple.seproxy.exceptions.IOReaderException;
import org.keyple.util.event.Topic;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * Example of @{@link SeProxyService} implementation based on the @{@link AndroidNfcPlugin}
 *
 */
public class MainActivity extends AppCompatActivity
        implements ReaderObserver, Topic.Subscriber<Event> {


    private static final String TAG = MainActivity.class.getSimpleName();


    // Simple text on screen
    private TextView mText;
    private String testType;
    private AbstractLogicManager cardAccessManager;

    /**
     * SE Proxy setting of the AndroidNfcPlugin
     * 
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);


        // initialize SEProxy with Android Plugin
        Log.d(TAG, "Initialize SEProxy with Android Plugin");
        SeProxyService seProxyService = SeProxyService.getInstance();
        List<ReadersPlugin> plugins = new ArrayList<ReadersPlugin>();
        plugins.add(AndroidNfcPlugin.getInstance());
        seProxyService.setPlugins(plugins);

        // add NFC Fragment to activity in order to communicate with Android Plugin
        Log.d(TAG, "Add NFC Fragment to activity in order to communicate with Android Plugin");
        Fragment nfcFragment = AndroidNfcFragment.newInstance();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragtrans = fm.beginTransaction();
        fragtrans.add(nfcFragment, "nfc");
        fragtrans.commit();


        try {
            // define task as an observer for ReaderEvents
            Log.d(TAG, "Define task as an observer for ReaderEvents");
            ProxyReader reader = seProxyService.getPlugins().get(0).getReaders().get(0);
            ((ObservableReader) reader).addObserver(this);

            initKeepChannelAccessTest();

            mText = (TextView) findViewById(R.id.text);
            mText.setText("Waiting for a tag");

        } catch (IOReaderException e) {
            e.printStackTrace();
        }


    }

    /**
     * Declaration of the Activity as "observer" of
     * the @{@link org.keyple.plugin.androidnfc.AndroidNfcReader}
     */
    @Override
    protected void onResume() {
        super.onResume();

    }

    /**
     * Revocation of the Activity from @{@link org.keyple.plugin.androidnfc.AndroidNfcReader}
     * "observers
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            Log.d(TAG, "Remove task as an observer for ReaderEvents");
            SeProxyService seProxyService = SeProxyService.getInstance();
            ProxyReader reader = seProxyService.getPlugins().get(0).getReaders().get(0);
            ((ObservableReader) reader).deleteObserver(this);

        } catch (IOReaderException e) {
            e.printStackTrace();
        }
    }


    /**
     * Management of SE insertion event to operate a ticketing processing
     * 
     * @param readerEvent : event received from SEProxyService
     */
    @Override
    public void notify(final ReaderEvent readerEvent) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "New ReaderEvent received : " + readerEvent.getEventType().toString());

                switch (readerEvent.getEventType()) {
                    case SE_INSERTED:
                        mText.append("\n ---- \n");
                        mText.append("Tag detected");
                        try {

                            cardAccessManager.run();

                        } catch (InconsistentCommandException e) {
                            e.printStackTrace();
                        }
                        break;

                    case SE_REMOVAL:
                        mText.append("\n ---- \n");
                        mText.append("Connection closed to tag");
                        break;

                    case IO_ERROR:
                        mText.append("\n ---- \n");
                        mText.setText("Error reading card");
                        break;

                }
            }
        });
    }


    /**
     * Observes Card Access when an event is received
     * 
     * @param event
     */
    public void update(Event event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mText.append("\n ---- \n");
                mText.append(event.toString());
            }
        });
    }



    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.simpleTestButton:
                if (checked)
                    Log.i(TAG, "switched to Basic Card Access Test");
                    initBasicCardAccessTest();
                    break;
            case R.id.keepChannelButton:
                if (checked)
                    Log.i(TAG, "switched to Keep Channel Card Access Test");
                    initKeepChannelAccessTest();
                    break;
        }
    }



    private void initBasicCardAccessTest(){

        try{

        SeProxyService seProxyService = SeProxyService.getInstance();
        ProxyReader reader = seProxyService.getPlugins().get(0).getReaders().get(0);

        cardAccessManager = new BasicCardAccessManager();
        ((BasicCardAccessManager) cardAccessManager).setPoReader(reader);

        cardAccessManager.getTopic().addSubscriber(this);
        }catch (IOReaderException e){
            e.printStackTrace();
        }

    }

    private void initKeepChannelAccessTest(){

        try{

            SeProxyService seProxyService = SeProxyService.getInstance();
            ProxyReader reader = seProxyService.getPlugins().get(0).getReaders().get(0);

            cardAccessManager = new KeepOpenCardAccessManager();
            ((KeepOpenCardAccessManager) cardAccessManager).setPoReader(reader);

            cardAccessManager.getTopic().addSubscriber(this);
        }catch (IOReaderException e){
            e.printStackTrace();
        }

    }
}
