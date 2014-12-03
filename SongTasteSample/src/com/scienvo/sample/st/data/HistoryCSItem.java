package com.scienvo.sample.st.data;

import com.orm.SugarRecord;

public class HistoryCSItem extends SugarRecord<HistoryCSItem>{
	public String csToken;
	public int pos;
	public int fromTop;
	public HistoryCSItem(){
	}
	public HistoryCSItem(String id,int pos,int fromTop){
		this.csToken = id;
		this.pos = pos;
		this.fromTop = fromTop;
	}
}
