package com.wallpaper.core;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class ManifestParser {

	private static final String TAG = "ManifestParser";

	public ArrayList<NodeCategory> getResults(JSONObject object) {
		try {
			ArrayList<NodeCategory> nodeList = new ArrayList<NodeCategory>();
			JSONArray catList = object.getJSONObject("wallpapers")
					.getJSONArray("category");
			for (int x = 0; x < catList.length(); x++) {
				JSONObject catObject = catList.getJSONObject(x);
				NodeCategory node = parseCategory(catObject);
				nodeList.add(node);
			}

			if (nodeList.size() > 1) {
				NodeCategory allNode = new NodeCategory();
				allNode.name = "All";
				allNode.wallpaperList = new ArrayList<NodeWallpaper>();

				for (int x = 0; x < nodeList.size(); x++) {
					allNode.wallpaperList.addAll(nodeList.get(x).wallpaperList);
				}

				nodeList.add(0, allNode);
			}

			return nodeList;
		} catch (Exception e) {
			Log.e(TAG, "", e);
			return null;
		}
	}

	private NodeCategory parseCategory(JSONObject category) {
		try {
			NodeCategory catNode = new NodeCategory();
			catNode.wallpaperList = new ArrayList<NodeWallpaper>();
			catNode.name = getString(category, "name");

			JSONArray array = category.getJSONArray("wallpaper");
			for (int x = 0; x < array.length(); x++) {
				JSONObject obj = array.getJSONObject(x);

				NodeWallpaper node = new NodeWallpaper();
				node.name = getString(obj, "name");
				node.author = getString(obj, "author");
				node.thumbUrl = getString(obj, "thumbUrl");
				node.url = getString(obj, "url");
				catNode.wallpaperList.add(node);
			}

			return catNode;
		} catch (Exception e) {
			Log.e(TAG, "", e);
			return null;
		}
	}

	private String getString(JSONObject obj, String tag) throws JSONException {
		if (obj != null && tag != null) {
			if (obj.has(tag) && !obj.isNull(tag)) {
				return obj.getString(tag);
			}
		}

		return null;
	}
}
