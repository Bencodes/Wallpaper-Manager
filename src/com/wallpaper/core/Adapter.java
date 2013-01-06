package com.wallpaper.core;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.SpinnerAdapter;

import java.util.ArrayList;

public class Adapter extends BaseAdapter implements SpinnerAdapter {

	private final String TAG = "Adapter";
	private final OnGetViewListener mListener;
	private ArrayList<?> mData;
	private final LayoutInflater mInflater;

	public static interface OnGetViewListener {
		public View getView (int position, View convertView, ViewGroup parent, LayoutInflater mInflater);
	}

	public Adapter (OnGetViewListener listener, Activity activity) {
		this.mData = new ArrayList<Object>();
		this.mInflater = LayoutInflater.from(activity);
		this.mListener = listener;
	}

	public Adapter (OnGetViewListener listener, Activity activity, ArrayList<?> data) {
		this.mData = (data == null) ? new ArrayList<Object>() : data;
		this.mInflater = LayoutInflater.from(activity);
		this.mListener = listener;
	}

	public ArrayList<?> getData () {
		return this.mData;
	}

	public void setData (ArrayList<?> data) {
		this.mData = data;
	}

	public void clearData () {
		this.mData.clear();
	}

	@Override
	public int getCount () {
		if (mData == null)
			Log.d(TAG, "getCount() Data Set Is Null");
		return (mData != null) ? mData.size() : 0;
	}

	@Override
	public Object getItem (int position) {
		if (mData == null)
			Log.d(TAG, "getItem(int position) Data Set Is Null");
		return (mData != null) ? mData.get(position) : null;
	}

	@Override
	public long getItemId (int position) {
		if (mData == null)
			Log.d(TAG, "getItemId(int position) Data Set Is Null");
		return (mData != null) ? position : 0;
	}

	@Override
	public View getView (int position, View convertView, ViewGroup parent) {
		return (mListener == null) ? new LinearLayout(mInflater.getContext()) : this.mListener.getView(position, convertView, parent, mInflater);
	}

	@Override
	public View getDropDownView (int position, View convertView, ViewGroup parent) {
		return (mListener == null) ? new LinearLayout(mInflater.getContext()) : this.mListener.getView(position, convertView, parent, mInflater);
	}

}
