package com.jelly.eatme.view.fragment.impl;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import com.jelly.eatme.content.PlacesContentProvider;
import com.jelly.eatme.view.activity.impl.EatmeActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlaceListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final Logger Log = LoggerFactory.getLogger(PlaceListFragment.class.getSimpleName());

    protected Cursor cursor = null;
    protected SimpleCursorAdapter adapter;
    protected EatmeActivity activity;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.activity = (EatmeActivity) getActivity();
        Log.debug("Start places fragment");
        // Create a new SimpleCursorAdapter that displays the name of each nearby venue and the current distance to it.
        this.adapter = new SimpleCursorAdapter(
                this.activity,
                android.R.layout.two_line_list_item,
                this.cursor,
                new String[]{PlacesContentProvider.KEY_NAME, PlacesContentProvider.KEY_DISTANCE},
                new int[]{android.R.id.text1, android.R.id.text2},
                0);
        // Allocate the adapter to the List displayed within this fragment.
        setListAdapter(this.adapter);

        // Populate the adapter / list using a Cursor Loader.
        getLoaderManager().initLoader(0, null, this);
    }

    /**
     * {@inheritDoc}
     * When a venue is clicked, fetch the details from your server and display the detail page.
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long theid) {
        super.onListItemClick(l, v, position, theid);
        // Find the ID and Reference of the selected venue.
        // These are needed to perform a lookup in our cache and the Google Places API server respectively.
        Cursor c = this.adapter.getCursor();
        c.moveToPosition(position);
        String reference = c.getString(c.getColumnIndex(PlacesContentProvider.KEY_REFERENCE));
        String id = c.getString(c.getColumnIndex(PlacesContentProvider.KEY_ID));
        this.activity.selectPlace(reference, id);
    }

    /**
     * {@inheritDoc}
     * This loader will return the ID, Reference, Name, and Distance of all the venues
     * currently stored in the {@link PlacesContentProvider}.
     */
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[]{PlacesContentProvider.KEY_ID, PlacesContentProvider.KEY_NAME, PlacesContentProvider.KEY_DISTANCE, PlacesContentProvider.KEY_REFERENCE};
        Log.debug("OnCreateLoader loading provider URI '{}'", PlacesContentProvider.CONTENT_URI.toString());
        return new CursorLoader(this.activity, PlacesContentProvider.CONTENT_URI, projection, null, null, null);
    }

    /**
     * {@inheritDoc}
     * When the loading has completed, assign the cursor to the adapter / list.
     */
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    /**
     * {@inheritDoc}
     */
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

}