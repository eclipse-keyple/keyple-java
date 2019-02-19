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
package org.eclipse.keyple.example.android.omapi;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;


/**
 * Example of Android application to test
 * 
 * @{@link org.eclipse.keyple.plugin.android.omapi.AndroidOmapiPlugin}
 *
 */
public class MainActivity extends AppCompatActivity {


    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String TAG_OMAPI_TEST_VIEW = "tagomapitestfragment";


    private DrawerLayout mDrawerLayout;

    /**
     * Setup navigation drawer
     * 
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * Define UI elements
         */
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        setupNavDrawer();
        /**
         * end of Define UI elements
         */
        activateOMAPITestView();

    }

    /**
     * Inject OMAPI Test View fragment into activity
     */
    private void activateOMAPITestView() {
        // init OMAPI Test Fragment
        Log.d(TAG, "Insert OMAPI Test View  Fragment");
        OMAPITestFragment omapiTestFragment = OMAPITestFragment.newInstance();
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, omapiTestFragment, TAG_OMAPI_TEST_VIEW)
                .addToBackStack(null).commit();
    }

    /**
     * Configure Navigation Drawer
     */
    private void setupNavDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here
                        Log.d(TAG, "Item selected from drawer: " + menuItem.getTitle());

                        switch (menuItem.getItemId()) {
                            case R.id.nav_omapi:
                                activateOMAPITestView();
                                break;
                            default:
                                throw new IllegalArgumentException("Invalid menuItem");

                        }
                        return true;
                    }
                });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);



    }

    // 'Open' event for navigation drawer
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
