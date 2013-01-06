package com.wallpaper.core;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ManifestParser {

	private static final String TAG = "ManifestParser";

	public ArrayList<NodeCategory> getResults (JSONObject object) {
		try {
			ArrayList<NodeCategory> nodeList = new ArrayList<NodeCategory>();
			JSONArray catList = getJSONArray(getJSONObject(object, "wallpapers"), "category");
			for (int x = 0; x < catList.length(); x++) {
				JSONObject catObject = getJSONObject(catList, x);
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

	private NodeCategory parseCategory (JSONObject category) {
		try {
			NodeCategory catNode = new NodeCategory();
			catNode.wallpaperList = new ArrayList<NodeWallpaper>();
			catNode.name = getString(category, "name");

			JSONArray array = getJSONArray(category, "wallpaper");
			if (array == null) {
				return catNode;
			}

			for (int x = 0; x < array.length(); x++) {
				JSONObject obj = this.getJSONObject(array, x);

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

	public String getString (JSONObject obj, String tag) throws JSONException {
		return (obj != null && !obj.isNull(tag)) ? obj.getString(tag) : null;
	}

	public int getInt (JSONObject obj, String tag) throws JSONException {
		return (obj != null && !obj.isNull(tag)) ? obj.getInt(tag) : -1;
	}

	public boolean getBoolean (JSONObject obj, String tag) throws JSONException {
		return (obj != null && !obj.isNull(tag)) ? obj.getBoolean(tag) : false;
	}

	public JSONObject getJSONObject (JSONObject obj, String tag) throws JSONException {
		return (obj != null && !obj.isNull(tag)) ? obj.getJSONObject(tag) : null;
	}

	public JSONObject getJSONObject (JSONArray obj, int x) throws JSONException {
		return (obj != null && !obj.isNull(x)) ? obj.getJSONObject(x) : null;
	}

	public JSONArray getJSONArray (JSONObject obj, String tag) throws JSONException {
		return (obj != null && !obj.isNull(tag)) ? obj.getJSONArray(tag) : null;
	}
}
