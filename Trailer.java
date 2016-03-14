package com.aimtech.android.movies;

/**
 * Created by Andy on 14/03/2016.
 */
public class Trailer {
    private String mTitle;
    private String mVideoId;

    public Trailer(String mTitle, String mVideoId) {
        this.mTitle = mTitle;
        this.mVideoId = mVideoId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getVideoId() {
        return mVideoId;
    }

    public void setVideoId(String mVideoId) {
        this.mVideoId = mVideoId;
    }
}
