package org.keyple.plugin.androidnfc;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.keyple.seproxy.exceptions.IOReaderException;

/**
 * Created by Olivier Delcroix on 05/03/2018.
 */

public class AndroidNfcFragment extends Fragment {


    private static final String TAG = AndroidNfcFragment.class.getSimpleName();

    private NfcAdapter nfcAdapter;


    public AndroidNfcFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment NFCFragment.
     */
    public static AndroidNfcFragment newInstance() {
        AndroidNfcFragment fragment = new AndroidNfcFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // Must be set to true
        Log.d(TAG, "onCreate");

        nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());

        if (nfcAdapter == null){
            Log.w(TAG, "Your device does not support NFC");
        };

        if(!nfcAdapter.isEnabled()){
            Log.w(TAG, "PLease enable NFC to communicate with NFC Elements");
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return null;
    }


    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "onResume");

        //if the fragment was created following an intent of NfcAdapter.ACTION_TECH_DISCOVERED TAG
        Intent intent = getActivity().getIntent();
        Log.d(TAG, "Intent : " + intent.getAction());
        if(intent != null &&
                intent.getAction()!=null &&
                intent.getAction().equals(NfcAdapter.ACTION_TECH_DISCOVERED)){
            //handle intent
            try {
                Log.d(TAG, "Handle ACTION TECH intent");

                ((AndroidNfcReader) AndroidNfcPlugin.getInstance().getReaders().get(0)).processIntent(intent);

            } catch (IOReaderException e) {
                e.printStackTrace();
            }
        }


        Bundle options = new Bundle();
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 5000);

        try {
            nfcAdapter.enableReaderMode(
                    getActivity(),
                    ((AndroidNfcReader) AndroidNfcPlugin.getInstance().getReaders().get(0)),
                    NfcAdapter.FLAG_READER_NFC_B | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                    null);

        } catch (IOReaderException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "on Pause Fragment");
        Log.i(TAG, "Stopping Read Write Mode");

        nfcAdapter.disableReaderMode(getActivity());

    }




    @Override
    public void onDetach() {
        super.onDetach();
    }


}
