package com.thefan.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends Activity {

    Button PlayPause;
    static Context context;
    boolean isPlaying;
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
        PlayPause = (Button) findViewById(R.id.playpause);

        stationName.setText(station);
        streamName.setText("Title");
        streamURL.setText("Artist");
        PlayPause.setText("Play");

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

                if (PlayPause.getText().equals("Play")) {
                    mHandler = new Handler();
                    startRepeatingTask();
                    startService(streamService);
                    PlayPause.setText("Stop");
                } else {
                    stopRepeatingTask();
                    stopService(streamService);
                    PlayPause.setText("Play");
                }
            }
        });
    }

    public void getPrefs() {
        isPlaying = prefs.getBoolean("isPlaying", false);
        if (isPlaying) {
            PlayPause.setText("Stop");
        } else {
            PlayPause.setText("Play");
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

