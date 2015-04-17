package com.thefan.android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import java.io.IOException;

public class BackgroundService extends Service {
    private static final String TAG = "StreamService";
    private static boolean isPlaying;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    String url;
    String station;

    // Change this int to some number specifically for this app
    int notifId = 918;

    private int mInterval = 5000; // 5 seconds by default, can be changed later
    private Handler mHandler;

    String artist;
    String title;

    // MediaStyle Notification
    Notification n;
    NotificationManager notificationManager;
    MediaPlayer mp;
    MediaSessionCompat ms;
    Bitmap artwork = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_launcher);

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        // Init the SharedPreferences and Editor
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        url = prefs.getString("URL", "http://176.31.115.196:8214/");
        station = prefs.getString("STATION", "FOOBAR");
        editor = prefs.edit();

        // Set up the buffering notification
        notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(NOTIFICATION_SERVICE);
        Context context = getApplicationContext();

        String notifTitle = context.getResources().getString(R.string.app_name);
        String notifMessage = context.getResources().getString(R.string.buffering);

        n = new Notification();
        n.icon = R.drawable.ic_launcher;
        n.tickerText = "Buffering";
        n.when = System.currentTimeMillis();

        Intent nIntent = new Intent(context, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, nIntent, 0);

        n.setLatestEventInfo(context, notifTitle, notifMessage, pIntent);

        notificationManager.notify(notifId, n);

        ComponentName c = new ComponentName("com.thefan.android", "BackgroundService");
        ms = new MediaSessionCompat(this, "TheFan", c,  pIntent);
        ms.setMetadata(new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, artwork)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Pink Floyd")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "Dark Side of the Moon")
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "The Great Gig in the Sky")
                .build());
        // Indicate you're ready to receive media commands
        ms.setActive(true);
        // Attach a new Callback to receive MediaSession updates
        ms.setCallback(new MediaSessionCompat.Callback() {
            // Implement your callbacks
        });
        // Indicate you want to receive transport controls via your Callback
        ms.setFlags(MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        // Create a new Notification
        final Notification noti = new Notification.Builder(this)
                // Hide the timestamp
                .setShowWhen(false)
                        // Set the Notification style
                .setStyle(new Notification.MediaStyle()
                        // Attach our MediaSession token
                        .setMediaSession(ms.getSessionToken())
                                // Show our playback controls in the compat view
                        .setShowActionsInCompactView(0, 1, 2))
                        // Set the Notification color
                .setColor(0xFFDB4437)
                        // Set the large and small icons
                .setLargeIcon(artwork)
                .setSmallIcon(R.drawable.your_small_icon)
                        // Set Notification content information
                .setContentText("Pink Floyd")
                .setContentInfo("Dark Side of the Moon")
                .setContentTitle("The Great Gig in the Sky")
                        // Add some playback controls
                .addAction(R.drawable.your_prev_icon, "prev", retreivePlaybackAction(3))
                .addAction(R.drawable.your_pause_icon, "pause", retreivePlaybackAction(1))
                .addAction(R.drawable.your_next_icon, "next", retreivePlaybackAction(2))
                .build();

        // Do something with your TransportControls
        final MediaControllerCompat.TransportControls controls = ms.getController().getTransportControls();

        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(1, noti);

        mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mp.setDataSource(url);
            mp.prepare();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "SecurityException");
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "IllegalStateException");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "IOException");
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(TAG, "onStart");
        mp.start();
        // Set the isPlaying preference to true
        editor.putBoolean("isPlaying", true);
        editor.commit();

        mHandler = new Handler();
        startRepeatingTask();

        isPlaying = true;

        Context context = getApplicationContext();
        String notifTitle = context.getResources().getString(R.string.app_name);
        String notifMessage = context.getResources().getString(R.string.now_playing);

        n.icon = R.drawable.ic_launcher;
        n.tickerText = notifMessage;
        n.flags = Notification.FLAG_NO_CLEAR;
        n.when = System.currentTimeMillis();

        Intent nIntent = new Intent(context, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, nIntent, 0);

        n.setLatestEventInfo(context, notifTitle, notifMessage, pIntent);
        // Change 5315 to some nother number
        notificationManager.notify(notifId, n);
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            Log.i("Artist", "Repeat");
            MainActivity.meta.update();
            artist = metaFetch.getArtist();
            title = metaFetch.getTitle();

            Context context = getApplicationContext();
            String notifTitle = title;
            String notifMessage = artist;

            n.icon = R.drawable.ic_launcher;
            n.tickerText = notifMessage;
            n.flags = Notification.FLAG_NO_CLEAR;
            n.when = System.currentTimeMillis();

            Intent nIntent = new Intent(context, MainActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(context, 0, nIntent, 0);

            n.setLatestEventInfo(context, notifTitle, notifMessage, pIntent);
            // Change 5315 to some nother number
            notificationManager.notify(notifId, n);

            mHandler.postDelayed(mStatusChecker, mInterval);
        }
    };

    private void notifBuilder() {

    }

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    public static boolean getIsPlaying() {
        return isPlaying;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        stopRepeatingTask();
        isPlaying = false;
        mp.stop();
        mp.release();
        mp = null;
        editor.putBoolean("isPlaying", false);
        editor.commit();
        notificationManager.cancel(notifId);
    }

}