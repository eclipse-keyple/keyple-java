package org.keyple.plugin.androidnfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.util.Log;

import org.keyple.seproxy.ReadersPlugin;
import org.keyple.seproxy.SeProxyService;

import java.util.ArrayList;
import java.util.List;


//TODO change to appcompataibility
public abstract class NFCListenerAbstractActivity extends Activity  implements NfcAdapter.ReaderCallback {

    private static final String TAG = NFCListenerAbstractActivity.class.getSimpleName();
    private NfcAdapter nfcAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null){
            Log.w(TAG, "Your device does not support NFC");
        };

        if(!nfcAdapter.isEnabled()){
            Log.w(TAG, "PLease enable NFC to communicate with NFC Elements");
        }

        Log.i(TAG, "Initialize Keyple with the NFC Android Plugin");
        SeProxyService seProxyService = SeProxyService.getInstance();
        List<ReadersPlugin> plugins = new ArrayList<ReadersPlugin>();
        plugins.add(AndroidNfcPlugin.getInstance());
        seProxyService.setPlugins(plugins);

    }

    @Override
    public void onResume() {
        super.onResume();
        if(nfcAdapter!= null){
            setupForegroundDispatch(this,nfcAdapter);
        }

    }


    @Override
    public void onTagDiscovered(Tag tag) {
        //implementation with readermode
    }

    @Override
    public void onNewIntent(Intent intent) {

        Log.d(TAG, "On New Intent : " + intent.toString());

        if (intent != null && intent.getAction() != null) {
            final String action = intent.getAction();
            if (action.equals(NfcAdapter.ACTION_TECH_DISCOVERED)){
                //AndroidNfcPlugin.getInstance().plugReader(nfcAdapter);
                AndroidNfcReader.getInstance().processIntent(intent);

                onSEconnected();
            }
        }
    }

    //notify method for the ticketing application activity
    public abstract void onSEconnected();



    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "on Pause");
        if (nfcAdapter != null){
            stopForegroundDispatch(this,nfcAdapter);
        }
    }


    /**
     * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    private void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        Log.i(TAG, "Setup Mode Foreground Dispatch");
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        //listen for intent for ISO DEP protocol
        IntentFilter[] filters = new IntentFilter[] { new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)};
        String[][] techList = new String[][]{new String[]{IsoDep.class.getName()}};

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    /**
     * @param activity The corresponding {@link Activity} requesting to stop the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    private  void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        Log.i(TAG, "Stop Foreground Dispatch");
        adapter.disableForegroundDispatch(activity);
    }


    private  void setupReaderMode(final Activity activity, NfcAdapter adapter) {
        Bundle options = new Bundle();
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 5000);

        adapter.enableReaderMode(
                activity,
                (NfcAdapter.ReaderCallback) activity,
                NfcAdapter.FLAG_READER_NFC_B | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                null);


    }

    private  void stopReaderMode(final Activity activity, NfcAdapter adapter) {
        adapter.disableReaderMode(activity);
    }




}
