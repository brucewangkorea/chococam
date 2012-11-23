package com.chocopepper.chococam.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chocopepper.chococam.R;
import com.chocopepper.chococam.network.SocialServerApis;
import com.chocopepper.chococam.util.Constants;
import com.chocopepper.chococam.util.Logger;
import com.chocopepper.chococam.util.MyProgressDialog;

public class PhotoUploadActivity extends Activity {
	private static final String TAG = Logger.makeLogTag(PhotoUploadActivity.class);
	
	private EditText mEditMessage;
	private EditText mEditTitle;
//	private ImageView imgPreview;	
	
//	/**
//	 * nerine 12-09-17 
//	 * 이미지 리사이징
//	 * @param bm
//	 * @param imgView
//	 * @return
//	 */
//	private float getBitmapScalingFactor(Bitmap bm, ImageView imgView) {
//		// Get display width from device
//		int displayWidth = getWindowManager().getDefaultDisplay().getWidth();
//
//		// Get margin to use it for calculating to max width of the ImageView
//		FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) imgView.getLayoutParams();
//		int leftMargin = layoutParams.leftMargin;
//		int rightMargin = layoutParams.rightMargin;
//
//		// Calculate the max width of the imageView
//		int imageViewWidth = displayWidth - (leftMargin + rightMargin);
//
//		// Calculate scaling factor and return it
//		return ((float) imageViewWidth / (float) bm.getWidth());
//	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upload_photo);

		Intent i = getIntent();
		
		final String video_path = i.getStringExtra("video_path"); 
		
		
		// MainActivity.mImageCaptureUri;
		TextView txtTitle = (TextView) findViewById(R.id.title);
		mEditMessage = (EditText) findViewById(R.id.etxtMessage);
		mEditTitle = (EditText) findViewById(R.id.etxtTitle);
		Button btnSend = (Button) findViewById(R.id.btnSend);
		
		Resources res = getResources();
		txtTitle.setText(res.getString(R.string.upload_photo));

//		imgPreview = (ImageView) findViewById(R.id.imgScreenShot);
//		String strImgFile = MainActivity.mLastPictureFilePath;
//		Bitmap myBitmap = BitmapFactory.decodeFile(strImgFile);
		
//		// Get scaling factor to fit the max possible width of the ImageView
//		float scalingFactor = getBitmapScalingFactor(myBitmap, imgPreview);
//
//		// Create a new bitmap with the scaling factor
//		Bitmap newBitmap = ImageUtil.ScaleBitmap(myBitmap, scalingFactor);
//    	
//		imgPreview.setImageBitmap(newBitmap);
		
		btnSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if( mEditTitle.getText().toString().length()<1 ){
					Toast.makeText(PhotoUploadActivity.this, "제목을 입력해 주세요.", Toast.LENGTH_SHORT).show();
					return;
				}
				if( mEditMessage.getText().toString().length()<1 ){
					Toast.makeText(PhotoUploadActivity.this, "메시지를 입력해 주세", Toast.LENGTH_SHORT).show();
					return;
				}
				
				new UploadVideoTask().execute(video_path, mEditTitle.getText().toString(), mEditMessage.getText().toString());				
			}
		});

	}
	
	class UploadVideoTask extends AsyncTask<String, String, Boolean> {
		private MyProgressDialog mProgress = null;

		@Override
		protected Boolean doInBackground(String... params) {
			boolean result = false;
			String strFilePath = params[0];			
			String strTitle = params[1];
			String strMsg = params[2];

			Context ctx = PhotoUploadActivity.this;

			SocialServerApis mSocialconnection = SocialServerApis.getInstance(ctx);
			List<String> recepients = new ArrayList<String>();
			recepients.add("Afafadfadf");
			recepients.add("BBBBB");
			SocialServerApis.ChocoPost post = mSocialconnection
					.chocoUploadMovie(
							strFilePath, 
							strMsg,
							strTitle,
							"public",
							recepients);
			result = post!=null;

			return result;
		}

		@Override
		protected void onPreExecute() {
			mProgress = MyProgressDialog.show(PhotoUploadActivity.this, "", "");
			super.onPreExecute();
		}

		protected void onProgressUpdate(String... progress) {
		}

		protected void onPostExecute(Boolean result) {
			if (mProgress != null) {
				mProgress.dismiss();
			}			
			PhotoUploadActivity.this.finish();
		}
	}// end of AsyncTask
}
