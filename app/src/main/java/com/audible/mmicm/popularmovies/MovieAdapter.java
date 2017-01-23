package com.audible.mmicm.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
/**
 * Created by mmicm on 12/27/16.
 */

public class MovieAdapter extends ArrayAdapter<Movie> {

    static public enum MovieListType {
        POPULAR, TOP_RATED
    }
    private MovieListType movieListType;

    public MovieAdapter(Context context, ArrayList<Movie> movies, MovieListType sortByType) {
        super(context, 0, movies);
        movieListType = sortByType;
    }

    public void setMovieListType(MovieListType type) {
        this.movieListType = type;
    }

    public MovieListType getMovieListType() {
        return this.movieListType;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Movie movie = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.movie_cell, parent, false);
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.movieCellImageView);
        Picasso.with(getContext()).load(movie.imageUrl).into(imageView);

        TextView titleTextView = (TextView) convertView.findViewById(R.id.movieCellTextViewTitle);
        titleTextView.setText(movie.title);

        //for displaying either the popularity score OR the user rating score, depending on movieListType
        TextView infoTextView = (TextView) convertView.findViewById(R.id.movieCellTextViewScore);

        if (movieListType == MovieListType.POPULAR) {
            int popularityRanking = position + 1;
            infoTextView.setText("Popularity Rank: " + Integer.toString(popularityRanking));
        } else if (movieListType == MovieListType.TOP_RATED) {
            infoTextView.setText("User Rating: " + movie.rating.toString());
        } else {
            infoTextView.setText("");
        }

        return convertView;
    }

}
