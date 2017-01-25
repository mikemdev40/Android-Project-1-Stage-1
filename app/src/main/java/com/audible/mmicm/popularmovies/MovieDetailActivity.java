package com.audible.mmicm.popularmovies;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MovieDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        // in case the device is rotated, let the fragment manager handle reinstating the previous
        // state of the fragment rather than adding a new one (which will create another copy)
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            String movieId = intent.getStringExtra(MainActivity.MOVIE_ID);
            MovieDetailFragment fragment = MovieDetailFragment.newInstance(movieId);
            getSupportFragmentManager().beginTransaction().add(R.id.detail_fragment_container_single, fragment).commit();
        }
    }
}
