package com.evansappwriter.movielist.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.evansappwriter.movielist.R;
import com.evansappwriter.movielist.core.BundledData;
import com.evansappwriter.movielist.core.MovieListParser;
import com.evansappwriter.movielist.core.MovieListService;
import com.evansappwriter.movielist.model.Movie;
import com.evansappwriter.movielist.util.AltArrayAdapter;
import com.evansappwriter.movielist.util.Keys;
import com.evansappwriter.movielist.util.Utils;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeStandalonePlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MovieListFragment extends BaseListFragment<Movie> {
    private static final String TAG = "MovieListFragment";

    private static final int REQ_START_STANDALONE_PLAYER = 1;
    private static final int REQ_RESOLVE_SERVICE_MISSING = 2;

    private boolean isInitialAPICall;

    private BaseActivity mActivity;

    // empty public constructor
    // read here why this is needed:
    // http://developer.android.com/reference/android/app/Fragment.html
    @SuppressWarnings("unused")
    public MovieListFragment() {

    }

    public static MovieListFragment newInstance(Bundle b) {
        MovieListFragment f = new MovieListFragment();
        if (b != null) {
            f.setArguments(b);
        }
        f.setHasOptionsMenu(true);
        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (BaseActivity) activity;
        setRetainInstance(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isInitialAPICall = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isInitialAPICall) {
            makeAPICall(onPrepareGetNew());
        }
    }

    @Override
    protected AltArrayAdapter<Movie> onCreateEmptyAdapter() {
        return new AltArrayAdapter<>(mActivity,
                new int[]{R.layout.movie, R.layout.popular_movie},
                null,
                new int[]{R.id.movie_backdrop, R.id.movie_poster, R.id.title, R.id.overview});
    }

    @Override
    protected AltArrayAdapter.ViewBinder onCreateViewBinder() {
        return new BinderMovies(mActivity);
    }

    @Override
    protected Bundle onPrepareGetNew() {
        isInitialAPICall = true;
        Bundle params = new Bundle();
        params.putString(Keys.KEY_ENDPOINT, MovieListService.ENDPOINT_NOW_PLAYING);
        return params;
    }

    @Override
    protected Bundle onPrepareGetOlder() {
        Bundle params = new Bundle();
        params.putString(Keys.KEY_ENDPOINT, MovieListService.ENDPOINT_POPULAR);
        return params;
    }

    protected void makeAPICall(Bundle params) {
        String endpoint = params.getString(Keys.KEY_ENDPOINT);

        MovieListService.getInstance(mActivity).get(endpoint, null, new MovieListService.OnUIResponseHandler() {

            @Override
            public void onSuccess(String payload) {
                if (mActivity == null || mActivity.isFinishing()) {
                    return;
                }

                if (payload != null) {
                    BundledData data = new BundledData(MovieListParser.TYPE_PARSER_NOWPLAYING);
                    data.setHttpData(payload);
                    MovieListParser.parseResponse(data);
                    if (data.getAuxData() == null) {
                        Utils.printLogInfo(TAG, "Parsing error: ", "");
                        mActivity.showError(getString(R.string.error_title), getString(R.string.movie_get_error), null);
                    } else {
                        ArrayList<Movie> movies = (ArrayList<Movie>) data.getAuxData()[0];
                        if (movies.size() == 0) {
                            toggleEmptyListMessage(true);
                        } else {
                            Collections.sort(movies);
                            showNew(movies);
                        }
                    }
                } else {
                    Utils.printLogInfo(TAG, "Payload error: ", "No Payload but a status code of 200");
                    mActivity.showError(getString(R.string.error_title), getString(R.string.movie_get_error), null);
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(String errorTitle, String errorText, int dialogId) {
                if (mActivity == null || mActivity.isFinishing()) {
                    return;
                }

                swipeRefreshLayout.setRefreshing(false);
                mActivity.showError(getString(R.string.error_title), getString(R.string.movie_get_error), null);
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int pos, long id) {

        getVideo((Movie) getListAdapter().getItem(pos));
    }

    private boolean canResolveIntent(Intent intent) {
        List<ResolveInfo> resolveInfo = mActivity.getPackageManager().queryIntentActivities(intent, 0);
        return resolveInfo != null && !resolveInfo.isEmpty();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_START_STANDALONE_PLAYER && resultCode != mActivity.RESULT_OK) {
            YouTubeInitializationResult errorReason =
                    YouTubeStandalonePlayer.getReturnedInitializationResult(data);
            if (errorReason.isUserRecoverableError()) {
                errorReason.getErrorDialog(mActivity, 0).show();
            } else {
                String errorMessage =
                        String.format(getString(R.string.error_player), errorReason.toString());
                Toast.makeText(mActivity, errorMessage, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getVideo(final Movie m) {
        mActivity.showProgress(getString(R.string.progress_text_get_video));
        MovieListService.getInstance(mActivity).get("/" + m.getId() + MovieListService.ENDPOINT_VIDEOS, null, new MovieListService.OnUIResponseHandler() {

            @Override
            public void onSuccess(String payload) {
                if (mActivity == null || mActivity.isFinishing()) {
                    return;
                }

                if (payload != null) {
                    BundledData data = new BundledData(MovieListParser.TYPE_PARSER_VIDEOS);
                    data.setHttpData(payload);
                    MovieListParser.parseResponse(data);
                    if (data.getAuxData() == null) {
                        Utils.printLogInfo(TAG, "Parsing error: ", "");
                        mActivity.showMessage(getString(R.string.error_title), getString(R.string.video_error), null);
                    } else {
                        String video_key = (String) data.getAuxData()[0];
                        Intent i = null;
                        if (TextUtils.isEmpty(video_key)) {
                            Utils.printLogInfo(TAG, "No video in payload: ", "");
                            mActivity.showMessage(getString(R.string.error_title), getString(R.string.video_error), null);
                        } else if (m.getVoteAvg() < Keys.POPULAR_AVG) {
                            i = new Intent(mActivity, MovieDetailActivity.class);
                            i.putExtra(Keys.KEY_ID, m.getId());
                            i.putExtra(Keys.KEY_TITLE, m.getTitle());
                            i.putExtra(Keys.KEY_OVERVIEW, m.getOverview());
                            i.putExtra(Keys.KEY_RELEASE_DATE, m.getReleaseDate());
                            i.putExtra(Keys.KEY_VOTE_AVG, m.getVoteAvg());
                            i.putExtra(Keys.KEY_VIDEO_KEY, video_key);
                            startActivity(i);
                        } else {
                            i = YouTubeStandalonePlayer.createVideoIntent(
                                    mActivity, Keys.YOUTUBE_API_KEY, video_key, 0, true, false);
                            if (i != null) {
                                if (canResolveIntent(i)) {
                                    startActivityForResult(i, REQ_START_STANDALONE_PLAYER);
                                } else {
                                    // Could not resolve the intent - must need to install or update the YouTube API service.
                                    YouTubeInitializationResult.SERVICE_MISSING
                                            .getErrorDialog(mActivity, REQ_RESOLVE_SERVICE_MISSING).show();
                                }
                            }
                        }
                    }
                } else {
                    Utils.printLogInfo(TAG, "Payload error: ", "No Payload but a status code of 200");
                    mActivity.showMessage(getString(R.string.error_title), getString(R.string.video_error), null);
                }
                mActivity.dismissProgress();
            }

            @Override
            public void onFailure(String errorTitle, String errorText, int dialogId) {
                if (mActivity == null || mActivity.isFinishing()) {
                    return;
                }

                mActivity.dismissProgress();
                mActivity.showMessage(getString(R.string.error_title), getString(R.string.video_error), null);
            }
        });

    }

    @Override
    public void onDetach() {
        mActivity = null;
        super.onDetach();
    }
}