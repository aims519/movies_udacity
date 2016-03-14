package com.aimtech.android.movies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Andy on 14/03/2016.
 */
public class TrailerListAdapter extends ArrayAdapter<Trailer> {

    // Constructor
    public TrailerListAdapter(Context context, List<Trailer> objects) {
        super(context,0, objects);
    }

    // Descible how to
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Trailer trailer = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.trailer_list_item,parent,false);
        }



        TextView trailerName = (TextView) convertView.findViewById(R.id.trailer_name_text_view);
        trailerName.setText(trailer.getTitle());

        return convertView;

    }
}
