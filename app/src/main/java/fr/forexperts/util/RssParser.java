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

package fr.forexperts.util;

import android.util.Xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import fr.forexperts.model.RssItem;

public class RssParser {

    public List<RssItem> parse(InputStream inputStream) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            inputStream.close();
        }
    }

    private List<RssItem> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "rss");
        String title = null;
        String link = null;
        String pubDate = null;
        String thumbnail = null;
        String text = null;
        List<RssItem> items = new ArrayList<RssItem>();

        int event = parser.getEventType();
        while (event != XmlPullParser.END_DOCUMENT) {
            String name = parser.getName();
            switch (event){
                case XmlPullParser.START_TAG:
                    break;
                case XmlPullParser.TEXT:
                    text = parser.getText();
                    break;

                case XmlPullParser.END_TAG:
                    if (name.equals("title")) {
                        title = text;
                    } else if (name.equals("link")) {
                        link = text;
                    } else if (name.equals("pubDate")) {
                        pubDate = modifyPubDate(text);;
                    } else if (name.equals("media:thumbnail")) {
                        if (parser.getAttributeValue(null, "width").equals("144")) {
                            thumbnail = parser.getAttributeValue(null, "url");
                        }
                    }
                    break;
            }
            event = parser.next();

            if (title != null && link != null && pubDate != null && thumbnail != null) {
                RssItem item = new RssItem(title, link, pubDate, thumbnail);
                items.add(item);

                title = null;
                link = null;
                pubDate = null;
                thumbnail = null;
            }
        }
        return items;
    }

    private String modifyPubDate(String pubDate) {
        String time;

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);

        int pubDay = Integer.parseInt(pubDate.substring(5, 7));
        String pubHourString = pubDate.substring(17, 19);
        int pubHour = Integer.parseInt(pubHourString);
        String pubMinuteString = pubDate.substring(20, 22);

        if (day == pubDay) {
            if ((hour - pubHour) < 12) {
                if ((hour - pubHour) == 1) {
                    time = (hour - pubHour) + " hour ago";
                } else {
                    time = (hour - pubHour) + " hours ago";
                }
            } else {
                time = pubHourString + "." + pubMinuteString + " GMT";
            }
        } else {
            time = pubHourString + "." + pubMinuteString + " GMT";
        }

        return time;
    }
}