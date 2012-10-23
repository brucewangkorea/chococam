package com.chocopepper.chococam.model;

import android.app.Activity;
import android.os.AsyncTask;

import com.chocopepper.chococam.dao.UserService;
import com.chocopepper.chococam.network.SocialServerApis;
import com.chocopepper.chococam.util.Logger;
import com.chocopepper.chococam.util.MyProgressDialog;

/**
 * 2012-05-10 brucewang 자기 자신의 사용자 정보를 저장하기 위한 Singleton 입니다. 자기 정보는 수시로 받을 필요가
 * 없기때문에 한번 받았으면 되도록이면 그 정보를 재활용하는것이 좋을 듯 해서...
 * 
 * @author brucewang
 * 
 */
public class MyUserInfo {
	private static MyUserInfo mInstance;
	private static final String TAG = Logger.makeLogTag(MyUserInfo.class);
	public static MyUserInfo getInstance() {
		if (mInstance == null) {
			mInstance = new MyUserInfo();
		}
		return mInstance;
	}

	public MyUserInfo() {

	}

	public interface MyUserInfoListener {
		void whenMyInfoIsReceived(SocialServerApis.ChocoUser myinfo);
	}

	private SocialServerApis.ChocoUser mMyUserInfo;
	/**
	 * 2012-05-10 brucewang 자기 정보를 반환합니다.
	 * 
	 * @param ctx
	 * @param bForceDownload
	 *            true 라면 무조건 서버로부터 정보를 다시 다운로드 합니다. false 라면 기존에 받아 온 정보가 있다면 그
	 *            정보를 그대로 반환합니다.
	 * @return
	 */
	public void getMyUserInfo(Activity ctx, boolean bForceDownload,
			MyUserInfoListener listener) {
		if (mMyUserInfo == null || bForceDownload) {
			new GetDataTask().setContext(ctx).setCompletionListener(listener).execute();
		} else {
			if (listener!=null) {
				listener.whenMyInfoIsReceived(mMyUserInfo);
			}
		}
	}

	public void updateMyUserInfo(SocialServerApis.ChocoUser user) {
		if (mMyUserInfo == null)
			mMyUserInfo = new SocialServerApis.ChocoUser();
		mMyUserInfo.copyFromOther(user);
	}
	
	/**
	 * 2012-05-10 brucewang 서버로부터 현재 사용자의 관련 설정 정보를 확인하는 AsyncTask.
	 * 
	 * @author brucewang
	 * 
	 */
	class GetDataTask extends AsyncTask<Activity, String, Boolean> {
		private MyProgressDialog mProgress = null;

		private Activity mCtx = null;
		public GetDataTask setContext(Activity ctx) {
			mCtx = ctx;
			return this;
		}
		
		MyUserInfoListener mListener=null;
		public GetDataTask setCompletionListener(MyUserInfoListener listener){
			mListener = listener;
			return this;
		}

		@Override
		protected Boolean doInBackground(Activity... params) {
			boolean result = false;
			if (mCtx == null) {
				return false;
			}
			SocialServerApis socialconnection = SocialServerApis
					.getInstance(mCtx);
			mMyUserInfo = socialconnection.chocoGetUserInfo(UserService.getMyUserId2(mCtx));

			return result;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgress = MyProgressDialog.show(mCtx, "", "");
		}

		protected void onProgressUpdate(String... progress) {
		}

		protected void onPostExecute(Boolean result) {
			try{
				if(mProgress!=null){ mProgress.dismiss(); }
				if (mListener != null) {
					/**
					 * TEST TEST
					 */
					if(mMyUserInfo==null){
						SocialServerApis.ChocoUser user = new SocialServerApis.ChocoUser();
						user.email = "test@test.com";
						user.avatar_url.original = "http://mac.softpedia.com/screenshots/thumbs/AngryBird-Replacement-Icon-thumb.jpg";
						user.avatar_url.thumbnail = "http://mac.softpedia.com/screenshots/thumbs/AngryBird-Replacement-Icon-thumb.jpg";
						mMyUserInfo = user;
					}
					mListener.whenMyInfoIsReceived(mMyUserInfo);
				}
			}catch(Exception e){
				Logger.e(TAG, e.toString());
			}
		}
	}
}
