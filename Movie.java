package com.aimtech.android.movies;

/**
 * Created by Andy on 12/03/2016.
 */
public class Movie {
    private String mTitle;
    private String mReleaseDate;
    private String mOverview;
    private String mPosterImageUrl;
    private String mBackdropUrl;
    private String mVoteAverage;
    private String _id;

    public Movie(String mTitle, String mReleaseDate, String mOverview, String posterImageUrl, String backdropUrl, String voteAverage, String _id) {
        this.mTitle = mTitle;
        this.mReleaseDate = mReleaseDate;
        this.mOverview = mOverview;
        this.mPosterImageUrl = posterImageUrl;
        this.mBackdropUrl = backdropUrl;
        this.mVoteAverage = voteAverage;
        this._id = _id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        mReleaseDate = releaseDate;
    }

    public String getOverview() {
        return mOverview;
    }

    public void setOverview(String mOverview) {
        this.mOverview = mOverview;
    }

    public String getPosterImageUrl() {
        return mPosterImageUrl;
    }

    public void setPosterImageUrl(String mPosterImageUrl) {
        this.mPosterImageUrl = mPosterImageUrl;
    }

    public String getBackdropUrl() {
        return mBackdropUrl;
    }

    public void setBackdropUrl(String mBackdropUrl) {
        this.mBackdropUrl = mBackdropUrl;
    }

    public String getVoteAverage() {
        return mVoteAverage;
    }

    public void setVoteAverage(String mVoteAverage) {
        this.mVoteAverage = mVoteAverage;
    }

    public String getId() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
    }
}
