/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.plugin.androidnfc;

import static org.keyple.plugin.androidnfc.Tools.byteArrayToSHex;
import java.util.ArrayList;
import java.util.List;
import org.keyple.seproxy.ApduRequest;
import org.keyple.seproxy.ProxyReader;
import org.keyple.seproxy.SeRequest;
import org.keyple.seproxy.SeResponse;
import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by ixxi on 18/01/2018.
 */

public class ReaderThread extends Thread {

    private Activity myActivity;
    private boolean mInterrupt;
    private ProgressDialog myDialog;
    private TextView myScrollTextView;

    public static final String TAG = "ReaderThread";

    // byte[] SELECT_APPLI = {0x00, (byte) 0xA4, 0x04, 0x00, 0x10, (byte) 0xA0, 0x00, 0x00, 0x04,
    // 0x04, 0x01, 0x25, 0x09, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    byte[] AID = {(byte) 0xA0, 0x00, 0x00, 0x04, 0x04, 0x01, 0x25, 0x09, 0x01, 0x01, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00};
    byte[] READ_RECORD_07 = {(byte) 0x00, (byte) 0xB2, (byte) 0x01, (byte) 0x3C, (byte) 0x1D};
    AndroidNFCReader androidNFCReader = null;


    /**
     * thread of the ticketing process
     *
     * @param activity the activity bound to the NFC plugin
     * @param androidNFCPlugin plugin
     */
    public ReaderThread(TextView txtView, Activity activity, AndroidNFCPlugin androidNFCPlugin)
            throws Exception {

        myActivity = activity;
        mInterrupt = false;
        myDialog = new ProgressDialog(myActivity);
        myDialog.setIndeterminate(true);
        myDialog.setCancelable(true);
        myScrollTextView = txtView;

        for (ProxyReader el : androidNFCPlugin.getReaders()) {
            // Add of the single NFC reader
            androidNFCReader = (AndroidNFCReader) el;
        }

    }

    /**
     * End of the thread
     *
     */
    public void EndThread() {
        mInterrupt = true;
    }

    /**
     * GUI to notify the start of the waiting card
     *
     */
    private void StartWaiting() {
        myActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                myDialog.setMessage("Reading...");
                myDialog.show();
            }
        });
    }

    /**
     * Display of the text on the GUI
     *
     */
    private void DisplayText(CharSequence text) {
        final String myText = text.toString();

        myActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                myScrollTextView.setText(myText);
            }
        });
    }

    /**
     * GUI to notify the closing of the waiting of card
     *
     */
    private void StopWaiting() {
        myActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                myDialog.dismiss();
            }
        });
    }

    @Override
    public void run() {
        boolean bRet = true;
        byte[] bCOM = new byte[1];
        byte[] pDataOUT = new byte[256];
        long[] lenOUT = new long[1];
        short[] SW = new short[1];

        StartWaiting();
        setPriority(Thread.MAX_PRIORITY);
        try {
            Transaction: {
                // Detection of the card
                while (androidNFCReader.searchCard(bCOM, pDataOUT, lenOUT) == false) {
                    if (mInterrupt == true) {
                        Log.i(TAG, "mInterrupt: " + mInterrupt);
                        break Transaction;
                    }
                }

                // Identification of the protocol
                Log.i(TAG, "Protocol:" + bCOM[0]);
                switch (bCOM[0]) {
                    case CSC_Protocol.Mifare:
                        Log.i(TAG, "Mifare\n");
                        break;
                    case CSC_Protocol.CalypsoA:
                    case CSC_Protocol.CalypsoB:
                        Log.i(TAG, "Calypso\n");
                        break;
                    case CSC_Protocol.Innovatron:
                        Log.i(TAG, "Innovatron\n");
                        break;
                    case CSC_Protocol.ISOA:
                    case CSC_Protocol.ISOA_3:
                        Log.i(TAG, "ISO 14443 Type A\n");
                        break;
                    case CSC_Protocol.ISOB:
                        Log.i(TAG, "ISO 14443 Type B\n");
                        break;
                    default:
                        Log.i(TAG, "Unknown\n");
                        break;
                }

                // Ticketing process
                ApduRequest APDUReq = new ApduRequest(READ_RECORD_07, true);
                List<ApduRequest> APDUReqList = new ArrayList<>();
                APDUReqList.add(APDUReq);
                SeRequest seReq = new SeRequest(AID, (APDUReqList), true);
                SeResponse seResp = androidNFCReader.transmit(seReq);

                // Log of response
                //
                // Log.i(TAG, getName() + " : ReceptOut : " +
                // byteArrayToSHex(seResp.getFci().getbytes()));

                // Display the result on the GUI
                String res = byteArrayToSHex(seResp.getFci().getbytes());
                int nbResp = seResp.getApduResponses().size();
                for (int i = 0; i < nbResp; i++) {
                    res += ("\n" + byteArrayToSHex(seResp.getApduResponses().get(i).getbytes()));
                }
                DisplayText("Res : " + res);


            }

            androidNFCReader.disconnect();
        } catch (Exception e) {
            Tools.ToastErr(myActivity, e.getMessage());
        }

        setPriority(Thread.MIN_PRIORITY);
        StopWaiting();
    }
}
