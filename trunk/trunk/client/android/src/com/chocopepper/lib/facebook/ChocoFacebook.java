package com.chocopepper.lib.facebook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;

import com.chocopepper.chococam.util.ApplicationUtils;
import com.chocopepper.chococam.util.Logger;
import com.chocopepper.chococam.R;
import com.chocopepper.lib.facebook.SessionEvents.AuthListener;
import com.chocopepper.lib.facebook.SessionEvents.LogoutListener;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;

public class ChocoFacebook {
	private String[] mFacebookPermissions = {"email", "user_about_me",
			"read_friendlists",
			// "xmpp_login",
			"publish_actions", "offline_access", "publish_stream",
			"user_photos",
			// "publish_checkins",
			"photo_upload"};
	private String FACEBOOK_APP_ID = "148226865310626";
	private static ChocoFacebook mInstance = null;
	private Handler mHandler;
	private Context mContext;

	/*
	 *  
	 * 사용자 자신의 Facebook 정보가 반환된 경우에 호출되는 Listener
	 * 
	 */
	public class FacebookUserRequestListener extends BaseRequestListener {
		@Override
		public void onComplete(final String response, final Object state) {
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(response);

				FacebookUtility.userUID = jsonObject.getString("id");
				String img_url = jsonObject.getString("picture");
				FacebookUtility.email = jsonObject.getString("email");
				FacebookUtility.name = jsonObject.getString("name");
				FacebookUtility.profilePhotoUrl = img_url;
				
				// 2012-10-09 brucewang
				// Facebook 사용자 이미지 정보 형식이 변경되었음.
				JSONObject j;
				try {
					j = new JSONObject(img_url);
					String str = j.getString("data");
					j = new JSONObject(str);
					FacebookUtility.profilePhotoUrl = j.getString("url");
				} catch (Exception e) {
					//Logger.e(TAG, e.toString());
				}

				ApplicationUtils.saveMyFacebookId(mContext, FacebookUtility.userUID);
				
				
				for( BaseRequestListener listener : mMyInfoListeners ){
					listener.onComplete(response, state);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}// end of 'FacebookUserRequestListener'
	
	
	private static LinkedList<BaseRequestListener> mMyInfoListeners = new LinkedList<BaseRequestListener>();
	public void addFacebookMyInfoListener(BaseRequestListener listener){
		mMyInfoListeners.add(listener);
	}
	public void removeFacebookMyInfoListener(BaseRequestListener listener){
		mMyInfoListeners.remove(listener);
	}

	
	/*
	 * --------------------------------------------------------------------
	 * Facebook app 설정 관련.
	 * --------------------------------------------------------------------
	 */
	public void setFacebookAppId(String[] strPermissions) {
		mFacebookPermissions = strPermissions;
	}
	public void setFacebookPermissions(String str) {
		FACEBOOK_APP_ID = str;
	}

	/*
	 * --------------------------------------------------------------------
	 * Singleton 관련.
	 * --------------------------------------------------------------------
	 */
	public static synchronized ChocoFacebook getInstance(Context ctx) {
		if (null == mInstance) {
			mInstance = new ChocoFacebook(ctx);
		}
		return mInstance;
	}
	public ChocoFacebook(Context ctx) {
		mContext = ctx;
		FacebookUtility.mFacebook = new Facebook(FACEBOOK_APP_ID);
		FacebookUtility.mAsyncRunner = new AsyncFacebookRunner(
				FacebookUtility.mFacebook);
		mHandler = new Handler();
		
		this.addAuthListener( new AuthListener() {
			@Override
			public void onAuthSucceed() {
				SessionStore.save(FacebookUtility.mFacebook, mContext);
				/*
				 * Facebook에 이미 로그인 되어 있는 상태라면 자신의 정보를 받아온다.
				 */
				getMyUserInfo(new FacebookUserRequestListener());
			}

			@Override
			public void onAuthFail(String error) {
				Logger.e("TAG", error);
			}
		});
		this.addLogoutListener(new LogoutListener() {
			@Override
			public void onLogoutBegin() {
			}

			@Override
			public void onLogoutFinish() {
				SessionStore.clear(mContext);
			}
		});
	}
	public void addAuthListener(AuthListener authlistener) {
		if (authlistener != null) {
			SessionEvents.addAuthListener(authlistener);
		}
	}
	public void addLogoutListener(LogoutListener logoutlistener) {
		if (logoutlistener != null) {
			SessionEvents.addLogoutListener(logoutlistener);
		}
	}

	/*
	 * 2012-05-05 brucewang 명시적으로 listner를 지웁니다.
	 */
	public void removeAuthListener(AuthListener listener) {
		SessionEvents.removeAuthListener(listener);
	}
	public void removeLogoutListener(LogoutListener listener) {
		SessionEvents.removeLogoutListener(listener);
	}

	/*
	 * --------------------------------------------------------------------
	 * Login/Logout 관련.
	 * --------------------------------------------------------------------
	 */
	public boolean loginRequired() {
		return !FacebookUtility.mFacebook.isSessionValid();
	}

	public void login(Activity act, int iActivityCode) {

		FacebookUtility.mFacebook.authorize(act, mFacebookPermissions,
				iActivityCode, new LoginDialogListener());
	}
	public void logout(Activity act) {
		SessionEvents.onLogoutBegin();
		AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(
				FacebookUtility.mFacebook);
		asyncRunner.logout(act, new LogoutRequestListener());
	}

	/*--------------------------------------------------------------------
	 * Activity onResume에 넣어줌.
	 --------------------------------------------------------------------*/
	public void whenOnResume(Activity act) {
		/*
		 * 앱이 실행/resume되었을 때 Facebook 세션을 확인, 로그아웃되었으면 로그아웃되었음을 표시. 로그인 되었으면
		 * (필요시) access token을 업데이트.
		 */
		if (FacebookUtility.mFacebook != null) {
			if (!FacebookUtility.mFacebook.isSessionValid()) {
				// Logged out
			} else {
				FacebookUtility.mFacebook.extendAccessTokenIfNeeded(act, null);
			}
		}
	}

	/*--------------------------------------------------------------------
	 * Login/Logout session listener
	 --------------------------------------------------------------------*/
	private final class LoginDialogListener implements DialogListener {
		@Override
		public void onComplete(Bundle values) {
			SessionEvents.onLoginSuccess();
		}

		@Override
		public void onFacebookError(FacebookError error) {
			SessionEvents.onLoginError(error.getMessage());
		}

		@Override
		public void onError(DialogError error) {
			SessionEvents.onLoginError(error.getMessage());
		}

		@Override
		public void onCancel() {
			SessionEvents.onLoginError("Action Canceled");
		}
	}

	private class LogoutRequestListener extends BaseRequestListener {
		@Override
		public void onComplete(String response, final Object state) {
			/*
			 * callback should be run in the original thread, not the background
			 * thread
			 */
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					SessionEvents.onLogoutFinish();
				}
			});
		}
	}

	/*
	 * ----------------------------------------------------- 사용자 자신의 Facebook
	 * 정보를 받아옴. -----------------------------------------------------
	 */
	public void getMyUserInfo(BaseRequestListener handler) {
		Bundle params = new Bundle();
		params.putString("fields", "name, picture, email");
		FacebookUtility.mAsyncRunner.request("me", params, handler);
	}

	/*
	 * ----------------------------------------------------- 사용자 자신의 Facebook
	 * 정보를 받아옴. -----------------------------------------------------
	 */
	public void getMyFriendsList(BaseRequestListener handler) {
		Bundle params = new Bundle();
		params.putString("fields", "name, picture, email");
		FacebookUtility.mAsyncRunner.request("me/friends", params, handler);
	}

	/*
	 * ----------------------------------------------------- 친구 담벼락에 글 남기기.
	 * -----------------------------------------------------
	 */
	public void postMessageToFriend(String userid, String msg, String link) {
		postMessageToFriend(userid, msg, link,
				mContext.getString(R.string.app_name),
				mContext.getString(R.string.facebook_app_desc),
				FacebookUtility.getAppIconUrl());
	}
	public void postMessageToFriend(String userid, String msg, String link,
			String caption, String description, String pictureUrl) {
		try {
			Bundle parameters = new Bundle();
			parameters.putString("message", msg);
			parameters.putString("caption", caption);
			parameters.putString("description", description);
			parameters.putString("picture", pictureUrl);
			if (link != null) {
				parameters.putString("link", link);
			}
			parameters.putString("name",
					mContext.getString(R.string.facebook_app_desc));

			@SuppressWarnings("unused")
			String str = FacebookUtility.mFacebook.request(userid + "/feed",
					parameters, "POST");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void postImageonWall(String strImgFilePath, String strMessage) {
		String response = "";
		try {
			String DIRECTORY_PATH = strImgFilePath;
			Bundle params = new Bundle();
			Bitmap bitmap = BitmapFactory.decodeFile(DIRECTORY_PATH);
			byte[] data = null;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
			data = baos.toByteArray();
			// params.putString("filename", "test.png");
			params.putString("message", strMessage);
			params.putByteArray("image", data);
			// params.putString("caption", "test caption");

			AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(
					FacebookUtility.mFacebook);

			mAsyncRunner.request("me/photos", params, "POST",
					new SampleUploadListener(), null);
			mAsyncRunner.request(response, new SampleUploadListener());
			// Logger.e(TAG, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class SampleUploadListener extends BaseRequestListener {

		@SuppressWarnings("unused")
		public void onComplete(final String response, final Object state) {
			try {
				// process the response here: (executed in background thread)
				Logger.d("Facebook-Example", "Response: " + response.toString());
				JSONObject json = Util.parseJson(response);
				final String src = json.getString("src");

				// then post the processed result back to the UI thread
				// if we do not do this, an runtime exception will be generated
				// e.g. "CalledFromWrongThreadException: Only the original
				// thread that created a view hierarchy can touch its views."

			} catch (JSONException e) {
				Logger.w("Facebook-Example", "JSON Error in response");
			} catch (FacebookError e) {
				Logger.w("Facebook-Example",
						"Facebook Error: " + e.getMessage());
			}
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
		}
	}
}
