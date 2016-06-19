package com.evansappwriter.movielist.core;

import com.evansappwriter.movielist.model.Movie;
import com.evansappwriter.movielist.util.Keys;
import com.evansappwriter.movielist.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

/**
 * various parsers that handle the responses fetched from the server
 */
public final class MovieListParser {
    private static final String TAG = "TELMATE.PARSER";

    public static final int TYPE_PARSER_ERROR = -1;
    public static final int TYPE_PARSER_NONE = 0;
    public static final int TYPE_PARSER_NOWPLAYING = 1;
    public static final int TYPE_PARSER_VIDEOS = 2;


    // this class cannot be instantiated
    private MovieListParser() {

    }

    public static void parseResponse(BundledData data) {
        int parserType = data.getParserType();

        Utils.printLogInfo(TAG, data.getHttpData());

        switch (parserType) {
            case TYPE_PARSER_NOWPLAYING:
                parseMovieList(data);
                break;
            case TYPE_PARSER_VIDEOS:
                parseVideos(data);
                break;
            case TYPE_PARSER_ERROR:
                parseError(data);
                break;
            case TYPE_PARSER_NONE:
            default:
                // no parse needed
                break;
        }
    }

    private static void parseMovieList(BundledData data) {
        if (getStringObject(data.getHttpData()) == null) {
            data.setAuxData();
            return;
        }

        try {
            // starting to parse...
            JSONObject jObject = new JSONObject(data.getHttpData());
            JSONArray jsonArray = jObject.getJSONArray(KEY_RESULTS);

            // ensure resources get cleaned up timely and properly
            data.setHttpData(null);

            ArrayList<Movie> movies = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jo = jsonArray.getJSONObject(i);
                Movie movie = new Movie();
                movie.setId(jo.getInt(Keys.KEY_ID));
                movie.setTitle(jo.getString(Keys.KEY_TITLE));
                movie.setOverview(jo.getString(Keys.KEY_OVERVIEW));
                movie.setPosterPath(jo.getString(Keys.KEY_POSTER_PATH));
                movie.setBackdropPath(jo.getString(Keys.KEY_BACKDROP_PATH));
                movie.setReleaseDate(jo.getString(Keys.KEY_RELEASE_DATE));
                movie.setVoteAvg(jo.getDouble(Keys.KEY_VOTE_AVG));

                movies.add(movie);
            }
            Collections.sort(movies);
            data.setAuxData(movies);
        } catch (Exception e) {
            Utils.printStackTrace(e);
            data.setHttpData(null);
            data.setAuxData();
        }
    }

    private static void parseVideos(BundledData data) {
        if (getStringObject(data.getHttpData()) == null) {
            data.setAuxData();
            return;
        }

        try {
            // starting to parse...
            JSONObject jObject = new JSONObject(data.getHttpData());
            JSONArray jsonArray = jObject.getJSONArray(KEY_RESULTS);

            // ensure resources get cleaned up timely and properly
            data.setHttpData(null);

            JSONObject jo = jsonArray.getJSONObject(jsonArray.length()-1);
            String key = jo.getString(Keys.KEY_VIDEO_KEY);
            data.setAuxData(key);
        } catch (Exception e) {
            Utils.printStackTrace(e);
            data.setHttpData(null);
            data.setAuxData();
        }
    }

    private static void parseError(BundledData data) {
        if (getStringObject(data.getHttpData()) == null) {
            data.setAuxData("Bad Payload", data.getHttpData());
            return;
        }

        try {
            JSONObject json = new JSONObject(data.getHttpData());

            // ensure resources get cleaned up timely and properly
            data.setHttpData(null);


            String status = "" + json.getInt(KEY_STATUS_CODE);
            String status_msg = json.getString(KEY_STATUS_MESSAGE);

            data.setAuxData(status, status_msg);
        } catch (Exception e) {
            Utils.printStackTrace(e);
            data.setAuxData("Server Error", data.getHttpData());
            data.setHttpData(null);
        }
    }

    private static String getStringObject(String txt) {
        return txt == null ? null : txt.equalsIgnoreCase("null") ? null : txt;
    }

    public static final String KEY_RESULTS = "results";
    public static final String KEY_STATUS_CODE = "status_code";
    public static final String KEY_STATUS_MESSAGE= "status_message";

}
