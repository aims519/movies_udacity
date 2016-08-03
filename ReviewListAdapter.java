package com.aimtech.android.movies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Andy on 15/03/2016.
 */
public class ReviewListAdapter extends ArrayAdapter<Review> {

    // Constructor
    public ReviewListAdapter(Context context, List<Review> objects) {
        super(context, 0, objects);
    }

    // Descible how to
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Review review = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.review_list_item, parent, false);
        }

        TextView reviewAuthor = (TextView) convertView.findViewById(R.id.review_author_text_view);
        TextView reviewContent = (TextView) convertView.findViewById(R.id.review_content_text_view);
        TextView reviewUrl = (TextView) convertView.findViewById(R.id.review_url);

        reviewAuthor.setText("Author : " + review.getAuthor());
        reviewContent.setText(review.getContent());
        reviewUrl.setText("Full Review : " + review.getUrl());

        return convertView;

    }
}
