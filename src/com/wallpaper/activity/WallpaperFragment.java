package com.wallpaper.activity;

import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.wallpaper.core.NodeWallpaper;
import com.wallpaper.core.uk.co.senab.photoview.PhotoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class WallpaperFragment extends SherlockFragment {

	private final String TAG = "WallpaperFragment";
	public static final String FRAGMENT_TAG = "WallpaperFragment";

	public static final String BUNDLE_TAG = "wallpaper_fragment_data";
	private NodeWallpaper mNode;
	private ProgressBar mPending;
	private PhotoView mImageView;
	private View mView;

	private boolean mImageDrawableSet = false;
	private boolean mApplyImageOnDisplay = false;
	private boolean mSaveImageOnDisplay = false;

	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		super.setHasOptionsMenu(true);
		super.setRetainInstance(false);

		this.mView = inflater.inflate(R.layout.fragment_full_wallpaper, container, false);
		return this.mView;
	}

	@Override
	public void onActivityCreated (Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.mNode = (NodeWallpaper) super.getArguments().getSerializable(BUNDLE_TAG);

		final ActionBar ab = ((SherlockFragmentActivity) super.getActivity()).getSupportActionBar();
		if (ab != null) {
			ab.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			ab.setDisplayHomeAsUpEnabled(true);
			ab.setDisplayShowHomeEnabled(false);
			ab.setDisplayShowTitleEnabled(true);

			final String title = super.getResources().getString(R.string.config_full_screen_wallpaper_title);
			if (title == null || title.length() <= 0) {
				ab.setTitle(mNode.name);
			} else {
				ab.setTitle(title);
			}
		}

		this.mPending = (ProgressBar) super.getView().findViewById(R.id.pending);
		this.mImageView = (PhotoView) mView.findViewById(R.id.wp_image);

		ImageLoader.getInstance().displayImage(mNode.url, mImageView, new ImageLoadingListener() {
			@Override
			public void onLoadingStarted () {
				mImageDrawableSet = false;
				mImageView.setVisibility(View.GONE);
				mPending.setVisibility(View.VISIBLE);
			}

			@Override
			public void onLoadingFailed (FailReason failReason) {
				mImageDrawableSet = false;
				Toast.makeText(getActivity(), "Image Failed To Load!", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onLoadingComplete (Bitmap bitmap) {
				mImageDrawableSet = true;
				mImageView.setVisibility(View.VISIBLE);
				mImageView.setImageBitmap(bitmap);
				mImageView.setZoomable(true);
				mPending.setVisibility(View.GONE);

				if (mApplyImageOnDisplay)
					applyImage();

				if (mSaveImageOnDisplay)
					exportImage();
			}

			@Override
			public void onLoadingCancelled () {
			}
		});
	}

	@Override
	public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu (Menu menu) {
		menu.add(1, 1, 1, "Save").setIcon(android.R.drawable.ic_menu_save).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.add(2, 2, 2, "Apply").setIcon(android.R.drawable.ic_menu_set_as).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
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

	public void exportImage () {
		if (this.mImageDrawableSet == false) {
			this.mSaveImageOnDisplay = true;
			return;
		}

		try {
			final Bitmap bitmap = getImageBitmap();
			if (bitmap == null) {
				Toast.makeText(getActivity(), "Something Went Wrong, Please Try Again!", Toast.LENGTH_SHORT).show();
				return;
			}

			final File dir = new File(Environment.getExternalStorageDirectory(), super.getResources().getString(R.string.config_external_storage_folder));

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

			super.getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + dir.toString())));
			Toast.makeText(getActivity(), "Wallpaper Saved To, " + img.toString() + "!", Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Log.e(TAG, "", e);
			Toast.makeText(getActivity(), "Something Went Wrong, Please Try Again!", Toast.LENGTH_SHORT).show();
		}
	}

	public void applyImage () {
		if (this.mImageDrawableSet == false) {
			this.mApplyImageOnDisplay = true;
			return;
		}

		try {
			final Bitmap bitmap = getImageBitmap();
			if (bitmap == null) {
				Toast.makeText(getActivity(), "Something Went Wrong, Please Try Again!", Toast.LENGTH_SHORT).show();
				return;
			}

			final WallpaperManager wpManager = WallpaperManager.getInstance(getActivity());
			if (wpManager == null) {
				Toast.makeText(getActivity(), "Something Went Wrong, Please Try Again!", Toast.LENGTH_SHORT).show();
				return;
			}

			wpManager.setBitmap(bitmap);
			Toast.makeText(getActivity(), "Wallpaper Set!", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Log.e(TAG, "", e);
			Toast.makeText(getActivity(), "Something Went Wrong, Please Try Again!", Toast.LENGTH_SHORT).show();
		}
	}

	public Bitmap getImageBitmap () {
		try {
			final Drawable drawable = this.mImageView.getDrawable();
			if (drawable instanceof BitmapDrawable) {
				return ((BitmapDrawable) drawable).getBitmap();
			}

			Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
			drawable.draw(canvas);
			return bitmap;
		} catch (Exception e) {
			Log.e(TAG, "", e);
			return null;
		}
	}

}
