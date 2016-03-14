package com.aimtech.android.movies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andy on 12/03/2016.
 */
public class MovieDetailActivity extends AppCompatActivity {

    private final String LOG_TAG = MovieDetailActivity.class.getSimpleName();

    private String GOOGLE_API_KEY = "AIzaSyA7_BRC-95ESmz91vy9uEG1WrW4yX17gaQ";
    private String YOUTUBE_VIDEO_ID = "7jIBCiYg58k";

    private TextView mOverviewTextView;
    private ImageView mBackdropImageView;
    private TextView mReleaseDateTextView;
    private TextView mRatingTextView;
    private Button mPlayTrailerButton;
    private String mMovieID;
    private TextView trailerHeader;

    private TrailerListAdapter mTrailerListAdapter;
    private ListView mTrailerListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_detail_layout);

        // Make the trailers header invisible until trailers are found?
        trailerHeader = (TextView) findViewById(R.id.trailers_header);
        trailerHeader.setVisibility(View.INVISIBLE);

        mMovieID = getIntent().getStringExtra(MainActivity.EXTRA_MOVIE_ID);
        Log.d(LOG_TAG, "Movie ID retrieved for TRAILERS : " + mMovieID);

        setTitle(getIntent().getStringExtra(MainActivity.EXTRA_MOVIE_TITLE));

        mOverviewTextView = (TextView) findViewById(R.id.movie_overview_text_view);
        mOverviewTextView.setText(getIntent().getStringExtra(MainActivity.EXTRA_MOVIE_OVERVIEW));

        mBackdropImageView = (ImageView) findViewById(R.id.movie_backdrop_image_view);
        Picasso.with(getBaseContext())
                .load(getIntent().getStringExtra(MainActivity.EXTRA_MOVIE_BACKDROP_URL))
                .into(mBackdropImageView);

        mReleaseDateTextView = (TextView) findViewById(R.id.release_date_text_view);
        mReleaseDateTextView.setText("Release Date : " + getIntent().getStringExtra(MainActivity.EXTRA_MOVIE_RELEASE_DATE));

        mRatingTextView = (TextView) findViewById(R.id.average_rating_text_view);
        mRatingTextView.setText("Avg. Rating : " + getIntent().getStringExtra(MainActivity.EXTRA_MOVIE_RATING));



        // list view for the current movie's trailers
        mTrailerListView = (ListView) findViewById(R.id.detail_trailer_list_view);


        //Start the AsyncTask to load the trailers
        FetchMovieTrailers fetchMovieTrailers = new FetchMovieTrailers();
        fetchMovieTrailers.execute(new String[]{mMovieID});

    }


    private class FetchMovieTrailers extends AsyncTask<String,Void,List<Trailer>>{
        final String LOG_TAG = FetchMovieTrailers.class.getSimpleName();
        private List<Trailer> mMovieTrailers;

        @Override
        protected List<Trailer> doInBackground(String... params) {

            //Check there has been a parameter passed
            if (params == null) {
                return null;
            }

            //Constuct the query
            final String BASE_URI = "https://api.themoviedb.org/3/";
            final String API_KEY_VALUE = "270ae994ca14c19a3254d0b306bc9174";
            final String MOVIE_ID = params[0];
            final String PARAM_API = "api_key";

            final String PATH_TRAILERS = "videos";
            final String PATH_MOVIES = "movie";

            Uri uri = Uri.parse(BASE_URI).buildUpon()
                    .appendPath(PATH_MOVIES)
                    .appendPath(MOVIE_ID)
                    .appendPath(PATH_TRAILERS)
                    .appendQueryParameter(PARAM_API, API_KEY_VALUE)
                    .build();

            Log.d(LOG_TAG,"Uri Built : " + uri.toString());

            Log.d(LOG_TAG, "Started background task...");




            // Declare the httpconnection and bufferedReader outside the try/catch block
            HttpURLConnection httpURLConnection = null;
            BufferedReader bufferedReader = null;

            try {
                // Initialise the destination Uri, passed to the doInBackground method from MainActivity
                URL mDestinationUrl = new URL(uri.toString());
                Log.d(LOG_TAG, "Url Generated : " + mDestinationUrl.toString());

                // Create the URL connection and input stream
                httpURLConnection = (HttpURLConnection) mDestinationUrl.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                if (inputStream == null) {
                    return null;
                }

                // String builder to construc the final result
                StringBuilder stringBuilder = new StringBuilder();

                // BUffered reader is used to read through the inputStream data
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                // BUIld up the result by reading through the data
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }

                // Return the result after parsing
                try {
                    List<Trailer> parsedTrailerList = parseRawTrailerJson(stringBuilder.toString());
                    return parsedTrailerList;
                } catch (JSONException e) {
                    Log.d(LOG_TAG, "Unable to parse result");
                }

                // If all else fails...
                return null;

            } catch (MalformedURLException e) {
                Log.d(LOG_TAG, "Malformed URL : " + e);
            } catch (IOException e) {
                Log.d(LOG_TAG, "Error retrieving data : " + e);
            } finally {
                // Clean up : close the connection and reader
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }

                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        Log.d(LOG_TAG, "Error closing reader : " + e);
                    }
                }
            } // End of Try/Catch

            // If all else fails
            return null;
        }

        // Function to parse the JSON. For now get the names of the movies in a String array
        private List<Trailer> parseRawTrailerJson(String rawJsonString) throws JSONException {
            mMovieTrailers = new ArrayList<Trailer>();

            // Constants to assist with JSON parsing
            final String MDB_RESULTS_ARRAY = "results";
            final String MDB_TRAILER_TITLE = "name";
            final String MDB_TRAILER_KEY = "key";



            JSONObject rootJsonObject = new JSONObject(rawJsonString);
            JSONArray resultsArray = rootJsonObject.getJSONArray(MDB_RESULTS_ARRAY);


            if (resultsArray != null) {
                // Store Movie Objects in the list
                for (int i = 0; i < resultsArray.length(); i++) {
                    String trailerKey = resultsArray.getJSONObject(i).getString(MDB_TRAILER_KEY);
                    String trailerTitle = resultsArray.getJSONObject(i).getString(MDB_TRAILER_TITLE);
                    Trailer newTrailer = new Trailer(trailerTitle,trailerKey);
                    mMovieTrailers.add(newTrailer);
                }
                return mMovieTrailers;
            } else {
                Log.d(LOG_TAG, "Parse Raw JSON : Error Parsing result");
            }



            // If all else fails
            return null;
        }

        @Override
        protected void onPostExecute(List<Trailer> result) {
            super.onPostExecute(result);

            if (result != null) {

                // Log the output in some way
                StringBuilder stringBuilder = new StringBuilder();
                for (Trailer trailer : result) {
                    stringBuilder.append(trailer.getTitle() + " - " + trailer.getVideoId() + "\n");
                }
                Log.d(LOG_TAG, "Post Execute, Result Returned : \n" + stringBuilder);
            } else {
                Log.d(LOG_TAG, "Post Execute, Result Null");
            }

            // Update the UI if there are any trailers
            if(mMovieTrailers.size() >0) {
                mTrailerListAdapter = new TrailerListAdapter(getBaseContext(), mMovieTrailers);
                mTrailerListView.setAdapter(mTrailerListAdapter);
                UIUtils.setListViewHeightBasedOnItems(mTrailerListView);
                trailerHeader.setVisibility(View.VISIBLE);

                // Start listening for clicks. Launches YouTube Trailer
                mTrailerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        Trailer clickedTrailer = (Trailer) parent.getItemAtPosition(position);
                        String trailerKey = clickedTrailer.getVideoId();

                        startYouTubeTrailer(trailerKey);
                    }
                });
            } else {
                trailerHeader.setVisibility(View.INVISIBLE);
            }
        }
    }


    // Function to create and launch YouTubeIntents
    private void startYouTubeTrailer(String key){
        Log.d(LOG_TAG, "Trailer Intent Set. Vid ID : " + key);
        Intent intent = YouTubeStandalonePlayer.createVideoIntent(this,GOOGLE_API_KEY,key,0,true,false);
        startActivity(intent);
    }

    // Function to re-format the date
    private String formatDate(String rawDate){
        return null;
    }

}
