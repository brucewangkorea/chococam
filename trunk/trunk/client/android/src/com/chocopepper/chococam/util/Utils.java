package com.chocopepper.chococam.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Environment;
import android.view.View;

import com.chocopepper.chococam.activity.profile.UserInfoActivity;

public class Utils {

	public static AccountDialog startAlert = null;
	public static AccountDialog emailAlert = null;
	public static AccountDialog facebookAlert = null;
	public static AccountDialog FriendsInvateAlert = null;
	public static AccountDialog FriendsInvateMsgAlert = null;
	public static AccountDialog gamzilAlert = null;
	public static AccountDialog gamzilMsgAlert = null;
	public static AccountDialog questEndAlert = null;
	
	

	// 초성 검사하는 함수
	public static boolean isChosung(String userName)
	{	
		CharSequence temp = userName;
		// 자음 또는 모음만으로 된 경우.
		String patternRegex = "[ㄱ-ㅎㅏ-ㅣ]";
		Pattern pattern = Pattern.compile(patternRegex);
		Matcher match = pattern.matcher(temp);
		boolean bFound = match.find();
		return bFound;
	}
	
	/**
	 * 디바이스의 해상도 크기 가져오는 함수
	 * @param context
	 * @return
	 */
	public static int getDeviceSizeWidth(Context context)
	{
		float screenWidth = context.getResources().getDisplayMetrics().widthPixels;		
		return (int)screenWidth;
	}
	
	public static int getDeviceSizeHeight(Context context)
	{		
		float screenHeight = context.getResources().getDisplayMetrics().heightPixels;		
		return (int)screenHeight;
	}
	
	
	public static String[] getNamesOfTopmostActivity(Context context){
		ActivityManager am = (ActivityManager) context.
		    getSystemService(Activity.ACTIVITY_SERVICE);
		String packageName = am.getRunningTasks(1).get(0).topActivity.getPackageName();
		String className = am.getRunningTasks(1).get(0).topActivity.getClassName();
		String[] result = new String[2];
		result[0] = packageName;
		result[1] = className;
		return result;
	}

	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}

	// 2012-5-02 brucewang
	// 친구 목록을 불러와서 친구를 선택하여 친구에게 직접 이메일 전송. (App이 직접 처리)
	//
	// ex) sendInvitationEmailTo(
	// ProjectG.this,
	// "test@test.com",
	// "겜질 같이 해 볼래?",
	// "같이 하자. 설치링크는 말야 '....'"
	public static boolean sendInvitationEmailTo(Context ctx, String strEmail, // 메시지
																				// 수신인의
																				// 이메일
																				// 주소.
			String subject, String body) {
		boolean ret = false;
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_EMAIL, new String[]{strEmail});
		i.putExtra(Intent.EXTRA_SUBJECT, subject);
		i.putExtra(Intent.EXTRA_TEXT, body);
		try {
			ctx.startActivity(Intent.createChooser(i, "Send mail..."));
		} catch (android.content.ActivityNotFoundException ex) {
			Logger.e("Utils", "There are no email clients installed.");
		}
		return ret;
	}

	public static void openUserInfoPage(Context ctx, String user_id) {
		Intent i = new Intent(ctx, UserInfoActivity.class);
		i.putExtra(Constants.TARGET_USER_ID, user_id);
		ctx.startActivity(i);
	}

	

	/**
	 * 회원 가입 시 오류 팝업
	 * 
	 * @param context
	 * @param msg
	 */
	// public static void alertCheckError(Context context, String msg)
	// {
	// Resources res = context.getResources();
	// AlertDialog.Builder chkErrorAlert = new AlertDialog.Builder(context);
	// chkErrorAlert.setTitle("");
	// chkErrorAlert.setMessage(msg);
	// chkErrorAlert.setPositiveButton(res.getString(android.R.string.ok), new
	// DialogInterface.OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	//
	// }
	// });
	//
	// chkErrorAlert.show();
	// }


	/**
	 * 이모티콘 팝업 호출
	 * 
	 * @param v
	 */
	public static void PopShow(View v, Activity parent, int positon, String callActivity) {
		Pop pop = new Pop(v, parent, positon, callActivity);
		pop.show();
	}

	/**
	 * 이모티콘 팝업 호출
	 * 
	 * @param v
	 * @param parent
	 * @param positon
	 */
