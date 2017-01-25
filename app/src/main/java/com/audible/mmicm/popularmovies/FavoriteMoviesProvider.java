package com.audible.mmicm.popularmovies;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by mmicm on 1/21/17.
 */

public class FavoriteMoviesProvider extends ContentProvider {

    public static final String CONTENT_AUTHORITY = "com.android.popularmovies.app.moviesprovider";
    public static final Uri BASE_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(MovieDetailsDatabaseHelper.MOVIE_TABLE).build();

    public static final String DIR_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + MovieDetailsDatabaseHelper.MOVIE_TABLE;

    static final int ALL_MOVIES = 100;

    private static final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        matcher.addURI(CONTENT_AUTHORITY, MovieDetailsDatabaseHelper.MOVIE_TABLE, ALL_MOVIES);
    }

    private MovieDetailsDatabaseHelper databaseHelper;

    @Override
    public boolean onCreate() {
        databaseHelper = new MovieDetailsDatabaseHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int uriMatch = matcher.match(uri);
        switch (uriMatch) {
            case ALL_MOVIES:
                return DIR_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor returnCursor;
        final SQLiteDatabase database = databaseHelper.getReadableDatabase();

        switch (matcher.match(uri)) {
            case ALL_MOVIES: {
                returnCursor = database.query(
                        MovieDetailsDatabaseHelper.MOVIE_TABLE,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return returnCursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase database = databaseHelper.getWritableDatabase();
        final int match = matcher.match(uri);
        Uri returnUri;

        switch (match) {
            case ALL_MOVIES:
                long _id = database.insert(MovieDetailsDatabaseHelper.MOVIE_TABLE, null, values);
                if ( _id > 0 )
                    returnUri = ContentUris.withAppendedId(CONTENT_URI, _id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase database = databaseHelper.getWritableDatabase();
        final int match = matcher.match(uri);
        int rowsDeleted;

        switch (match) {
            case ALL_MOVIES:
                rowsDeleted = database.delete(MovieDetailsDatabaseHelper.MOVIE_TABLE, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase database = databaseHelper.getWritableDatabase();
        final int match = matcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case ALL_MOVIES:
                rowsUpdated = database.delete(MovieDetailsDatabaseHelper.MOVIE_TABLE, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}
