package com.scheidegger.jegplayer.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.scheidegger.jegplayer.R;
import com.scheidegger.jegplayer.controller.DBHandler;
import com.scheidegger.jegplayer.controller.MusicItemListAdapter;
import com.scheidegger.jegplayer.service.PlayerService;
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
    private TextView txtElapsed;
    private ImageButton btnPlayPause;
    private Messenger mService;
    final Messenger mMessenger = new Messenger(new JegPlayerMessageHandler());
    private HashMap<String, JegMusic> songList;
    private DBHandler db;
    private boolean mIsPlayerServiceRunning;
    private boolean isPlaying;
    private boolean mIsBound;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i(TAG, "In On-Service-Connected");
            mService = new Messenger(service);
            Log.i(TAG, "Service Attached");
            try {
                Message msg = Message.obtain(null, PlayerService.MSG_CLIENT_REGISTER);
                msg.replyTo = mMessenger;
                mService.send(msg);
            }
            catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.i(TAG, "In On-Service-Disconnected");
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
            Toast.makeText(getApplicationContext(), "Service Disconnected.", Toast.LENGTH_SHORT).show();
        }
    };

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
        txtElapsed = (TextView) this.findViewById(R.id.cur_music_elapsed);
        txtElapsed.setText("");

        db = new DBHandler(this);
        addRecordsToDB();

        restoreFromBundle(savedInstanceState);

        startPlayerService();

        registerForContextMenu(lstSongs);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        Log.i(TAG, "In On-Create-Context-Menu");
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Select The Action");
        menu.add(0, v.getId(), 0, "Play from Start");//groupId, itemId, order, title
        menu.add(0, v.getId(), 0, "Description");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        Log.i(TAG, info.id + " - "+item.getItemId());
        JegMusic selMusic = getSortedSongList(songList).get((int)info.id);

        if(item.getTitle()=="Play from Start"){
            playMusicFromService(selMusic);
            txtMusicName.setText(selMusic.getName());
        }
        else if(item.getTitle()=="Description"){
            Intent intent = new Intent(this, MusicDetail.class);
            intent.putExtra("title", selMusic.getName());
            intent.putExtra("text", selMusic.getCountry());
            intent.putExtra("description", selMusic.getDescription());
            startActivity(intent);
        }else{
            return false;
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "in On-Save-Instance");
        super.onSaveInstanceState(outState);
        outState.putString("musicName", txtMusicName.getText().toString());
        outState.putBoolean("playing", isPlaying);
    }

    private void restoreFromBundle(Bundle savedState) {
        Log.i(TAG, "in Restore-From-Bundle");
        if (savedState!=null) {
            txtMusicName.setText(savedState.getString("musicName"));
            isPlaying = savedState.getBoolean("playing");
            setPlayPauseImage(isPlaying);
        }
    }

    private void startPlayerService() {
        Log.i(TAG, "Starting Player Service");

        startService(new Intent(this, PlayerService.class));

        doBindService();
        mIsPlayerServiceRunning = true;
    }

    private void sendMessageToService(int messageId, int arg) {
        if(mIsPlayerServiceRunning) {
            if(mService != null) {
                try {
                    Message msg = Message.obtain(null, messageId, arg, -1);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }
                catch (RemoteException e) {
                }
            } else {
                Log.i(TAG, "Service is not bound");
            }
        }
    }

    private void playMusicFromService(int resId){
        sendMessageToService(PlayerService.MSG_PLAY_MUSIC_BY_RES_ID, resId);
    }

    private void playMusicFromService(JegMusic jegMusic) {
        if(mIsPlayerServiceRunning) {
            if(mService != null) {
                try {
                    Message msg = Message.obtain(null, PlayerService.MSG_PLAY_MUSIC_BY_JEG_MUSIC, jegMusic);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }
                catch (RemoteException e) {
                }
            }
        }
    }

    private void stopMusicFromService() {
        sendMessageToService(PlayerService.MSG_STOP_MUSIC, -1);
    }

    private void toggleFromMusicService() {
        sendMessageToService(PlayerService.MSG_TOGGLE_MUSIC, -1);
    }

    private void pauseMusicFromService() {
        sendMessageToService(PlayerService.MSG_PAUSE_MUSIC, -1);
    }

    private void continueMusicFromService() {
        sendMessageToService(PlayerService.MSG_PLAY_MUSIC, -1);
    }

    private void stopPlayerService() {
        stopService(new Intent(getBaseContext(), PlayerService.class));
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "In On-Resume");
        super.onResume();
        checkPlayerStatus();
    }

    private void checkPlayerStatus() {
        Log.i(TAG, "Checking Player Status");
        sendMessageToService(PlayerService.MSG_CHECK_PLAYER_STATE, -1);
    }


    private List<JegMusic> getSortedSongList(Map<String, JegMusic> map) {
        List<JegMusic> result = new ArrayList<>(map.values());
        Collections.sort(result);
        return result;
    }

    private void generateSongList() {
        songList = new HashMap<>();

        songList.put("bensoundbrazilsamba",
                new JegMusic(0,"bensoundbrazilsamba","bensoundbrazilsamba.mp3","Brazil",240000,
                "Samba is a Brazilian musical genre and dance style, with its roots in Africa " +
                        "via the West African slave trade and African religious traditions, " +
                        "particularly of Angola"));
        songList.put("bensoundcountryboy",
                new JegMusic(0,"bensoundcountryboy","bensoundcountryboy.mp3","USA",207000,
                "Country music is a genre of American popular music that originated in the " +
                        "Southern United States in the 1920s"));
        songList.put("bensoundindia",
                new JegMusic(0,"bensoundindia","bensoundindia.mp3","India",253000,
                "The music of India includes multiple varieties of folk music, pop, and Indian " +
                        "classical music. India's classical music tradition, including " +
                        "Hindustani music and Carnatic, has a history spanning millennia and " +
                        "developed over several eras"));
        songList.put("bensoundlittleplanet",
                new JegMusic(0,"bensoundlittleplanet","bensoundlittleplanet.mp3","Iceland",396000,
                        "The music of Iceland includes vibrant folk and pop traditions. Well-known " +
                        "artists from Iceland include medieval music group Voces Thules, " +
                        "alternative rock band The Sugarcubes, singers Björk and Emiliana " +
                        "Torrini, postrock band Sigur Rós and indie folk/indie pop band Of " +
                        "Monsters and Men"));
        songList.put("bensoundpsychedelic",
                new JegMusic(0,"bensoundpsychedelic","bensoundpsychedelic.mp3","South Korea",236000,
                "The Music of South Korea has evolved over the course of the decades since the " +
                        "end of the Korean War, and has its roots in the music of the Korean " +
                        "people, who have inhabited the Korean peninsula for over a millennium. " +
                        "Contemporary South Korean music can be divided into three different " +
                        "main categories: Traditional Korean folk music, popular music, or " +
                        "Kpop, and Westerninfluenced non-popular music"));
        songList.put("bensoundrelaxing",
                new JegMusic(0,"bensoundrelaxing","bensoundrelaxing.mp3","Indonesia",288000,
                "The music of Indonesia demonstrates its cultural diversity, the local musical " +
                        "creativity, as well as subsequent foreign musical influences that shaped " +
                        "contemporary music scenes of Indonesia. Nearly thousands of Indonesian islands " +
                        "having its own cultural and artistic history and character"));
        songList.put("bensoundtheelevatorbossanova",
                new JegMusic(0,"bensoundtheelevatorbossanova",
                "bensoundtheelevatorbossanova.mp3","Brazil",254000,
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

        playMusic(musicName);
    }

    public void playMusic(String musicName){
        Log.i(TAG, "On Play Music");
        txtMusicName.setText(musicName);

        int soundId = songList.get(musicName).getResId();

        playMusicFromService(soundId);
        playMusicFromService(songList.get(musicName));
        btnPlayPause.setImageResource(R.drawable.control_pause);
    }

    public void stopMusic() {
        stopMusicFromService();
        btnPlayPause.setImageResource(R.drawable.control_play);
    }

    @Override
    public void onClick(View clickedView) {
        Log.i(TAG, "in On-Click");
        String musicName = txtMusicName.getText().toString();
        switch (clickedView.getId()) {
            // Play + Pause
            case R.id.btn_play_pause :
                if(isPlaying) {
                    pauseMusicFromService();
                } else {
                    continueMusicFromService();
                }
                break;

            // Stop
            case R.id.btn_stop :
                stopMusic();
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
            //stopMusic();
            doUnbindService();
        } else {
            Log.i(TAG, "Not finishing");
        }
    }

    void doBindService() {
        bindService(new Intent(this, PlayerService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }
    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, PlayerService.MSG_CLIENT_UNREGISTER);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }
                catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    private class JegPlayerMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PlayerService.MSG_UPDATE_PLAYER_STATUS:
                    updatePlayerStatus(msg.arg1);
                    break;
                case PlayerService.MSG_UPDATE_SONG_NAME:
                    String str1 = msg.obj.toString();
                    txtMusicName.setText(str1);
                    break;
                case PlayerService.MSG_UPDATE_ELAPSED_TIME:
                    updateElapsed(msg.arg1, msg.arg2);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void updateElapsed(int curElapsed, int totalDuration) {
        curElapsed = (curElapsed>0?curElapsed:0);
        txtElapsed.setText(String.format("%d:%02d", curElapsed / 60000, (curElapsed % 60000) / 1000));
    }

    private void updatePlayerStatus(int playerStatus) {
        switch (playerStatus){
            case PlayerService.PLAYER_ST_PAUSED:
            case PlayerService.PLAYER_ST_STOPPED:
                isPlaying = false;
                break;
            case PlayerService.PLAYER_ST_PLAYING:
                isPlaying = true;
                break;
        }
        setPlayPauseImage(isPlaying);
    }

    private void setPlayPauseImage(Boolean isPlaying) {
        if(isPlaying) {
            btnPlayPause.setImageResource(R.drawable.control_pause);
        } else {
            btnPlayPause.setImageResource(R.drawable.control_play);
        }
    }
}
