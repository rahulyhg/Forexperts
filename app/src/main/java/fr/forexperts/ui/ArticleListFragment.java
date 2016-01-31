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

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.util.LruCache;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import fr.carykatz.forexperts.R;
import fr.forexperts.model.RssItem;
import fr.forexperts.service.DataService;
import fr.forexperts.util.RssParser;

public class ArticleListFragment extends Fragment implements OnRefreshListener {

    /**
     * The serialization (saved instance state) Bundle key representing the activated item position.
     * Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    private LruCache<String, Bitmap> mMemoryCache;

    private Bitmap mPlaceHolderBitmap;

    private static ListView mListView;
    private static ListView mListViewStock;

    private static ProgressBar mProgressBar;

    private PullToRefreshLayout mPullToRefreshLayout;

    /**
     * Local service which gets the major cross prices
     */
    public static DataService mService;

    /**
     * Boolean flag to indicate if the activity is bind with the local service
     */
    private static boolean mBound = false;

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

    @Override
    public void onRefreshStarted(View view) {
        String RSS_LINK = "http://feeds.bbci.co.uk/news/business/economy/rss.xml";
        LoadingNewsTask task = new LoadingNewsTask();
        task.execute(RSS_LINK);
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ArticleListFragment newInstance() {
        ArticleListFragment fragment = new ArticleListFragment();
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon
     * screen orientation changes).
     */
    public ArticleListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @TargetApi(12)
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR1) {
                    return bitmap.getByteCount() / 1024;
                } else {
                    return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
                }
            }
        };

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_news, container, false);

        mListView = (ListView) rootView.findViewById(R.id.newsList);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mPullToRefreshLayout = (PullToRefreshLayout) rootView.findViewById(R.id.ptr_layout);

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

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri uri = Uri.parse(((RssItem) mListView.getAdapter().getItem(position)).getLink());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        String RSS_LINK = "http://feeds.bbci.co.uk/news/business/economy/rss.xml";
        LoadingNewsTask task = new LoadingNewsTask();
        task.execute(RSS_LINK);

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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
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
                Intent detailIntent = new Intent(getActivity(), GraphActivity.class);
                detailIntent.putExtra("code", result[0]);
                startActivity(detailIntent);
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

    public class LoadingNewsTask extends AsyncTask<String, Void, String> {

        public List<RssItem> rssItems;

        @Override
        protected String doInBackground(String... data) {
            try {
                RssParser parser = new RssParser();
                rssItems = parser.parse(getInputStream(data[0]));
            } catch (XmlPullParserException e) {
            } catch (IOException e) {
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            mProgressBar.setVisibility(View.GONE);
            mListView.setAdapter(new RssAdapter(rssItems));
            // Notify PullToRefreshLayout that the refresh has finished
            mPullToRefreshLayout.setRefreshComplete();
        }

        public InputStream getInputStream(String link) {
            try {
                URL url = new URL(link);
                return url.openStream();
            } catch (IOException e) {
                return null;
            }
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
            ArrayAdapter listAdapter =
                    new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, jsonStock);
            mListViewStock.setAdapter(listAdapter);
        }
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            mListView.setItemChecked(mActivatedPosition, false);
        } else {
            mListView.setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    private class RssAdapter extends BaseAdapter {

        private final List<RssItem> items;

        public RssAdapter(List<RssItem> items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity())
                        .inflate(R.layout.list_item_article, container, false);
            }

            Typeface tfl = Typeface.createFromAsset(getActivity().getAssets(),
                    "fonts/Roboto-Regular.ttf");
            Typeface tft = Typeface.createFromAsset(getActivity().getAssets(),
                    "fonts/Roboto-Light.ttf");

            TextView title = (TextView) convertView.findViewById(R.id.article_title);
            TextView author = (TextView) convertView.findViewById(R.id.article_author);
            TextView time = (TextView) convertView.findViewById(R.id.article_time);

            title.setText(items.get(position).getTitle());
            title.setTypeface(tfl);
            author.setText("BBC");
            author.setTypeface(tft);
            time.setText(items.get(position).getPubDate());
            time.setTypeface(tft);

            final ImageView thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
            loadBitmap(items.get(position).getThumbnail(), thumbnail);

            return convertView;
        }

        public void loadBitmap(String imageUrl, ImageView imageView) {
            final String imageKey = imageUrl;
            final Bitmap bitmap = getBitmapFromMemCache(imageKey);

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
                final AsyncDrawable asyncDrawable =
                        new AsyncDrawable(getResources(), mPlaceHolderBitmap, task);
                imageView.setImageDrawable(asyncDrawable);
                task.execute(imageUrl);
            }
        }
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 16;

        if (reqWidth == 0 || reqHeight == 0) return inSampleSize;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public static boolean cancelPotentialWork(int resId, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final int bitmapWorkerTaskResId = bitmapWorkerTask.resId;
            if (bitmapWorkerTaskResId != resId) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private int resId = 0;

        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        private Bitmap download_Image(String url) {
            // TODO: Replace this part of code
            // Just for testing, allow network access in the main thread
            // NEVER use this is productive code
            StrictMode.ThreadPolicy policy = new StrictMode.
                    ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            Bitmap bmp = null;
            try {
                URL ulrn = new URL(url);
                HttpURLConnection con = (HttpURLConnection)ulrn.openConnection();
                InputStream is = con.getInputStream();
                bmp = BitmapFactory.decodeStream(is);
            if (null != bmp)
                return bmp;
            } catch(Exception e) {
            }

            return bmp;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            return download_Image(params[0]);
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask =
                        getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap,
                             BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }
}
