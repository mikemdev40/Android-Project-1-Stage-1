package com.audible.mmicm.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static String SORT_BY = "com.audible.mmicm.sortBy";
    public final static String MOVIE_ID = "com.audible.mmicm.movieID";

    private MovieAdapter adapter;
    private String selectedSort = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            selectedSort = savedInstanceState.getString(SORT_BY);
            Log.d("main activity", "!=null" + selectedSort);

        } else {
            selectedSort = "popular";
            Log.d("main activity", "==null" + selectedSort);
        }

        setContentView(R.layout.activity_main);

        ArrayList<Movie> movies = new ArrayList<Movie>();
        adapter = new MovieAdapter(this, movies, getSortTypeForString(selectedSort));
        GridView gridView = (GridView) findViewById(R.id.gridview);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie selectedMovie = adapter.getItem(position);
                Intent movieDetailIntent = new Intent(MainActivity.this, MovieDetailActivity.class);
                movieDetailIntent.putExtra(MOVIE_ID, selectedMovie.id);
                startActivity(movieDetailIntent);
            }
        });

        if (NetworkUtility.isConnected(this)) {
            updateMovies(selectedSort);
        } else {
            Toast.makeText(MainActivity.this, "No Internet! Connect and try again.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        selectedSort = savedInstanceState.getString(SORT_BY);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(SORT_BY, selectedSort);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sort_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sortByPopularity) {
            adapter.setMovieListType(MovieAdapter.MovieListType.POPULAR);
            selectedSort = "popular";
            updateMovies(selectedSort);
            return true;
        } else if (item.getItemId() == R.id.sortByRating) {
            adapter.setMovieListType(MovieAdapter.MovieListType.TOP_RATED);
            selectedSort = "top_rated";
            updateMovies(selectedSort);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateMovies(String sortByValue) {
        DownloadMovieDataTask task = new DownloadMovieDataTask();
        String urlString = getURL(sortByValue);
        task.execute(urlString);
    }

    private String getURL(String sortByValue) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https");
        builder.authority("api.themoviedb.org");
        builder.appendPath("3");
        builder.appendPath("movie");
        builder.appendPath(sortByValue);
        builder.appendQueryParameter("api_key","301995e2246067ea90f17ab07e41cdf6");
        builder.appendQueryParameter("language", "en");

        Uri uri = builder.build();

        return uri.toString();
    }

    private MovieAdapter.MovieListType getSortTypeForString(String sortBy) {
        if (sortBy.equals("popular")) {
            return MovieAdapter.MovieListType.POPULAR;
        } else if (sortBy.equals("top_rated")) {
            return MovieAdapter.MovieListType.TOP_RATED;
        } else {
            return null;
        }
    }

    private Movie[] parseResults(String stringToParse) throws JSONException {

        if (stringToParse == null || stringToParse.equals("")) {
            return new Movie[0];
        }

        JSONObject object = new JSONObject(stringToParse);
        JSONArray results = object.getJSONArray("results");

        Movie[] movies = new Movie[results.length()];

        for(int i = 0; i < results.length(); i++) {
            String id;
            String title;
            String imageUrl;
            String overview;
            Double rating;
            String releaseDate;

            JSONObject movieData = results.getJSONObject(i);
            id = movieData.getString("id");
            title = movieData.getString("original_title");
            imageUrl = getImageUrl(movieData.getString("poster_path"));
            overview = movieData.getString("overview");
            rating = movieData.getDouble("vote_average");
            releaseDate = movieData.getString("release_date");
            Movie movie = new Movie(id, title, imageUrl, overview, rating, releaseDate);
            movies[i] = movie;
        }
        return movies;
    }

    private String getImageUrl(String url) {
        String urlWithoutSlash = url.substring(1);
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http");
        builder.authority("image.tmdb.org");
        builder.appendPath("t");
        builder.appendPath("p");
        builder.appendPath("w342");
        builder.appendPath(urlWithoutSlash);

        Uri uri = builder.build();

        return uri.toString();
    }

    private class DownloadMovieDataTask extends AsyncTask<String, Void, String> {
        private final String log_tag = DownloadMovieDataTask.class.getSimpleName();

        protected String doInBackground(String... urls) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieString = null;

            try {
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    Log.d(log_tag, "inputStream == null");
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    Log.d(log_tag, "buffer length == 0");
                    return null;
                }
                movieString = buffer.toString();
                return movieString;
            } catch (IOException e) {
                Log.e(log_tag, "Error ", e);
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("MainActivity", "Error closing stream", e);
                    }
                }
            }
        }
        protected void onPostExecute(String result) {
            try {
                Movie[] results = parseResults(result);
                adapter.clear();
                adapter.addAll(results);

                TextView titleText = (TextView) findViewById(R.id.textOverGridview);

                if (adapter.getMovieListType() == MovieAdapter.MovieListType.POPULAR) {
                    titleText.setText(getResources().getString(R.string.main_screen_title_popular));
                } else if (adapter.getMovieListType() == MovieAdapter.MovieListType.TOP_RATED) {
                    titleText.setText(getResources().getString(R.string.main_screen_title_top_rated));
                }
            } catch (JSONException e) {
                Log.d(log_tag, "JSON EXCEPTION");
            }
        }
    }
}
