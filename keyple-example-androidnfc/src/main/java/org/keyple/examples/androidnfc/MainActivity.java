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
import org.keyple.calypso.commands.po.PoRevision;
import org.keyple.calypso.commands.po.builder.ReadRecordsCmdBuild;
import org.keyple.calypso.commands.po.builder.UpdateRecordCmdBuild;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.plugin.androidnfc.AndroidNfcFragment;
import org.keyple.plugin.androidnfc.AndroidNfcPlugin;
import org.keyple.seproxy.ApduRequest;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.ByteBufferUtils;
import org.keyple.seproxy.ObservableReader;
import org.keyple.seproxy.ProxyReader;
import org.keyple.seproxy.ReaderEvent;
import org.keyple.seproxy.ReaderObserver;
import org.keyple.seproxy.ReadersPlugin;
import org.keyple.seproxy.SeProxyService;
import org.keyple.seproxy.SeRequest;
import org.keyple.seproxy.SeResponse;
import org.keyple.seproxy.exceptions.IOReaderException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

/**
 * Example of @{@link SeProxyService} implementation based on the @{@link AndroidNfcPlugin}
 *
 */
public class MainActivity extends AppCompatActivity implements ReaderObserver {


    private static final String TAG = MainActivity.class.getSimpleName();


    // Simple text on screen
    private TextView mText;


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
        SeProxyService seProxyService = SeProxyService.getInstance();
        List<ReadersPlugin> plugins = new ArrayList<ReadersPlugin>();
        plugins.add(AndroidNfcPlugin.getInstance());
        seProxyService.setPlugins(plugins);

        // add NFC Fragment to activity in order to communicate with Android Plugin
        Log.d(TAG, "add Android NFC Fragment");
        Fragment nfcFragment = AndroidNfcFragment.newInstance();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragtrans = fm.beginTransaction();
        fragtrans.add(nfcFragment, "nfc");
        fragtrans.commit();


    }

    /**
     * Declaration of the Activity as "observer" of
     * the @{@link org.keyple.plugin.androidnfc.AndroidNfcReader}
     */
    @Override
    protected void onResume() {
        super.onResume();


        try {
            SeProxyService seProxyService = SeProxyService.getInstance();
            ProxyReader reader = seProxyService.getPlugins().get(0).getReaders().get(0);
            ((ObservableReader) reader).addObserver(this);

        } catch (IOReaderException e) {
            e.printStackTrace();
        }


        mText = (TextView) findViewById(R.id.text);
        mText.setText("Waiting for a tag");
    }

    /**
     * Revocation of the Activity from @{@link org.keyple.plugin.androidnfc.AndroidNfcReader}
     * "observers
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
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
                        try {

                            SeProxyService seProxyService = SeProxyService.getInstance();
                            List<ReadersPlugin> readersPlugin = seProxyService.getPlugins();
                            ProxyReader poReader = readersPlugin.get(0).getReaders().get(0);

                            String poAid = "A000000291A000000191";
                            String t2UsageRecord1_dataFill = "0102030405060708090A0B0C0D0E0F10"
                                    + "1112131415161718191A1B1C1D1E1F20"
                                    + "2122232425262728292A2B2C2D2E2F30";

                            ReadRecordsCmdBuild poReadRecordCmd_T2Env = null;
                            poReadRecordCmd_T2Env = new ReadRecordsCmdBuild(PoRevision.REV3_1,
                                    (byte) 0x01, true, (byte) 0x14, (byte) 0x20);
                            ReadRecordsCmdBuild poReadRecordCmd_T2Usage = new ReadRecordsCmdBuild(
                                    PoRevision.REV3_1, (byte) 0x01, true, (byte) 0x1A, (byte) 0x30);
                            UpdateRecordCmdBuild poUpdateRecordCmd_T2UsageFill =
                                    new UpdateRecordCmdBuild(PoRevision.REV3_1, (byte) 0x01,
                                            (byte) 0x1A,
                                            ByteBufferUtils.fromHex(t2UsageRecord1_dataFill));

                            // Get PO ApduRequest List
                            List<ApduRequest> poApduRequestList = new ArrayList<ApduRequest>();
                            poApduRequestList.add(poReadRecordCmd_T2Env.getApduRequest());
                            poApduRequestList.add(poReadRecordCmd_T2Usage.getApduRequest());
                            poApduRequestList.add(poUpdateRecordCmd_T2UsageFill.getApduRequest());

                            SeRequest poRequest = new SeRequest(ByteBufferUtils.fromHex(poAid),
                                    poApduRequestList, false);
                            mText.append("\n--\nTransmit : ");
                            for (ApduRequest req : poApduRequestList) {
                                // Log.i(TAG,r.toString());
                                mText.append("\n" + ByteBufferUtils.toHex(req.getBuffer()));
                            }

                            SeResponse poResponse = poReader.transmit(poRequest);
                            mText.append("\n--\nResponses : ");
                            for (ApduResponse res : poResponse.getApduResponses()) {
                                // Log.i(TAG,r.toString());
                                mText.append("\n" + ByteBufferUtils.toHex(res.getBuffer()) + " "
                                        + String.valueOf(res.getStatusCode()));
                            }

                        } catch (InconsistentCommandException e) {
                            e.printStackTrace();
                        } catch (IOReaderException e) {
                            e.printStackTrace();
                        }
                        break;


                    case SE_REMOVAL:
                        mText.setText("Waiting for a tag");
                        break;

                    case IO_ERROR:
                        mText.setText("Error reading card");
                        break;

                }

            }
        });


    }


}
