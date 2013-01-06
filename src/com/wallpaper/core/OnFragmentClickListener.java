package com.wallpaper.core;

import com.actionbarsherlock.app.ActionBar.OnNavigationListener;

public interface OnFragmentClickListener extends OnNavigationListener {
	public void onCategorySelected (NodeCategory node);

	public void onWallpaperSelected (NodeWallpaper node);
}
