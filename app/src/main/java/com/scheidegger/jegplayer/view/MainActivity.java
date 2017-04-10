package com.scheidegger.jegplayer.view;

import android.content.res.Resources;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.scheidegger.jegplayer.R;
import com.scheidegger.jegplayer.controller.DBHandler;
import com.scheidegger.jegplayer.controller.MusicItemListAdapter;
import com.scheidegger.jegplayer.model.JegMusic;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    protected final String TAG = MainActivity.class.getSimpleName();

    private ListView lstSongs;
    private TextView txtMusicName;
    private ImageButton btnPlayPause;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private List<JegMusic> songList;
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

        db = new DBHandler(this);
        addRecordsToDB();

    }

    private void generateSongList() {
        songList = new ArrayList<>();

        songList.add(new JegMusic(0,"bensoundbrazilsamba","bensoundbrazilsamba.mp3","Brazil",240,
                "Samba is a Brazilian musical genre and dance style, with its roots in Africa " +
                        "via the West African slave trade and African religious traditions, " +
                        "particularly of Angola"));
        songList.add(new JegMusic(0,"bensoundcountryboy","bensoundcountryboy.mp3","USA",207,
                "Country music is a genre of American popular music that originated in the " +
                        "Southern United States in the 1920s"));
        songList.add(new JegMusic(0,"bensoundindia","bensoundindia.mp3","India",253,
                "The music of India includes multiple varieties of folk music, pop, and Indian " +
                        "classical music. India's classical music tradition, including " +
                        "Hindustani music and Carnatic, has a history spanning millennia and " +
                        "developed over several eras"));
        songList.add(new JegMusic(0,"bensoundlittleplanet","bensoundlittleplanet.mp3","Iceland",
                396, "The music of Iceland includes vibrant folk and pop traditions. Well-known " +
                        "artists from Iceland include medieval music group Voces Thules, " +
                        "alternative rock band The Sugarcubes, singers Björk and Emiliana " +
                        "Torrini, postrock band Sigur Rós and indie folk/indie pop band Of " +
                        "Monsters and Men"));
        songList.add(new JegMusic(0,"bensoundpsychdelic","bensoundpsychdelic.mp3","South Korea",236,
                "The Music of South Korea has evolved over the course of the decades since the " +
                        "end of the Korean War, and has its roots in the music of the Korean " +
                        "people, who have inhabited the Korean peninsula for over a millennium. " +
                        "Contemporary South Korean music can be divided into three different " +
                        "main categories: Traditional Korean folk music, popular music, or " +
                        "Kpop, and Westerninfluenced non-popular music"));
        songList.add(new JegMusic(0,"bensoundrelaxing","bensoundrelaxing.mp3","Indonesia",288,
                "The music of Indonesia demonstrates its cultural diversity, the local musical " +
                        "creativity, as well as subsequent foreign musical influences that shaped " +
                        "contemporary music scenes of Indonesia. Nearly thousands of Indonesian islands " +
                        "having its own cultural and artistic history and character"));
        songList.add(new JegMusic(0,"bensoundtheelavatorbossanova",
                "bensoundtheelavatorbossanova.mp3","Brazil",254,
                "Samba is a Brazilian musical genre and dance style, with its roots in Africa " +
                        "via the West African slave trade and African religious traditions, " +
                        "particularly of Angola"));
    }

    private void addRecordsToDB() {

        for (JegMusic music : songList) {
            db.addMusic(music);
        }
    }

    private void addSongsToList() {
        //Get the list of songs
        Log.i(TAG, "in addSongsToList()");

        //TODO : Compare list against DB
        /*
        Field[] fields = R.raw.class.getFields();
        for(int count=0; count < fields.length; count++){
            Field curField = fields[count];

            String fileName = curField.getName();
            Log.i("Raw Asset: ", fileName);
            songList.add(fileName);
        }
        */

        MusicItemListAdapter sngListAdapter = new MusicItemListAdapter(songList, getApplicationContext());
        lstSongs.setAdapter(sngListAdapter);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View clickedView, int position, long id) {
        Log.i(TAG, "in onItemClick()");
        JegMusic music = songList.get(position);
        String musicName = music.getName();

        /*
        String message = "You clicked position " + position + " which is id: " + id + " and String " + musicName;
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        */

        txtMusicName.setText(musicName);
        playMusic(musicName);
    }

    public void playMusic(String musicName){
        Log.i(TAG, "On Play Music");
        Resources res = this.getResources();
        int soundId = res.getIdentifier(musicName, "raw", this.getPackageName());
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(this, soundId);
        Log.i(TAG, "Playing song " + soundId);
        mediaPlayer.start();
        btnPlayPause.setImageResource(R.drawable.control_pause);

    }

    @Override
    public void onClick(View clickedView) {
        Log.i(TAG, "in On-Click");
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
                    String musicName = txtMusicName.getText().toString();
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
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                    btnPlayPause.setImageResource(R.drawable.control_play);
                }
                break;

            case R.id.btn_previous :
                if (mediaPlayer!=null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                break;

            case R.id.btn_next :
                if (mediaPlayer!=null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
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
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        } else {
            Log.i(TAG, "Not finishing");
        }
    }
}