//	public static void PopShow(View v) {
//		Pop pop = new Pop(v);
//		pop.show();
//	}

	/**
	 * 유저 프로필 이미지 경로를 저장 하는 함수.
	 * 
	 * @param context
	 *            , path
	 * @return result
	 */
	public static void recordMyProFileImagePath(Context context, String path) {
		final SharedPreferences prefs = context.getSharedPreferences(
				Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);

		if (path == null || path.length() == 0) {
			return;
		} else {
			prefs.edit().putString(Constants.PREFS_PROFILE_IMAGE_PATH, path)
					.commit();
		}
	}

	/**
	 * 폰 내부에 저장된 유저 프로필 이미지 경로를 반환 하는 함수.
	 * 
	 * @param context
	 * @return path
	 */
	public static String getMyProFileImagePath(Context context) {
		final SharedPreferences prefs = context.getSharedPreferences(
				Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		String path = prefs.getString(Constants.PREFS_PROFILE_IMAGE_PATH, null);
		return path;
	}

	/**
	 * 폰과 동기화된 이메일 계정을 가져온다.
	 * 
	 * @param context
	 * @return
	 */
	public static String getMyGoogleEmail(Context context) {
		AccountManager mgr = AccountManager.get(context);
		Account[] accts = mgr.getAccounts();
		final int count = accts.length;
		Account acct = null;
		String email = null;

		for (int i = 0; i < count; i++) {
			acct = accts[i];
			Logger.e("ANDROES", "Account - name=" + acct.name + ", type="
					+ acct.type);
			email = acct.name;
		}

		if (email != null) {
			return email;
		} else {
			return "Unknown";
		}
	}

	/**
	 * 서버에서 내려온 이미지 표시 할 때 이미지의 전체 경로를 반환
	 * 
	 */
	public static String getServerImageUrl(String path) {
		String result = path;
		if( path==null ){
			
			return "";
		}
		if( !path.contains(Constants.SERVER_IMAGE_PATH) ){
			if( !path.startsWith("/") ){
				result = Constants.SERVER_IMAGE_PATH + path;
			}
			else if( path.startsWith("http") ){
				result = path;
			}
			else{
				result = Constants.SERVER_IMAGE_PATH + "/" + path;
			}
			result = Constants.SERVER_IMAGE_PATH + path;
		}
		return result;
	}
	
	
	/**
	 * nerine 12-06-19
	 * 현재 실행 중인 앱 패키지 명 반환 함수
	 */
	public static ArrayList<String> getCurrentRunAppPackageName(Context context)
	{
		ArrayList<String> resultArray = new ArrayList<String>();		
		ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);    	
    	List<ActivityManager.RunningTaskInfo> rt = mActivityManager.getRunningTasks(100);
		
		for(RunningTaskInfo str: rt)
		{	
			resultArray.add(str.topActivity.getPackageName().toString());		
		}
		
		return resultArray;
	}
	
	/**
	 * 게임 db에 있는 date의 Format형식을 yyyy:mm:hh로 update 했는지 체크
	 * 태수 2012.06.19 
	 * @param context
	 * @param result    false : 한번도 안함, true : 1번 했음
	 */
	public static void setCheckDbDateUpdate(Context context, boolean result)
	{
		SharedPreferences prefs = context.getSharedPreferences("DateUpdate", Context.MODE_PRIVATE);
		prefs.edit().putBoolean("Isupdate", result).commit();
	}
	
	public static boolean getCheckDbDateUpdate(Context context)
	{
		SharedPreferences prefs = context.getSharedPreferences("DateUpdate", Context.MODE_PRIVATE);
		return prefs.getBoolean("Isupdate", false);
	}
	
	public static void saveViewToImageFile(View v){
		Bitmap bitmap;
		View v1 = v.getRootView();
		v1.setDrawingCacheEnabled(true);
		bitmap = Bitmap.createBitmap(v1.getDrawingCache());
		v1.setDrawingCacheEnabled(false);

		String path = Environment.getExternalStorageDirectory().toString();
		OutputStream fOut = null;
		try{
			File file = new File(path, "FitnessGirl.jpg");
			fOut = new FileOutputStream(file);
			
			bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
			fOut.flush();
			fOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}