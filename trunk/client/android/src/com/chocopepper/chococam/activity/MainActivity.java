package com.chocopepper.chococam.activity;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.chocopepper.chococam.R;
import com.chocopepper.chococam.activity.account.FacebookOrEmailActivity;
import com.chocopepper.chococam.activity.friends.FeedsFragmentActivity;
import com.chocopepper.chococam.activity.friends.manage.FriendsActivity;
import com.chocopepper.chococam.activity.profile.UserInfoActivity;
import com.chocopepper.chococam.dao.UserService;
import com.chocopepper.chococam.network.SocialServerApis;
import com.chocopepper.chococam.util.ApplicationUtils;
import com.chocopepper.chococam.util.Constants;
import com.chocopepper.chococam.util.Logger;
import com.chocopepper.chococam.util.MyProgressDialog;
import com.chocopepper.chococam.util.Utils;
import com.chocopepper.lib.facebook.BaseRequestListener;
import com.chocopepper.lib.facebook.ChocoFacebook;
import com.chocopepper.lib.facebook.FacebookUtility;
import com.chocopepper.lib.facebook.SessionEvents.AuthListener;
import com.chocopepper.lib.facebook.SessionEvents.LogoutListener;

/**
 * 메인이 되는 Activity. Tab Bar 의 메인.
 * 
 * @author KHAN0405
 * 
 */
