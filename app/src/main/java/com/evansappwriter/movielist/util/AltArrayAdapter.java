package com.evansappwriter.movielist.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * An easy adapter to map data from a list of objects to views defined in an XML file.
 */
@SuppressWarnings("unchecked")
public class AltArrayAdapter<T extends Comparable<? super T>> extends BaseAdapter implements Filterable {

    /**
     * Contains the list of objects that represent the data of this ArrayAdapter.
     * The content of this list is referred to as "the array" in the documentation.
     */
    private List<T> mObjects;

    /**
     * Lock used to modify the content of {@link #mObjects}. Any write operation
     * performed on the array should be synchronized on this lock. This lock is also
     * used by the filter (see {@link #getFilter()} to make a synchronized copy of
     * the original array of data.
     */
    private final Object mLock = new Object();

    private boolean mDataValid;

    private int[] mTo; // a list of view ids representing the views to which the data must be bound

    private ViewBinder mViewBinder;

    private final int[] mLayouts;

    /**
     * The resources indicating what views to inflate to display the content of this array adapter in a drop down
     * widget.
     */
    private int[] mDropDownLayouts;

    /**
     * Indicates whether or not {@link #notifyDataSetChanged()} must be called whenever
     * {@link #mObjects} is modified.
     */
    private boolean mNotifyOnChange = true;

    // A copy of the original mObjects array, initialized from and then used instead as soon as
    // the mFilter ArrayFilter is used. mObjects will then only contain the filtered values.
    private List<T> mOriginalValues;
    private ArrayFilter mFilter;

    private final Context mContext;
    private final LayoutInflater mInflater;

    /**
     * Constructor.
     *
     * @param context The context where the ListView is running
     * @param layouts array of resource identifiers of layout files that define the views for this list item.
     *                getViewTypeCount() will return the length of this array.
     * @param objects The list of objects. Can be null if the data is not available yet.
     * @param to      The views that should display column in the "from" parameter. The first N views in this list are given
     *                the values of the first N columns in the from parameter. Can be null if the data is not available
     *                yet.
     */
    public AltArrayAdapter(Context context, int[] layouts, List<T> objects, int[] to) {
        mContext = context;

        mLayouts = mDropDownLayouts = layouts;
        mObjects = objects;
        mTo = to;

        mDataValid = objects != null;

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Adds the specified object at the end of the array.
     *
     * @param object The object to add at the end of the array.
     */
    public void add(T object) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.add(object);
            } else {
                mObjects.add(object);
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Adds the specified Collection at the end of the array.
     *
     * @param collection The Collection to add at the end of the array.
     */
    public void addAll(Collection<? extends T> collection) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.addAll(collection);
            } else {
                mObjects.addAll(collection);
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Adds the specified items at the end of the array.
     *
     * @param items The items to add at the end of the array.
     */
    public void addAll(T... items) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                Collections.addAll(mOriginalValues, items);
            } else {
                Collections.addAll(mObjects, items);
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Inserts the specified object at the specified index in the array.
     *
     * @param object The object to insert into the array.
     * @param index  The index at which the object must be inserted.
     */
    public void insert(T object, int index) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.add(index, object);
            } else {
                mObjects.add(index, object);
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Removes the specified object from the array.
     *
     * @param object The object to remove.
     */
    public void remove(T object) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.remove(object);
            } else {
                mObjects.remove(object);
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Remove all elements from the list.
     */
    public void clear() {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.clear();
            } else {
                mObjects.clear();
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Sorts the content of this adapter using the specified comparator.
     *
     * @param comparator The comparator used to sort the objects contained
     *                   in this adapter.
     */
    public void sort(Comparator<? super T> comparator) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                Collections.sort(mOriginalValues, comparator);
            } else {
                Collections.sort(mObjects, comparator);
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Sorts the content of this adapter using the default comparator.
     */
    public void sort() {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                Collections.sort(mOriginalValues);
            } else {
                Collections.sort(mObjects);
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }


    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        mNotifyOnChange = true;
    }

    /**
     * Control whether methods that change the list ({@link #add},
     * {@link #insert}, {@link #remove}, {@link #clear}) automatically call
     * {@link #notifyDataSetChanged}.  If set to false, caller must
     * manually call notifyDataSetChanged() to have the changes
     * reflected in the attached view.
     * <p/>
     * The default is true, and calling notifyDataSetChanged()
     * resets the flag to true.
     *
     * @param notifyOnChange if true, modifications to the list will
     *                       automatically call {@link
     *                       #notifyDataSetChanged}
     */
    public void setNotifyOnChange(boolean notifyOnChange) {
        mNotifyOnChange = notifyOnChange;
    }

    /**
     * Returns the context associated with this array adapter. The context is used
     * to create views from the resource passed to the constructor.
     *
     * @return The Context associated with this adapter.
     */
    public Context getContext() {
        return mContext;
    }

    @Override
    public int getCount() {
        if (mDataValid && mObjects != null) {
            return mObjects.size();
        }
        return 0;
    }

    /**
    * Returns the array.
    */
    public List<T> getItems() {
        return mObjects;
    }

    @Override
    public T getItem(int position) {
        if (mDataValid && mObjects != null) {
            return mObjects.get(position);
        }
        return null;
    }

    /**
     * Returns the position of the specified item in the array.
     *
     * @param item The item to retrieve the position of.
     * @return The position of the specified item.
     */
    public int getPosition(T item) {
        if (mDataValid && mObjects != null) {
            return mObjects.indexOf(item);
        }
        return 0;
    }

//    @Override
//    public boolean hasStableIds() {
//        return ???;
//    }
//
//    @Override
//    public long getItemId(int position) {
//        if (mDataValid && mObjects != null) {
//            if (position >= 0 && position < getCount()) {
//                return getItem(position).hashCode();
//            }
//
//            return 0;
//        }
//
//        return 0;
//    }

    @Override
    public long getItemId(int position) {
        if (mDataValid && mObjects != null) {
            return position;
        }
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the array is valid");
        }

        if (position < 0 || position >= getCount()) {
            throw new IllegalStateException("couldn't move array to position " + position);
        }

        T object = mObjects.get(position);

        View v = convertView == null ? newView(mContext, position, parent) : convertView;
        bindView(mContext, v, object);
        return v;
    }

