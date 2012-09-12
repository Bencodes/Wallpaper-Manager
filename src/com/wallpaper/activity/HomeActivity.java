package com.wallpaper.activity;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
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
			RestClient.get(url, new RestClientHandler(this));
		} else {
			if (savedInstanceState.containsKey(KEY_DATA)) {
				this.mCurrentSelection = savedInstanceState
						.getInt(KEY_POSITION);
				this.onSuccess((ArrayList<NodeCategory>) savedInstanceState
						.getSerializable(KEY_DATA));
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			final FragmentManager fm = super.getSupportFragmentManager();
			fm.popBackStack();
			return true;
		}

		default: {
			return super.onOptionsItemSelected(item);
		}
		}
	}

	@Override
	public void onSuccess(ArrayList<NodeCategory> response) {
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

	public void startFragment(Fragment fragment, boolean isMain) {
		final FragmentManager fm = super.getSupportFragmentManager();
		final FragmentTransaction transaction = fm.beginTransaction();

		if (isMain) {
			for (int x = 0; x < fm.getBackStackEntryCount(); x++)
				fm.popBackStack();
			transaction.replace(R.id.container, fragment);
		} else {
			transaction.replace(R.id.container, fragment);
			transaction.addToBackStack(null);
		}

		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		transaction.commit();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (this.mData != null) {
			outState.putSerializable(KEY_DATA, this.mData);
			outState.putInt(KEY_POSITION, this.mCurrentSelection);
		}
	}

	@Override
	public void onCategorySelected(NodeCategory node) {
		final SherlockFragment frag = new CategoryFragment();
		final Bundle args = new Bundle();
		args.putSerializable(CategoryFragment.BUNDLE_TAG, node.wallpaperList);
		frag.setArguments(args);
		this.startFragment(frag, true);
	}

	@Override
	public void onWallpaperSelected(NodeWallpaper node) {
		final SherlockFragment frag = new WallpaperFragment();
		final Bundle args = new Bundle();
		args.putSerializable(WallpaperFragment.BUNDLE_TAG, node);
		frag.setArguments(args);
		this.startFragment(frag, false);
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

}
