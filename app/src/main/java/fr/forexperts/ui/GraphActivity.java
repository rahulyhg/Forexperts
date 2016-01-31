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

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import fr.carykatz.forexperts.R;
import fr.forexperts.util.DownloadImageTask;

import static fr.forexperts.util.LogUtils.makeLogTag;

public class GraphActivity extends Activity implements View.OnClickListener {
    private static final String TAG = makeLogTag(GraphActivity.class);

    private static Button m1dChartButton;
    private static ImageView mChart;

    private static String mStockCode;

    public static final String EXTRA_CODE = "fr.carykatz.forexperts.extra.EXTRA_CODE";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        Intent intent = getIntent();
        mStockCode = intent.getStringExtra(EXTRA_CODE);

        ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setTitle(mStockCode);
        }

        m1dChartButton = (Button) findViewById(R.id.button_1d_chart);
        Button m5dChartButton = (Button) findViewById(R.id.button_5d_chart);
        Button m1mChartButton = (Button) findViewById(R.id.button_1m_chart);
        Button m6mChartButton = (Button) findViewById(R.id.button_6m_chart);
        Button m1yChartButton = (Button) findViewById(R.id.button_1y_chart);
        Button m5yChartButton = (Button) findViewById(R.id.button_5y_chart);
        mChart = (ImageView) findViewById(R.id.chart);

        m1dChartButton.setOnClickListener(this);
        m5dChartButton.setOnClickListener(this);
        m1mChartButton.setOnClickListener(this);
        m6mChartButton.setOnClickListener(this);
        m1yChartButton.setOnClickListener(this);
        m5yChartButton.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        m1dChartButton.performClick();
    }

    @Override
    public void onClick(View v) {
        String period = "1d";
        switch (v.getId()) {
            case R.id.button_1d_chart:
                period = "1d";
                break;
            case R.id.button_5d_chart:
                period = "5d";
                break;
            case R.id.button_1m_chart:
                period = "1m";
                break;
            case R.id.button_6m_chart:
                period = "6m";
                break;
            case R.id.button_1y_chart:
                period = "1y";
                break;
            case R.id.button_5y_chart:
                period = "5y";
                break;
            default:
        }

        mChart.setTag("http://chart.finance.yahoo.com/z?s=" + mStockCode +
                "&t=" + period + "&q=l&l=off&z=l");
        DownloadImageTask imageTask = new DownloadImageTask();
        imageTask.execute(mChart);
    }
}
