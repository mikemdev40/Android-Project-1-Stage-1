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

    public final static String MOVIE_ID = "com.audible.mmicm.movieID";

    private MovieAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<Movie> movies = new ArrayList<Movie>();
        adapter = new MovieAdapter(this, movies);
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

        //when app first starts, initial sorting is by popularity
        updateMovies("popular");
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
            updateMovies("popular");
            return true;
        } else if (item.getItemId() == R.id.sortByRating) {
            adapter.setMovieListType(MovieAdapter.MovieListType.TOP_RATED);
            updateMovies("top_rated");
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
        builder.appendQueryParameter("api_key","ENTER_YOUR_API_KEY_HERE!");
        builder.appendQueryParameter("language", "en");

        Uri uri = builder.build();

        return uri.toString();
    }

    private Movie[] parseResults(String stringToParse) throws JSONException {

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
                    titleText.setText("Showing Most Popular Movies");
                } else if (adapter.getMovieListType() == MovieAdapter.MovieListType.TOP_RATED) {
                    titleText.setText("Showing Highest Rated Movies");
                }

            } catch (JSONException e) {
                Log.d(log_tag, "JSON EXCEPTION");
            }
        }
    }
}
