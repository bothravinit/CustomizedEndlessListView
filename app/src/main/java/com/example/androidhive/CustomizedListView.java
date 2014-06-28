package com.example.androidhive;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;

public class CustomizedListView extends Activity {
    // All static variables
    static final String URL = "http://api.androidhive.info/music/music.xml";
    // XML node keys
    static final String KEY_SONG = "song"; // parent node
    static final String KEY_ID = "id";
    static final String KEY_TITLE = "title";
    static final String KEY_ARTIST = "artist";
    static final String KEY_DURATION = "duration";
    static final String KEY_THUMB_URL = "thumb_url";

    private Toast mToast;
    private String mClickMessage;
    private String mScrollMessage;

    ListView list;
    ProgressBar progress;
    LazyAdapter adapter;
    ArrayList<HashMap<String, String>> songsList;

    boolean loadingInProgress = false;
    LayoutInflater inflater;
    View view;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inflater = (LayoutInflater) this.getSystemService((Context.LAYOUT_INFLATER_SERVICE));

//        View view = inflater.inflate(R.layout.list_item,null);
//        TextView viewById = (TextView) view.findViewById(R.id.test_text);
//        viewById.setText("changed");

        view = inflater.inflate(R.layout.list_item, null);

        ((TextView)view).setText("changing");



        View view1 = inflater.inflate(R.layout.progress_list_item,null);
        setContentView(R.layout.main);


        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        mToast.setGravity(Gravity.CENTER, 0, 0);


        list = (ListView) findViewById(R.id.list);


        progress = (ProgressBar) view1;//.findViewById(R.id.progress_separate);
        songsList = new ArrayList<HashMap<String, String>>();

        new LoadXML().execute();
        // Getting adapter by passing xml data ArrayList
        adapter = new LazyAdapter(this, songsList);
        list.setAdapter(adapter);


        // Click event for single list row
        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                mClickMessage = "Item clicked: " + position;
                refreshToast();
            }
        });

        list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                mScrollMessage = "Scroll (first: " + firstVisibleItem + ", count = " + visibleItemCount + ", totalCount = " + totalItemCount + ")";
                refreshToast();
                if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount > 0) {
                    if (!loadingInProgress) {
                        loadingInProgress = true;
                        new LoadXML().execute();
                    }
                }
            }
        });
        list.addHeaderView(view);
        list.addFooterView(view1);
    }

    private void refreshToast() {
        StringBuffer buffer = new StringBuffer();

        if (!TextUtils.isEmpty(mClickMessage)) {
            buffer.append(mClickMessage);
        }

        if (!TextUtils.isEmpty(mScrollMessage)) {
            if (buffer.length() != 0) {
                buffer.append("\n");
            }

            buffer.append(mScrollMessage);
        }

//        if (!TextUtils.isEmpty(mStateMessage)) {
//            if (buffer.length() != 0) {
//                buffer.append("\n");
//            }
//
//            buffer.append(mStateMessage);
//        }

        mToast.setText(buffer.toString());
        mToast.show();
    }

    public static int dip(Context context, int pixels) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pixels * scale + 0.5f);
    }

    class LoadXML extends AsyncTask<Void, Integer, ArrayList<HashMap<String, String>>> {


        @Override
        protected ArrayList<HashMap<String, String>> doInBackground(Void... params) {
            if(songsList.size() > 80)
                return null;

            ArrayList<HashMap<String, String>> songsList1 = new ArrayList<HashMap<String, String>>();
            ;

            XMLParser parser = new XMLParser();
            String xml = parser.getXmlFromUrl(URL); // getting XML from URL
            Document doc = parser.getDomElement(xml); // getting DOM element

            NodeList nl = doc.getElementsByTagName(KEY_SONG);
            // looping through all song nodes <song>
            for (int i = 0; i < nl.getLength(); i++) {
                // creating new HashMap
                HashMap<String, String> map = new HashMap<String, String>();
                Element e = (Element) nl.item(i);
                // adding each child node to HashMap key => value
                map.put(KEY_ID, parser.getValue(e, KEY_ID));
                map.put(KEY_TITLE, parser.getValue(e, KEY_TITLE));
                map.put(KEY_ARTIST, parser.getValue(e, KEY_ARTIST));
                map.put(KEY_DURATION, parser.getValue(e, KEY_DURATION));
                map.put(KEY_THUMB_URL, parser.getValue(e, KEY_THUMB_URL));

                // adding HashList to ArrayList
                songsList1.add(map);
            }
//            private void sleep() {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Log.e("THREAD", e.toString());
            }
//            }

            Log.i("list size ", String.valueOf(songsList1.size()));

            return songsList1;
        }

        @Override
        protected void onPreExecute() {
//            list.setLayoutParams(new LinearLayout.LayoutParams());
//            list.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dip(getBaseContext(),380)));
            if(songsList.size() > 80) {
                boolean b = list.removeHeaderView(view);
                Log.i(CustomizedListView.class.getCanonicalName(),"boolean : " + b);

                return;

            }

            progress.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected void onPostExecute(ArrayList<HashMap<String, String>> hashMaps) {
            if(hashMaps == null || hashMaps.size() == 0)
                return;
            songsList.addAll(hashMaps);
            adapter.notifyDataSetChanged();
            loadingInProgress = false;
            progress.setVisibility(ProgressBar.INVISIBLE);
//            if(songsList.size() == hashMaps.size())
//                list.addFooterView(progress);
            list.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        }
    }

}