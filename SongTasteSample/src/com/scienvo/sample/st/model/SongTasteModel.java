package com.scienvo.sample.st.model;

import org.json.JSONObject;

import android.content.Context;
import android.os.Message;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.scienvo.sample.st.data.SongTasteItem;
import com.scienvo.sample.st.data.SongTasteReply;

public class SongTasteModel extends BaseModel{
	String url = "http://songtaste.com/api/android/rec_list.php?p=%d&n=20&tmp=0.7421970851719379&qq-pf-to=pcqq.c2c";
	private SongTasteItem[] data;
	private int pageToten = 1;
	private RequestHandler requestHandler;
	private String currentMp3File;
	
	private final String key = "st";

	public SongTasteModel(Context context,RequestHandler reqHandler) {
		this.requestHandler = reqHandler;
	}
	
	public SongTasteItem[] getData(){
		return data;
	}
	
	public void refresh(final int id) {
		String json = getRequest(key);
		if(json == null || json.length() == 0){
			update(id);
		}
		else{
			SongTasteReply reply = fromGson(json,
					SongTasteReply.class);
			if (reply.code == 1) {
				data = reply.data;
				pageToten++;
				Message msg = requestHandler.obtainMessage();
				msg.arg1 = 0;
				msg.arg2 = id;
				requestHandler.sendMessage(msg);
			}
		}
	}
	public void update(final int id){
		JsonObjectRequest getRequest = new JsonObjectRequest(
				Request.Method.GET, String.format(url, pageToten), null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						saveRequest(key,response.toString());
						SongTasteReply reply = fromGson(response.toString(),
								SongTasteReply.class);
						if (reply.code == 1) {
							data = reply.data;
							pageToten++;
							Message msg = requestHandler.obtainMessage();
							msg.arg1 = 0;
							msg.arg2 = id;
							requestHandler.sendMessage(msg);
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						requestHandler.sendEmptyMessage(1);
					}
				});
		queue.add(getRequest);
	}

	public void getMore(final int id) {
		JsonObjectRequest getRequest = new JsonObjectRequest(
				Request.Method.GET, String.format(url, pageToten), null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						SongTasteReply reply = fromGson(response.toString(),
								SongTasteReply.class);
						if (reply.code == 1) {
							data = reply.data;
							pageToten++;
							Message msg = requestHandler.obtainMessage();
							msg.arg1 = 0;
							msg.arg2 = id;
							requestHandler.sendMessage(msg);
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						requestHandler.sendEmptyMessage(1);
					}
				});
		queue.add(getRequest);
	}

	public void getMp3File(final int id,String url) {
		StringRequest mp3Request = new StringRequest(url,
				new Listener<String>() {

					@Override
					public void onResponse(String response) {
						currentMp3File = getMp3FromXml(response);
						Message msg = requestHandler.obtainMessage();
						msg.arg1 = 0;
						msg.arg2 = id;
						requestHandler.sendMessage(msg);
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						currentMp3File = null;
					}
				});
		queue.add(mp3Request);
	}
	
	public String getCurrentMp3File(){
		return currentMp3File;
	}

	private String getMp3FromXml(String res) {
		int start = res.indexOf("<url>") + "<url>".length();
		int end = res.indexOf("</url>");
		String mp3String = null;
		if (end > start && start > 0) {
			mp3String = res.substring(start, end);
		}
		return mp3String;
	}
}
