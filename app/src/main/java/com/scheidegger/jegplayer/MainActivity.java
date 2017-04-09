package com.scheidegger.jegplayer;

import android.content.res.Resources;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    protected final String TAG = MainActivity.class.getSimpleName();

    private ListView lstSongs;
    private Button btnPlayPause;
    private MediaPlayer mediaPlayer = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "in On-Create");
        setContentView(R.layout.activity_main);

        lstSongs = (ListView) this.findViewById(R.id.lst_songs);
        addSongsToList();
        lstSongs.setOnItemClickListener(this);

        btnPlayPause = (Button) this.findViewById(R.id.btn_play_pause);
        btnPlayPause.setOnClickListener(this);


    }

    private void addSongsToList() {
        //Get the list of songs
        Log.i(TAG, "in addSongsToList()");
        List<String> songList = new ArrayList<>();

        Field[] fields = R.raw.class.getFields();
        for(int count=0; count < fields.length; count++){
            Field curField = fields[count];

            String fileName = curField.getName();
            Log.i("Raw Asset: ", fileName);
            songList.add(fileName);
        }

        ArrayAdapter<String> sngListAdapter = new ArrayAdapter<>(this, R.layout.song_list_item, songList);

        lstSongs.setAdapter(sngListAdapter);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View clickedView, int position, long id) {
        Log.i(TAG, "in onItemClick()");
        TextView textView = (TextView) clickedView;
        String musicName = textView.getText().toString();
        String message = "You clicked position " + position + " which is id: " + id + " and String " + musicName;
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        Resources res = this.getResources();
        int soundId = res.getIdentifier(musicName, "raw", this.getPackageName());
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(this, soundId);
        mediaPlayer.start();
        btnPlayPause.setText("Pause");
    }

    @Override
    public void onClick(View clickedView) {
        Log.i(TAG, "in On-Click");
        switch (clickedView.getId()) {
            case R.id.btn_play_pause :
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    btnPlayPause.setText("Play");
                } else {
                    mediaPlayer.start();
                    btnPlayPause.setText("Pause");
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
            mediaPlayer.release();
            mediaPlayer = null;
        } else {
            Log.i(TAG, "Not finishing");
        }
    }
}
