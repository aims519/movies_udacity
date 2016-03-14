package com.aimtech.android.movies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

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

public class MainActivity extends AppCompatActivity {

    // COnstants for sending extras to detail activity
    public static final String EXTRA_MOVIE_OVERVIEW = "movieOverview";
    public static final String EXTRA_MOVIE_TITLE = "movieTitle";
    public static final String EXTRA_MOVIE_ID = "movieId";
    public static final String EXTRA_MOVIE_BACKDROP_URL = "movieBackdropUrl";
    public static final String EXTRA_MOVIE_RELEASE_DATE = "movieReleaseDate";
    public static final String EXTRA_MOVIE_RATING = "movieRating";


    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;

    // Declare shared preferences here to be accessed elsewhere in the class
    private SharedPreferences sharedPreferences;


    // Inflates the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Handles menu click events
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Retrieve current sort setting
        String currentSortPref = sharedPreferences.getString(getString(R.string.pref_sort_order_key), getString(R.string.pref_sort_order_default));
        String newSortPref = currentSortPref;
        //Set iup a preference editor
        SharedPreferences.Editor editor = sharedPreferences.edit();

        switch (item.getItemId()) {
            case R.id.menu_option_sort:
                break;
            case R.id.menu_option_search:
                Intent toSearchIntent = new Intent();
                toSearchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .setClass(this, SearchActivity.class);
                startActivity(toSearchIntent);
                break;

            case R.id.sort_most_popular:
                if (currentSortPref != "most_popular") {
                    editor.putString(getString(R.string.pref_sort_order_key), "most_popular")
                            .apply();
                    newSortPref = sharedPreferences.getString(getString(R.string.pref_sort_order_key), getString(R.string.pref_sort_order_default));

                }
                break;

            case R.id.sort_highest_rated:
                if (currentSortPref != "most_votes") {
                    editor.putString(getString(R.string.pref_sort_order_key), "most_votes")
                            .apply();
                    newSortPref = sharedPreferences.getString(getString(R.string.pref_sort_order_key), getString(R.string.pref_sort_order_default));

                }
                break;

            case R.id.sort_highest_revenue:
                if (currentSortPref != "highest_revenue") {
                    editor.putString(getString(R.string.pref_sort_order_key), "highest_revenue")
                            .apply();
                    newSortPref = sharedPreferences.getString(getString(R.string.pref_sort_order_key), getString(R.string.pref_sort_order_default));

                }
                break;

        }
        updateResultsAndActivityTitle(newSortPref);
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialise the default shared preferences.
        // This is called only once
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        // Get the current preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String curr_sort_pref = sharedPreferences.getString(getString(R.string.pref_sort_order_key), "Not found!");
        updateActivtyTitle(curr_sort_pref);
        Log.d(LOG_TAG,"Preference found on create, and passed to API methods : " + curr_sort_pref);

        // API CALL - also sets mAdapter in postExecute
        updateResultsAndActivityTitle(curr_sort_pref);


