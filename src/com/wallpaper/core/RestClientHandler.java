package com.wallpaper.core;

import java.util.ArrayList;

import org.json.JSONObject;

import com.wallpaper.core.loopj.android.http.JsonHttpResponseHandler;

public class RestClientHandler extends JsonHttpResponseHandler {

	private final OnRestResponseHandler mOnRestResponseHandler;

	public interface OnRestResponseHandler {
		public void onSuccess(ArrayList<NodeCategory> response);
	}

	public RestClientHandler(OnRestResponseHandler listener) {
		mOnRestResponseHandler = listener;
	}

	@Override
	public void onSuccess(JSONObject array) {
		mOnRestResponseHandler
				.onSuccess(new ManifestParser().getResults(array));
	}

}
