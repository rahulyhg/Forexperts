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
import android.content.SharedPreferences;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import fr.carykatz.forexperts.R;
import fr.forexperts.model.PortfolioListItem;
import fr.forexperts.service.DataService;

public class PortfolioFragment extends Fragment implements OnRefreshListener {
    private static Button mAddButton;
    private static ListView mListView;
    private static ListView mListViewStock;
    private static ProgressBar mProgressBar;

    private static List<PortfolioListItem> mData = new ArrayList<PortfolioListItem>();

    private static PortfolioListAdapter portfolioListAdapter;

    private PullToRefreshLayout mPullToRefreshLayout;

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
        public void onItemSelect(String position);
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
    public static PortfolioFragment newInstance() {
        return new PortfolioFragment();
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon
     * screen orientation changes).
     */
    public PortfolioFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_portfolio, container, false);

        mAddButton = (Button) rootView.findViewById(R.id.button_add_ticker);
        mListView = (ListView) rootView.findViewById(R.id.portfolioList);
        mPullToRefreshLayout = (PullToRefreshLayout) rootView.findViewById(R.id.ptr_layout);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        // Now setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(getActivity())
                .options(Options.create()
                        // Here we make the refresh scroll distance to 75% of the refreshable view's height
                        .scrollDistance(.30f)
                                // Here we define a custom header layout which will be inflated and used
                        .headerLayout(R.layout.customised_header)
                                // Here we define a custom header transformer which will alter the header
                                // based on the current pull-to-refresh state
                        .headerTransformer(new CustomisedHeaderTransformer())
                        .build())
                        // Mark All Children as pullable
                .allChildrenArePullable()
                        // Set the OnRefreshListener
                .listener(this)
                        // Finally commit the setup to our PullToRefreshLayout
                .setup(mPullToRefreshLayout);

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog();
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCallback.onItemSelect(mData.get(position).getCode());
            }
        });

        portfolioListAdapter = new PortfolioListAdapter(getActivity(), mData);
        mListView.setAdapter(portfolioListAdapter);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Bind to Service
        Intent intent = new Intent(getActivity(), DataService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        SharedPreferences prefs = getActivity().getSharedPreferences("portfolio", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Map<String, ?> values = prefs.getAll();

        mData.clear();
        portfolioListAdapter.notifyDataSetChanged();

        if (values.size() == 0) {
            mProgressBar.setVisibility(View.GONE);
            mAddButton.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.VISIBLE);

            String code = "";
            for (Map.Entry<String, ?> entre : values.entrySet()) {
                code = code + entre.getValue() + ",";
            }
            code = code.substring(0, code.length() - 1);

            editor.clear();
            editor.apply();

            AddTickerTask task = new AddTickerTask();
            task.execute(code);
        }
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
        inflater.inflate(R.menu.portfolio, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                mPullToRefreshLayout.setRefreshing(true);
                onRefreshStarted(getView());
                return true;
            case R.id.action_add:
                showAddDialog();
                return true;
            case R.id.action_order:
                ArrayList<String> codesList = new ArrayList<String>();
                for (int i = 0; i < mData.size(); i++) {
                    codesList.add(mData.get(i).getCode());
                }
                Collections.sort(codesList, ALPHABETICAL_ORDER);

                String codesLine = "";
                for (int i = 0; i < codesList.size(); i++) {
                    codesLine = codesLine + codesList.get(i) + ",";
                }

                if (codesLine.length() > 0) {
                    codesLine = codesLine.substring(0, codesLine.length() - 1);
                    mData.clear();
                    AddTickerTask addTickerTask = new AddTickerTask();
                    addTickerTask.execute(codesLine);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static Comparator<String> ALPHABETICAL_ORDER = new Comparator<String>() {
        public int compare(String str1, String str2) {
            int res = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
            return (res != 0) ? res : str1.compareTo(str2);
        }
    };

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

                AddTickerTask task = new AddTickerTask();
                task.execute(result[0]);

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

    @Override
    public void onRefreshStarted(View view) {
        String codes = "";
        for (int i = 0; i < mData.size(); i++) {
            codes = codes + mData.get(i).getCode() + ",";
        }
        if (codes.length() > 0) {
            codes = codes.substring(0, codes.length() - 1);
            mData.clear();
            AddTickerTask task = new AddTickerTask();
            task.execute(codes);
        }
    }

    public class AddTickerTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... data) {
            while (mService == null);

            String code = data[0];
            String[] codes = code.split(",");
            ArrayList<String[]> dataList = mService.downloadData(code, "nl1c1p2");

            for (int i = 0; i < dataList.size(); i++) {
                String name = codes[i];
                String nameData = dataList.get(i)[0];
                String priceData = dataList.get(i)[1];
                String netVariation = dataList.get(i)[2];
                String percentVariation = dataList.get(i)[3];

                mData.add(new PortfolioListItem(name, nameData, priceData, netVariation, percentVariation));

                SharedPreferences prefs = getActivity().getSharedPreferences("portfolio", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                editor.putString(nameData.substring(1, nameData.length() - 1), name);
                editor.apply();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            portfolioListAdapter.notifyDataSetChanged();
            mAddButton.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            // Notify PullToRefreshLayout that the refresh has finished
            mPullToRefreshLayout.setRefreshComplete();
            //callAsynchronousTask();
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
            mListViewStock.setAdapter(
                    new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, jsonStock));
        }
    }

    public class PortfolioListAdapter extends ArrayAdapter<PortfolioListItem> {

        private List<PortfolioListItem> mData;

        public PortfolioListAdapter(Context context, List<PortfolioListItem> data) {
            super(context, R.layout.portfolio_item, data);
            mData = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.portfolio_item, parent, false);
            }

            Typeface tf = Typeface.createFromAsset(getActivity().getAssets(),
                    "fonts/Roboto-Light.ttf");
            Typeface tfr = Typeface.createFromAsset(getActivity().getAssets(),
                    "fonts/Roboto-Regular.ttf");

            TextView titleView = (TextView)convertView.findViewById(R.id.code_view);
            TextView subtitleView = (TextView)convertView.findViewById(R.id.name_view);
            TextView priceView = (TextView)convertView.findViewById(R.id.price_view);
            TextView netVariationView = (TextView)convertView.findViewById(R.id.netVariation_view);
            TextView percentVariationView = (TextView)convertView.findViewById(R.id.percentVariation_view);

            PortfolioListItem object = mData.get(position);

            if (object.getCode() != null) {
                titleView.setText(object.getCode());
                titleView.setTypeface(tfr);
            }
            if (object.getName() != null) {
                subtitleView.setText(object.getName().substring(1, object.getName().length() - 1));
                subtitleView.setTypeface(tf);
            }
            if (object.getPrice() != null) {
                priceView.setText(object.getPrice());
                priceView.setTypeface(tf);
            }
            if (object.getPercentVariation() != null) {
                percentVariationView.setText("("+ object.getPercentVariation().
                        substring(1, object.getPercentVariation().length() - 1) + ")");
                percentVariationView.setTypeface(tf);
            }
            if (object.getNetVariation() != null) {
                netVariationView.setText(object.getNetVariation());
                netVariationView.setTypeface(tf);
            }

            if (netVariationView.getText() != null) {
                String signe = ((String)netVariationView.getText()).substring(0,1);
                if (signe.equals("+")) {
                    netVariationView.setText(object.getNetVariation() + "");
                    netVariationView.setTextColor(Color.parseColor("#629c21"));
                    percentVariationView.setTextColor(Color.parseColor("#629c21"));
                } else if (signe.equals("-")) {
                    netVariationView.setText("" + object.getNetVariation() + "");
                    netVariationView.setTextColor(Color.parseColor("#cb0800"));
                    percentVariationView.setTextColor(Color.parseColor("#cb0800"));
                }
            }

            return convertView;
        }
    }
}
