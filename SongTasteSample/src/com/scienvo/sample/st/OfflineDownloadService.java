package com.scienvo.sample.st;

import java.util.HashMap;
import java.util.Map;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.orm.SugarRecord;
import com.scienvo.sample.st.framework.MyApplication;
import com.scienvo.sample.st.model.BaseModel;
import com.scienvo.sample.st.wrapper.CSViewWrapper.CSItem;

public class OfflineDownloadService extends IntentService{
	public OfflineDownloadService(String name) {
		super(name);
	}
	public OfflineDownloadService(){
		super("OfflineDownloadService");
	}
	RequestQueue queue;
	@Override
	protected void onHandleIntent(Intent intent) {
		L("onHandleIntent");
		final int page = 1;
		String url = "http://www.weiduwu.net/app/getContentList.json";
		final String contentUrl = "http://www.weiduwu.net/app/getContentByid.json";
				if(paraItems == null) return;
				for(CSItem d : paraItems){
					requestContent(contentUrl,d);
				}
	}
	
	private void requestContent(final String contentUrl,final CSItem item){
		StringRequest sr = new StringRequest(Request.Method.POST, contentUrl,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						DBCSItem s = new DBCSItem();
						s.csId = item.id;
						s.title = item.title;
						s.cnt = item.duan_count;
						s.contentsJson = response;
						s.save();
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
					}
				}) {
			@Override
			protected Map<String, String> getParams() {
				Map<String, String> params = new HashMap<String, String>();
				params.put("page", String.valueOf(1));
				params.put("pageSize", String.valueOf(item.duan_count));
				params.put("id", item.id);
				return params;
			}
		};
		queue.add(sr);
	}
	
	private void L(String msg) {
		Log.d("SongTaste", msg);
	}
	
	public static void start(CSItem[] d){
		paraItems = d;
		Intent intent = new Intent(MyApplication.getSugarContext(),OfflineDownloadService.class);
		MyApplication.getSugarContext().startService(intent);
	}
	
	public static class DBCSItem extends SugarRecord<DBCSItem>{
		public String csId;
		public int cnt;
		public String title;
		public String contentsJson;
		public DBCSItem(){}
		public void dump(){
			L("title = "+title+","+cnt);
		}
		private void L(String msg) {
			Log.d("SongTaste", msg);
		}
	}
	
	private static CSItem[] paraItems;
	
}
