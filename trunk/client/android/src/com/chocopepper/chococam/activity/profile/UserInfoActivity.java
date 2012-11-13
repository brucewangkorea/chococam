package com.chocopepper.chococam.activity.profile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chocopepper.chococam.R;
import com.chocopepper.chococam.activity.account.FacebookOrEmailActivity;
import com.chocopepper.chococam.dao.UserService;
import com.chocopepper.chococam.model.MyUserInfo;
import com.chocopepper.chococam.network.SocialServerApis;
import com.chocopepper.chococam.util.Constants;
import com.chocopepper.chococam.util.ImageLoader;
import com.chocopepper.chococam.util.Logger;
import com.chocopepper.chococam.util.MyProgressDialog;
import com.chocopepper.chococam.util.Utils;
import com.chocopepper.lib.facebook.ChocoFacebook;
import com.chocopepper.lib.facebook.SessionEvents.AuthListener;
import com.chocopepper.lib.facebook.SessionEvents.LogoutListener;

public class UserInfoActivity extends Activity implements OnClickListener {

	private final String TAG = "UserInfoActivity";

	private SocialServerApis.ChocoUser mMyUserInfo = null;

	private String img_send_error_msg = "";
	private String img_send_error_code = "";

	boolean quest2_check = false;
	boolean quest3_check = false;

	SocialServerApis mSocialconnection = null;
	public final int USER_NAME_RESULT_CODE = 999;
	public final int ACTIVITY_MSG_RESULT_CODE = 9999;

	public Uri mImageCaptureUri;

	/**
	 * TODO nerine 12-08-20 옵티머스LTE2와 같은 특정기기에서 Uri를 intent로 받을 때 못받는 경우를 대비한
	 * static변수
	 */
	public static Uri mImageCaptureUriTemp;

	// 이미지 캐슁 준비
	ImageLoader imageLoader_user;
	ImageLoader imageLoader_game;

	// 위젯 설정
	private ImageView imgprofile;
	private LinearLayout llName_info;	
	private TextView txtName_info;	
	private Button mBtnLogout;
	
