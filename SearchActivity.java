package com.aimtech.android.movies;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andy on 13/03/2016.
 */
public class SearchActivity extends AppCompatActivity {

    static final String LOG_TAG = SearchActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;

    private Button mGoButton;
    private EditText mSearchQueryTextView;
    private String mQueryString;
    private String encodedQuery;

    private int mBlankSearchAttempts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBlankSearchAttempts = 0;

        setContentView(R.layout.search_activity_layout);
        setTitle("Search");

        mSearchQueryTextView = (EditText) findViewById(R.id.search_text_field);

        // Hook up the recyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.search_results_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        // Use a list layout manager
        mLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(mLayoutManager);


        mGoButton = (Button) findViewById(R.id.go_button);
        mGoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide the keyboard
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

                mQueryString = mSearchQueryTextView.getText().toString();

                // Encode the query for API
                try {
                    encodedQuery = URLEncoder.encode(mQueryString, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    Log.d(LOG_TAG, "Unable to encode query string : " + e);
                }

                Log.d(LOG_TAG, "Go button clicked. Query : " + mQueryString +
                        "\nEncoded Query : " + encodedQuery);

                if (mQueryString.length() == 0) {
                    mBlankSearchAttempts = messagesForBlankSearches(mBlankSearchAttempts);
                    Log.d(LOG_TAG,"Search Attempts Updated : " + mBlankSearchAttempts);
                    return;
                }
                FetchSearchMovieData fetchSearchMovieData = new FetchSearchMovieData();
                String[] builtUrl = new String[]{buildSearchDestinationUri(encodedQuery)};
                Log.d(LOG_TAG, "Built Url for Search API Query : " + builtUrl[0]);
                fetchSearchMovieData.execute(builtUrl);
            }
        });

    }





    // AsyncTask Class
    private class FetchSearchMovieData extends AsyncTask<String, Void, List<Movie>> {
        private final String LOG_TAG = FetchSearchMovieData.class.getSimpleName();
        private List<Movie> mParsedMovieList;

        @Override
        protected List<Movie> doInBackground(String... params) {
            Log.d(LOG_TAG, "Started Search background task...");

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

            SearchMoviesAdapter searchMoviesAdapter = new SearchMoviesAdapter(result, getBaseContext());

            // Update the UI
            if(result.size() == 0){
                Toast.makeText(getBaseContext(),"No Results Found",Toast.LENGTH_SHORT).show();
                Toast.makeText(getBaseContext(),"You Lose",Toast.LENGTH_LONG).show();
            }

            mAdapter = searchMoviesAdapter;
            mRecyclerView.setAdapter(mAdapter);
            Log.d(LOG_TAG, "RecyclerView adapter set. Adapter size : " + mRecyclerView.getAdapter().getItemCount());


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

    // Helper function to create the Uri for the API
    private String buildSearchDestinationUri(String searchQuery) {
        Uri destinationUri;

        final String BASE_URI = "https://api.themoviedb.org/3/";
        final String API_KEY_VALUE = "270ae994ca14c19a3254d0b306bc9174";

        final String PATH_SEARCH = "search";
        final String PATH_MOVIES = "movie";
        final String PARAM_QUERY = "query";
        final String PARAM_LANGUAGE = "language";
        final String PARAM_API = "api_key";

        //Sort order from sharedPreferences
        String PARAM_SORT_VALUE = null;

        Log.d(LOG_TAG,"Sort Order for URL : " + PARAM_SORT_VALUE);

        // Build URI
        destinationUri = Uri.parse(BASE_URI).buildUpon()
                .appendPath(PATH_SEARCH)
                .appendPath(PATH_MOVIES)
                .appendQueryParameter(PARAM_QUERY,searchQuery)
                .appendQueryParameter(PARAM_LANGUAGE, "en")
                .appendQueryParameter(PARAM_API, API_KEY_VALUE)
                .build();

        return destinationUri.toString();
    }

    private int messagesForBlankSearches(int tries){
        switch (tries){
            case 0:
                Toast.makeText(getBaseContext(), "Please enter a title.", Toast.LENGTH_SHORT).show();
                tries++;
                return tries;
            case 1:
                Toast.makeText(getBaseContext(), "No, really. You need to enter something.", Toast.LENGTH_SHORT).show();
                tries++;
                return tries;
            case 2:
                Toast.makeText(getBaseContext(), "Look, I really can't search for nothing. It will get you nowhere.", Toast.LENGTH_LONG).show();
                tries++;
                return tries;
            case 3:
                Toast.makeText(getBaseContext(), "Seriously?", Toast.LENGTH_SHORT).show();
                tries++;
                return tries;
            case 4:
                Toast.makeText(getBaseContext(), "We exhausted all the comic possibilities of this scenario two clicks ago.", Toast.LENGTH_LONG).show();
                Toast.makeText(getBaseContext(), "Move on.", Toast.LENGTH_SHORT).show();
                tries++;
                return tries;
            case 5:
                Toast.makeText(getBaseContext(), "Is this really amusing to you?", Toast.LENGTH_SHORT).show();
                tries++;
                return tries;
            case 6:
                Toast.makeText(getBaseContext(), "OK. Once more and I'm leaving.", Toast.LENGTH_SHORT).show();
                tries++;
                return tries;
            case 7:
                Toast.makeText(getBaseContext(), "I mean it. One more click and I am gone.", Toast.LENGTH_SHORT).show();
                tries++;
                return tries;
            case 8:
                Toast.makeText(getBaseContext(), "Bye.", Toast.LENGTH_SHORT).show();
                tries++;
                return tries;
            case 9:
                Toast.makeText(getBaseContext(), "...", Toast.LENGTH_SHORT).show();
                tries++;
                return tries;
            case 10:
                tries++;
                return tries;
            case 11:
                tries++;
                return tries;
            case 12:
                tries++;
                return tries;
            case 13:
                Toast.makeText(getBaseContext(), "Wow.", Toast.LENGTH_LONG).show();
                Toast.makeText(getBaseContext(), "You actually kept going.", Toast.LENGTH_LONG).show();
                Toast.makeText(getBaseContext(), "Alright, this is getting sad. Be my guest.", Toast.LENGTH_LONG).show();
                Toast.makeText(getBaseContext(), "You won't hear from me again.", Toast.LENGTH_LONG).show();
                tries++;
                return tries;

            default:
                // Do nothing
                break;
        }

        return 14;
    }



}
