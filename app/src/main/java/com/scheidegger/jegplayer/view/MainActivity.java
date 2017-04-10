package com.scheidegger.jegplayer.view;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.scheidegger.jegplayer.R;
import com.scheidegger.jegplayer.controller.DBHandler;
import com.scheidegger.jegplayer.controller.MusicItemListAdapter;
import com.scheidegger.jegplayer.model.JegMusic;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    protected final String TAG = MainActivity.class.getSimpleName();

    private ListView lstSongs;
    private TextView txtMusicName;
    private ImageButton btnPlayPause;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private HashMap<String, JegMusic> songList;
    private DBHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "in On-Create");
        setContentView(R.layout.activity_main);

        lstSongs = (ListView) this.findViewById(R.id.lst_songs);
        generateSongList();
        addSongsToList();
        lstSongs.setOnItemClickListener(this);

        btnPlayPause = (ImageButton) this.findViewById(R.id.btn_play_pause);
        btnPlayPause.setOnClickListener(this);

        this.findViewById(R.id.btn_previous).setOnClickListener(this);
        this.findViewById(R.id.btn_stop).setOnClickListener(this);
        this.findViewById(R.id.btn_next).setOnClickListener(this);

        txtMusicName = (TextView) this.findViewById(R.id.cur_music_name);
        txtMusicName.setText("");

        db = new DBHandler(this);
        addRecordsToDB();

    }

    private List<JegMusic> getSortedSongList(Map<String, JegMusic> map) {
        List<JegMusic> result = new ArrayList<>(map.values());
        Collections.sort(result);
        return result;
    }

    private void generateSongList() {
        songList = new HashMap<>();

        songList.put("bensoundbrazilsamba",
                new JegMusic(0,"bensoundbrazilsamba","bensoundbrazilsamba.mp3","Brazil",240,
                "Samba is a Brazilian musical genre and dance style, with its roots in Africa " +
                        "via the West African slave trade and African religious traditions, " +
                        "particularly of Angola"));
        songList.put("bensoundcountryboy",
                new JegMusic(0,"bensoundcountryboy","bensoundcountryboy.mp3","USA",207,
                "Country music is a genre of American popular music that originated in the " +
                        "Southern United States in the 1920s"));
        songList.put("bensoundindia",
                new JegMusic(0,"bensoundindia","bensoundindia.mp3","India",253,
                "The music of India includes multiple varieties of folk music, pop, and Indian " +
                        "classical music. India's classical music tradition, including " +
                        "Hindustani music and Carnatic, has a history spanning millennia and " +
                        "developed over several eras"));
        songList.put("bensoundlittleplanet",
                new JegMusic(0,"bensoundlittleplanet","bensoundlittleplanet.mp3","Iceland",
                396, "The music of Iceland includes vibrant folk and pop traditions. Well-known " +
                        "artists from Iceland include medieval music group Voces Thules, " +
                        "alternative rock band The Sugarcubes, singers Björk and Emiliana " +
                        "Torrini, postrock band Sigur Rós and indie folk/indie pop band Of " +
                        "Monsters and Men"));
        songList.put("bensoundpsychedelic",
                new JegMusic(0,"bensoundpsychedelic","bensoundpsychedelic.mp3","South Korea",236,
                "The Music of South Korea has evolved over the course of the decades since the " +
                        "end of the Korean War, and has its roots in the music of the Korean " +
                        "people, who have inhabited the Korean peninsula for over a millennium. " +
                        "Contemporary South Korean music can be divided into three different " +
                        "main categories: Traditional Korean folk music, popular music, or " +
                        "Kpop, and Westerninfluenced non-popular music"));
        songList.put("bensoundrelaxing",
                new JegMusic(0,"bensoundrelaxing","bensoundrelaxing.mp3","Indonesia",288,
                "The music of Indonesia demonstrates its cultural diversity, the local musical " +
                        "creativity, as well as subsequent foreign musical influences that shaped " +
                        "contemporary music scenes of Indonesia. Nearly thousands of Indonesian islands " +
                        "having its own cultural and artistic history and character"));
        songList.put("bensoundtheelevatorbossanova",
                new JegMusic(0,"bensoundtheelevatorbossanova",
                "bensoundtheelevatorbossanova.mp3","Brazil",254,
                "Samba is a Brazilian musical genre and dance style, with its roots in Africa " +
                        "via the West African slave trade and African religious traditions, " +
                        "particularly of Angola"));

        Field[] fields = R.raw.class.getFields();
        for(int count=0; count < fields.length; count++){
            Field curField = fields[count];

            String fileName = curField.getName();
            int fileResId = getResources().getIdentifier(fileName,"raw",this.getPackageName());
            Log.i(TAG, "Found Res Id [" + fileResId + "] for " + fileName);
            songList.get(fileName).setResId(fileResId);
        }

    }

    private void addRecordsToDB() {

        for (JegMusic music : songList.values()) {
            db.addMusic(music);
        }
    }

    private void addSongsToList() {
        //Get the list of songs
        Log.i(TAG, "in addSongsToList()");

        MusicItemListAdapter sngListAdapter = new MusicItemListAdapter(getSortedSongList(songList), getApplicationContext());
        lstSongs.setAdapter(sngListAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View clickedView, int position, long id) {
        Log.i(TAG, "in onItemClick()");
        JegMusic music = getSortedSongList(songList).get(position);
        String musicName = music.getName();

        /*
        String message = "You clicked position " + position + " which is id: " + id + " and String " + musicName;
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        */

        playMusic(musicName);
    }

    public void playMusic(String musicName){
        Log.i(TAG, "On Play Music");

        txtMusicName.setText(musicName);

        int soundId = songList.get(musicName).getResId();
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(this, soundId);
        Log.i(TAG, "Playing song " + soundId);
        mediaPlayer.start();
        btnPlayPause.setImageResource(R.drawable.control_pause);
        CustomNotification(songList.get(musicName));

    }

    public void stopMusic() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        btnPlayPause.setImageResource(R.drawable.control_play);

        NotificationManager manager = (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(0);

    }

    @Override
    public void onClick(View clickedView) {
        Log.i(TAG, "in On-Click");
        String musicName;
        switch (clickedView.getId()) {
            // Play + Pause
            case R.id.btn_play_pause :
                if (mediaPlayer!=null) {
                    if(mediaPlayer.isPlaying()){
                        mediaPlayer.pause();
                        btnPlayPause.setImageResource(R.drawable.control_play);
                    } else {
                        Log.i(TAG,"Not Playing");
                        mediaPlayer.start();
                        btnPlayPause.setImageResource(R.drawable.control_pause);
                    }
                } else {
                    musicName = txtMusicName.getText().toString();
                    if(musicName.equals("")){
                        Toast.makeText(this, "No music selected!", Toast.LENGTH_SHORT).show();
                    } else {
                        playMusic(musicName);
                    }
                }
                break;

            // Stop
            case R.id.btn_stop :
                if (mediaPlayer!=null) {
                    stopMusic();
                }
                break;

            case R.id.btn_previous :
                musicName = txtMusicName.getText().toString();
                if (!musicName.isEmpty()) {
                    List<JegMusic> sortedArray = getSortedSongList(songList);
                    int id = sortedArray.indexOf(songList.get(musicName));
                    playMusic(sortedArray.get((id>0?id-1:id)).getName());
                }
                break;

            case R.id.btn_next :
                musicName = txtMusicName.getText().toString();
                if (!musicName.isEmpty()) {
                    List<JegMusic> sortedArray = getSortedSongList(songList);
                    int id = sortedArray.indexOf(songList.get(musicName));
                    id = (id<sortedArray.size()-1?id+1:id);
                    playMusic(sortedArray.get(id).getName());
                }
                break;
            default :
                Log.e(TAG, "Button click not implemented!");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "in On-Destroy");
        if (isFinishing()) {
            Log.i(TAG, "Finishing activity");
            stopMusic();
        } else {
            Log.i(TAG, "Not finishing");
        }
    }

    public void CustomNotification(JegMusic music) {
        // Using RemoteViews to bind custom layouts into Notification
        RemoteViews remoteViews = new RemoteViews(getPackageName(),
                R.layout.musicnotification);

        // Set Notification Title
        String strSongTitle = music.getName();
        // Set Notification Text
        String strCountry = music.getCountry();

        // Open NotificationView Class on Notification Click
        Intent intent = new Intent(this, MusicNotificationView.class);
        // Send data to NotificationView Class
        intent.putExtra("title", strSongTitle);
        intent.putExtra("text", strCountry);


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
        notificationmanager.notify(0, builder.build());

    }
}
