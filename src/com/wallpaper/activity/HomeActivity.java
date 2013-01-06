package com.wallpaper.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.wallpaper.core.*;
import com.wallpaper.core.Adapter.OnGetViewListener;
import com.wallpaper.core.RestClientHandler.OnRestResponseHandler;

import java.util.ArrayList;

public class HomeActivity extends SherlockFragmentActivity implements OnRestResponseHandler, OnBackStackChangedListener, OnGetViewListener, OnFragmentClickListener {

	private final String TAG = "HomeActivity";
	private final String KEY_LIST_DATA = "list_cache";
	private final String KEY_LIST_POSITION = "list_position";


	private static ImageLoader mImageLoader;
	private ArrayList<NodeCategory> mData;
	private int mPosition = -1;
	private boolean mIgnoreSelection = false;

	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.getSupportFragmentManager().addOnBackStackChangedListener(this);
		super.setContentView(R.layout.activity_home);
		this.loadData(savedInstanceState);

		if (savedInstanceState == null) {
			BitmapDisplayer displayer = getResources().getBoolean(R.bool.config_enable_image_fade_in)
					? new FadeInBitmapDisplayer(getResources().getInteger(R.integer.config_fade_in_time))
					: new SimpleBitmapDisplayer();

			final DisplayImageOptions options = new DisplayImageOptions.Builder()
					.displayer(displayer)
					.cacheInMemory()
					.cacheOnDisc()
					.build();

			final ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
					.threadPriority(Thread.NORM_PRIORITY - 1)
					.offOutOfMemoryHandling()
					.tasksProcessingOrder(QueueProcessingType.FIFO)
					.defaultDisplayImageOptions(options)
					.build();

			mImageLoader = ImageLoader.getInstance();
			mImageLoader.init(config);
		}
	}

	@Override
	public void onSaveInstanceState (Bundle outState) {
		if (this.mData != null) {
			outState.putSerializable(KEY_LIST_DATA, this.mData);
			outState.putInt(KEY_LIST_POSITION, this.mPosition);
		}
		super.onSaveInstanceState(outState);
	}

	public void loadData (Bundle savedInstanceState) {

		// Check Network State
		if (!NetworkUtil.getNetworkState(this)) {
			final RetryFragment fragment = RetryFragment.getFragmentWithMessage("No connection");
			this.addFragment(fragment, RetryFragment.TAG, true);
			return;
		}

		if (savedInstanceState == null || savedInstanceState.get(KEY_LIST_DATA) == null) {
			final String url = super.getResources().getString(R.string.config_wallpaper_manifest_url);
			if (url != null && URLUtil.isValidUrl(url)) {
				// Add Loading Fragment
				final LoadingFragment fragment = new LoadingFragment();
				this.addFragment(fragment, LoadingFragment.TAG, true);

				// Load Data
				final RestClientHandler handler = new RestClientHandler(this);
				RestClient.get(this, url, handler);
			}
		} else {
			Log.i(TAG, "Restored Instance");
			this.mData = (ArrayList<NodeCategory>) savedInstanceState.get(KEY_LIST_DATA);
			this.mPosition = savedInstanceState.getInt(KEY_LIST_POSITION);
			if (this.mPosition != -1) {
				mIgnoreSelection = true;
			}

			this.configureActionBar();
		}
	}

	@Override
	public void onResponse (ArrayList<NodeCategory> response) {
		this.mData = response;

		// Add ERROR Fragment
		if (this.mData == null) {
			final RetryFragment fragment = RetryFragment.getFragmentWithMessage();
			this.addFragment(fragment, RetryFragment.TAG, true);
			return;
		}

		if (this.mData.isEmpty()) {
			this.addFragment(null, null, true);
			Toast.makeText(getApplicationContext(), "Empty Manifest!", Toast.LENGTH_SHORT).show();
			return;
		}

		this.configureActionBar();
	}


	public void configureActionBar () {
		super.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		super.getSupportActionBar().setDisplayShowHomeEnabled(true);

		if (this.mData.size() == 1) {
			final NodeCategory node = this.mData.get(0);
			super.getSupportActionBar().setDisplayShowTitleEnabled(true);
			super.getSupportActionBar().setTitle(node.name);
			this.onCategorySelected(node);
		} else {
			super.getSupportActionBar().setDisplayShowTitleEnabled(false);
			super.getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			super.getSupportActionBar().setListNavigationCallbacks(new Adapter(this, this, this.mData), this);
			if (this.mPosition != -1) {
				this.mIgnoreSelection = true;
				super.getSupportActionBar().setSelectedNavigationItem(this.mPosition);
			}
		}
	}


	public void addFragment (SherlockFragment fragment, String tag, boolean clearStack) {
		final FragmentManager fm = super.getSupportFragmentManager();
		final FragmentTransaction transaction = fm.beginTransaction();

		// Clear Back Stack
		if (clearStack) {
			for (int x = 0; x < fm.getBackStackEntryCount(); x++) {
				fm.popBackStack();
			}
		}

		if (fragment != null) {
			if (!clearStack) {
				transaction.replace(R.id.container, fragment, tag);
				transaction.addToBackStack(null);
				transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			} else {
				transaction.replace(R.id.container, fragment, tag);
			}

			try {
				transaction.commit();
			} catch (Exception e) {
			}
		}
	}


	@Override
	public boolean onNavigationItemSelected (int itemPosition, long itemId) {
		Log.i(TAG, "Item Selection: " + itemPosition);
		this.mPosition = itemPosition;
		if (this.mIgnoreSelection == true) {
			this.mIgnoreSelection = false;
			return false;
		}

		this.onCategorySelected(this.mData.get(itemPosition));
		return false;
	}

	@Override
	public void onCategorySelected (NodeCategory node) {
		final SherlockFragment frag = new CategoryFragment();
		final Bundle args = new Bundle();
		args.putSerializable(CategoryFragment.BUNDLE_TAG, node.wallpaperList);
		frag.setArguments(args);
		this.addFragment(frag, CategoryFragment.FRAGMENT_TAG, true);
	}

	@Override
	public void onWallpaperSelected (NodeWallpaper node) {
		final SherlockFragment frag = new WallpaperFragment();
		final Bundle args = new Bundle();
		args.putSerializable(WallpaperFragment.BUNDLE_TAG, node);
		frag.setArguments(args);
		this.addFragment(frag, WallpaperFragment.FRAGMENT_TAG, false);
	}

	@Override
	public View getView (int position, View convertView, ViewGroup parent, LayoutInflater mInflater) {
		final TextView t = new TextView(this);
		t.setText(this.mData.get(position).name);
		t.setTextSize(20);
		t.setPadding(5, 6, 2, 6);
		t.setSingleLine(true);
		return t;
	}

	@Override
	public void onBackStackChanged () {
		final FragmentManager fm = super.getSupportFragmentManager();
		if (fm.getBackStackEntryCount() == 0) {
			this.configureActionBar();
		}
	}


	private final String HIDE = "Hide Launcher Icon";
	private final String SHOW = "Show Launcher Icon";
	private final int HIDE_ID = 0;
	private final int SHOW_ID = 1;

	private final int FLAG_SHOW = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
	private final int FLAG_HIDE = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
	private final int DONT_KILL_APP = PackageManager.DONT_KILL_APP;

	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		menu.clear();
		menu.add(3, 3, 3, ((isShowing()) ? HIDE : SHOW)).setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home: {
				final FragmentManager fm = super.getSupportFragmentManager();
				fm.popBackStack();
				return true;
			}

			default: {
				if (item.getTitle().equals(SHOW)) {
					Log.i(TAG, "Show");
					super.showDialog(SHOW_ID);
					return true;
				} else if (item.getTitle().equals(HIDE)) {
					Log.i(TAG, "Hide");
					super.showDialog(HIDE_ID);
					return true;
				} else {
					return super.onOptionsItemSelected(item);
				}
			}
		}
	}

	@Override
	protected Dialog onCreateDialog (int id) {
		if (id == HIDE_ID) {
			return (new AlertDialog.Builder(this)).setCancelable(true).setTitle("Hide App Icon").setIcon(android.R.drawable.ic_dialog_alert).setMessage("This option will remove the icon from your app drawer.").setPositiveButton("Remove", new DialogInterface.OnClickListener() {
				public void onClick (DialogInterface dialog, int which) {
					hideIcon();
					dialog.dismiss();
				}
			}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick (DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			}).create();
		} else if (id == SHOW_ID) {
			return (new AlertDialog.Builder(this)).setCancelable(true).setTitle("Show App Icon").setIcon(android.R.drawable.ic_dialog_alert).setMessage("This option will restore the icon into your app drawer.").setPositiveButton("Show", new DialogInterface.OnClickListener() {
				public void onClick (DialogInterface dialog, int which) {

					showIcon();
					dialog.dismiss();
				}
			}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick (DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			}).create();
		}

		return null;
	}

	public boolean isShowing () {
		PackageManager p = super.getPackageManager();
		return (p.getComponentEnabledSetting(getComponenetName()) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || p.getComponentEnabledSetting(getComponenetName()) == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
	}

	private void hideIcon () {
		getPackageManager().setComponentEnabledSetting(getComponenetName(), FLAG_HIDE, DONT_KILL_APP);
	}

	private void showIcon () {
		getPackageManager().setComponentEnabledSetting(getComponenetName(), FLAG_SHOW, DONT_KILL_APP);
	}

	private ComponentName getComponenetName () {
		ComponentName c = new ComponentName(this, LauncherActivity.class);
		return c;
	}


}
