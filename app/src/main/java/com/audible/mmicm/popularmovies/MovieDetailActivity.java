package com.audible.mmicm.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;

public class MovieDetailActivity extends AppCompatActivity {

    private MovieDetailsDatabaseHelper databaseHelper;
    private Button favoritesButton;

    private String movieId;
    private String title;
    private String tagline;
    private String imageUrl;
    private String overview;
    private Double rating;
    private Integer budget;
    private String releaseDate;
    private Integer runtime;
    private Integer revenue;

    private Integer favoriteId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        Intent intent = getIntent();
        movieId = intent.getStringExtra(MainActivity.MOVIE_ID);
        favoritesButton = (Button) findViewById(R.id.movieDetailFavoriteButton);
        favoritesButton.setEnabled(false);

        databaseHelper = new MovieDetailsDatabaseHelper(this);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        if (NetworkUtility.isConnected(this)) {
            updateMovieData(movieId);
        } else {
            Toast.makeText(MovieDetailActivity.this, "No Internet! Connect and try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFavoriteButton() {
        if (loadFavoriteStatus()) {
            favoritesButton.setText("Remove from favorites");
            favoritesButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    removeFromFavorites();
                }
            });
        } else {
            favoritesButton.setText("Add to favorites");
            favoritesButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    saveToFavorites();
                }
            });
        }
    }

    private boolean loadFavoriteStatus() {
        String selection = MovieDetailsDatabaseHelper.MovieDetail.COLUMN_MOVIE_ID + " = ?";
        String[] selectionArgs = {movieId};
        String[] projection = {
                MovieDetailsDatabaseHelper.MovieDetail.COLUMN_MOVIE_ID
        };
        Cursor cursor = getContentResolver().query(
                FavoriteMoviesProvider.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );
        if (null != cursor && cursor.getCount() > 0) {
            return true;
        } else {
            return false;
        }
    }

    private void saveToFavorites() {
        ContentValues valuesToSave = new ContentValues();
        valuesToSave.put(MovieDetailsDatabaseHelper.MovieDetail.COLUMN_MOVIE_ID, movieId);
        valuesToSave.put(MovieDetailsDatabaseHelper.MovieDetail.COLUMN_MOVIE_TITLE, title);
        valuesToSave.put(MovieDetailsDatabaseHelper.MovieDetail.COLUMN_MOVIE_IMAGEURL, getImageFromUrl(imageUrl));
        Uri response = getContentResolver().insert(FavoriteMoviesProvider.CONTENT_URI, valuesToSave);
        updateFavoriteButton();
    }

    private void removeFromFavorites() {
        String selection = MovieDetailsDatabaseHelper.MovieDetail.COLUMN_MOVIE_ID + " = ?";
        String[] selectionArgs = {movieId};
        getContentResolver().delete(FavoriteMoviesProvider.CONTENT_URI, selection, selectionArgs);
        updateFavoriteButton();
    }

    private String getImageFromUrl(String imageUrl) {
        //we want to load smaller images in the main view, so save the URL to the smaller version
        return imageUrl.replaceAll("w780", "w342");
    }

    private void updateMovieData(String movieId) {
        DownloadMovieInfoDataTask task = new DownloadMovieInfoDataTask();
        String urlString = getURL(movieId);
        task.execute(urlString);
    }

    private String getURL(String movieId) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https");
        builder.authority("api.themoviedb.org");
        builder.appendPath("3");
        builder.appendPath("movie");
        builder.appendPath(movieId);
        builder.appendQueryParameter("api_key","301995e2246067ea90f17ab07e41cdf6");
        builder.appendQueryParameter("language", "en");

        Uri uri = builder.build();

        return uri.toString();
    }

    private String getImageUrl(String url) {
        String urlWithoutSlash = url.substring(1);
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http");
        builder.authority("image.tmdb.org");
        builder.appendPath("t");
        builder.appendPath("p");
        builder.appendPath("w780");
        builder.appendPath(urlWithoutSlash);

        Uri uri = builder.build();

        return uri.toString();
    }

    private class DownloadMovieInfoDataTask extends AsyncTask<String, Void, String> {
        private final String log_tag = DownloadMovieInfoDataTask.class.getSimpleName();

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
                parseAndUpdateUI(result);
            } catch (JSONException e) {
                Log.d(log_tag, "JSON EXCEPTION");
            }
        }
    }

    private void parseAndUpdateUI(String stringToParse) throws JSONException {

        if (stringToParse == null || stringToParse.equals("")) {
            return;
        }

        JSONObject object = new JSONObject(stringToParse);

        title = object.getString("original_title");
        tagline = object.getString("tagline");
        imageUrl = getImageUrl(object.getString("poster_path"));
        overview = object.getString("overview");
        rating = object.getDouble("vote_average");
        budget = object.getInt("budget");
        releaseDate = object.getString("release_date");
        runtime = object.getInt("runtime");
        revenue = object.getInt("revenue");

        TextView titleText = (TextView) findViewById(R.id.movieDetailTextViewTitle);
        titleText.setText(title);

        TextView taglineText = (TextView) findViewById(R.id.movieDetailTextViewTagline);
        taglineText.setText(tagline);

        ImageView imageView = (ImageView) findViewById(R.id.movieDetailImageView);
        Picasso.with(this).load(imageUrl).into(imageView);

        TextView overviewText = (TextView) findViewById(R.id.movieDetailTextViewOverview);
        overviewText.setText(overview);

        TextView releaseDateText = (TextView) findViewById(R.id.movieDetailTextViewReleaseDate);
        releaseDateText.setText(releaseDate);

        //strings using formats
        TextView ratingText = (TextView) findViewById(R.id.movieDetailTextViewRating);
        ratingText.setText(String.format(Locale.ENGLISH, "%.2f", rating));

        //for strings that require string resources
        Resources res = getResources();

        TextView runtimeText = (TextView) findViewById(R.id.movieDetailTextViewRuntime);
        String runtimeString = String.format(res.getString(R.string.movie_detail_minutes), runtime);
        runtimeText.setText(runtimeString);

        //format dollar strings to show dollar sign and commas
        TextView budgetText = (TextView) findViewById(R.id.movieDetailTextViewBudget);
        String budgetString = String.format(res.getString(R.string.movie_detail_dollars), NumberFormat.getIntegerInstance().format(budget));
        budgetText.setText(budgetString);

        TextView revenueText = (TextView) findViewById(R.id.movieDetailTextViewRevenue);
        String revenueString = String.format(res.getString(R.string.movie_detail_dollars), NumberFormat.getIntegerInstance().format(revenue));
        revenueText.setText(revenueString);

        favoritesButton.setEnabled(true);
        updateFavoriteButton();
    }
}
