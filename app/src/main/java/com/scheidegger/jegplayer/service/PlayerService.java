package com.scheidegger.jegplayer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.scheidegger.jegplayer.R;
import com.scheidegger.jegplayer.controller.MusicItemListAdapter;
import com.scheidegger.jegplayer.model.JegMusic;
import com.scheidegger.jegplayer.view.MusicDetail;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class PlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    private final String TAG = PlayerService.class.getSimpleName();

    public static final int MSG_CLIENT_REGISTER = 0;
    public static final int MSG_CLIENT_UNREGISTER = 1;
    public static final int MSG_UPDATE_PLAYER_STATUS = 2;
    public static final int MSG_PLAY_MUSIC_BY_RES_ID = 3;
    public static final int MSG_STOP_MUSIC = 4;
    public static final int MSG_PLAY_MUSIC = 5;
    public static final int MSG_PAUSE_MUSIC = 6;
    public static final int MSG_TOGGLE_MUSIC = 7;
    public static final int MSG_CHECK_PLAYER_STATE = 8;
    public static final int MSG_UPDATE_ELAPSED_TIME = 9;
    public static final int MSG_PLAY_MUSIC_BY_JEG_MUSIC = 10;

    public static final int PLAYER_ST_PLAYING = 0;
    public static final int PLAYER_ST_PAUSED = 1;
    public static final int PLAYER_ST_STOPPED = 2;

    private int totalDuration;
    private int currentDuration;

    private Timer timer = new Timer();

    ArrayList<Messenger> mClients = new ArrayList<>(); // Keeps track of all current registered clients.
    final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target we publish for clients to send messages to IncomingHandler.

    private MediaPlayer mediaPlayer;
    private static boolean mIsRunning = false;
    private JegMusic currentMusic;

    public static boolean isRunning()
    {
        return mIsRunning;
    }

    private class IncomingHandler extends Handler { @Override
        public void handleMessage(Message msg) {
        Log.i(TAG, "received " + msg.what);
            switch (msg.what) {
                case MSG_CLIENT_REGISTER:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_CLIENT_UNREGISTER:
                    mClients.remove(msg.replyTo);
                    Log.i(TAG, "Total clients: " + mClients.size());
                    break;
                /*case MSG_PLAY_MUSIC_BY_RES_ID:
                    musicResId = msg.arg1;
                    Log.i(TAG,"Play " + musicResId);
                    playSong(musicResId);
                    break;*/
                case MSG_STOP_MUSIC:
                    Log.i(TAG,"Stop");
                    stopSong();
                    break;
                case MSG_PLAY_MUSIC:
                    Log.i(TAG,"Play " );
                    playSong();
                    break;
                case MSG_PAUSE_MUSIC:
                    Log.i(TAG,"Pause " );
                    pauseSong();
                    break;
                case MSG_TOGGLE_MUSIC:
                    toggleMusic();
                    break;
                case MSG_CHECK_PLAYER_STATE:
                    checkPlayerStatus();
                    break;
                case MSG_PLAY_MUSIC_BY_JEG_MUSIC:
                    currentMusic = (JegMusic) msg.obj;
                    playSong(currentMusic);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void checkPlayerStatus() {
        if (mediaPlayer!=null) {
            if(mediaPlayer.isPlaying()){
                updateUIPlayerStatus(PLAYER_ST_PLAYING);
            } else {
                Log.i(TAG,"Not Playing");
                updateUIPlayerStatus(PLAYER_ST_STOPPED);
            }
        } else {
            updateUIPlayerStatus(PLAYER_ST_STOPPED);
        }
    }

    private void toggleMusic() {
        if (mediaPlayer!=null) {
            if(mediaPlayer.isPlaying()){
                pauseSong();
            } else {
                Log.i(TAG,"Not Playing");
                playSong(currentMusic);
            }
        } else {
            playSong(currentMusic);
        }
    }

    private void updateElapsedTime() {
        for (int i = mClients.size()-1; i >= 0; i--) {
            try {
                // Send data as an Integer
                mClients.get(i).send(Message.obtain(null, MSG_UPDATE_ELAPSED_TIME, currentDuration, totalDuration));
            }
            catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }

    private void updateUIPlayerStatus(int intvaluetosend) {
        for (int i = mClients.size()-1; i >= 0; i--) {
            try {
                // Send data as an Integer
                mClients.get(i).send(Message.obtain(null, MSG_UPDATE_PLAYER_STATUS, intvaluetosend, 0));
            }
            catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }

    private void stopSong() {
        if(mediaPlayer!=null) {
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        updateUIPlayerStatus(PLAYER_ST_STOPPED);

        NotificationManager manager = (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2121);

    }

    private void playSong(JegMusic currentMusic) {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(this, currentMusic.getResId());
        Log.i(TAG, "Playing song " + currentMusic.getName());
        playSong();
        CustomNotification(currentMusic);
    }

    private void playSong() {
        mediaPlayer.start();
        updateUIPlayerStatus(PLAYER_ST_PLAYING);
        totalDuration = mediaPlayer.getDuration();
    }

    private void pauseSong() {
        mediaPlayer.pause();
        updateUIPlayerStatus(PLAYER_ST_PAUSED);
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "in On-Create");
        super.onCreate();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);

        timer.scheduleAtFixedRate(new TimerTask(){ public void run() {onTimerTick();}}, 0, 100L);

    }

    private void onTimerTick() {
        if (mediaPlayer != null) {
            synchronized(mediaPlayer) {
                if (mediaPlayer.isPlaying()) {
                    currentDuration = mediaPlayer.getCurrentPosition();
                    updateElapsedTime();
                }
            }
        }
        if(mClients.size()<=0){
            Log.i(TAG, "" + mClients.size());
            stopForeground(true);
            stopSong();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "In On-Bind");
        return mMessenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        Log.i(TAG, "Service Started");
        return START_STICKY;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.i(TAG,"In On-Completion");
        updateUIPlayerStatus(PLAYER_ST_STOPPED);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }

    public void CustomNotification(JegMusic music) {
        // Using RemoteViews to bind custom layouts into Notification
        RemoteViews remoteViews = new RemoteViews(getPackageName(),
                R.layout.musicnotification);

        // Set Notification Song Title
        String strSongTitle = music.getName();
        // Set Notification Song Country
        String strCountry = music.getCountry();

        // Open NotificationView Class on Notification Click
        Intent intent = new Intent(this, MusicDetail.class);
        // Send data to NotificationView Class
        intent.putExtra("title", strSongTitle);
        intent.putExtra("text", strCountry);
        intent.putExtra("description", music.getDescription());

        // Open NotificationView.java Activity
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        // Set Icon
        builder.setSmallIcon(R.mipmap.ic_launcher)
                // Set Ticker Message
                .setTicker("Jeg Player")
                // Dismiss Notification
                .setAutoCancel(false)
                // Set PendingIntent into Notification
                .setContentIntent(pIntent)
                //Set notification as ongoing
                .setOngoing(true)
                // Set RemoteViews into Notification
                .setContent(remoteViews);

        // Locate and set the Image into customnotificationtext.xml ImageViews
        remoteViews.setImageViewResource(R.id.imagenotileft,R.mipmap.ic_launcher);
        remoteViews.setImageViewResource(R.id.imagenotiright,
                MusicItemListAdapter.getFlagResIdByCountryName(strCountry));

        // Locate and set the Text into customnotificationtext.xml TextViews
        remoteViews.setTextViewText(R.id.title, strSongTitle);
        remoteViews.setTextViewText(R.id.text, strCountry);

        // Create Notification Manager
        NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Build Notification with Notification Manager
        Notification n = builder.build();
        notificationmanager.notify(2121, n);
        startForeground(2121, n);

    }
    
}