    public View newView(Context context, int position, ViewGroup parent) {
        int type = getItemViewType(position);
        // Utils.printLogInfo("AAA", "getItemViewType: ", type, ", position: ", position);

        View v = mInflater.inflate(mLayouts[type], parent, false);

        ViewHolder vh = new ViewHolder();

        int count = mTo.length;
        vh.views = new View[count];

        for (int i = 0; i < count; ++i) {
            vh.views[i] = v.findViewById(mTo[i]);
        }

        v.setTag(vh);

        return v;
    }

    /**
     * Binding is done via the {@link ViewBinder#setViewValue(Context, View, T)}.
     *
     * @see #getViewBinder()
     * @see #setViewBinder(ViewBinder)
     */
    public void bindView(Context context, View convertView, Object object) {
        ViewHolder vh = (ViewHolder) convertView.getTag();

        int count = mTo.length; // should be == vh.view.length
        for (int i = 0; i < count; ++i) {
            View v = vh.views[i];
            if (v != null) {
                mViewBinder.setViewValue(context, v, object);
            }
        }
    }

    /**
     * <p>
     * Sets the layout resources to create the drop down views.
     * </p>
     *
     * @param layouts the layout resources defining the drop down views
     * @see #getDropDownView(int, View, ViewGroup)
     */
    public void setDropDownLayouts(int[] layouts) {
        mDropDownLayouts = layouts;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the array is valid");
        }
        if (position < 0 || position >= mObjects.size()) {
            throw new ArrayIndexOutOfBoundsException("couldn't move array to position " + position);
        }

        T object = mObjects.get(position);

        View v = convertView == null ? newDropDownView(mContext, position, parent) : convertView;

