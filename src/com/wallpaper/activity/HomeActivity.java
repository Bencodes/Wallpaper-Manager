package com.wallpaper.activity;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.wallpaper.activity.R;
import com.wallpaper.core.Adapter;
import com.wallpaper.core.Adapter.OnGetViewListener;
import com.wallpaper.core.NodeCategory;
import com.wallpaper.core.NodeWallpaper;
import com.wallpaper.core.OnFragmentClickListener;
import com.wallpaper.core.RestClient;
import com.wallpaper.core.RestClientHandler;
import com.wallpaper.core.RestClientHandler.OnRestResponseHandler;

public class HomeActivity extends SherlockFragmentActivity implements
		OnGetViewListener, OnRestResponseHandler, OnNavigationListener,
		OnFragmentClickListener, OnBackStackChangedListener {

	private final String TAG = "HomeActivity";

	private final String HIDE = "Hide Launcher Icon";
	private final String SHOW = "Show Launcher Icon";
	private final int HIDE_ID = 0;
	private final int SHOW_ID = 1;

	private final int FLAG_SHOW = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
	private final int FLAG_HIDE = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
	private final int DONT_KILL_APP = PackageManager.DONT_KILL_APP;

	private Adapter mAdapter;
	private ArrayList<NodeCategory> mData;
	private int mCurrentSelection = -1;
	private boolean ignoreSelection = false;
	private boolean ignoreTriggered = false;
	private final String KEY_DATA = "data_cache";
	private final String KEY_POSITION = "current_position";

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.getSupportFragmentManager().addOnBackStackChangedListener(this);
		setContentView(R.layout.activity_home);

		if (savedInstanceState == null
				|| (savedInstanceState.get(KEY_DATA) == null)) {
			final String url = super.getResources().getString(
					R.string.config_wallpaper_manifest_url);
			RestClient.get(super.getApplicationContext(), url,
					new RestClientHandler(this));
		}

		else {
			if (savedInstanceState.containsKey(KEY_DATA)) {
				this.mCurrentSelection = savedInstanceState
						.getInt(KEY_POSITION);
				this.onSuccess((ArrayList<NodeCategory>) savedInstanceState
						.getSerializable(KEY_DATA));
			}
		}
	}

	@Override
	public void onSuccess(ArrayList<NodeCategory> response) {
		Log.i(TAG, "");
		this.mData = response;
		this.mAdapter = new Adapter(this, this, this.mData);

		super.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		super.getSupportActionBar().setDisplayShowHomeEnabled(true);

		if (!this.mData.isEmpty() && mData.size() == 1) {
			final NodeCategory node = this.mData.get(0);
			super.getSupportActionBar().setDisplayShowTitleEnabled(true);
			super.getSupportActionBar().setTitle(node.name);
			this.onCategorySelected(node);
			return;
		}

		super.getSupportActionBar().setDisplayShowTitleEnabled(false);
		super.getSupportActionBar().setNavigationMode(
				ActionBar.NAVIGATION_MODE_LIST);
		super.getSupportActionBar().setListNavigationCallbacks(mAdapter, this);
		if (this.mCurrentSelection != -1) {
			this.ignoreSelection = true;
			super.getSupportActionBar().setSelectedNavigationItem(
					mCurrentSelection);
		}
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		this.mCurrentSelection = itemPosition;
		if (ignoreSelection == true && this.ignoreTriggered == false) {
			this.ignoreTriggered = true;
			return false;
		}

		this.onCategorySelected(this.mData.get(mCurrentSelection));
		return true;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent,
			LayoutInflater mInflater) {
		final TextView t = new TextView(this);
		t.setText(this.mData.get(position).name);
		t.setTextSize(20);
		t.setPadding(5, 6, 2, 6);
		t.setSingleLine(true);
		return t;
	}

	public void startFragment(Fragment fragment, String tag, boolean isMain) {
		final FragmentManager fm = super.getSupportFragmentManager();
		final FragmentTransaction transaction = fm.beginTransaction();

		if (isMain) {
			for (int x = 0; x < fm.getBackStackEntryCount(); x++)
				fm.popBackStack();

			transaction.replace(R.id.container, fragment, tag);
		} else {
			transaction.replace(R.id.container, fragment, tag);
			transaction.addToBackStack(null);
		}

		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		transaction.commit();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (this.mData != null) {
			outState.putSerializable(KEY_DATA, this.mData);
			outState.putInt(KEY_POSITION, this.mCurrentSelection);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onCategorySelected(NodeCategory node) {
		if (super.getSupportFragmentManager().findFragmentByTag(WallpaperFragment.FRAGMENT_TAG) != null) {
			return;
		}

		final SherlockFragment frag = new CategoryFragment();
		final Bundle args = new Bundle();
		args.putSerializable(CategoryFragment.BUNDLE_TAG, node.wallpaperList);
		frag.setArguments(args);
		this.startFragment(frag, CategoryFragment.FRAGMENT_TAG, true);
	}

	@Override
	public void onWallpaperSelected(NodeWallpaper node) {
		final SherlockFragment frag = new WallpaperFragment();
		final Bundle args = new Bundle();
		args.putSerializable(WallpaperFragment.BUNDLE_TAG, node);
		frag.setArguments(args);
		this.startFragment(frag, WallpaperFragment.FRAGMENT_TAG, false);
	}

	@Override
	public void onBackStackChanged() {
		final FragmentManager fm = super.getSupportFragmentManager();
		if (fm.getBackStackEntryCount() == 0) {
			this.ignoreSelection = false;
			this.ignoreTriggered = false;
			this.onSuccess(this.mData);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(3, 3, 3, ((isShowing()) ? HIDE : SHOW)).setShowAsAction(
				MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == HIDE_ID) {
			return (new AlertDialog.Builder(this))
					.setCancelable(true)
					.setTitle("Hide App Icon")
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setMessage(
							"This option will remove the icon from your app drawer.")
					.setPositiveButton("Remove",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									hideIcon();
									dialog.dismiss();
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							}).create();
		}

		else if (id == SHOW_ID) {
			return (new AlertDialog.Builder(this))
					.setCancelable(true)
					.setTitle("Show App Icon")
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setMessage(
							"This option will restore the icon into your app drawer.")
					.setPositiveButton("Show",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {

									showIcon();
									dialog.dismiss();
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							}).create();
		}

		return null;
	}

	public boolean isShowing() {
		PackageManager p = super.getPackageManager();
		return (p.getComponentEnabledSetting(getComponenetName()) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || p
				.getComponentEnabledSetting(getComponenetName()) == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
	}

	private void hideIcon() {
		getPackageManager().setComponentEnabledSetting(getComponenetName(),
				FLAG_HIDE, DONT_KILL_APP);
	}

	private void showIcon() {
		getPackageManager().setComponentEnabledSetting(getComponenetName(),
				FLAG_SHOW, DONT_KILL_APP);
	}

	private ComponentName getComponenetName() {
		ComponentName c = new ComponentName(this, LauncherActivity.class);
		return c;
	}

}
