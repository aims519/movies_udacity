package com.aimtech.android.movies;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Andy on 12/03/2016.
 */
public class PopularMoviesAdapter extends RecyclerView.Adapter<PopularMoviesAdapter.PopularMoviesViewHolder> {

    private static final String LOG_TAG = PopularMoviesAdapter.class.getSimpleName();
    private List<Movie> mMovielist;
    private Context mContext;

    // Constructor for Adapter
    public PopularMoviesAdapter(List<Movie> movieList,Context context) {
        this.mMovielist = movieList;
        this.mContext = context;
    }


    // Inner ViewHolder Class
    public class PopularMoviesViewHolder extends RecyclerView.ViewHolder {
        protected TextView mTitleTextView;
        protected ImageView mPosterImageView;

        public PopularMoviesViewHolder(View view) {
            super(view);
            mPosterImageView = (ImageView) view.findViewById(R.id.poster_image_view);
        }
    }


    // Create new Views (invoked by the layout manager in MainActivity)
    @Override
    public PopularMoviesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Create new view, setting attachToRoot = false
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.popular_movie_item,parent,false);

        return new PopularMoviesViewHolder(v);
    }


    //Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(PopularMoviesViewHolder holder, final int position) {

        final Movie currentMovie = mMovielist.get(position);

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Picasso.with(mContext)
                .load(currentMovie.getPosterImageUrl())
                .placeholder(R.drawable.no_image_placeholder)
                .into(holder.mPosterImageView);

        // Add an onClick listener to the item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent toDetailIntent = new Intent();
                toDetailIntent.putExtra(MainActivity.EXTRA_MOVIE_OVERVIEW,currentMovie.getOverview())
                        .putExtra(MainActivity.EXTRA_MOVIE_TITLE,currentMovie.getTitle())
                        .putExtra(MainActivity.EXTRA_MOVIE_ID,currentMovie.getId())
                        .putExtra(MainActivity.EXTRA_MOVIE_BACKDROP_URL, currentMovie.getBackdropUrl())
                        .putExtra(MainActivity.EXTRA_MOVIE_RELEASE_DATE,currentMovie.getReleaseDate())
                        .putExtra(MainActivity.EXTRA_MOVIE_RATING,currentMovie.getVoteAverage())
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .setClass(mContext, MovieDetailActivity.class);
                mContext.startActivity(toDetailIntent);
            }
        });

    };




    // Need to have this...
    @Override
    public int getItemCount() {
        if (mMovielist != null) {
            return mMovielist.size();
        } else {
            return 0;
        }
    }
}
