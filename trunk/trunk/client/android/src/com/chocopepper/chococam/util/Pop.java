package com.chocopepper.chococam.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

import com.chocopepper.chococam.R;
import com.chocopepper.chococam.activity.friends.FeedsFragmentActivity;
import com.chocopepper.chococam.network.SocialServerApis;

public class Pop extends PopView implements OnClickListener {
	SocialServerApis mSocialconnection = null;
	
	private int position = -1;
	
	Activity parentContext = null;
	private final Context context;
	private final LayoutInflater inflater;
	private final View root;
	private ViewGroup mTrack;
	private String callActivity = "";

	// 위젯 설정	
	private final Button btnLike;
	
	private String mPostId="1";

	public Pop(View anchor, Activity parent, int position, String callActivity){
		super(anchor);
		
		this.position = position;
		
		this.callActivity = callActivity;
		
		parentContext = parent;		
		
		
		// 12-10-15 nerine
		// 인기 포스트를 불러 올때는 리턴이 포스트 형식으로 데이터를 넘기기 때문에 id를 피드가 아닌 포스트로 받아온다.
		if(callActivity.equals("PopularFeedsActivity"))
		{
			SocialServerApis.ChocoPost postData = (SocialServerApis.ChocoPost)anchor.getTag();
			mPostId = postData._id;
		}
		else
		{
			SocialServerApis.ChocoFeed postData = (SocialServerApis.ChocoFeed)anchor.getTag();
			mPostId = postData.post._id;
		}
		
		// TODO
		// 포스트의 아이디 확인
		Logger.e("pop", "postid = " + mPostId);
		
		context = anchor.getContext();

		mSocialconnection = SocialServerApis.getInstance(anchor.getContext());

		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		root = (ViewGroup) inflater.inflate(R.layout.popview, null);
		setContentView(root);
		mTrack = (ViewGroup) root.findViewById(R.id.viewRow);

		// 위젯 설정		
		btnLike = (Button) mTrack.findViewById(R.id.btnLike);
		btnLike.setOnClickListener(this);
	
	}
	
//	public Pop(View anchor){
//		super(anchor);
//		
//		mPostId = anchor.getTag().toString();
//		
//		Logger.e("pop", "mPostId = " + mPostId);
//		
//		context = anchor.getContext();
//				
//		mSocialconnection = SocialServerApis.getInstance(anchor.getContext());
//		
//		inflater = (LayoutInflater) context
//				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		
//		root = (ViewGroup) inflater.inflate(R.layout.popview, null);
//		setContentView(root);
//		mTrack = (ViewGroup) root.findViewById(R.id.viewRow);
//
//		// 위젯 설정		
//		btnLike = (Button) mTrack.findViewById(R.id.btnLike);	
//		btnLike.setOnClickListener(this);
//	
//	}

	public void show() {
		preShow();

		int rootWidth;
		int rootHeight;
		int xPos, yPos;
		int[] location = new int[2];
		anchor.getLocationOnScreen(location);

		Rect anchorRect = new Rect(location[0], location[1], location[0]
				+ anchor.getWidth(), location[1] + anchor.getHeight());

		root.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		rootHeight = root.getMeasuredHeight();
		rootWidth = root.getMeasuredWidth();

		double dipRootWidth = rootWidth * 0.66;
		double dipRootHeight = (rootHeight / 2) * 0.4; 
		xPos = anchorRect.left - (int)dipRootWidth;	
		yPos = anchorRect.top - (int)dipRootHeight;

		root.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		root.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		window.showAtLocation(this.anchor, Gravity.NO_GRAVITY, xPos, yPos);
	}
	
	/*
	 * 2012-05-26 brucewang
	 * 네트워크 작업이 완료되었을 때 호출되는 함수들을 위한 interface
	 */
	public static interface EmoticonTaskListener {
        public void onComplete(int code, String errormessage);
    }
//	private EmoticonTaskListener mEmoticonTaskListener=null;
//	public void setEmoticonTaskListener(EmoticonTaskListener listener){
//		mEmoticonTaskListener = listener;
//	}

	@Override
	public void onClick(View v) {		
		switch (v.getId()) {			
			case R.id.btnLike : // Like				
				sendLikeRequestToServer(mPostId);
				break;
			default :
				break;
		}
	}

	
	/**
	 * 2012-10-15 nerine
	 * 서버에 좋아요 정보를 전송합니다.
	 * 
	 * @param post_feed_id
	 */
	private void sendLikeRequestToServer(String post_feed_id) 
	{
		_ServerCommTaskData param = new _ServerCommTaskData();		
		param.post_feed_id = post_feed_id;		
		new SendEmoticonTask().execute(param);		
	}
	
	class _ServerCommTaskData {
		String post_feed_id;
		// : 포스트 내용. (can be null. null이면 내용 없음을 의미)		
	}
	
	class SendEmoticonTask extends AsyncTask<_ServerCommTaskData, String, Boolean> {
		private String mErrString = "";
		
		@Override
		protected Boolean doInBackground(_ServerCommTaskData... params) {
			boolean result = false;
			_ServerCommTaskData param = params[0];

			result = mSocialconnection.chocoLikePost(param.post_feed_id);
			
			return result;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();			
			
		}

		protected void onProgressUpdate(String... progress) {
		}

		protected void onPostExecute(Boolean result) {			
			Resources res = context.getResources();
			
			if(result)
			{
				Logger.e("Pop", "라이크 등록 성공");
			}
			else
			{
				Logger.e("Pop", "라이크 등록 실패");
				// 이미 라이크를 등록한 포스트 일 경우
				mErrString = res.getString(R.string.already_liked_it);
			}
			/**
			 * 게임피드 액티비티로 위치와 이모티콘 아이디를 전달한다.
			 */
			
			if(parentContext != null)
			{	
				if(callActivity.equals("FriendsFeedActivity") || callActivity.equals("PopularFeedsActivity"))
				{
					((FeedsFragmentActivity)parentContext).uiUpdateSubPage(result, mErrString, position);
				}				
				else
				{}
				
			}			
			Pop.this.dismiss();
		}
	}
}
