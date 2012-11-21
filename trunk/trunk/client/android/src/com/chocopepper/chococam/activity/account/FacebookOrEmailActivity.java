package com.chocopepper.chococam.activity.account;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chocopepper.chococam.ProjectG;
import com.chocopepper.chococam.R;
import com.chocopepper.chococam.activity.friends.FriendsFeedActivity;
import com.chocopepper.chococam.dao.UserService;
import com.chocopepper.chococam.network.SocialServerApis;
import com.chocopepper.chococam.util.Constants;
import com.chocopepper.chococam.util.ImageViewRounded;
import com.chocopepper.chococam.util.Logger;
import com.chocopepper.chococam.util.MyProgressDialog;
import com.chocopepper.lib.facebook.BaseRequestListener;
import com.chocopepper.lib.facebook.ChocoFacebook;
import com.chocopepper.lib.facebook.FacebookUtility;
import com.chocopepper.lib.facebook.SessionEvents.AuthListener;
import com.chocopepper.lib.facebook.SessionEvents.LogoutListener;

public class FacebookOrEmailActivity extends Activity {
	private static final String TAG = Logger
			.makeLogTag(FacebookOrEmailActivity.class);
	SocialServerApis mSocialconnection;
	private ChocoFacebook mChocoFacebook;
	private Handler mFacebookHandler;
	private FacebookSessionListener mFacebookSessionListener = new FacebookSessionListener();
	private FacebookUserRequestListener mMyFacebookInfoListner = new FacebookUserRequestListener();

	TextView txtTitle;
	Button mBtnLogInOutFacebook;
	Button mBtnLogOutFacebook;
	ImageViewRounded mImgViewFacebook;
	RelativeLayout relativeUseFacebook;
	
	
	
	// 메인 화면 호출
	private void moveToMainActivity() {
		Intent i = new Intent(FacebookOrEmailActivity.this, ProjectG.class);
		i.putExtra(Constants.RESULT_START_MAINACTIVITY, true);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(i);
		finish();
	}

	/*
	 * 사용자 자신의 Facebook 정보가 반환된 경우에 호출되는 Listener
	 */
	public class FacebookUserRequestListener extends BaseRequestListener {
		@Override
		public void onComplete(final String response, final Object state) {
			// Main UI thread 에서 뭔가 작업이 필요할 때.
			mFacebookHandler.post(new Runnable() {
				@Override
				public void run() {
					
					
					
					mImgViewFacebook.setImageBitmap(FacebookUtility
							.getBitmap(FacebookUtility.profilePhotoUrl));
					String strMsg = String.format(FacebookOrEmailActivity.this
							.getString(R.string.format_logout_facebook),
							FacebookUtility.name);
					mBtnLogInOutFacebook.setText(strMsg);

					relativeUseFacebook.setVisibility(View.VISIBLE);
					mBtnLogOutFacebook.setVisibility(View.GONE);

					// 유저 등록 함수 호출
					SocialServerApis.ChocoUser user = SocialServerApis
							.getInstance(FacebookOrEmailActivity.this)
							.chocoRegisterUser(
									FacebookUtility.userUID,
									FacebookUtility.name,
									FacebookUtility.profilePhotoUrl);
					
					// 2012-11-21 brucewang
					// show progress when registering new user
					if(mProgress!=null){ mProgress.dismiss(); }
					
					if( user!=null ){
						UserService.recordMyUserId2( FacebookOrEmailActivity.this, user._id  );
						moveToMainActivity();
					}
				}
			});
		}
	}// end of 'FacebookUserRequestListener'

	MyProgressDialog mProgress=null;
	/*
	 * 
	 * Login/Logout session 관련 이벤트를 종합 관리하는 listener.
	 */
	private class FacebookSessionListener
			implements
				AuthListener,
				LogoutListener {
		@Override
		public void onAuthSucceed() {
			// 2012-11-21 brucewang
			// show progress when registering new user
			mProgress = MyProgressDialog.show(FacebookOrEmailActivity.this, "", "");
			mProgress.setCancelable(true);
		}
		@Override
		public void onAuthFail(String error) {
		}
		@Override
		public void onLogoutBegin() {
		}
		@Override
		public void onLogoutFinish() {
			mBtnLogInOutFacebook.setText(R.string.login_to_facebook);
			mImgViewFacebook.setImageResource(R.drawable.facebook_icon1);

			relativeUseFacebook.setVisibility(View.GONE);
			mBtnLogOutFacebook.setVisibility(View.VISIBLE);
		}
	}// end of 'FacebookSessionListener'

