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

public class PortfolioListItem {

    private String mCode;
    private String mName;
    private String mPrice;
    private String mNetVariation;
    private String mPercentVariation;

    public PortfolioListItem(String code, String name, String price, String netVariation,
                             String percentVariation) {
        mCode = code;
        mName = name;
        mPrice = price;
        mNetVariation = netVariation;
        mPercentVariation = percentVariation;
    }

    public String getCode() {
        return mCode;
    }

    public String getName() {
        return mName;
    }

    public String getPrice() {
        return mPrice;
    }

    public String getNetVariation() {
        return mNetVariation;
    }

    public String getPercentVariation() { return mPercentVariation; }
}