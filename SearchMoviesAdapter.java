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
 * Created by Andy on 13/03/2016.
 */
public class SearchMoviesAdapter extends RecyclerView.Adapter<SearchMoviesAdapter.SearchMoviesViewHolder> {

    private static final String LOG_TAG = PopularMoviesAdapter.class.getSimpleName();
    private List<Movie> mMovielist;
    private Context mContext;

    // Constructor for Adapter
    public SearchMoviesAdapter(List<Movie> movieList, Context context) {
        this.mMovielist = movieList;
        this.mContext = context;
    }


    // Inner ViewHolder Class
    public class SearchMoviesViewHolder extends RecyclerView.ViewHolder {
        protected TextView mTitleTextView;
        protected ImageView mPosterImageView;
        protected TextView mReleaseDateTextView;

        public SearchMoviesViewHolder(View view) {
            super(view);
            mPosterImageView = (ImageView) view.findViewById(R.id.search_item_poster);
            mTitleTextView = (TextView) view.findViewById(R.id.search_item_title);
            mReleaseDateTextView = (TextView) view.findViewById(R.id.search_item_release_date);
        }
    }


    // Create new Views (invoked by the layout manager in MainActivity)
    @Override
    public SearchMoviesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Create new view, setting attachToRoot = false
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_item,parent,false);

        return new SearchMoviesViewHolder(v);
    }


    //Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(SearchMoviesViewHolder holder, final int position) {

        final Movie currentMovie = mMovielist.get(position);

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Picasso.with(mContext)
                .load(currentMovie.getPosterImageUrl())
                .placeholder(R.drawable.no_image_placeholder)
                .into(holder.mPosterImageView);

        holder.mTitleTextView.setText(currentMovie.getTitle());
        holder.mReleaseDateTextView.setText(currentMovie.getReleaseDate());

        // Add an onClick listener to the item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent toDetailIntent = new Intent();
                toDetailIntent.putExtra(MainActivity.EXTRA_MOVIE_OVERVIEW, currentMovie.getOverview())
                        .putExtra(MainActivity.EXTRA_MOVIE_ID,currentMovie.getId())
                        .putExtra(MainActivity.EXTRA_MOVIE_TITLE, currentMovie.getTitle())
                        .putExtra(MainActivity.EXTRA_MOVIE_BACKDROP_URL, currentMovie.getBackdropUrl())
                        .putExtra(MainActivity.EXTRA_MOVIE_RELEASE_DATE, currentMovie.getReleaseDate())
                        .putExtra(MainActivity.EXTRA_MOVIE_RATING, currentMovie.getVoteAverage())
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
