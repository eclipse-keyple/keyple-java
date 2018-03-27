package org.keyple.examples.androidnfc;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.keyple.commands.InconsistentCommandException;
import org.keyple.example.common.AbstractLogicManager;
import org.keyple.example.common.BasicCardAccessManager;
import org.keyple.plugin.android.omapi.AndroidOmapiPlugin;
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

import java.util.ArrayList;
import java.util.List;


public class OMAPITestFragment extends Fragment implements ReaderObserver,
        Topic.Subscriber<AbstractLogicManager.Event> {


    private static final String TAG = OMAPITestFragment.class.getSimpleName();
    private static final String TAG_OMAPI_ANDROID_FRAGMENT = "keyple-omapi-android-fragment";

    private TextView mText;

    private AbstractLogicManager cardAccessManager;

    public static OMAPITestFragment newInstance() {
        return new OMAPITestFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize SEProxy with Android Plugin
        Log.d(TAG, "Initialize SEProxy with Android OMAPI Plugin ");
        SeProxyService seProxyService = SeProxyService.getInstance();
        List<ReadersPlugin> plugins = new ArrayList<ReadersPlugin>();
        plugins.add(AndroidOmapiPlugin.getInstance());
        seProxyService.setPlugins(plugins);

        // add NFC Fragment to activity in order to communicate with Android Plugin
        Log.d(TAG, "Add OMAPI Fragment to activity in order " +
                "to communicate with Android OMAPI Plugin");
        Fragment omapi = AndroidNfcFragment.newInstance();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragtrans = fm.beginTransaction();
        fragtrans.add(omapi, "omapi");
        fragtrans.commit();




        try {
            // define task as an observer for ReaderEvents
            Log.d(TAG, "Define acticity as observer for all ReaderEvents");
            for (ProxyReader reader : seProxyService.getPlugins().get(0).getReaders()){
                ((ObservableReader) reader).addObserver(this);
            }


        } catch (IOReaderException e) {
            e.printStackTrace();
        }


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_omapi_test, container, false);

        mText = (TextView) view.findViewById(R.id.text);
        mText.setText("Setting up Open Mobile API plugin for Keyple.");

        try {
            for(ProxyReader reader : SeProxyService.getInstance().getPlugins().get(0).getReaders()){
                mText.append("\nConnected to reader : " + reader.getName());
            }
        } catch (IOReaderException e) {
            e.printStackTrace();
        }

        return view;


    }


    /**
     * Observes Card Access when an event is received
     *
     * @param event
     */
    public void update(AbstractLogicManager.Event event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mText.append("\n ---- \n");
                mText.append(event.toString());
            }
        });
    }

    /**
     * Management of SE insertion event to operate a ticketing processing
     *
     * @param readerEvent : event received from SEProxyService
     */
    @Override
    public void notify(final ReaderEvent readerEvent) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "New ReaderEvent received : " +
                        readerEvent.getEventType().toString());

                switch (readerEvent.getEventType()) {
                    case SE_INSERTED:
                        mText.append("\n ---- \n");
                        mText.append("Tag detected");
                        try {


                            cardAccessManager = new BasicCardAccessManager();
                            ((BasicCardAccessManager) cardAccessManager).setPoReader(readerEvent.getReader());
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
     * Revocation of the Activity from @{@link org.keyple.plugin.androidnfc.AndroidNfcReader} list
     * of observers
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            Log.d(TAG, "Remove task as an observer for ReaderEvents");
            SeProxyService seProxyService = SeProxyService.getInstance();
            for (ProxyReader reader : seProxyService.getPlugins().get(0).getReaders()){
                ((ObservableReader) reader).deleteObserver(this);
            }

            FragmentManager fm = getFragmentManager();
            Fragment f = fm.findFragmentByTag(TAG_OMAPI_ANDROID_FRAGMENT);
            if(f!=null){
                fm.beginTransaction().remove(f).commit();
            }

        } catch (IOReaderException e) {
            e.printStackTrace();
        }
    }


}
