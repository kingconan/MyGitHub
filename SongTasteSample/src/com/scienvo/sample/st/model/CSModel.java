package com.scienvo.sample.st.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.scienvo.sample.st.OfflineDownloadService.DBCSItem;
import com.scienvo.sample.st.activity.CSContentActivity.ContentItem;
import com.scienvo.sample.st.wrapper.CSViewWrapper.CSItem;

public class CSModel extends BaseModel{
	String url = "http://www.weiduwu.net/app/getContentList.json";
	String contentUrl = "http://www.weiduwu.net/app/getContentByid.json";
	RequestHandler requestHandler;
	int page;
	private final String key = "cs";
	private  CSItem[] data;
	public CSModel(Context context,RequestHandler reqHandler) {
		this.requestHandler = reqHandler;
		initCache();
	}
	
	public CSItem[] getData(){
		return data;
	}
	
	public void refresh(final int id) {
		String json = getRequest(key);
		if(json == null || json.length() == 0){
			update(id);
		}
		else{
			data = fromGson(json, CSItem[].class);
			for(CSItem item : data){
				DBCSItem res = findContentById(item.id);
				if(res != null){
					item.content = BaseModel.fromGson(res.contentsJson,ContentItem[].class);
				}
			}
			Message msg = requestHandler.obtainMessage();
			msg.arg1 = 0;
			msg.arg2 = id;
			requestHandler.sendMessage(msg);
		}
	}
	public void update(final int id){
		page = 1;
		StringRequest sr = new StringRequest(Request.Method.POST, url,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						saveRequest(key, response);
						data =  fromGson(response, CSItem[].class);
						for(CSItem item : data){
							DBCSItem res = findContentById(item.id);
							if(res != null){
								item.content = BaseModel.fromGson(res.contentsJson,ContentItem[].class);
							}
						}
						Message msg = requestHandler.obtainMessage();
						msg.arg1 = 0;
						msg.arg2 = id;
						requestHandler.sendMessage(msg);
						page++;
						
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Message msg = requestHandler.obtainMessage();
						msg.arg1 = 1;
						msg.arg2 = id;
						requestHandler.sendMessage(msg);
					}
				}) {
			@Override
			protected Map<String, String> getParams() {
				Map<String, String> params = new HashMap<String, String>();
				params.put("page", String.valueOf(page++));
				params.put("pageSize", String.valueOf(20));
				return params;
			}
		};
		queue.add(sr);
	}

	public void getMore(final int id) {
		StringRequest sr = new StringRequest(Request.Method.POST, url,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						data = fromGson(response, CSItem[].class);
						if(data == null){
							
						}
						else{
							for(CSItem item : data){
								DBCSItem res = findContentById(item.id);
								if(res != null){
									item.content = BaseModel.fromGson(res.contentsJson,ContentItem[].class);
								}
							}	
						}
						page++;
						
						Message msg = requestHandler.obtainMessage();
						msg.arg1 = 0;
						msg.arg2 = id;
						requestHandler.sendMessage(msg);
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Message msg = requestHandler.obtainMessage();
						msg.arg1 = 1;
						msg.arg2 = id;
						requestHandler.sendMessage(msg);
					}
				}) {
			@Override
			protected Map<String, String> getParams() {
				Map<String, String> params = new HashMap<String, String>();
				params.put("page", String.valueOf(page++));
				params.put("pageSize", String.valueOf(20));
				return params;
			}
		};
		queue.add(sr);
	}
	List<DBCSItem> db;
	private void initCache(){
		db = DBCSItem.listAll(DBCSItem.class);
	}
	private DBCSItem findContentById(String id){
		if(db == null || db.size() == 0) return null;
		for(DBCSItem d : db){
			if(d.csId.equals(id)){
				return d;
			}
		}
		return null;
	}
	public static  void L(String msg) {
		Log.d("SongTaste", msg);
	}
}
