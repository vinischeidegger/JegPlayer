package com.scheidegger.jegplayer.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.scheidegger.jegplayer.R;
import com.scheidegger.jegplayer.model.JegMusic;
import java.util.List;

public class MusicItemListAdapter extends ArrayAdapter<JegMusic> {

    Context mContext;
    private List<JegMusic> dataset;
    private int lastPosition = -1;

    public MusicItemListAdapter(List<JegMusic> data, Context context) {
        super(context, R.layout.song_list_item, data);
        this.dataset = data;
        mContext = context;
    }

    // View lookup cache
    private static class ViewHolder {
        TextView txtMusicName;
        TextView txtCountry;
        TextView txtDuration;
        ImageView flag;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        JegMusic music = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        //final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.song_list_item, parent, false);
            viewHolder.txtMusicName = (TextView) convertView.findViewById(R.id.list_item_music_name);
            viewHolder.txtCountry = (TextView) convertView.findViewById(R.id.list_item_country);
            viewHolder.txtDuration = (TextView) convertView.findViewById(R.id.list_item_length);
            viewHolder.flag = (ImageView) convertView.findViewById(R.id.list_item_flag);

            //result = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            //result = convertView;
        }

        lastPosition = position;

        viewHolder.txtMusicName.setText(music.getName());
        viewHolder.txtCountry.setText(music.getCountry());
        int duration = music.getLength();
        viewHolder.txtDuration.setText(String.format("%d:%02d", duration / 60, duration % 60));
        //viewHolder.flag.setOnClickListener(this);
        //viewHolder.info.setTag(position);

        // Return the completed view to render on screen
        return convertView;
    }
}
