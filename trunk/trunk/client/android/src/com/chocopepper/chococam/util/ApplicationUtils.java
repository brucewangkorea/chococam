package com.chocopepper.chococam.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class ApplicationUtils {
	
	// 2012-05-11 brucewang
	// 자신의 Facebook ID를 저장.
	public static void saveMyFacebookId(Context context, String registrationId) {
		final SharedPreferences prefs = context.getSharedPreferences(
				Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString(Constants.PREFS_FACEBOOK_MY_ID, registrationId);
		editor.commit();

	}
	
	// 2012-05-11 brucewang
	// 로컬에 저장된 자신의 Facebook ID를 확인.
	public static String getMyFacebookId(Context context) {
		final SharedPreferences prefs = context.getSharedPreferences(
				Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		String registrationId = prefs.getString(
				Constants.PREFS_FACEBOOK_MY_ID, "");
		return registrationId;
	}
	
	
}
