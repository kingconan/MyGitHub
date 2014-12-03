package com.scienvo.sample.st.model;

import java.util.List;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.scienvo.gson.Gson;
import com.scienvo.gson.GsonBuilder;
import com.scienvo.sample.st.data.RequestCache;
import com.scienvo.sample.st.framework.MyApplication;

public class BaseModel {
	public static RequestQueue queue = Volley.newRequestQueue(MyApplication.getSugarContext());
	private static GsonBuilder gsonBuilder = new GsonBuilder();

	public static <T> void registerTypeAdapter(Class<T> clazz, Object obj) {
		gsonBuilder.registerTypeAdapter(clazz, obj);
	}

	public static <T> String toGson(T instance) {
		Gson gson = gsonBuilder.create();
		String res = gson.toJson(instance);
		return res;
	}

	public static <T> String toGson(T[] instanceArray) {
		Gson gson = gsonBuilder.create();
		String res = gson.toJson(instanceArray);
		return res;
	}

	public static <T> T fromGson(String gsonString, Class<T> clazz) {
		try {
			Gson gson = gsonBuilder.create();
			T ins = gson.fromJson(gsonString, clazz);
			return ins;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void saveRequest(String key,String json){
		List<RequestCache> res =  RequestCache.find(RequestCache.class, "KEY = ?",key);
		if(res == null || res.size() == 0){
			RequestCache c = new RequestCache(key, json);
			c.save();
		}
		else{
			RequestCache c = res.get(0);
			c.json = json;
			c.save();
		}
		
	}
	public String getRequest(String key){
		List<RequestCache> res =  RequestCache.find(RequestCache.class, "KEY = ?",key);
		if(res == null || res.size() == 0) return null;
		return res.get(0).json;
	}
}
