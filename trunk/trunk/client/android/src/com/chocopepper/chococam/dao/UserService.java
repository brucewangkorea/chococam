package com.chocopepper.chococam.dao;

import android.content.Context;
import android.content.SharedPreferences;

import com.chocopepper.chococam.util.Constants;

/**
 * 디바이스에 저장된 유저 정보를 관리하는 클래스.
 */
public class UserService {
	
	// 2012-10-04 brucewang
	public static boolean recordMyUserId2(Context context, String user_id) 
	{
    	final SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
    	prefs.edit().putString(Constants.PREFS_AUTH_TOKEN, user_id).commit();
    	return true;
	}
	public static String getMyUserId2(Context context) {		
		final SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String userId = prefs.getString(Constants.PREFS_AUTH_TOKEN, null);
        if(userId!=null && userId.length()==0){
        	userId = null;
        }
		return userId;
	}
	
	
//	// TODO userid 저장 하는 위치 변경 될 수 있음
//	/**
//	 * 사용자 등록 성공시 반환된 사용자 userid를 폰 내부에 저장하는 함수.
//	 * @param context, user_id	 
//	 */
//	public static boolean recordMyUserId(Context context, long user_id) {
//		
//    	final SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
//    	
//    	if(user_id == 0)
//    	{
//    		return false;
//    	}
//    	else
//    	{	
//    		prefs.edit().putLong(Constants.PREFS_USER_ID, user_id).commit();
//    		return true;
//    	}
//	}
	
	/**
	 * 폰 내부에 저장된 사용자 userid를 반환 하는 함수.
	 * @param context
	 * @return userid
	 */
	public static long getDefaultUserId(Context context) {		
		return 0;
	}

}
