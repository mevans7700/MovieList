package com.evansappwriter.movielist.ui;

import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.TextView;

import com.evansappwriter.movielist.R;
import com.evansappwriter.movielist.YouTubeFailureRecoveryActivity;
import com.evansappwriter.movielist.util.Keys;
import com.evansappwriter.movielist.util.Utils;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MovieDetailActivity extends YouTubeFailureRecoveryActivity {
    private String mYouTubeKey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_detail);

        initUI();
    }

    private void initUI () {
        Bundle b = getIntent().getExtras();
        // Title
        TextView title_tv = (TextView) findViewById(R.id.title);
        title_tv.setText(b.getString(Keys.KEY_TITLE));

        // Release Date
        TextView releasedate_tv = (TextView) findViewById(R.id.release_date);
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date date = null;
        try {
            date = fmt.parse(b.getString(Keys.KEY_RELEASE_DATE));
        } catch (ParseException e) {
            Utils.printStackTrace(e);
        }
        SimpleDateFormat fmtOut = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        releasedate_tv.setText(fmtOut.format(date));

        // Overview
        TextView overview_tv = (TextView) findViewById(R.id.overview);
        overview_tv.setText(b.getString(Keys.KEY_OVERVIEW));

        // Ratings
        RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingBar.setFocusable(false);
        ratingBar.setRating((float)b.getDouble(Keys.KEY_VOTE_AVG));

        // YouTubeVideo
        mYouTubeKey = b.getString(Keys.KEY_VIDEO_KEY);
        YouTubePlayerView youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
        youTubeView.initialize(Keys.YOUTUBE_API_KEY, this);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                        boolean wasRestored) {
        if (!wasRestored) {
            player.cueVideo(mYouTubeKey);
        }
    }

    @Override
    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return (YouTubePlayerView) findViewById(R.id.youtube_view);
    }
}
