package com.thefan.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class MainActivity extends Activity {

    ImageView PlayPause;
    LayerDrawable l;
    Drawable PlayIco;
    Drawable PauseIco;
    static Context context;
    boolean isPlaying = false;
    Intent streamService;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    String station = "91.8 The Fan";
    String stream = "Main Stream";
    String url = "http://198.100.144.46:8000/live";
    public static metaFetch meta = new metaFetch();

    private int mInterval = 5000; // 5 seconds by default, can be changed later
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        TextView stationName = (TextView) findViewById(R.id.player_station_name);
        TextView streamName = (TextView) findViewById(R.id.player_stream_name);
        TextView streamURL = (TextView) findViewById(R.id.player_stream_url);

        // getting intent data
        Intent in = getIntent();

        // Displaying all values on the screen
        PlayPause = (ImageView) findViewById(R.id.playpause);
        l = (LayerDrawable) getResources().getDrawable(R.drawable.pbutton);
        PlayIco = l.findDrawableByLayerId(R.id.plico);
        PauseIco = l.findDrawableByLayerId(R.id.paico);

        stationName.setText(station);
        streamName.setText("Title");
        streamURL.setText("Artist");

        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        getPrefs();
        editor = prefs.edit();
        editor.putString("URL", url);
        editor.putString("STATION", station);
        editor.commit();
        streamService = new Intent(MainActivity.this, BackgroundService.class);

        PlayPause.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (!isPlaying) {
                    mHandler = new Handler();
                    startRepeatingTask();
                    startService(streamService);
                } else {
                    stopRepeatingTask();
                    stopService(streamService);
                }
                isPlaying = !isPlaying;
            }
        });
    }

    public void getPrefs() {
        isPlaying = prefs.getBoolean("isPlaying", false);
        if (isPlaying) {
            PlayIco.setAlpha(0);
            PauseIco.setAlpha(255);
        } else {
            PlayIco.setAlpha(255);
            PauseIco.setAlpha(0);
        }
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            Log.i("Artist", "Repeat");
            MainActivity.meta.update();
            String artist = metaFetch.getArtist();
            String title = metaFetch.getTitle();

            TextView streamName = (TextView) findViewById(R.id.player_stream_name);
            TextView streamURL = (TextView) findViewById(R.id.player_stream_url);

            streamName.setText(title);
            streamURL.setText(artist);

            mHandler.postDelayed(mStatusChecker, mInterval);
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    @Override
    public void onDestroy() {
        stopRepeatingTask();
        if (isPlaying) {
            stopService(streamService);
        }
    }
}

