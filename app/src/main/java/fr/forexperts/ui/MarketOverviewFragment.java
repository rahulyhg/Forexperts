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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import fr.forexperts.util.TimeUtils;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fr.carykatz.forexperts.R;
import fr.forexperts.model.MarketListItem;
import fr.forexperts.service.DataService;
import fr.forexperts.ui.widget.LineChartView;

public class MarketOverviewFragment extends Fragment implements OnRefreshListener {
    private static final String TAG = "MarketOverviewFragment";

    private static ListView mListView;
    private static ListView mListViewStock;
    private static LineChartView mLineChartView;
    private static ProgressBar mProgressBar;
    private static PullToRefreshLayout mPullToRefreshLayout;
    private static TextView mStartDateChart;
    private static TextView mEndDateChart;

    private static List<MarketListItem> mData = new ArrayList<>();

    private static int[][] COLOR_BAR = {new int[]{62, 122, 224},
            new int[]{253, 153, 32},
            new int[]{218, 49, 15}};
    private static String[] CODE = {"^FCHI", "^GDAXI", "^FTSE"};

    /**
     * Local service which gets the major cross prices
     */
    public static DataService mService;

    /**
     * Boolean flag to indicate if the activity is bind with the local service
     */
    private static boolean mBound = false;

    /**
     * The fragment's current callback object, which is notified of list item clicks.
     */
    private Callbacks mCallback;

