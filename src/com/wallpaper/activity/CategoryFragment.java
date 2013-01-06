package com.wallpaper.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.wallpaper.core.GridFragment;
import com.wallpaper.core.NodeWallpaper;
import com.wallpaper.core.OnFragmentClickListener;
import com.wallpaper.core.com.jess.ui.TwoWayAdapterView;

import java.util.ArrayList;

public class CategoryFragment extends GridFragment {

	public static final String FRAGMENT_TAG = "CategoryFragment";
	public static final String BUNDLE_TAG = "category_fragment_data";
	public static String TAG = "CategoryFragment";

	private ArrayList<NodeWallpaper> mData;
	private OnFragmentClickListener mListener;

	private boolean mUseImageTitle;

	@SuppressWarnings("unchecked")
	@Override
	public void onActivityCreated (Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		super.setRetainInstance(false);

		this.mUseImageTitle = super.getResources().getBoolean(R.bool.config_enable_image_names);

		this.mData = (ArrayList<NodeWallpaper>) super.getArguments().getSerializable(BUNDLE_TAG);
		if (this.mData != null && !this.mData.isEmpty()) {
			super.setData(this.mData);
		}
	}

	@Override
	public void onAttach (Activity a) {
		super.onAttach(a);
		if (a instanceof OnFragmentClickListener) {
			mListener = (OnFragmentClickListener) a;
		}
	}

	@Override
	public boolean onItemLongClick (AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		return false;
	}

	@Override
	public View getView (int position, View convertView, ViewGroup parent, LayoutInflater inflater) {

		View view = inflater.inflate(R.layout.row_wallpaper_item, null, false);
		ImageView thumb = (ImageView) view.findViewById(R.id.wp_thumb);
		TextView title = (TextView) view.findViewById(R.id.wp_title);

		if (!this.mUseImageTitle) {
			view.findViewById(R.id.wp_title_bg).setVisibility(View.GONE);
		}

		final NodeWallpaper node = this.mData.get(position);
		title.setText(node.name);

		ImageLoader.getInstance().displayImage(node.thumbUrl, thumb);

		return view;
	}

	@Override
	public void onItemClick (TwoWayAdapterView<?> parent, View view, int position, long id) {
		mListener.onWallpaperSelected(this.mData.get(position));
	}

}
