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

package fr.forexperts.model;

public class StockDetailsItem {

    private String mName;
    private String mStockEchange;
    private String mPrice;
    private String mNetVariation;
    private String mPercentVariation;
    private String mLow;
    private String mHigh;
    private String mOpen;
    private String mVolume;
    private String mMktCap;
    private String mAvgVol;
    private String mYrLow;
    private String mYrHigh;

    public StockDetailsItem(String name, String stockEchange, String price, String netVariation,
                            String percentVariation, String low, String high, String open,
                            String volume, String mktCap, String avgVol, String yrLow, String yrHigh) {
        mName = name;
        mStockEchange = stockEchange;
        mPrice = price;
        mNetVariation = netVariation;
        mPercentVariation = percentVariation;
        mLow = low;
        mHigh = high;
        mOpen = open;
        mVolume = volume;
        mMktCap = mktCap;
        mAvgVol = avgVol;
        mYrLow = yrLow;
        mYrHigh = yrHigh;
    }

    public String getName() {
        return mName;
    }

    public String getStockEchange() {
        return mStockEchange;
    }

    public String getPrice() {
        return mPrice;
    }

    public String getNetVariation() {
        return mNetVariation;
    }

    public String getPercentVariation() { return mPercentVariation; }

    public String getLow() { return mLow; }

    public String getHigh() { return mHigh; }

    public String getOpen() { return mOpen; }

    public String getVolume() { return mVolume; }

    public String getMktCap() { return mMktCap; }

    public String getAvgVol() { return mAvgVol; }

    public String getYrLow() { return mYrLow; }

    public String getYrHigh() { return mYrHigh; }
}