    /**
     * A callback interface that all activities containing this fragment must implement. This
     * mechanism allows activities to be notified of item selections.
     */
    public interface Callbacks {
        void onItemSelect(String position);
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            DataService.LocalBinder binder = (DataService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
            mBound = false;
        }
    };

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static MarketOverviewFragment newInstance() {
        return new MarketOverviewFragment();
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the fragment
     */
    public MarketOverviewFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement onItemSelect");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_market_overview, container, false);

        mListView = (ListView) rootView.findViewById(R.id.indexList);
        mLineChartView = (LineChartView) rootView.findViewById(R.id.chart);
        mPullToRefreshLayout = (PullToRefreshLayout) rootView.findViewById(R.id.ptr_layout);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mStartDateChart = (TextView) rootView.findViewById(R.id.startDateChart);
        mEndDateChart = (TextView) rootView.findViewById(R.id.endDateChart);

        // Setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(getActivity())
                .options(Options.create()
                        .scrollDistance(.30f)
                        .headerLayout(R.layout.customised_header)
                        .headerTransformer(new CustomisedHeaderTransformer())
                        .build())
                .allChildrenArePullable()
                .listener(this)
                .setup(mPullToRefreshLayout);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCallback.onItemSelect(CODE[position]);
            }
        });

        Calendar c = Calendar.getInstance();
        String month = TimeUtils.formatMonth(c);
        int day = c.get(Calendar.DAY_OF_MONTH);
        String date = Integer.toString(day) + " " + month;
        mEndDateChart.setText(date);

        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/Roboto-Light.ttf");
        mStartDateChart.setTypeface(tf);
        mEndDateChart.setTypeface(tf);

        MarketOverviewTask marketOverviewTask = new MarketOverviewTask();
        marketOverviewTask.execute(CODE[0] + "," + CODE[1] + "," + CODE[2]);
        DownloadHistoricalDataTask downloadHistoricalDataTask = new DownloadHistoricalDataTask();
        downloadHistoricalDataTask.execute();

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Bind to Service
        Intent intent = new Intent(getActivity(), DataService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            getActivity().unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.market, menu);
    }

    // TODO
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                mPullToRefreshLayout.setRefreshing(true);
                onRefreshStarted(getView());
                return true;
            case R.id.action_search:
                showAddDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRefreshStarted(View view) {
        MarketOverviewTask marketOverviewTask = new MarketOverviewTask();
        marketOverviewTask.execute(CODE[0] + "," + CODE[1] + "," + CODE[2]);
    }

    public void showAddDialog() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View alertView = inflater.inflate(R.layout.alertdialog_add_value, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(alertView);
        final AlertDialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setSoftInputMode (WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.TOP | Gravity.CENTER;
        wmlp.x = 10;
        wmlp.y = 10;

        final EditText mStockName = (EditText) alertView.findViewById(R.id.stockName);
        mListViewStock = (ListView) alertView.findViewById(R.id.listViewStock);
        mListViewStock.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String text = (String)parent.getItemAtPosition(position);
                String[] result = text.split(" ");
                mCallback.onItemSelect(result[0]);
                dialog.dismiss();
            }
        });

        mStockName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                SuggestStockTask task = new SuggestStockTask();
                task.execute(mStockName.getText().toString());
            }
        });

        // TODO: Change hard coded width
        mStockName.setWidth(10000);

        dialog.show();
    }

    public class MarketOverviewTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... data) {
            while (mService == null);

            mData.clear();

            String code = data[0];
            ArrayList<String[]> dataList = mService.downloadData(code, "nl1c1p2");


            for (int i = 0; i < dataList.size(); i++) {
                String name = dataList.get(i)[0];
                String priceData = dataList.get(i)[1];
                String netVariation = dataList.get(i)[2];
                String percentVariation = dataList.get(i)[3];

                mData.add(new MarketListItem(name, priceData, netVariation, percentVariation, COLOR_BAR[i]));
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            mListView.setAdapter(new MarketArrayAdapter(mData));
            // Notify PullToRefreshLayout that the refresh has finished
            mPullToRefreshLayout.setRefreshComplete();
        }
    }

    public class DownloadHistoricalDataTask extends AsyncTask<String, Void, String> {
        List<Float> mDataCAC40 = new ArrayList<Float>();
        List<Float> mDataDAX = new ArrayList<Float>();
        List<Float> mDataFTSE = new ArrayList<Float>();

        @Override
        protected String doInBackground(String... data) {
            while (mService == null);

            ArrayList<String[]> dataList = mService.downloadHistoricalData(CODE[0]);

            for (int i = 1; i < dataList.size(); i++) {
                float close = Float.parseFloat(dataList.get(i)[4]);
                float percent = (close - Float.parseFloat(dataList.get(1)[1])) * 100 / Float.parseFloat(dataList.get(1)[1]);
                mDataCAC40.add(percent);
            }

            dataList = mService.downloadHistoricalData(CODE[1]);

            for (int i = 1; i < dataList.size(); i++) {
                float close = Float.parseFloat(dataList.get(i)[4]);
                float percent = (close - Float.parseFloat(dataList.get(1)[1])) * 100 / Float.parseFloat(dataList.get(1)[1]);
                mDataDAX.add(percent);
            }

            dataList = mService.downloadHistoricalData(CODE[2]);

            for (int i = 1; i < dataList.size(); i++) {
                float close = Float.parseFloat(dataList.get(i)[4]);
                float percent = (close - Float.parseFloat(dataList.get(1)[1])) * 100 / Float.parseFloat(dataList.get(1)[1]);
                mDataFTSE.add(percent);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            float[] mCac40 = new float[mDataCAC40.size()];
            for (int i = 0; i < mDataCAC40.size(); i++) {
                mCac40[i] = mDataCAC40.get(i);
                mCac40[i] = Math.abs(mCac40[i]);
            }

            float[] mDax = new float[mDataDAX.size()];
            for (int i = 0; i < mDataDAX.size(); i++) {
                mDax[i] = mDataDAX.get(i);
                mDax[i] = Math.abs(mDax[i]);
            }

            float[] mFtse = new float[mDataFTSE.size()];
            for (int i = 0; i < mDataFTSE.size(); i++) {
                mFtse[i] = mDataFTSE.get(i);
                mFtse[i] = Math.abs(mFtse[i]);
            }

            float[][] data = {mCac40, mDax, mFtse};
            mLineChartView.setChartData(data);
            mLineChartView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
        }
    }

    public class SuggestStockTask extends AsyncTask<String, Void, String[]> {
        @Override
        protected String[] doInBackground(String... name) {
            while (mService == null);

            return mService.findStockName(name[0]);
        }

        @Override
        protected void onPostExecute(String[] jsonStock) {
            ArrayAdapter listAdapter = new ArrayAdapter<>(
                    getActivity(),
                    android.R.layout.simple_list_item_1,
                    jsonStock);
            mListViewStock.setAdapter(listAdapter);
        }
    }

    private class MarketArrayAdapter extends BaseAdapter {
        private List<MarketListItem> mData;

        public MarketArrayAdapter(List<MarketListItem> data) {
            mData = data;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).
                        inflate(R.layout.market_list_view_item, container, false);
            }

            Typeface tf = Typeface.createFromAsset(getActivity().getAssets(),
                    "fonts/Roboto-Light.ttf");

            TextView nameView =
                    (TextView) convertView.findViewById(R.id.name_view);
            TextView priceView =
                    (TextView) convertView.findViewById(R.id.price_view);
            TextView netVariationView =
                    (TextView) convertView.findViewById(R.id.netVariation_view);
            TextView percentVariationView =
                    (TextView) convertView.findViewById(R.id.percentVariation_view);
            View colorBarView =
                    convertView.findViewById(R.id.colorBar_view);

            MarketListItem object = mData.get(position);

            if (object.getName() != null) {
                nameView.setText(object.getName().substring(1, object.getName().length() - 1));
                nameView.setTypeface(tf);
            }

            if (object.getPrice() != null) {
                priceView.setText(object.getPrice());
                priceView.setTypeface(tf);
            }

            if (object.getNetVariation() != null) {
                netVariationView.setText(object.getNetVariation());
                netVariationView.setTypeface(tf);
            }

            if (object.getPercentVariation() != null) {
                percentVariationView.setText("(" + object.getPercentVariation().
                        substring(2, object.getPercentVariation().length() - 1) + ")");
                percentVariationView.setTypeface(tf);
            }
            colorBarView.setBackgroundColor(Color.rgb(object.getColorBar(0), object.getColorBar(1),
                        object.getColorBar(2)));

            if (netVariationView.getText() != null) {
                String signe = ((String) netVariationView.getText()).substring(0,1);
                setTextColor(signe, netVariationView);
                setTextColor(signe, percentVariationView);
            }

            return convertView;
        }

        private void setTextColor(String signe, TextView textView) {
            if (signe.equals("+")) {
                textView.setTextColor(Color.parseColor("#629c21"));
            } else if (signe.equals("-")) {
                textView.setTextColor(Color.parseColor("#cb0800"));
            }
        }
    }
}