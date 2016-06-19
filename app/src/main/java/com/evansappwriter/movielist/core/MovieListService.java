package com.evansappwriter.movielist.core;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import com.evansappwriter.movielist.R;
import com.evansappwriter.movielist.util.Keys;
import com.evansappwriter.movielist.util.Utils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Set;

import cz.msebera.android.httpclient.Header;

public class MovieListService {
    private static final String TAG = "TELMATE.SERVICE";

    private static MovieListService mInstance = null;

    private static final int TIMEOUT_READ = 60000; // ms
    private static final int TIMEOUT_CONNECT = 15000; // ms

    // Standard and Demo ROMS have different api_key
    private static final String API_KEY = "a07e22bc18f5cb106bfe4cc1f83ad8ed";

    @SuppressWarnings("ConstantConditions")
    private static final String REST_API = " https://api.themoviedb.org/3/movie";

    public static final String ENDPOINT_NOW_PLAYING = "/now_playing";
    public static final String ENDPOINT_VIDEOS = "/videos";
    public static final String ENDPOINT_POPULAR = "/popular";


    private static Resources mRes;

    public interface OnUIResponseHandler {
        void onSuccess(String payload);
        void onFailure(String errorTitle, String errorText, int dialogId);
    }

    // private constructor prevents instantiation from other classes
    private MovieListService() {

    }

    /**
     * Creates a new instance of MovieListService.
     */
    public static MovieListService getInstance(Context context) {

        if (mInstance == null) {
            mInstance = new MovieListService();
        }

        mRes = context.getResources();

        return mInstance;
    }

    /**
     * *******************************************************************************************************
     */

    public void getMockVideos(Context context, OnUIResponseHandler handler) {
        InputStream in_s = context.getResources().openRawResource(R.raw.now_playing);

        StringBuffer fileContent = new StringBuffer("");

        byte[] buffer = new byte[1024];

        try {
            while (in_s.read(buffer) != -1) {
                fileContent.append(new String(buffer));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        handler.onSuccess(fileContent.toString());
    }

    public void get(final String endpoints, Bundle params, final OnUIResponseHandler handler) {
        Bundle urlParams = getAuthBundle();
        if (params != null) {
            urlParams.putAll(params);
        }

        String uri = REST_API + endpoints;
        uri += "?" + encodeUrl(urlParams);

        Utils.printLogInfo(TAG, "API URL: " + uri);
        AsyncHttpClient aClient = new AsyncHttpClient();
        aClient.setTimeout(TIMEOUT_READ);
        aClient.get(uri, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Utils.printLogInfo(TAG, "- Successful !: " + statusCode);

                processSuccessRepsonse(handler, new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Utils.printLogInfo(TAG, "- Failed !: " + statusCode);

                processFailureRepsonse(handler, new String(responseBody), e.toString());
            }
        });
    }

    public void post(String endpoints, Bundle params, final OnUIResponseHandler handler) {
        Bundle urlParams = getAuthBundle();

        String uri = REST_API + endpoints;
        uri += "?" + encodeUrl(urlParams);

        RequestParams requestparams = new RequestParams();
        for (String key : params.keySet()) {
            requestparams.put(key, params.get(key).toString());
        }

        Utils.printLogInfo(TAG, "API URL: " + uri);
        AsyncHttpClient aClient = new AsyncHttpClient();
        aClient.setTimeout(TIMEOUT_READ);
        aClient.post(uri, requestparams, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Utils.printLogInfo(TAG, "- Successful !: " + statusCode);

                processSuccessRepsonse(handler, new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Utils.printLogInfo(TAG, "- Failed !: " + statusCode);

                processFailureRepsonse(handler, new String(responseBody), e.toString());
            }
        });
    }

    private Bundle getAuthBundle() {
        Bundle params = new Bundle();

        params.putString(PARAM_API_KEY, API_KEY);

        return params;
    }

    public static String encodeUrl(Bundle parameters) {
        if (parameters == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder(200);
        boolean first = true;
        Set<String> keySet = parameters.keySet();

        for (String key : keySet) {
            Object parameter = parameters.get(key);

            if (!(parameter instanceof String)) {
                continue;
            }

            if (first) {
                first = false;
            } else {
                sb.append('&');
            }
            try {
                sb.append(URLEncoder.encode(key, HTTP.UTF_8));
            } catch (UnsupportedEncodingException e) {
                Utils.printStackTrace(e);
            }
            sb.append('=');
            try {
                sb.append(URLEncoder.encode(parameters.getString(key), HTTP.UTF_8));
            } catch (UnsupportedEncodingException e) {
                Utils.printStackTrace(e);
            }
        }
        return sb.toString();
    }

    private void processSuccessRepsonse(OnUIResponseHandler handler, String payload) {
        handler.onSuccess(payload);
    }

    private void processFailureRepsonse(OnUIResponseHandler handler, String payload, String exception) {
        String status = "";
        String status_msg = "";
        int dialogId = Keys.DIALOG_GENERAL_ERROR;
        if (payload != null) {
            BundledData data = new BundledData(MovieListParser.TYPE_PARSER_ERROR);
            data.setHttpData(payload);
            MovieListParser.parseResponse(data);
            status = (String) data.getAuxData()[0];
            status_msg = (String) data.getAuxData()[1];
            Utils.printLogInfo(TAG, "API error: ", status, status_msg);
        } else {
            status = mRes.getString(R.string.error_title);
            status_msg = mRes.getString(R.string.error_text);

            Utils.printLogInfo(TAG, "API error: ", exception);
        }

        handler.onFailure(status, status_msg, dialogId);
    }

    // PARAMS >>>>>>>>>

    public static final String PARAM_API_KEY = "api_key";
}