	public static String mStrEmailAddressGiven = "";
	public static boolean mbUseFacebookForLogin = true;

	@Override
	protected void onDestroy() {
		mChocoFacebook.removeLogoutListener(mFacebookSessionListener);
		mChocoFacebook.removeAuthListener(mFacebookSessionListener);
		mChocoFacebook.removeFacebookMyInfoListener(mMyFacebookInfoListner);
		super.onDestroy();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.facebook_or_email);

		mSocialconnection = SocialServerApis.getInstance(this
				.getApplicationContext());
		mFacebookHandler = new Handler();
		mChocoFacebook = ChocoFacebook.getInstance(this);

		Resources res = getResources();

		txtTitle = (TextView) findViewById(R.id.title);
		txtTitle.setText(res.getString(R.string.welcome_to_chococam));
		mImgViewFacebook = (ImageViewRounded) findViewById(R.id.imageFacebook);
		relativeUseFacebook = (RelativeLayout) findViewById(R.id.relativeUseFacebook);

		mBtnLogOutFacebook = (Button) findViewById(R.id.buttonFbLogout);
		mBtnLogOutFacebook.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mChocoFacebook.login(FacebookOrEmailActivity.this,
						Constants.FACEBOOK_AUTHORIZE_ACTIVITY_RESULT_CODE);
			}
		});

		mBtnLogInOutFacebook = (Button) findViewById(R.id.buttonLogInOutFacebook);
		mBtnLogInOutFacebook.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mChocoFacebook.logout(FacebookOrEmailActivity.this);
			}
		});

		/*
		 * 2012-08-20 brucewang 첫 가입화면인 이 시점에서, 사용자가 이미 Facebook 에 로그인 되어 있는가에
		 * 따라 적절한 UI를 표시해 주어야 한다. 사용자가 실제로 Gamchen에서 Facebook을 이용하는지 안하는지는 이후의
		 * 과정에서만 의미있는 판단 요소 일 뿐이다.
		 */
		if (!mChocoFacebook.loginRequired()) {
			relativeUseFacebook.setVisibility(View.VISIBLE);
			mBtnLogOutFacebook.setVisibility(View.GONE);
		} else {
			relativeUseFacebook.setVisibility(View.GONE);
			mBtnLogOutFacebook.setVisibility(View.VISIBLE);
		}
		if (mChocoFacebook != null) {
			mChocoFacebook.addAuthListener(mFacebookSessionListener);
			mChocoFacebook.addLogoutListener(mFacebookSessionListener);
			mChocoFacebook.addFacebookMyInfoListener(mMyFacebookInfoListner);

			if (mChocoFacebook.loginRequired()) {
				/*
				 * 2012-08-20 brucewang 이 시점에서는 사용자가 Facebook 을 사용하지 않기를 원할 수도
				 * 있기때문에 무조건 Login창을 띄워주진 않고, 명시적으로 사용자가 Facebook Login버튼을 눌렀을
				 * 때만 Facebook 로긴 창을 띄워주도록 한다.
				 */
				// mChocoFacebook.login(this,Constants.FACEBOOK_AUTHORIZE_ACTIVITY_RESULT_CODE);
			} else {
				// 이미 Facebook에 로그인 되어 있음.
				mChocoFacebook.getMyUserInfo(mMyFacebookInfoListner);
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
			/*
			 * if this is the activity result from authorization flow, do a call
			 * back to authorizeCallback Source Tag: login_tag
			 */
			case Constants.FACEBOOK_AUTHORIZE_ACTIVITY_RESULT_CODE : {
				Logger.e(TAG, "FACEBOOK_AUTHORIZE_ACTIVITY_RESULT_CODE");
				FacebookUtility.mFacebook.authorizeCallback(requestCode,
						resultCode, data);
				break;
			}
		}
	}// end of 'onActivityResult' function.
}
