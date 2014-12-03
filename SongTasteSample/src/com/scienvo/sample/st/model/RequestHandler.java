package com.scienvo.sample.st.model;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Message;

public class RequestHandler extends Handler{
	protected WeakReference<IDataReceiver> dataReceiver;
	
	public RequestHandler(IDataReceiver receiver){
		this.dataReceiver = new WeakReference<IDataReceiver>(receiver);
	}
	
	@Override
	public void handleMessage(Message msg) {
		if(dataReceiver == null) return;
		IDataReceiver receiver = dataReceiver.get();
		if(receiver != null){
			int what = msg.arg1;
			int id = msg.arg2;
			switch(what){
				case 0:
					receiver.onResponse(id);
					break;
				case 1:
					receiver.onError(id);
					break;
			}
		}
	}
	
	public interface IDataReceiver{
		public void onResponse(int id);
		public void onError(int id);
	}
}
