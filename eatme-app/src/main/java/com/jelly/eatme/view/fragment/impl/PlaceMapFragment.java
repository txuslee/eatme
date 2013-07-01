package com.jelly.eatme.view.fragment.impl;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jelly.eatme.application.EatmeConstants;
import com.jelly.eatme.content.PlacesContentProvider;
import com.jelly.eatme.view.activity.impl.EatmeActivity;

public class PlaceMapFragment extends SupportMapFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private String placeReference;
    private String placeId;

    private EatmeActivity activity;
    private GoogleMap map;

    public static PlaceMapFragment newInstance() {
        return new PlaceMapFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.activity = (EatmeActivity) getActivity();
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() != null) {
            this.placeReference = getArguments().getString(EatmeConstants.EXTRA_KEY_REFERENCE);
            this.placeId = getArguments().getString(EatmeConstants.EXTRA_KEY_ID);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = new String[]{
                PlacesContentProvider.KEY_ID,
                PlacesContentProvider.KEY_NAME,
                PlacesContentProvider.KEY_DISTANCE,
                PlacesContentProvider.KEY_REFERENCE,
                PlacesContentProvider.KEY_LOCATION_LAT,
                PlacesContentProvider.KEY_LOCATION_LNG
        };
        return new CursorLoader(this.activity, PlacesContentProvider.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        this.map = this.getMap();
        this.map.setMyLocationEnabled(true);
        //final Location location = this.map.getMyLocation();
        if (cursor.moveToFirst()) {
            final int idIndex = cursor.getColumnIndex(PlacesContentProvider.KEY_ID);
            final int nameIndex = cursor.getColumnIndex(PlacesContentProvider.KEY_NAME);
            final int referenceIndex = cursor.getColumnIndex(PlacesContentProvider.KEY_REFERENCE);
            final int latitudeIndex = cursor.getColumnIndex(PlacesContentProvider.KEY_LOCATION_LAT);
            final int longitudeIndex = cursor.getColumnIndex(PlacesContentProvider.KEY_LOCATION_LNG);
            for (int i = 0; i < cursor.getCount(); ++i) {
                final double latitude = cursor.getDouble(latitudeIndex);
                final double longitude = cursor.getDouble(longitudeIndex);
                final LatLng latLng = new LatLng(latitude, longitude);
                final String name = cursor.getString(nameIndex);
                final Marker marker = this.getMap().addMarker(new MarkerOptions().position(latLng).title(name));
                if (cursor.getString(idIndex).contentEquals(this.placeId) && cursor.getString(referenceIndex).contentEquals(this.placeReference)) {
                    this.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    marker.showInfoWindow();
                }
                cursor.moveToNext();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        if (this.map != null) {
            this.map.clear();
        }
    }

}
