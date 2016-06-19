package com.evansappwriter.movielist.model;


public class Movie implements Comparable<Movie> {
    public static final String TABLE_NAME = "Movies";

    private int mId;
    private String mTitle;
    private String mOverview;
    private double mVoteAvg;
    private String mBackdropPath;
    private String mPosterPath;
    private String mReleaseDate;

    public Movie() {
    }


    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getOverview() {
        return mOverview;
    }

    public void setOverview(String overview) {
        this.mOverview = overview;
    }

    public double getVoteAvg() {
        return mVoteAvg;
    }

    public void setVoteAvg(double voteAvg) {
        mVoteAvg = voteAvg;
    }

    public String getBackdropPath() {
        return mBackdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        mBackdropPath = backdropPath;
    }

    public String getPosterPath() {
        return mPosterPath;
    }

    public void setPosterPath(String posterPath) {
        mPosterPath = posterPath;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        mReleaseDate = releaseDate;
    }

    @Override
    public int compareTo(Movie v) {
        return 0;
    }
}
