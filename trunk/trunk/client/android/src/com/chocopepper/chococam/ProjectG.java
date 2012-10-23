package com.chocopepper.chococam;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;

import com.chocopepper.chococam.activity.ActivityUtils;
import com.chocopepper.chococam.activity.MainActivity;
import com.chocopepper.chococam.activity.account.FacebookOrEmailActivity;
import com.chocopepper.chococam.dao.UserService;
import com.chocopepper.chococam.network.SocialServerApis;
import com.chocopepper.chococam.util.ApplicationUtils;
import com.chocopepper.chococam.util.Constants;
import com.chocopepper.chococam.util.IOUtil;
import com.chocopepper.chococam.util.Logger;
import com.chocopepper.chococam.util.MyProgressDialog;

public class ProjectG extends Activity {

	private AlertDialog mDialog = null;
	private boolean isStart = false;
	private static final String TAG = Logger.makeLogTag(ProjectG.class);

	private Class<?> mainClass = null;
	
//	private SharedPreferences sharedPref;
	
	LogInTask logInTask;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);

		Intent i = getIntent();


		isStart = i.getBooleanExtra(Constants.RESULT_START_MAINACTIVITY, false);

		/* Set Log Level */
		Logger.setLogLevel(Logger.ALL);

		IOUtil.DBtoSdcardCopy("/data/data/com.chocopepper.chococam/databases",
				Constants.APP_NAME);

		// 초기 네트워크 상태를 세팅한다.
			// 2012-05-26 brucewang
			// UserID가 저장되지 않은 예외상황 처리.
			String myUserId = UserService.getMyUserId2(getApplicationContext());
			// 첫 실행이라면 가입 화면 부터 시작
			if ( myUserId==null ) {
				/**
				 * nerine 2012-06-18 사용자가 앱을 재설치 하거나 새로 설치 하였다는 플래그를 준다.
				 */
				Constants.RESETUP_APP = true;
				
				mainClass = FacebookOrEmailActivity.class;
				goNextActivity(mainClass);
				finish();
			} else {				
				login();
			}
	}

	private void login() {
		logInTask = new LogInTask();
		logInTask.execute();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

	}

	private void goNextActivity(Class<?> mainClass) {
//		sharedPref = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		
		Intent intent = new Intent(ProjectG.this, mainClass);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		ActivityUtils.setStartIntent(ProjectG.this, intent);
		intent.putExtra(Constants.RESULT_START_MAINACTIVITY, isStart);

		startActivity(intent);
	}
	


	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void finish() {
		if (logInTask != null) {
			logInTask.cancel(true);
		}
		super.finish();
	}

	@Override
	public void onBackPressed() {
		if (logInTask != null) {
			logInTask.cancel(true);
		}

		super.onBackPressed();
	}

	/**
	 * 로그인 실패 시 사용자에게 보여질 오류 팝업
	 * 
	 * @param msg
	 * @return
	 */
	private AlertDialog alertCheckLoginError(String msg) {
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle("");
		ab.setMessage(msg);
		ab.setCancelable(false);
		return ab.create();
	}

	class LogInTask extends AsyncTask<Boolean, String, String> {
		private MyProgressDialog mProgress = null;
		@Override
		protected String doInBackground(Boolean... params) {
			String myFacebookId = ApplicationUtils
					.getMyFacebookId(getApplicationContext());
			if (myFacebookId != null && myFacebookId.length() > 0) {
				Logger.i(TAG, String.format("MyFacebookID = %s", myFacebookId));
			}
			String authToken = SocialServerApis.getInstance(getApplicationContext())
					.chocoGetAuthToken(myFacebookId);
			return authToken;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			if(ProjectG.this.isFinishing())
				return;
			
			mProgress = MyProgressDialog.show(ProjectG.this, false, "", "");
			mProgress.addTask(this);
			
			mProgress.setCancelable(true);
			mProgress.setOnCancelListener(cancelListener);
		}

		protected void onPostExecute(String authToken) {
			if (authToken != null ){
				mainClass = MainActivity.class;
				goNextActivity(mainClass);
			} else {
				Resources res = getResources();
				mDialog = alertCheckLoginError(res
						.getString(R.string.userLoginError));
				mDialog.show();
				return;
			}

			if (mProgress != null) {
				mProgress.dismiss();
			}
			finish();
		}
	}

	public DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {

		@Override
		public void onCancel(DialogInterface dialog) {
			// TODO Auto-generated method stub
			Logger.i(TAG, "logInTask Task Cancel");

			finish();
		}
	};

}