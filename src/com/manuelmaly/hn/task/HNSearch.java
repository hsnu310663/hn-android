package com.manuelmaly.hn.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.manuelmaly.hn.model.HNFeed;
import com.manuelmaly.hn.model.HNPost;
import com.manuelmaly.hn.parser.BaseHTMLParser;

public class HNSearch {

	HNFeed Feed;
	String REQUEST_URL = "http://api.thriftdb.com/api.hnsearch.com/items/_search?q=";
	int limit = 60;
	int sort_mode = 0;
	String SearchWord = null;

	public HNSearch(String keyword) {
		SearchWord = keyword;
		Feed = new HNFeed(new ArrayList<HNPost>(), null, "");
		JSONObject json = null;
		Search(json);
		json = null;
	}
	

	public void Search(JSONObject json) {
		String jsonText = null;
		try {
			InputStream is = new URL(get_URL()).openStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is,
					Charset.forName("UTF-8")));
			jsonText = readAll(rd);
			json = new JSONObject(jsonText);
			is.close();
			JSONArray result = json.getJSONArray("results");
			for (int i = 0; i < limit; i++) {
				JSONObject temp = result.getJSONObject(i).getJSONObject("item");
				Feed.addPost(parser(temp));
			}
			result = null;
		} catch (Exception e) {
		}
	}

	public String get_URL() {
		String URL = REQUEST_URL + SearchWord + "&limit=" + limit;
	/*	switch (sort_mode) {
		case 0:
			URL = URL + "&sortby=create_ts%20desc";
			break;
		}*/
		return URL;
	}

	public void set_mode(int mode) {
		sort_mode = mode;
	}

	public static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public static JSONObject readJsonFromUrl(String url) throws IOException,
			JSONException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is,
					Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONObject json = new JSONObject(jsonText);
			return json;
		} finally {
			is.close();
		}
	}

	public static HNPost parser(JSONObject item) {
		HNPost result = null;
		String url, title, mURLDomain, username, id, num_comments, points, mUpvoteURL;
		try {
			url = item.getString("url");
			mURLDomain = BaseHTMLParser.getDomainName(url);
			title = item.getString("title");
			username = item.getString("username");
			id = item.getString("id");
			num_comments = item.getString("num_comments");
			points = item.getString("points");
			mUpvoteURL = null;
			result = new HNPost(url, title, mURLDomain, username, id,
					Integer.parseInt(num_comments), Integer.parseInt(points),
					mUpvoteURL);
		} catch (Exception e) {
		}
		return result;
	}

	public HNFeed get_Feed() {
		return Feed;
	}

}
