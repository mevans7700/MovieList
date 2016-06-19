package com.evansappwriter.movielist.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.evansappwriter.movielist.R;
import com.evansappwriter.movielist.util.Utils;

public class MovieListActivity extends BaseActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
    }

    private void initUI() {

        Bundle b = getIntent().getExtras();

        int code = (getClass().getName() + "MovieListFragment").hashCode();
        Utils.printLogInfo("FRAG", "id: ", code);

        FragmentManager fm = getFragmentManager();
        if (fm.findFragmentByTag("f" + code) == null) // first time in the activity
        {
            Utils.printLogInfo("FRAG", 'f', code);
            Fragment f = MovieListFragment.newInstance(b);

            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.placeholder, f, "f" + code);
            ft.commit();
        }
    }
}
