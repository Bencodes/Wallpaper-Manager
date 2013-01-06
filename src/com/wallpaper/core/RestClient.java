package com.wallpaper.core;

import android.content.Context;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class RestClient {
	public static AsyncHttpClient client = new AsyncHttpClient();

	public static void get (Context c, String url, AsyncHttpResponseHandler responseHandler) {
		client.get(c, url, responseHandler);
	}
}
