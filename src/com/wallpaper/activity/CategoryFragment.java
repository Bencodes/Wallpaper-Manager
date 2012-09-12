package com.wallpaper.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.wallpaper.activity.R;
import com.wallpaper.core.GridFragment;
import com.wallpaper.core.NodeWallpaper;
import com.wallpaper.core.OnFragmentClickListener;
import com.wallpaper.core.com.jess.ui.TwoWayAdapterView;
import com.wallpaper.core.com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class CategoryFragment extends GridFragment {

	public static final String BUNDLE_TAG = "category_fragment_data";
	public static String TAG = "CategoryFragment";

	private ArrayList<NodeWallpaper> mData;
	private OnFragmentClickListener mListener;

	@SuppressWarnings("unchecked")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mData = (ArrayList<NodeWallpaper>) super.getArguments()
				.getSerializable(BUNDLE_TAG);
		if (mData != null) {
			super.setData(mData);
		}
	}

	@Override
	public void onAttach(Activity a) {
		super.onAttach(a);
		if (a instanceof OnFragmentClickListener) {
			mListener = (OnFragmentClickListener) a;
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		return false;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent,
			LayoutInflater inflater) {

		Log.i(TAG, "Position: " + position);

		View view = inflater.inflate(R.layout.row_wallpaper_item, null, false);
		ImageView thumb = (ImageView) view.findViewById(R.id.wp_thumb);
		TextView title = (TextView) view.findViewById(R.id.wp_title);

		final NodeWallpaper node = this.mData.get(position);
		title.setText(node.name);
		UrlImageViewHelper.setUrlDrawable(thumb, node.thumbUrl);

		return view;
	}

	@Override
	public void onItemClick(TwoWayAdapterView<?> parent, View view,
			int position, long id) {
		mListener.onWallpaperSelected(this.mData.get(position));
	}

}
