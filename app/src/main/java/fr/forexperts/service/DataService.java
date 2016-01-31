/*
 * Copyright (C) 2014 Robert Baptiste
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.forexperts.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;

import static fr.forexperts.util.LogUtils.LOGE;

/**
 * This service uses the yahoo finance API.
 * It permits to get the current price of the major crosses.
 */
public class DataService extends Service {
    /**
     * Binder given to clients
     */
    private final IBinder mBinder = new LocalBinder();

    /**
     * Class used for the client Binder
     */
    public class LocalBinder extends Binder {
        public DataService getService() {
            // Return this instance of LocalService so clients can call public methods
            return DataService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /*********************************************************************************************
     *
     * Method for clients
     *
     *********************************************************************************************/

    /**
     * Download the price for the given cross.
     */
    public ArrayList<String[]> downloadData(String cross, String option) {
        String url = null;

        try {
            url = "http://download.finance.yahoo.com/d/quotes.csv?s=" + URLEncoder.encode(cross, "UTF-8") +
                        "&f=" + option + "&e=.csv";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return getCsvFromUrl(url);
    }

    /**
     * Download historical data for the given cross
     */
    public ArrayList<String[]> downloadHistoricalData(String code) {
        String url = null;

        try {
            Calendar c = Calendar.getInstance();
            int day = c.get(Calendar.DAY_OF_MONTH);
            int month = c.get(Calendar.MONTH);
            int year = c.get(Calendar.YEAR);

            url = "http://ichart.yahoo.com/table.csv?s=" + URLEncoder.encode(code, "UTF-8") +
                    "&a=0&b=1&c=2015&d=" + month + "&e=" + day + "&f=" + year + "&g=d&ignore=.csv";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return getCsvFromUrl(url);
    }

    private static ArrayList<String[]> getCsvFromUrl(String url) {
        ArrayList<String[]> data = new ArrayList<String[]>();

        try {
            HttpGet httpGet = new HttpGet(url);
            HttpParams httpParameters = new BasicHttpParams();
            DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);

            HttpResponse response = httpClient.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();

            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] price = line.split(",");
                    data.add(price);
                }
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    public String[] findStockName(String query) {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();

        try {
            HttpGet httpGet = new HttpGet("http://d.yimg.com/autoc.finance.yahoo.com/autoc?query=" +
                    URLEncoder.encode(query, "UTF-8") + "&region=US&lang=en-US&callback=" +
                    "YAHOO.util.ScriptNodeDataSource.callbacks");

            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();

            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));

                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return parseJson(builder.toString());
    }

    public String[] parseJson(String jsonText) {
        jsonText = jsonText.replace("YAHOO.util.ScriptNodeDataSource.callbacks(", "");
        String[] text = null;

        try {
            JSONObject data = new JSONObject(jsonText);
            JSONObject resultSet = data.getJSONObject("ResultSet");
            JSONArray result = resultSet.getJSONArray("Result");

            text = new String[result.length()];

            for (int i = 0; i < result.length(); i++) {
                JSONObject stock = result.getJSONObject(i);

                String code = stock.getString("symbol");
                String name = stock.getString("name");
                String exchange = "";

                if (stock.has("exchDisp")) {
                    exchange = stock.getString("exchDisp");
                }

                text[i] = code + " : " + name + " (" + exchange + ")";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return text;
    }
}