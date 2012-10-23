package com.chocopepper.chococam.model;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;

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
public class MyFriendsList {
	private static MyFriendsList mInstance;
	private static final String TAG = Logger.makeLogTag(MyFriendsList.class);
	private List<SocialServerApis.ChocoUser> mFriendsList = null;
	private Object mSyncObject =  new Object();
	
	
	public static MyFriendsList getInstance() {
		if (mInstance == null) {
			mInstance = new MyFriendsList();
		}
		return mInstance;
	}

	public MyFriendsList() {

	}

	public interface MyFriendsInfoListener {
		void whenMyFriendsListIsReceived(List<SocialServerApis.ChocoUser> friendslist);
	}

	public void getMyFriendsList(Activity ctx, boolean bForceDownload,
			MyFriendsInfoListener listener) {
		if (mFriendsList == null || bForceDownload) {
			new GetDataTask().setContext(ctx).setCompletionListener(listener).execute();
		} else {
			if (listener!=null) {
				listener.whenMyFriendsListIsReceived(mFriendsList);
			}
		}
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
		
		MyFriendsInfoListener mListener=null;
		public GetDataTask setCompletionListener(MyFriendsInfoListener listener){
			mListener = listener;
			return this;
		}

		@Override
		protected Boolean doInBackground(Activity... params) {
			boolean result = false;
			if (mCtx == null) {
				return false;
			}
			SocialServerApis socialserver = SocialServerApis.getInstance(mCtx);
			
			
			synchronized (mSyncObject) {
				mFriendsList = new ArrayList<SocialServerApis.ChocoUser>();
				 
				// Following list
				List<SocialServerApis.ChocoFollowInfo> list_following = socialserver.chocoFollowGetFollowings();
				for( SocialServerApis.ChocoFollowInfo follow : list_following ){
					follow.user.follow_accepted = follow.accepted;
					follow.user.i_am_following = true;
					mFriendsList.add( follow.user );
				}
				
				// Follower list
				List<SocialServerApis.ChocoFollowInfo> list_followers = socialserver.chocoFollowGetFollowers();
				for( SocialServerApis.ChocoFollowInfo follow : list_followers ){
					if( follow.accepted ){
						follow.user.is_follower = true;
						mFriendsList.add( follow.user );
					}
				}
			}
			
			

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
					if(mFriendsList==null){
						mFriendsList = new ArrayList<SocialServerApis.ChocoUser>();
					}
					mListener.whenMyFriendsListIsReceived(mFriendsList);
				}
			}catch(Exception e){
				Logger.e(TAG, e.toString());
			}
		}
	}
}
