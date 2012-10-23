package com.chocopepper.chococam.network;

import org.json.JSONObject;

import com.chocopepper.chococam.util.Logger;

public class ResponseParserUserRegistration {
	
	private static final String TAG = Logger.makeLogTag(ResponseParserUserRegistration.class);
	
	public static String parseUserJoin(String json){
		String str="";
		
		JSONObject j;
		try {
			j = new JSONObject(json);
//			info = j.getJSONObject("info");
			str = j.getString("user_id");
		} catch (Exception e) {
			Logger.e(TAG, e.toString());
		}
		
		return str;
	}
}
