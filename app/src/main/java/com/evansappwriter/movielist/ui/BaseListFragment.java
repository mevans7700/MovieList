package com.evansappwriter.movielist.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.evansappwriter.movielist.R;
import com.evansappwriter.movielist.util.AltArrayAdapter;
import com.evansappwriter.movielist.util.Utils;

import java.util.List;


public abstract class BaseListFragment<T extends Comparable<? super T>> extends Fragment {
    private static final String TAG = "BASE.LIST.FRAGMENT";

    protected SharedPreferences sharedPrefs;

    private AltArrayAdapter<T> mAdapter;
    private ListView mList;
    protected SwipeRefreshLayout swipeRefreshLayout;
    private boolean mRunNew = true;

    final private AdapterView.OnItemClickListener mOnClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            onListItemClick((ListView)parent, v, position, id);
        }
    };

    /**
     * Empty public constructor. Read here why this is needed:
     * http://developer.android.com/reference/android/app/Fragment.html
     */
    @SuppressWarnings("unused")
    public BaseListFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        sharedPrefs = ((BaseActivity) getActivity()).sharedPrefs;
        setRetainInstance(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Utils.printLogInfo(TAG, "onCreateView(): ", toString());
        View v = inflater.inflate(R.layout.basic_list, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Utils.printLogInfo(TAG, "onActivityCreated(): ", toString());

        // we send null here if setRetainInstance(true), to avoid state restore bug after onDestroy()
        super.onActivityCreated(getRetainInstance() ? null : savedInstanceState);

        initUI();

        if (mAdapter == null) {
            mAdapter = onCreateEmptyAdapter();
            mAdapter.setViewBinder(onCreateViewBinder());

            setListShown(true); // this instantly shows the progress indicator

            mList.setAdapter(mAdapter);

            boolean isEmpty = mAdapter.getCount() == 0;
            if (isEmpty) {
                setListShown(false);
            }

            getNew();
        } else {
            mList.setAdapter(mAdapter);
        }
    }

    private void initUI() {
        mList = (ListView) getView().findViewById(R.id.listview);
        mList.setOnItemClickListener(mOnClickListener);
        swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.srl_main);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mRunNew) {
                    mRunNew = false;
                    Toast.makeText(getActivity(), "Getting Popular Movie List", Toast.LENGTH_LONG).show();
                    getOlder();
                } else {
                    mRunNew = true;
                    Toast.makeText(getActivity(), "Getting Now Playing Movie List", Toast.LENGTH_LONG).show();
                    getNew();
                }

            }
        });
    }

    public ListView getListView() {
        return mList;
    }

    public ListAdapter getListAdapter() {
        return mAdapter;
    }

    /**
     * Creates an empty adapter we will use to display the loaded data.
     *
     * @return the adapter
     */
    protected abstract AltArrayAdapter<T> onCreateEmptyAdapter();

    protected abstract AltArrayAdapter.ViewBinder onCreateViewBinder();

    protected void onListItemClick(ListView parent, View v, int position, long id){

    }

    protected Bundle onPrepareGetNew() {
        return new Bundle();
    }

    public void getNew() {
        makeAPICall(onPrepareGetNew());
    }

    public void showNew(List<T> objects) {
        mAdapter.swapItems(objects);

        try {
            setListShown(true);
        } catch (IllegalStateException e) {
            Utils.printStackTrace(e);
        }

        if (objects != null) {
            onShowNew();
        }
    }

    /**
     * This runs on the UI thread. The default implementation does nothing.<br />
     * <p/>
     * This method is called at the end of the showNew() method.
     */
    protected void onShowNew() {

    }

    protected Bundle onPrepareGetOlder() {
        return new Bundle();
    }

    protected void makeAPICall(Bundle args) {

    }

    protected void getOlder() {
        makeAPICall(onPrepareGetOlder());
    }

    protected void showOlder(List<T> objects) {
        if (objects == null) {
            return;
        }

        if (objects.size() == 0) {
            return;
        }

        mAdapter.setNotifyOnChange(false);
        mAdapter.addAll(objects);
        mAdapter.setNotifyOnChange(true);

        mAdapter.sort();

        onShowOlder();
    }

    /**
     * This runs on the UI thread. The default implementation does nothing.<br />
     * <p/>
     * This method is called at the end of the showOlder() method.
     */
    protected void onShowOlder() {

    }

    public void toggleEmptyListMessage(boolean shouldDisplay) {
        if (getView() == null) {
            return;
        }

        // turn off spinning progress
        setListShown(true);

        getView().findViewById(R.id.empty_holder).setVisibility(shouldDisplay ? View.VISIBLE : View.GONE);
    }

    public void setListShown(boolean shown) {
        if (getView() == null) {
            return;
        }

        getView().findViewById(R.id.listContainer).setVisibility(shown ? View.VISIBLE : View.GONE);
        getView().findViewById(R.id.progressContainer).setVisibility(shown ? View.GONE : View.VISIBLE);
    }
}
