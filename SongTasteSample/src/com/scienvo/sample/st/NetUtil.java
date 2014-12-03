package com.scienvo.sample.st;

import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.telephony.TelephonyManager;

import com.scienvo.sample.st.framework.MyApplication;

public class NetUtil {
	
	public static DefaultHttpClient getThreadSafeClient() {

	    DefaultHttpClient client = new DefaultHttpClient();
	    ClientConnectionManager mgr = client.getConnectionManager();
	    HttpParams params = client.getParams();
	    client = new DefaultHttpClient(
	        new ThreadSafeClientConnManager(params,
	            mgr.getSchemeRegistry()), params);

	    return client;
	}
	

	
	protected void checkNetworkInfo(Context context) {
		ConnectivityManager conMan =
			(ConnectivityManager)
			context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		// mobile 3G Data Network
		State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
		
		// wifi
		State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
	}

	/**
	 * ping target url and return a bool.
	 */
	public static boolean pingURL(String urlString) {
		try {

			URL url = new URL(urlString);

			HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
			urlc.setRequestProperty("User-Agent", "scienvo Android");
			urlc.setRequestProperty("Connection", "close");
			urlc.setConnectTimeout(3000); // mTimeout is in m-seconds
			urlc.connect();

			if (urlc.getResponseCode() == 200) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	
	public static boolean isWifiConnected(final Context context){
		try{
			boolean state = false;
			final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo wifi_info = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if(wifi_info != null){
				state = wifi_info.isConnected();
			}
			return state;
		}catch(Exception e){
			return false;
		}
	}
	public static boolean isConnect(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo info = connectivity.getActiveNetworkInfo();
			if (info != null) {
				if (info.isConnected()) {
					return true;
				}
			}
		}
		return false;
	}
	public static boolean isMobileConnected(final Context context){
		boolean state = false;
		final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mobile_info = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if(mobile_info != null){
			state = mobile_info.isConnected();
		}
		return state;
	}
	
	public static boolean is3GConnected()
	{
		Context context = MyApplication.getSugarContext();
		ConnectivityManager mConnectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);  
		TelephonyManager mTelephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);  
		NetworkInfo info = mConnectivity.getActiveNetworkInfo();  
		  
		if (info == null || !mConnectivity.getBackgroundDataSetting()) {  
		    return false;  
		}  
		  
		int netType = info.getType();  
		int netSubtype = info.getSubtype();  
		
		if (netType == ConnectivityManager.TYPE_MOBILE  
		            && netSubtype == TelephonyManager.NETWORK_TYPE_UMTS  
		            && !mTelephony.isNetworkRoaming()) {  
		    return info.isConnected();  
		} else {  
		    return false;  
		}  
	}
}
