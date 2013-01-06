package com.wallpaper.core;

import android.util.Log;
import com.loopj.android.http.JsonHttpResponseHandler;
import org.json.JSONObject;

import java.util.ArrayList;

public class RestClientHandler extends JsonHttpResponseHandler {

	private final OnRestResponseHandler mOnRestResponseHandler;

	public interface OnRestResponseHandler {
		public void onResponse (ArrayList<NodeCategory> response);
	}

	public RestClientHandler (OnRestResponseHandler listener) {
		mOnRestResponseHandler = listener;
	}

	@Override
	public void onSuccess (JSONObject array) {
		final ArrayList<NodeCategory> data = new ManifestParser().getResults(array);
		if (data == null) {
			this.onFailure(new Throwable("Manifest Could Not Be Parsed!"), array);
			return;
		}
		mOnRestResponseHandler.onResponse(data);
	}

	@Override
	public void onFailure (Throwable msg, JSONObject response) {
		this.mOnRestResponseHandler.onResponse(null);
		if (msg != null) {
			Log.e("RestClientHandler", "", msg);
		}
	}

}