@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity implements OnTabChangeListener {

	private static final String TAG = Logger.makeLogTag(MainActivity.class);

	private final int RESULT_END_CODE = 99;

	private static final String MAIN_TAB_INDEX = "Main_Tab_index";

	boolean isStart = false;

	private int selectedTabIndex = 1;

	private TabHost tabHost;

	private TextView title;

	private TextView txtFriends;
	
	private TextView txtMyinfo;
	
	SocialServerApis mSocialconnection;	

	private Uri mImageCaptureUri;
	public static String mLastPictureFilePath = "";

	private SharedPreferences sharedPref;

	private ImageView manages;	

	private ChocoFacebook mChocoFacebook;
	@SuppressWarnings("unused")
	private Handler mFacebookHandler;
	private FacebookSessionListener mFacebookSessionListener = new FacebookSessionListener();
	private FacebookUserRequestListener mMyFacebookInfoListner = new FacebookUserRequestListener();

	/*
	 * 사용자 자신의 Facebook 정보가 반환된 경우에 호출되는 Listener
	 */
	public class FacebookUserRequestListener extends BaseRequestListener {
		@Override
		public void onComplete(final String response, final Object state) {
		}
	}// end of 'FacebookUserRequestListener'

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
		}
		@Override
		public void onAuthFail(String error) {
		}
		@Override
		public void onLogoutBegin() {
		}
		@Override
		public void onLogoutFinish() {
		}
	}// end of 'FacebookSessionListener'

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent i = getIntent();
		isStart = i.getBooleanExtra(Constants.RESULT_START_MAINACTIVITY, false);

		mFacebookHandler = new Handler();
		mChocoFacebook = ChocoFacebook.getInstance(this);
		mChocoFacebook.addAuthListener(mFacebookSessionListener);
		mChocoFacebook.addLogoutListener(mFacebookSessionListener);
		mChocoFacebook.addFacebookMyInfoListener(mMyFacebookInfoListner);

		if (!isStart) {
			String myFbId = ApplicationUtils.getMyFacebookId(this);
			if (myFbId != null && myFbId.length() > 0) {
				if (mChocoFacebook != null) {

					if (mChocoFacebook.loginRequired()) {
						mChocoFacebook
								.login(this,
										Constants.FACEBOOK_AUTHORIZE_ACTIVITY_RESULT_CODE);
					} else {
						// 이미 Facebook에 로그인 되어 있음.
						mChocoFacebook.getMyUserInfo(mMyFacebookInfoListner);
					}
				}
			}
		}

		setContentView(R.layout.mainview);

		// / FIXME 테스트용 로그 레벨. 정발시 삭제
		Logger.setLogLevel(Logger.ALL);

		// 각종 위젯을 세팅한다.
		setTabWidgets();

		mSocialconnection = SocialServerApis.getInstance(this);

		/**
		 * nerine 12-05-30 퀘스트 방식을 변경
		 */
		new GetDataTask().execute();

	}

	private void setTabWidgets() {
		sharedPref = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME,
				Context.MODE_PRIVATE);
		selectedTabIndex = sharedPref.getInt(MAIN_TAB_INDEX, 0);
		if (selectedTabIndex == 0) {
			Logger.i(TAG, "non saved tabIndex");
			selectedTabIndex = 1;
		}

		title = (TextView) findViewById(R.id.title);
		// title.setTypeface(font);
		title.setVisibility(View.GONE);

		// //////////////////////////// Title Content View Change
		// //////////////////////////////////////
		// // 로고
		LinearLayout logo = (LinearLayout) findViewById(R.id.logo);
		logo.setVisibility(View.VISIBLE);
		// // 친구 관리 화면
		manages = (ImageView) findViewById(R.id.btnManages);
		manages.setVisibility(View.VISIBLE);
		Intent manageIntent = new Intent(getApplicationContext(),
				FriendsActivity.class);
		// 2012-05-23 brucewang
		// '누구의' 친구목록을 보여줄 것인가 그 대상 id를 지정해 줍니다.
		manageIntent.putExtra(Constants.TARGET_USER_ID,
				UserService.getMyUserId(MainActivity.this));
		manages.setOnClickListener(new TitleClickListener(manageIntent));

		// //////////////////////////// Tab Content View Setup
		// //////////////////////////////////////
		// Tab 설정
		tabHost = getTabHost();
		tabHost.setOnTabChangedListener(this);
		// tabHost.set

		LayoutInflater inflater = getLayoutInflater();

		View friendsTabView = inflater.inflate(R.layout.tab_friends, null);
		View myinfoTabView = inflater.inflate(R.layout.tab_myinfo, null);
		View cameraTabView = inflater.inflate(R.layout.tab_camera, null);
		View invisibleTabView = inflater.inflate(R.layout.tabinvisible, null);

		

		txtFriends = (TextView) friendsTabView.findViewById(R.id.textView1);
		txtMyinfo = (TextView) myinfoTabView.findViewById(R.id.textView1);
		
		// 12-10-17 nerine
		// 디바이스 가로 크기가 500픽셀 이하면 탭과 탭에 글자가 겹치지 않도록 탭의 글자 크기를 조절 한다.
		int devicePixelWidth = Utils.getDeviceSizeWidth(this);

		if (devicePixelWidth < 500 && devicePixelWidth > 400) {
			txtFriends.setTextSize(11);			
			txtMyinfo.setTextSize(11);
		}
		else if(devicePixelWidth < 400)
		{
			txtFriends.setTextSize(9);			
			txtMyinfo.setTextSize(9);
		}


		/*
		 * 2012-08-14 brucewang Tab change event 를 제대로 받기 위해서는 보이지 않는 탭을 추가해 주어야
		 * 함.
		 */
		tabHost.addTab(tabHost.newTabSpec("invisible")
				.setIndicator(invisibleTabView)
				.setContent(new Intent(this, InvisibleActivity.class)));
		tabHost.addTab(tabHost.newTabSpec(getString(R.string.tab_friends))
				.setIndicator(friendsTabView)
				.setContent(new Intent(this, FeedsFragmentActivity.class)));
		tabHost.addTab(tabHost.newTabSpec("invisible")
				.setIndicator(cameraTabView)
				.setContent(new Intent(this, InvisibleActivity.class)));
		tabHost.addTab(tabHost.newTabSpec(getString(R.string.tab_profile))
				.setIndicator(myinfoTabView)
				.setContent(new Intent(this, UserInfoActivity.class)));

		/*
		 * 2012-08-14 brucewang '카메라 탭' 즉 3(0으로부터)번째 탭에 대해서 클릭 처리를 가로채서 해당 탭을
		 * 클릭해도 실제로 탭 전환은 이루어지지 않도록 함.
		 */
		tabHost.getTabWidget().getChildAt(2)
				.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						int action = event.getAction();
						if (action == MotionEvent.ACTION_UP) {
							SelectPhotoToShareAlert();
							return true; // Prevents from clicking
						}
						return false;
					}
				});
	}

	@Override
	public void onResume() {
		sharedPref = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME,
				Context.MODE_PRIVATE);
		selectedTabIndex = sharedPref.getInt(MAIN_TAB_INDEX, 0);
		if (selectedTabIndex == 0) {
			Logger.i(TAG, "non saved tabIndex");
			selectedTabIndex = 1;
		}
		Logger.d(TAG, "selectedTabIndex=" + selectedTabIndex);
		tabHost.setCurrentTab(selectedTabIndex);

		

		super.onResume();
	}

	/**
	 * 탭 체인지 될 때의 이벤트.
	 */
	@Override
	public void onTabChanged(String tabId) {
		int iCurTab = tabHost.getCurrentTab();
		if (iCurTab != 0) {
			// 상단 바 타이틀 바뀜 구현
			title.setText(tabId);

			// 현재 탭 상태 저장
			selectedTabIndex = tabHost.getCurrentTab();

			Editor editor = sharedPref.edit();
			editor.putInt(MAIN_TAB_INDEX, selectedTabIndex);
			editor.commit();

			// 현재 탭이 내정보이면 친구 관리 버튼을 숨기고 설정 버튼을 보인다.
			if (tabId.equals(getString(R.string.tab_profile))) {
				manages.setVisibility(View.GONE);				
			} else {				
				manages.setVisibility(View.VISIBLE);
			}
		}

		txtFriends.setTextColor(tabId.equals(getString(R.string.tab_friends))
				? Color.WHITE
				: Color.rgb(120, 114, 108));
		txtMyinfo.setTextColor(tabId.equals(getString(R.string.tab_profile))
				? Color.WHITE
				: Color.rgb(120, 114, 108));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.ActivityGroup#onDestroy()
	 */
	@Override
	protected void onDestroy() {

		mChocoFacebook.removeLogoutListener(mFacebookSessionListener);
		mChocoFacebook.removeAuthListener(mFacebookSessionListener);
		mChocoFacebook.removeFacebookMyInfoListener(mMyFacebookInfoListner);

		// 앱이 종료되면 챗 서비스도 종료하도록 한다.
		ActivityUtils.notifyFinish();		
		super.onDestroy();
	}

	class GetDataTask extends AsyncTask<Context, String, Boolean> {
		private MyProgressDialog mProgress = null;

		@Override
		protected Boolean doInBackground(Context... params) {
//			boolean result = false;

			Context ctx = MainActivity.this;

			SocialServerApis socialserver = SocialServerApis.getInstance(ctx);

			ArrayList<SocialServerApis.ChocoFeed> feeds = socialserver.chocoGetFeeds(0);
			

			return feeds!=null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgress = MyProgressDialog.show(MainActivity.this, "", "");
		}

		protected void onProgressUpdate(String... progress) {
		}

		protected void onPostExecute(Boolean result) {
			if (mProgress != null) {
				mProgress.dismiss();
			}
		}
	}

	/**
	 * 상단 바 클릭 리스너.
	 * 
	 * @author KHAN0405
	 * 
	 */
	private class TitleClickListener implements View.OnClickListener {
		Intent intent;
		public TitleClickListener(Intent intent) {
			this.intent = intent;
		}

		@Override
		public void onClick(View v) {
			// startActivity(intent);
			startActivityForResult(intent, RESULT_END_CODE);

		}
	}

	public View.OnClickListener noticeOkClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
		}
	};
	public View.OnClickListener rlClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Logger.d(TAG, "rl click");

		}
	};
	public View.OnClickListener btnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Logger.d(TAG, "btn click");
		}
	};
	public View.OnClickListener chkClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Logger.d(TAG, "chk click");
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == Constants.FACEBOOK_AUTHORIZE_ACTIVITY_RESULT_CODE) {
			FacebookUtility.mFacebook.authorizeCallback(requestCode,
					resultCode, data);
			Logger.e(TAG, "FACEBOOK_AUTHORIZE_ACTIVITY_RESULT_CODE");
		} else if (requestCode == RESULT_END_CODE) {
			if (resultCode == RESULT_OK && data != null) {
				boolean result = data.getBooleanExtra("End", false);

				if (result) {
					Intent i = new Intent(MainActivity.this,
							FacebookOrEmailActivity.class);
					startActivity(i);
					MainActivity.this.finish();
				}
			} else if (resultCode == RESULT_CANCELED) {

			}
		}

		switch (requestCode) {
			case Constants.PICK_FROM_ALBUM :
				if (data != null) {
					mImageCaptureUri = data.getData();
				} else {
					mImageCaptureUri = null;
				}
			case Constants.PICK_FROM_CAMERA : {
				whenCropCameraImageCompleted();
				break;
			}
		}// end of 'switch' cluase
	}

	// 업로드할 이미지 선택 팝업 띄움
	private void SelectPhotoToShareAlert() {

		DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				doTakeAlbumAction();
			}
		};

		DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		};
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		
		// 동영상 업로드로 문구 변경
		alert.setTitle(getString(R.string.upload_movie_title));
		alert.setNeutralButton(getString(R.string.upload_photo_from_gallery),
				albumListener);
		alert.setNegativeButton(getString(android.R.string.cancel),
				cancelListener);
		alert.show();
	}

	/**
	 * 앨범에서 이미지 가져오기
	 */
	private void doTakeAlbumAction() {
		// 앨범 호출
		Intent mediaChooser = new Intent(Intent.ACTION_PICK);
		//comma-separated MIME types
		mediaChooser.setType("video/*");
		//mediaChooser.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(mediaChooser, Constants.PICK_FROM_ALBUM);
	}

	private void whenCropCameraImageCompleted() {
		if (mImageCaptureUri == null) {
			return;
		}
		String strType = getContentResolver().getType(mImageCaptureUri);
		Cursor cursor = getContentResolver().query(mImageCaptureUri, null,
				null, null, null);
		String strTmpImgPath = "";
		
		
		if (cursor != null) {
			if (cursor.moveToNext()) {
				strTmpImgPath = cursor.getString(cursor
						.getColumnIndex(MediaStore.MediaColumns.DATA));
			}
		} else {
			strTmpImgPath = mImageCaptureUri.getPath();
		}

		// String
		File temporaryFile = new File(strTmpImgPath);
		/*
		 * 2012-08-18 brucewang 임시 파일이 없다면 더이상 진행하지 않고 종료.
		 */
		if (!temporaryFile.exists()) {
			Toast.makeText(MainActivity.this,
					MainActivity.this.getString(R.string.file_not_accessible),
					Constants.DEFAULT_TOAST_DURATION).show();
			return;
		}
		
		if( strType.contains("video") ){			
			Intent i = new Intent(MainActivity.this, PhotoUploadActivity.class);
			i.putExtra("video_path", strTmpImgPath);
			startActivity(i);
			return;
		}		
	}
}
