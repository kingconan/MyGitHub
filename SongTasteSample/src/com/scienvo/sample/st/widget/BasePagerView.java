package com.scienvo.sample.st.widget;

import com.scienvo.sample.st.model.RequestHandler;
import com.scienvo.sample.st.model.RequestHandler.IDataReceiver;

public class BasePagerView implements IDataReceiver{
	protected RequestHandler requesHandler;
	public BasePagerView(){
		requesHandler = new RequestHandler(this);
	}
	@Override
	public void onResponse(int id) {
		
	}
	@Override
	public void onError(int id) {
		
	}
}
