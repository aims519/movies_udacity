package com.aimtech.android.movies;

/**
 * Created by Andy on 15/03/2016.
 */
public class Review {
    private String mAuthor;
    private String mContent;
    private String mUrl;

    public Review(String mAuthor, String mContent, String url) {
        this.mAuthor = mAuthor;
        this.mContent = mContent;
        this.mUrl = url;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String mAuthor) {
        this.mAuthor = mAuthor;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String mContent) {
        this.mContent = mContent;
    }

    public String getUrl() {
        return mUrl;
    }
}
