// <!--all by kate -->
package com.manuelmaly.hn.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.manuelmaly.hn.model.HNFeed;
import com.manuelmaly.hn.model.HNPost;

public class HNSearch {

	HNFeed Feed;
	String REQUEST_URL = "http://api.thriftdb.com/api.hnsearch.com/items/_search?";
	int limit = 60;
	mode sort_mode = mode.Time;
	String Rank[][];
	int rank_number = 12;

	public enum mode {
        Time, Reader, Comment
    }
	
	public HNSearch() {
		init();
		Feed = new HNFeed(new ArrayList<HNPost>(), null, "");
	}

	public String get_keyword(){
	  return Rank[0][1];
	}
	public void set_keyword(String keyword) {
		Rank[0][1] = keyword;
	}

	public void Search() {
		Feed = new HNFeed(new ArrayList<HNPost>(), null, "");
		String jsonText = null;
		JSONObject json = null;
		try {
			InputStream is = new URL(get_URL()).openStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is,Charset.forName("UTF-8")));
			jsonText = readAll(rd);
			json = new JSONObject(jsonText);
			is.close();
			JSONArray result = json.getJSONArray("results");
			for (int i = 0; i < limit; i++) {
				JSONObject temp = result.getJSONObject(i).getJSONObject("item");
				Feed.addPost(parser(temp));
			}
			result = null;
		} 
		catch (Exception e) {
		}
	}

	public String get_URL() {
		switch (sort_mode) {
		case Time:
			Rank[10][1] =  "create_ts%20desc";
			break;
		case Reader:
			Rank[10][1] =  "points%20desc";
			break;
		case Comment:
			Rank[10][1] =  "num_comments%20desc";
			break;
		}
		String URL = REQUEST_URL;
		for(int i = 0;i<rank_number;i++)
				URL = URL + "&" + Rank[i][0] + "=" + Rank[i][1];
		return URL;
	}

	public void set_mode(mode the_mode) {
		sort_mode = the_mode;
	}

	public static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public static JSONObject readJsonFromUrl(String url) throws IOException,JSONException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is,Charset.forName("UTF-8")));
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
			mURLDomain = item.getString("domain");
			title = item.getString("title");
			username = item.getString("username");
			id = item.getString("id");
			num_comments = item.getString("num_comments");
			points = item.getString("points");
			mUpvoteURL = null;
			result = new HNPost(url, title, mURLDomain, username, id,Integer.parseInt(num_comments), Integer.parseInt(points),mUpvoteURL);
		} catch (Exception e) {
		}
		return result;
	}

	public HNFeed get_Feed() {
		return Feed;
	}
	
	void init(){
		Rank = new String[rank_number][2];
		Rank[0][0] = "q";																Rank[0][1]= "";
		Rank[1][0] = "weights[title]";													Rank[1][1]="2.0";
		Rank[2][0] = "weights[text]";													Rank[2][1]="0";
		Rank[3][0] = "weights[url]";													Rank[3][1]="0.7";
		Rank[4][0] = "weights[type]";													Rank[4][1]="1.0";
		Rank[5][0] = "weights[domain]";													Rank[5][1]="2.0";
		Rank[6][0] = "boosts[fields][points]";											Rank[6][1]="0.15";
		Rank[7][0] = "boosts[fields][num_comments]";									Rank[7][1]="0.15";
		Rank[8][0] = "boosts[functions][pow(2,div(div(ms(create_ts,NOW),3600000),72))]";Rank[8][1]="200.0";
		Rank[9][0] = "pretty_print";													Rank[9][1]="true";
		Rank[10][0] = "sortby";                 										Rank[10][1]="";
		Rank[11][0] = "limit";                 										    Rank[11][1]=""+limit;
	}

}
