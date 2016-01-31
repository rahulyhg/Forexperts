/*
 * Copyright 2014 Robert Baptiste
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.forexperts.ui;

import android.content.Context;
import android.net.ConnectivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import fr.carykatz.forexperts.R;

import static fr.forexperts.util.LogUtils.LOGD;
import static fr.forexperts.util.LogUtils.LOGE;
import static fr.forexperts.util.LogUtils.makeLogTag;

public class HomeActivity extends FragmentActivity implements
        MarketOverviewFragment.Callbacks,
        PortfolioFragment.Callbacks {
    private static final String TAG = makeLogTag(HomeActivity.class);

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link android.support.v4.view.ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (!isOnline()) {
            // Ask user to connect his device
            Toast.makeText(this,
                    "Please connect your device to the Internet",
                    Toast.LENGTH_LONG).show();
            LOGD(TAG, "onCreate: Close app because device is OFFLINE");

            // Close the application
            finish();
        } else {
            // Create the adapter that will return a fragment for each of the three
            // primary sections of the app.
            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

            // Set up the ViewPager with the sections adapter.
            mViewPager = (ViewPager) findViewById(R.id.pager);
            mViewPager.setAdapter(mSectionsPagerAdapter);
            mViewPager.setOffscreenPageLimit(3);
        }
    }

    /**
     * Callback method from {@link fr.forexperts.ui.PortfolioFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelect(String code) {
        Intent detailIntent = new Intent(this, GraphActivity.class);
        detailIntent.putExtra(GraphActivity.EXTRA_CODE, code);
        startActivity(detailIntent);
    }

    // Returns whether we are connected to the internet.
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(
                Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    /**
     * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment
     * corresponding to one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return MarketOverviewFragment.newInstance();
                case 1:
                    return PortfolioFragment.newInstance();
                case 2:
                    return ArticleListFragment.newInstance();
                default:
                    LOGE(TAG, "getItem: position is unknown: " + position);
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.title_market_section);
                case 1:
                    return getString(R.string.title_portfolio_section);
                case 2:
                    return getString(R.string.title_news_section);
                default:
                    return getString(R.string.title_error_section);
            }
        }
    }
}