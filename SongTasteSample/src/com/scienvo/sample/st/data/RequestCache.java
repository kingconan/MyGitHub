package com.scienvo.sample.st.data;

import com.orm.SugarRecord;

public class RequestCache extends SugarRecord<RequestCache>{
	public String key;
	public String json;
	public RequestCache(){
	}
	public RequestCache(String key,String json){
		this.key = key;
		this.json = json;
	}
}