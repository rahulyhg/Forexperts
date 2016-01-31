/*
 * Copyright 2015 Robert Baptiste
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

import java.util.Calendar;

public class TimeUtils {
    public static String formatMonth(Calendar c) {
        switch (c.get(Calendar.MONTH)) {
            case Calendar.JANUARY:
                return "JAN";
            case Calendar.FEBRUARY:
                return "FEB";
            case Calendar.MARCH:
                return "MAR";
            case Calendar.APRIL:
                return "APR";
            case Calendar.MAY:
                return "MAY";
            case Calendar.JUNE:
                return "JUN";
            case Calendar.JULY:
                return "JUL";
            case Calendar.AUGUST:
                return "AUG";
            case Calendar.SEPTEMBER:
                return "SEP";
            case Calendar.OCTOBER:
                return "OCT";
            case Calendar.NOVEMBER:
                return "NOV";
            case Calendar.DECEMBER:
                return "DEC";
            default:
                return "N/A";
        }
    }

    public static String formatDate(Calendar c) {
        String month = TimeUtils.formatMonth(c);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR);
        int minute = c.get(Calendar.MINUTE);
        String pm = c.get(Calendar.AM_PM) == 0 ? "AM" : "PM";

        return month + " " + day + ", " + hour + "." + minute + " " + pm + " EST";
    }

    public static String formatTime(Calendar c) {
        int hour = c.get(Calendar.HOUR);
        int minute = c.get(Calendar.MINUTE);
        String pm = c.get(Calendar.AM_PM) == 0 ? "AM" : "PM";

        return hour + "." + minute + pm;
    }
}
