package com.wallpaper.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.wallpaper.activity.R;
import com.wallpaper.core.NodeWallpaper;
import com.wallpaper.core.PendingImageView;
import com.wallpaper.core.PendingImageView.OnDisplayImage;
import com.wallpaper.core.com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class WallpaperFragment extends SherlockFragment implements
		OnDisplayImage {

	public static final String BUNDLE_TAG = "wallpaper_fragment_data";
	private NodeWallpaper mNode;
	private ProgressBar mPending;
	private PendingImageView mImageView;
	private View mView;

	private boolean mImageDrawableSet = false;
	private boolean mApplyImageOnDisplay = false;
	private boolean mSaveImageOnDisplay = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		super.setHasOptionsMenu(true);
		super.setRetainInstance(true);

		this.mView = inflater.inflate(R.layout.fragment_full_wallpaper,
				container, false);
		return this.mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.mNode = (NodeWallpaper) super.getArguments().getSerializable(
				BUNDLE_TAG);

		final ActionBar ab = ((SherlockFragmentActivity) super.getActivity())
				.getSupportActionBar();
		if (ab != null) {
			ab.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			ab.setDisplayHomeAsUpEnabled(true);
			ab.setDisplayShowHomeEnabled(false);
			ab.setDisplayShowTitleEnabled(true);
			ab.setTitle(mNode.name);
		}

		this.mPending = (ProgressBar) super.getView()
				.findViewById(R.id.pending);
		this.mImageView = (PendingImageView) mView.findViewById(R.id.wp_image);
		this.mImageView.setOnImageDisplayListener(this);

		UrlImageViewHelper.setUrlDrawable(this.mImageView, this.mNode.url);
	}

	@Override
	public void onImageDisplayImage(boolean success) {
		if (success) {
			this.mImageDrawableSet = true;
			this.mPending.setVisibility(View.GONE);
			this.mImageView.setZoomable(true);

			if (this.mApplyImageOnDisplay)
				this.applyImage();
			if (this.mSaveImageOnDisplay)
				this.exportImage();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.add(1, 1, 1, "Save").setIcon(android.R.drawable.ic_menu_save)
				.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);

		menu.add(2, 2, 2, "Apply").setIcon(android.R.drawable.ic_menu_set_as)
				.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getTitle().equals("Save")) {
			this.exportImage();
			return true;
		} else if (item.getTitle().equals("Apply")) {
			this.applyImage();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	public void exportImage() {
		if (!this.mImageDrawableSet) {
			this.mApplyImageOnDisplay = true;
			return;
		}

		try {
			final Bitmap bitmap = this.mImageView.getBitmap();
			if (bitmap == null) {
				throw new Exception("Bitmap returned null");
			}

			final File dir = new File(
					Environment.getExternalStorageDirectory(), super
							.getResources().getString(
									R.string.config_external_storage_folder));
			if (!dir.exists()) {
				dir.mkdirs();
			}

			final File img = new File(dir, this.mNode.name + ".png");
			if (img.exists()) {
				img.delete();
			}

			final OutputStream outStream = new FileOutputStream(img);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
			outStream.flush();
			outStream.close();

			super.getActivity().sendBroadcast(
					new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"
							+ dir.toString())));
			Toast.makeText(getActivity(),
					"Wallpaper Saved To, " + img.toString() + "!",
					Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Toast.makeText(getActivity(),
					"Something Went Wrong, Please Try Again!",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void applyImage() {
		if (!this.mImageDrawableSet) {
			this.mApplyImageOnDisplay = true;
			return;
		}

		try {
			final Bitmap bitmap = this.mImageView.getBitmap();
			if (bitmap == null) {
				throw new Exception("Bitmap returned null");
			}

			final WallpaperManager wpManager = WallpaperManager
					.getInstance(super.getActivity());
			wpManager.setBitmap(bitmap);
			Toast.makeText(getActivity(), "Wallpaper Set!", Toast.LENGTH_SHORT)
					.show();
		} catch (Exception e) {
			Toast.makeText(getActivity(),
					"Something Went Wrong, Please Try Again!",
					Toast.LENGTH_SHORT).show();
		}
	}

}