        bindView(mContext, v, object);
        return v;
    }

    public View newDropDownView(Context context, int position, ViewGroup parent) {
        int type = getItemViewType(position);
        // Utils.printLogInfo("AAA", "getItemViewType: ", type, ", position: ", position);
        return mInflater.inflate(mDropDownLayouts[type], parent, false);
    }

    /**
     * Returns the {@link ViewBinder} used to bind data to views.
     *
     * @return a ViewBinder or null if the binder does not exist
     * @see #bindView(Context, View, T
     * @see #setViewBinder(ViewBinder)
     */
    public ViewBinder getViewBinder() {
        return mViewBinder;
    }

    /**
     * Sets the binder used to bind data to views.
     *
     * @param viewBinder the binder used to bind data to views, can be null to remove the existing binder
     * @see #bindView(Context, View, T)
     * @see #getViewBinder()
     */
    public void setViewBinder(ViewBinder viewBinder) {
        mViewBinder = viewBinder;
    }

    @Override
    public int getViewTypeCount() {
        return mLayouts.length;
    }

    @Override
    public int getItemViewType(int position) {
        if (mViewBinder == null) {
            Utils.printLogInfo("AAA", "binder null");
            return 0;
        }

        return mViewBinder.getItemViewType(mObjects, position);
    }

    /**
     * Swap in a new array, returning the old array.
     *
     * @param objects The new array to be used.
     * @return Returns the previously set array, or null if there was not one.
     * If the given new array is the same instance as the previously set
     * array, null is also returned.
     */
    public List<T> swapItems(List<T> objects) {
        // Utils.printLogInfo("AAA", objects == null ? "null" : TextUtils.join(", ", objects.toArray(new T[0])));

        if (objects == mObjects) {
            return null;
        }

        List<T> oldObjects = mObjects;

        mObjects = objects;

        if (objects == null) {
            mDataValid = false;

            // notify the observers about the lack of a data set
            notifyDataSetInvalidated();
        } else {
            mDataValid = true;

            // notify the observers about the new objects
            notifyDataSetChanged();
        }

        return oldObjects;
    }

    private static class ViewHolder {
        View[] views;
    }

    /**
     * This class can be used by external clients of AltArrayAdapter to bind values from the object to views.
     * <p/>
     * You should use this class to bind values from the object to views that are not directly supported by
     * AltArrayAdapter or to change the way binding occurs for views supported by AltArrayAdapter.
     */
    public static interface ViewBinder<T> {
        /**
         * Binds the object data to the specified view.
         *
         * @param view   the view to bind the data to
         * @param object the object to get the data from
         */
        void setViewValue(Context context, View view, T object);

        int getItemViewType(List<T> objects, int position);
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    /**
     * <p>An array filter constrains the content of the array adapter with
     * a prefix. Each item that does not start with the supplied prefix
     * is removed from the list.</p>
     */
    private class ArrayFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mOriginalValues == null) {
                synchronized (mLock) {
                    mOriginalValues = new ArrayList<T>(mObjects);
                }
            }

            if (prefix == null || prefix.length() == 0) {
                ArrayList<T> list;
                synchronized (mLock) {
                    list = new ArrayList<T>(mOriginalValues);
                }
                results.values = list;
                results.count = list.size();
            } else {
                String prefixString = prefix.toString().toLowerCase();

                ArrayList<T> values;
                synchronized (mLock) {
                    values = new ArrayList<T>(mOriginalValues);
                }

                final ArrayList<T> newValues = new ArrayList<T>();

                for (final T value : values) {
                    final String valueText = value.toString().toLowerCase();

                    // First match against the whole, non-splitted value
                    if (valueText.startsWith(prefixString)) {
                        newValues.add(value);
                    } else {
                        final String[] words = valueText.split(" ");

                        // Start at index 0, in case valueText starts with space(s)
                        for (String word : words) {
                            if (word.startsWith(prefixString)) {
                                newValues.add(value);
                                break;
                            }
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mObjects = (List<T>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
