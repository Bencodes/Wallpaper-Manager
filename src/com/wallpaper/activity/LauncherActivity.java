package com.wallpaper.activity;

import android.content.Intent;
import android.os.Bundle;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class LauncherActivity extends SherlockFragmentActivity {

	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Intent intent = new Intent(this, HomeActivity.class);
		super.startActivity(intent);
	}

}
