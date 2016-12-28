package com.audible.mmicm.popularmovies;

/**
 * Created by mmicm on 12/27/16.
 */

public class Movie {
    String id;
    String title;
    String imageUrl;
    String overview;
    Double rating;
    String releaseDate;

    public Movie(String id, String title, String imageUrl, String overview, Double rating, String releaseDate) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.overview = overview;
        this.rating = rating;
        this.releaseDate = releaseDate;
    }
}