	private ChocoFacebook mChocoFacebook;
	private Handler mFacebookHandler;
	private FacebookSessionListener mFacebookSessionListener = new FacebookSessionListener();
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
			LogoutAndStartAgain();
		}
	}// end of 'FacebookSessionListener'

	GetDataTask getDataTask;

	String mStrUserId;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_info);

		Intent i = getIntent();
		if (i != null) {
			mStrUserId = i.getStringExtra(Constants.TARGET_USER_ID);
		} 
		if(mStrUserId==null || mStrUserId.length()<1){
			mStrUserId = UserService.getMyUserId2(this);
		}
		mSocialconnection = SocialServerApis.getInstance(UserInfoActivity.this);

		imageLoader_user = new ImageLoader(this);
		imageLoader_game = new ImageLoader(this);

		imageLoader_user.setStubImageResourceId(R.drawable.default_user);

		// 위젯 설정
		imgprofile = (ImageView) findViewById(R.id.imgprofile);
		llName_info = (LinearLayout) findViewById(R.id.llName_info);
		txtName_info = (TextView) findViewById(R.id.txtName_info);

		llName_info.setOnClickListener(this);
		txtName_info.setOnClickListener(this);
		imgprofile.setOnClickListener(this);
		
		// 2012-11-13 brucewang
		// 로그아웃 기능 구현.
		mChocoFacebook = ChocoFacebook.getInstance(UserInfoActivity.this);
		mChocoFacebook.addLogoutListener(mFacebookSessionListener);
		mBtnLogout = (Button)findViewById(R.id.btnLogout);
		mBtnLogout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if( mChocoFacebook.loginRequired()==false ){
					mChocoFacebook.logout(UserInfoActivity.this);
				}
				else{
					LogoutAndStartAgain();
				}
			}
		});
	}
	
	private void LogoutAndStartAgain(){
		mChocoFacebook.removeAuthListener(mFacebookSessionListener);
		UserService.recordMyUserId2(UserInfoActivity.this, "");
		Intent i = new Intent(UserInfoActivity.this,
				FacebookOrEmailActivity.class);
		i.putExtra("SIGNUP", true);
		startActivity(i);
		UserInfoActivity.this.finish();
	}

	@Override
	public void onResume() {
		super.onResume();
		getDataTask = new GetDataTask();
		getDataTask.execute(UserInfoActivity.this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.llName_info :
				moveUserNameActivity();
				break;
			case R.id.txtName_info :
				moveUserNameActivity();
				break;
			case R.id.imgprofile :
				profileImgSelectAlert();
				break;
			default :
				break;
		}
	}

	/**
	 * 카메라에서 이미지 가져오기
	 */
	private void doTakePhotoAction() {

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// 임시로 사용할 파일의 경로를 생성
		String url = "tmp_" + String.valueOf(System.currentTimeMillis())
				+ ".png";

		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			// TODO
			// 카메라 촬영 후 임시 변수에 Uri를 저장한다.
			mImageCaptureUri = Uri.fromFile(new File(Constants.SD_PATH, url));

			if (mImageCaptureUri != null) {
				mImageCaptureUriTemp = mImageCaptureUri;
			} else {
				Logger.e(TAG, "mImageCaptureUri NULL!!");
			}
		} else {
			// sd카드가 마운트 되지 않은 상태이면
			Toast.makeText(
					UserInfoActivity.this,
					getResources().getString(
							R.string.sdcard_error_please_insert),
					Constants.DEFAULT_TOAST_DURATION).show();
		}

		intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
				mImageCaptureUri);
		// intent.putExtra("return-data", true);
		startActivityForResult(intent, Constants.PICK_FROM_CAMERA);
	}

	/**
	 * 앨범에서 이미지 가져오기
	 */
	private void doTakeAlbumAction() {
		// 앨범 호출
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
		startActivityForResult(intent, Constants.PICK_FROM_ALBUM);
	}

	// 유저 이름 변경 액티비티로 이동
	private void moveUserNameActivity() {
		Intent i = new Intent(UserInfoActivity.this, UserNameActivity.class);
		i.putExtra(Constants.RESULT_USER_NAME, txtName_info.getText()
				.toString());
		startActivityForResult(i, USER_NAME_RESULT_CODE);
	}

	// 프로필 사진 업로드할 이미지 선택 팝업 띄움
	private void profileImgSelectAlert() {
		DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				doTakePhotoAction();
			}
		};

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

		alert.setTitle(getString(R.string.upload_photo_title));
		alert.setPositiveButton(getString(R.string.upload_photo_from_camera),
				cameraListener);
		alert.setNeutralButton(getString(R.string.upload_photo_from_gallery),
				albumListener);
		alert.setNegativeButton(getString(android.R.string.cancel),
				cancelListener);
		alert.show();
	}

	/**
	 * 데이터를 설정한다.
	 */
	private void loadData() {

		if (mMyUserInfo != null) {
			if(mMyUserInfo.avatar_url!=null && mMyUserInfo.avatar_url.original!=null){
				imageLoader_user.DisplayImage(
						Utils.getServerImageUrl(mMyUserInfo.avatar_url.original),
						imgprofile);
			}
			txtName_info.setText(mMyUserInfo.name);
			
			
			// 2012-11-13 brucewang
			// 로그아웃 기능 구현.
			String strMsg = String.format(UserInfoActivity.this
					.getString(R.string.format_logout_facebook),
					mMyUserInfo.name);
			mBtnLogout.setText(strMsg);
		} else {
			Logger.e(TAG, "mMyUserInfo is Null!!");
		}
	}

	/**
	 * 폰 내부에 사용자 메시지 저장하는 함수
	 * 
	 * @param context
	 * 
	 */
	public void recordMyAcitivyMessage(String str) {
		final SharedPreferences prefs = getSharedPreferences(
				Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		final String aMsg = prefs.getString(
				Constants.PREFS_USER_ACTIVITY_MESSAGE, "");

		if (str != null) {
			prefs.edit().putString(Constants.PREFS_USER_ACTIVITY_MESSAGE, str)
					.commit();
		} else {
			prefs.edit().putString(Constants.PREFS_USER_ACTIVITY_MESSAGE, aMsg)
					.commit();
		}
	}

	/**
	 * 폰 내부에 저장된 사용자 메시지 불러오는 함수
	 * 
	 * @param context
	 * @return Activity Message
	 */
	public String getMyActivityMessage() {
		Resources res = getResources();
		final SharedPreferences prefs = getSharedPreferences(
				Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		String aMsg = prefs.getString(Constants.PREFS_USER_ACTIVITY_MESSAGE,
				res.getString(R.string.default_value));
		return aMsg;
	}

	/**
	 * 액티비티 그룹으로 부터 프로필 사진 변경하도록 전달 받음
	 * 
	 * @param photo
	 */
	public void setResultImgProfile(Bitmap photo) {
		imgprofile.setImageBitmap(photo);
	}

	class GetDataTask extends AsyncTask<Context, String, Boolean> {
		private MyProgressDialog mProgress = null;

		@Override
		protected Boolean doInBackground(Context... params) {
			boolean result = false;

			Context ctx = UserInfoActivity.this;

			SocialServerApis response = SocialServerApis.getInstance(ctx);

			mMyUserInfo = response.chocoGetUserInfo(mStrUserId);

			/**
			 * 사용자의 친구 정보 , 사용자의 게임 정보를 가져온다.
			 */
			if (mMyUserInfo != null) {
				MyUserInfo.getInstance().updateMyUserInfo(mMyUserInfo);
				result = true;
			} else {
				Logger.e(TAG, "User info from server is NULL!");
				mMyUserInfo = new SocialServerApis.ChocoUser();
			}

			return result;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgress = MyProgressDialog.show(UserInfoActivity.this, "", "");

			mProgress.setCancelable(true);
			mProgress.setOnCancelListener(cancelListener);
		}

		protected void onProgressUpdate(String... progress) {
		}

		protected void onPostExecute(Boolean result) {
			if (mProgress != null) {
				mProgress.dismiss();
			}

			if (result) {
				loadData();
			} else {
				Logger.e(TAG, "error cause by : server error");
			}

		}
	}

	public DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {

		@Override
		public void onCancel(DialogInterface dialog) {
			Logger.i(TAG, "UserInfo Task Cancel");
			if (getDataTask != null) {
				getDataTask.cancel(true);
			}
		}
	};

	/**
	 * @author brucewang
	 * 
	 */
	class SendPhotoTask extends AsyncTask<String, String, Boolean> {
		private MyProgressDialog mProgress = null;

		@Override
		protected Boolean doInBackground(String... params) {
			Context ctx = UserInfoActivity.this;

			String strFilePath = params[0];

			SocialServerApis social = SocialServerApis.getInstance(ctx);
			SocialServerApis.ChocoUser user = social.chocoUpdateUser(
					mStrUserId, null, null, strFilePath);

			return (user != null);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			mProgress = MyProgressDialog.show(UserInfoActivity.this, "", "");
		}

		protected void onProgressUpdate(String... progress) {
		}

		protected void onPostExecute(Boolean result) {
			if (mProgress != null) {
				mProgress.dismiss();
			}

			if (result) {
				loadData();
			} else if (img_send_error_code.equals("410")) {
				Toast.makeText(UserInfoActivity.this, img_send_error_msg,
						Constants.DEFAULT_TOAST_DURATION).show();
			}
		}
	}

	/**
	 * @author brucewang
	 * 
	 */
	class ChangeMyUserNameTask extends AsyncTask<String, String, Boolean> {
		private MyProgressDialog mProgress = null;

		@Override
		protected Boolean doInBackground(String... params) {
			Context ctx = UserInfoActivity.this;

			if (mMyUserInfo != null) {
				String strNewUserName = params[0];

				SocialServerApis social = SocialServerApis.getInstance(ctx);
				mMyUserInfo.name = strNewUserName;
//				SocialServerApis.ChocoUser basicuserinfo = mMyUserInfo;

				SocialServerApis.ChocoUser user = social.chocoUpdateUser(
						mStrUserId, strNewUserName, null, null);
				if(user != null){
					mMyUserInfo = user;
				}
				return user != null;
			} else {
				Logger.e(TAG, "My User information is null");
				return false;
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			Activity ctx = UserInfoActivity.this;
			mProgress = MyProgressDialog.show(ctx, "", "");
		}

		protected void onProgressUpdate(String... progress) {
		}

		protected void onPostExecute(Boolean result) {
			if (mProgress != null) {
				mProgress.dismiss();
			}
			if (result) {
				loadData();
				getDataTask = new GetDataTask();
				getDataTask.execute(UserInfoActivity.this);
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}

		super.onActivityResult(requestCode, resultCode, data);

		Logger.e(TAG, "requestcode = " + requestCode);

		if (requestCode == USER_NAME_RESULT_CODE) {
			if (resultCode == RESULT_OK && data != null) {
				String result = data.getStringExtra(Constants.RESULT_USER_NAME);

				Logger.e(TAG, "result = " + result);

				if (result == null || result.length() <= 0) {
					Logger.e(TAG, "Error cause by : user name is null!!");
				} else {
					new ChangeMyUserNameTask().execute(result);
				}
			}
		}
		switch (requestCode) {
			case Constants.CROP_FROM_CAMERA : {

				// 크롭이 된 이후의 이미지를 넘겨 받음 이미지뷰에 이미지를 보여준다거나 부가적인 작업 이후에 임시 파일 삭제
				final Bundle extras = data.getExtras();
				Bitmap photo = null;
				if (extras != null) {
					photo = extras.getParcelable("data");
					// setResultImgProfile(photo);
				}

				File file = null;

				file = new File(getCacheDir(), Constants.PROFILE_IMAGE);

				String state = Environment.getExternalStorageState();
				if (state.contentEquals(Environment.MEDIA_MOUNTED)) {
					file = new File(Constants.SD_PATH
							+ Constants.IMAGE_CACHE_PATH,
							Constants.PROFILE_IMAGE);
				} else {
					file = new File(getCacheDir(), Constants.PROFILE_IMAGE);
				}

				Logger.e(TAG, "fullfilePath = " + file.toString());

				new SendPhotoTask().execute(file.toString());

				FileOutputStream filestream = null;
				try {
					filestream = new FileOutputStream(file);

					// 저장에 성공했다면 이미지 경로를 저장한다.
					Utils.recordMyProFileImagePath(this, file.toString());

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				photo.compress(CompressFormat.PNG, 0, filestream);

				/**
				 * TODO nerine 12-08-20 카메라에서 가져온 uri가 null이라면 이미 저장한 static 변수의
				 * uri를 가져온다
				 */
				if (mImageCaptureUri != null) {
					// 임시 파일 삭제
					File f = new File(mImageCaptureUri.getPath());
					if (f.exists()) {
						// Logger.e(TAG, "tempImagePath = " +
						// mImageCaptureUri.getPath());
						f.delete();
					}
				} else {
					File f = new File(mImageCaptureUriTemp.getPath());
					if (f.exists()) {
						// Logger.e(TAG, "tempImagePath = " +
						// mImageCaptureUriTemp.getPath());
						f.delete();
					}
				}
				// 사용을 하고 난 후에는 static 변수를 다시 초기화 시킨다.
				mImageCaptureUriTemp = null;
				
				getDataTask = new GetDataTask();
				getDataTask.execute(UserInfoActivity.this);
				
				break;
			}

			case Constants.PICK_FROM_ALBUM : {
				// 이후의 처리가 카메라와 같으므로 일단 break없이 진행합니다.
				// 실제 코드에서는 좀더 합리적인 방법을 선택하시기 바랍니다.

				mImageCaptureUri = data.getData();
				// Logger.e(TAG, "strTempImagePath(Album) = " + strTmpImgPath);
			}

			case Constants.PICK_FROM_CAMERA : {

				// 이미지를 가져온 이후의 리사이즈할 이미지 크기를 결정합니다.
				// 이후에 이미지 크롭 어플리케이션을 호출하게 됩니다.

				// Cursor cursor = getContentResolver().query(mImageCaptureUri,
				//
				// null, null, null, null);
				//
				// String strTmpImgPath = "";
				//
				// if (mImageCaptureUri == null) {
				// return;
				// }
				//
				// if (cursor != null) {
				// if (cursor.moveToNext()) {
				// strTmpImgPath =
				// cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
				// }
				// } else {
				// strTmpImgPath = mImageCaptureUri.getPath();
				// }

				Intent intent = new Intent("com.android.camera.action.CROP");

				/**
				 * TODO nerine 12-08-20 카메라로 부터 가져온 Uri가 null일 때 예외처리 이미지 경로를
				 * 특정기기에서 null로 가져오는데 최초 카메라 촬영시 저장한 임시 저장경로를 static으로 아에 가지고
				 * 있도록 처리
				 */
				if (mImageCaptureUri != null) {
					intent.setDataAndType(mImageCaptureUri, "image/*");
				} else {
					intent.setDataAndType(mImageCaptureUriTemp, "image/*");
				}

				intent.putExtra("outputX", 90);
				intent.putExtra("outputY", 90);
				intent.putExtra("aspectX", 1);
				intent.putExtra("aspectY", 1);
				intent.putExtra("scale", true);
				intent.putExtra("return-data", true);

				startActivityForResult(intent, Constants.CROP_FROM_CAMERA);

				break;
			}
		}
	}

	// 20120525_arisu717 - 종료 확인 다이알로그 [[
	private final DialogInterface.OnClickListener mlistenerExitYes = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dlg, int nWhich) {
			UserInfoActivity.this.finish();
		}
	};

	private final DialogInterface.OnClickListener mlistenerExitNo = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dlg, int nWhich) {
			dlg.cancel();
		}
	};

	@Override
	public boolean onKeyDown(int nKeyCode, KeyEvent event) {
		boolean fHandled = false;

		switch (nKeyCode) {
			case KeyEvent.KEYCODE_BACK :
				final AlertDialog.Builder builder = new AlertDialog.Builder(
						this);
				builder.setCancelable(false)
						.setTitle(R.string.dlg_exit_title)
//						.setIcon(R.drawable.ic_launcher)
						.setMessage(R.string.dlg_exit_msg)
						.setPositiveButton(R.string.dlg_exit_yes,
								mlistenerExitYes)
						.setNegativeButton(R.string.dlg_exit_no,
								mlistenerExitNo);
				final Dialog dlg = builder.create();
				dlg.show();
				fHandled = true;
		}

		return (fHandled) ? fHandled : super.onKeyDown(nKeyCode, event);
	}
	// ]]

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, R.string.reload, 0, R.string.reload).setIcon(
				R.drawable.reload);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {			
			case R.string.reload :
				getDataTask = new GetDataTask();
				getDataTask.execute(UserInfoActivity.this);
				return true;
		}
		return false;
	}
}
