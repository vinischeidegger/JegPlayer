package com.scheidegger.jegplayer.view;

import android.content.Intent;
import android.support.annotation.StringDef;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.scheidegger.jegplayer.R;

public class MusicDetail extends AppCompatActivity {

    private final String TAG = MusicDetail.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_detail);

        Intent intent = getIntent();

        String musicName = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");

//        intent.putExtra("text", strCountry);

        Log.i(TAG, musicName + " - " + description);
    }
}
