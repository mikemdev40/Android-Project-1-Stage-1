package com.audible.mmicm.popularmovies;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by mmicm on 1/8/17.
 */

public class MovieDetailsDatabaseHelper extends SQLiteOpenHelper {

    static class MovieDetail implements BaseColumns {
        static final String COLUMN_MOVIE_ID = "movieID";
        static final String COLUMN_MOVIE_TITLE = "title";
        static final String COLUMN_MOVIE_IMAGEURL = "imageUrl";
    }

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "FavoriteMovies.db";
    public static final String MOVIE_TABLE = "Movies";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + MOVIE_TABLE + " (" +
                    MovieDetailsDatabaseHelper.MovieDetail._ID + " INTEGER PRIMARY KEY," +
                    MovieDetailsDatabaseHelper.MovieDetail.COLUMN_MOVIE_ID + " TEXT," +
                    MovieDetailsDatabaseHelper.MovieDetail.COLUMN_MOVIE_TITLE + " TEXT," +
                    MovieDetailsDatabaseHelper.MovieDetail.COLUMN_MOVIE_IMAGEURL + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + MOVIE_TABLE;

    public MovieDetailsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        Log.d(this.getClass().getSimpleName(), "on create " + db.toString());
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        Log.d(this.getClass().getSimpleName(), "on open " + db.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
