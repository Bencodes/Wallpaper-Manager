package com.wallpaper.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragment;

public class RetryFragment extends SherlockFragment implements OnClickListener {

	public static final String TAG = "RetryFragment";
	private static final String KEY_MESSAGE = "";

	public static RetryFragment getFragmentWithMessage (String msg) {
		final RetryFragment fragment = new RetryFragment();
		if (msg != null) {
			final Bundle args = new Bundle();
			args.putString(KEY_MESSAGE, msg);
			fragment.setArguments(args);
		}

		return fragment;
	}

	public static RetryFragment getFragmentWithMessage () {
		return getFragmentWithMessage(null);
	}

	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.fragment_retry, null, false);
	}

	@Override
	public void onViewCreated (View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final Bundle args = super.getArguments();
		final TextView tv = (TextView) view.findViewById(R.id.text_retry);
		final Button bt = (Button) view.findViewById(R.id.button_retry);

		bt.setOnClickListener(this);
		if (args != null && args.get(KEY_MESSAGE) != null) {
			tv.setText(args.getString(KEY_MESSAGE));
		}
	}

	@Override
	public void onClick (View v) {
		if (super.getActivity() instanceof HomeActivity) {
			HomeActivity a = (HomeActivity) super.getActivity();
			a.loadData(null);
		}
	}
}