        // Hook up the recyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.popular_movies_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        // Use a grid layout manager, with 2 columns for now
        mLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);





    }


    // Helper function to create the Uri for the API
    private String buildDestinationUri(String sortPref) {
        Uri destinationUri;

        final String BASE_URI = "https://api.themoviedb.org/3/";
        final String API_KEY_VALUE = "270ae994ca14c19a3254d0b306bc9174";

        final String PATH_DISCOVER = "discover";
        final String PATH_MOVIES = "movie";
        final String PARAM_SORT = "sort_by";
        final String PARAM_LANGUAGE = "language";
        final String PARAM_API = "api_key";

        //Sort order from sharedPreferences
        String PARAM_SORT_VALUE = null;

        switch (sortPref){
            case "most_popular":
                PARAM_SORT_VALUE = "popularity.desc";
                break;
            case "most_votes":
                PARAM_SORT_VALUE = "vote_count.desc";
                break;
            case "highest_revenue":
                PARAM_SORT_VALUE = "revenue.desc";
                break;
        }

        Log.d(LOG_TAG,"Sort Order for URL : " + PARAM_SORT_VALUE);

        // Build URI
        destinationUri = Uri.parse(BASE_URI).buildUpon()
                .appendPath(PATH_DISCOVER)
                .appendPath(PATH_MOVIES)
                .appendQueryParameter(PARAM_SORT, PARAM_SORT_VALUE)
                .appendQueryParameter(PARAM_LANGUAGE, "en")
                .appendQueryParameter(PARAM_API, API_KEY_VALUE)
                .build();

        return destinationUri.toString();
    }


    // AsyncTask Class
    private class FetchMovieData extends AsyncTask<String, Void, List<Movie>> {
        private final String LOG_TAG = FetchMovieData.class.getSimpleName();
        private List<Movie> mParsedMovieList;

        @Override
        protected List<Movie> doInBackground(String... params) {
            Log.d(LOG_TAG, "Started background task...");

            //Check there has been a parameter passed
            if (params == null) {
                return null;
            }


            // Declare the httpconnection and bufferedReader outside the try/catch block
            HttpURLConnection httpURLConnection = null;
            BufferedReader bufferedReader = null;

            try {
                // Initialise the destination Uri, passed to the doInBackground method from MainActivity
                URL mDestinationUrl = new URL(params[0]);
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
                    List<Movie> parsedResultList = parseRawJson(stringBuilder.toString());
                    return parsedResultList;
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
        private List<Movie> parseRawJson(String rawJsonString) throws JSONException {
            mParsedMovieList = new ArrayList<Movie>();

            // Constants to assist with JSON parsing
            final String MDB_RESULTS_ARRAY = "results";
            final String MDB_MOVIE_TITLE = "title";
            final String MDB_MOVIE_RELEASE_DATE = "release_date";
            final String MDB_MOVIE_RATING = "vote_average";
            final String MDB_MOVIE_OVERVIEW = "overview";
            final String MDB_MOVIE_POSTER_PATH = "poster_path";
            final String MDB_MOVIE_BACKDROP_PATH = "backdrop_path";
            final String MDB_MOVIE_ID = "id";


            JSONObject rootJsonObject = new JSONObject(rawJsonString);
            JSONArray resultsArray = rootJsonObject.getJSONArray(MDB_RESULTS_ARRAY);


            if (resultsArray != null) {
                // Store Movie Objects in the list

                for (int i = 0; i < resultsArray.length(); i++) {
                    String title = resultsArray.getJSONObject(i).getString(MDB_MOVIE_TITLE);
                    String overview = resultsArray.getJSONObject(i).getString(MDB_MOVIE_OVERVIEW);
                    String releaseDate = resultsArray.getJSONObject(i).getString(MDB_MOVIE_RELEASE_DATE);
                    String rating = resultsArray.getJSONObject(i).getString(MDB_MOVIE_RATING);

                    String posterPath = resultsArray.getJSONObject(i).getString(MDB_MOVIE_POSTER_PATH);
                    String posterUrl = buildImageUrl(posterPath);

                    String backdropPath = resultsArray.getJSONObject(i).getString(MDB_MOVIE_BACKDROP_PATH);
                    String backdropUrl = buildImageUrl(backdropPath);

                    String id = resultsArray.getJSONObject(i).getString(MDB_MOVIE_ID);
                    Movie newMovie = new Movie(title, releaseDate, overview, posterUrl, backdropUrl, rating, id);

                    mParsedMovieList.add(newMovie);
                }
                return mParsedMovieList;
            } else {
                Log.d(LOG_TAG, "Parse Raw JSON : Error Parsing result");
            }

            // If all else fails
            return null;
        }

        @Override
        protected void onPostExecute(List<Movie> result) {
            super.onPostExecute(result);

            // Log the output in some way
            StringBuilder stringBuilder = new StringBuilder();
            for (Movie movie : result) {
                stringBuilder.append(movie.getId() + " - " + movie.getTitle() + " - " + movie.getReleaseDate() + " - " + movie.getPosterImageUrl() + "\n");
            }
            Log.d(LOG_TAG, "Post Execute, Result Returned : \n" + stringBuilder);

            PopularMoviesAdapter popularMoviesAdapter = new PopularMoviesAdapter(result, getBaseContext());
            Log.d(LOG_TAG, "Adapter Size : " + popularMoviesAdapter.getItemCount());

            // Update the UI
            mAdapter = popularMoviesAdapter;
            mRecyclerView.setAdapter(mAdapter);
            Log.d(LOG_TAG, "RecyclerView adapter set. Adapter size : " + mRecyclerView.getAdapter().getItemCount());


            String currentSortPref = sharedPreferences.getString(getString(R.string.pref_sort_order_key), getString(R.string.pref_sort_order_default));
            updateActivtyTitle(currentSortPref);


        }

    }

    private String buildImageUrl(String ImgPath) {
        final String BASE_URL = "http://image.tmdb.org/t/p";
        final String PATH_SIZE = "w500";
        final String PATH_IMG = ImgPath;

        Uri uri = Uri.parse(BASE_URL).buildUpon()
                .appendEncodedPath(PATH_SIZE)
                .appendEncodedPath(PATH_IMG)
                .build();
        return uri.toString();
    }


    // Helper function to update the results on the screen and the activity title, based on a preference value
    private void updateResultsAndActivityTitle(String sortPref) {

        FetchMovieData fetchMovieData = new FetchMovieData();
        String[] builtUrl = new String[]{ buildDestinationUri(sortPref)};
        Log.d(LOG_TAG,"Built Url for AP Query : " + builtUrl[0]);
        fetchMovieData.execute(builtUrl);
    }

    private void updateActivtyTitle(String sortPref){
        switch (sortPref) {
            case "most_popular":
                setTitle("Most Popular Now");
                break;
            case "most_votes":
                setTitle("Most Votes");
                break;
            case "highest_revenue":
                setTitle("Highest Revenue");
                break;
        }
    }

}
