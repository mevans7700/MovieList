package com.evansappwriter.movielist.ui;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.evansappwriter.movielist.R;
import com.evansappwriter.movielist.model.Movie;
import com.evansappwriter.movielist.util.AltArrayAdapter;
import com.evansappwriter.movielist.util.Keys;
import com.evansappwriter.movielist.util.Utils;

import java.util.List;


public class BinderMovies implements AltArrayAdapter.ViewBinder<Movie>  {
    private static final String TAG = "BinderMovies";

    private static final String IMAGE_PATH = "http://image.tmdb.org/t/p/w500";

    private final int mImageWidth;
    private final int mImageHeight;


    public BinderMovies(Context context) {
        Resources res = context.getResources();
        mImageWidth = (int) (res.getDimension(R.dimen.movie_width));
        mImageHeight = (int) (res.getDimension(R.dimen.movie_height));
    }

    @Override
    public void setViewValue(Context context, View view, Movie object) {
        switch (view.getId()) {
            case R.id.movie_backdrop:
                String backdropPath = IMAGE_PATH + object.getBackdropPath();
                Utils.printLogInfo(TAG, "Cover Url: ", backdropPath);
                ImageView iv = (ImageView) view;
                iv.setClipToOutline(true);
                Glide.with(context.getApplicationContext())
                        .load(backdropPath)
                        .placeholder(R.drawable.default_backdrop)
                        .error(R.drawable.default_backdrop)
                        .into(iv);
                break;
            case R.id.movie_poster:
                String posterPath = IMAGE_PATH + object.getPosterPath();
                Utils.printLogInfo(TAG, "Cover Url: ", posterPath);
                ImageView iv2 = (ImageView) view;
                iv2.setClipToOutline(true);
                Glide.with(context.getApplicationContext())
                        .load(posterPath)
                        .placeholder(R.drawable.default_movie)
                        .error(R.drawable.default_movie)
                        .override(mImageWidth,mImageHeight)
                        .into(iv2);

                break;
            case R.id.title:
                ((TextView) view).setText(object.getTitle());
                break;

            case R.id.overview:
                ((TextView) view).setText(object.getOverview());
                break;
        }

    }

    @Override
    public int getItemViewType(List<Movie> objects, int position) {
        return objects.get(position).getVoteAvg() < Keys.POPULAR_AVG ? 0 : 1;
    }
}
