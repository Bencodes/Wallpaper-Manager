package com.wallpaper.core;

import com.wallpaper.core.loopj.android.http.AsyncHttpClient;
import com.wallpaper.core.loopj.android.http.AsyncHttpResponseHandler;

public class RestClient {
	public static AsyncHttpClient client = new AsyncHttpClient();

	public static void get(String url, AsyncHttpResponseHandler responseHandler) {
		client.get(url, responseHandler);
	}
}
