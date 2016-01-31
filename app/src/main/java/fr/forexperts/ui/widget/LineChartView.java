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

package fr.forexperts.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.View;

public class LineChartView extends View {

    private static final int MIN_LINES = 4;
    private static final int MAX_LINES = 7;
    private static final int[] DISTANCES = {1, 2, 5};
    private static final int[][] COLORS = {{62, 122, 224}, {253, 153, 32}, {218, 49, 15}};

    private float[][] datapoints;
    private Paint paint = new Paint();

    public LineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Sets the y data points of the line chart. The data points are assumed to
     * be positive and equally spaced on the x-axis. The line chart will be
     * scaled so that the entire height of the view is used.
     *
     * @param   datapoints   y values of the line chart
     */
    public void setChartData(float[][] datapoints) {
        this.datapoints = datapoints.clone();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBackground(canvas);
        drawLineChart(canvas);
    }

    private void drawBackground(Canvas canvas) {
        float maxValue = getMax(datapoints);
        int range = getLineDistance(maxValue);

        paint.setStyle(Style.STROKE);
        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(1);
        for (int y = 0; y < maxValue; y += range) {
            final float yPos = getYPos(y);
            canvas.drawLine(0, yPos, getWidth(), yPos, paint);
        }
    }

    private void drawLineChart(Canvas canvas) {
        for (int j = 0; j < datapoints.length; j++) {
            DrawingPath path = new DrawingPath(2, Color.rgb(COLORS[j][0], COLORS[j][1], COLORS[j][2]));
            path.moveTo(getXPos(0), getYPos(datapoints[j][0]));
            for (int i = 1; i < datapoints[j].length; i++) {
                path.lineTo(getXPos(i), getYPos(datapoints[j][i]));
            }

            paint.setStyle(Style.STROKE);
            paint.setStrokeWidth(path.Width);
            paint.setColor(path.Color);
            paint.setAntiAlias(true);
            paint.setShadowLayer(4, 2, 2, 0x80000000);
            paint.setShadowLayer(0, 0, 0, 0);
            canvas.drawPath(path, paint);
        }
    }

    private int getLineDistance(float maxValue) {
        int distance;
        int distanceIndex = 0;
        int distanceMultiplier = 1;
        int numberOfLines = MIN_LINES;

        do {
            distance = DISTANCES[distanceIndex] * distanceMultiplier;
            numberOfLines = (int) FloatMath.ceil(maxValue / distance);

            distanceIndex++;
            if (distanceIndex == DISTANCES.length) {
                distanceIndex = 0;
                distanceMultiplier *= 10;
            }
        } while (numberOfLines < MIN_LINES || numberOfLines > MAX_LINES);

        return distance;
    }

    private float getMax(float[][] array) {
        float max = array[0][0];
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                if (array[i][j] > max) {
                    max = array[i][j];
                }
            }
        }
        return max;
    }

    private float getYPos(float value) {
        float height = getHeight() - getPaddingTop() - getPaddingBottom();
        float maxValue = getMax(datapoints);

        // scale it to the view size
        value = (value / maxValue) * height;

        // invert it so that higher values have lower y
        value = height - value;

        // offset it to adjust for padding
        value += getPaddingTop();

        return value;
    }

    private float getXPos(float value) {
        float width = getWidth() - getPaddingLeft() - getPaddingRight();
        float maxValue = datapoints[0].length - 1;

        // scale it to the view size
        value = (value / maxValue) * width;

        // offset it to adjust for padding
        value += getPaddingLeft();

        return value;
    }

    private class DrawingPath extends Path {
        public int Color;
        public float Width;

        public DrawingPath(float w, int c) {
            Color = c;
            Width = w;
        }
    }
}