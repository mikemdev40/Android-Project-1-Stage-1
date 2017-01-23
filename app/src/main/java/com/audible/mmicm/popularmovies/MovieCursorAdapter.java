package com.audible.mmicm.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by mmicm on 1/22/17.
 */

public class MovieCursorAdapter extends CursorAdapter {

    public MovieCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.movie_cell, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(MovieDetailsDatabaseHelper.MovieDetail.COLUMN_MOVIE_IMAGEURL));
        ImageView imageView = (ImageView) view.findViewById(R.id.movieCellImageView);
        Picasso.with(context).load(imageUrl).into(imageView);

        String title = cursor.getString(cursor.getColumnIndexOrThrow(MovieDetailsDatabaseHelper.MovieDetail.COLUMN_MOVIE_TITLE));
        TextView titleTextView = (TextView) view.findViewById(R.id.movieCellTextViewTitle);
        titleTextView.setText(title);
    }
}
