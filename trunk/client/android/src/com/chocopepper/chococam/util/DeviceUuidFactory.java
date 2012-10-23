package com.chocopepper.chococam.util;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

public class DeviceUuidFactory { 
	
    protected static UUID uuid;
    
    /**
     * 현재 디바이스의 unique한 ID를 반환 하는 함수
     * @param context
     * @return
     */    
    public static String getCurDeviceUUID(Context context) {
    	
        final String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);

        try {
            if (!"9774d56d682e549c".equals(androidId) && androidId != null) {
                uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
            } else {
                final String deviceId = 
                	((TelephonyManager) context.getSystemService( Context.TELEPHONY_SERVICE )).getDeviceId();
                uuid = deviceId != null ? 
                		UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")) : UUID.randomUUID();
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    	
    	return uuid.toString();
    }
    
    /**
     * 생성된 비밀번호 (UUID)를 폰 내부에 저장하는 함수.
     * @param context, str
     * @return result
     */
    public static boolean recordMyPassword(Context context, String str)
    {
    	final SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		final String password = prefs.getString(Constants.PREFS_DEVICE_ID, null);
    	
    	if(str == null)
    	{
    		return false;
    	}
    	else if(password == null)
    	{
    		prefs.edit().putString(Constants.PREFS_DEVICE_ID, str).commit();
    		return true;
    	}
    	else
    	{	
    		//이미 등록된 패스워드가 있음 
    		return true;
    	}		
    }
    

    /**
     * 폰 내부에 저장된 비밀번호 (UUID) 를 반환 하는 함수.
     * @param context
     * @return password
     */
    public static String getMyPassword(Context context)
    {    	
    	final SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String password = prefs.getString(Constants.PREFS_DEVICE_ID, null);    	
    	return password;
    }
}
