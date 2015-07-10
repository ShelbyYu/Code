package com.xueyu.util;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

public class SingleHttpClient {
	private static DefaultHttpClient instance;
	public static String content=null;
	public static String mainHost="http://www.jcodecraeer.com";
	public static Cookie cookie;
	
	public SingleHttpClient(){}
	public static DefaultHttpClient getInstance() {
		if(instance==null){
			instance=new DefaultHttpClient();
		}
		return instance;
	}
	
}
