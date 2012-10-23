/*
 * Copyright (C) 2011 ChocoPepper Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chocopepper.chococam.activity.friends.manage;

import java.util.ArrayList;
import java.util.List;

import lib.pulltorefresh.PullToRefresh;
import lib.pulltorefresh.PullToRefreshBase.OnRefreshListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.chocopepper.chococam.R;
import com.chocopepper.chococam.activity.friends.FeedsListAdapter;
import com.chocopepper.chococam.activity.friends.PopularPostsListAdapter;
import com.chocopepper.chococam.model.MyUserInfo;
import com.chocopepper.chococam.model.MyUserInfo.MyUserInfoListener;
import com.chocopepper.chococam.network.SocialServerApis;
import com.chocopepper.chococam.util.Constants;
import com.chocopepper.chococam.util.DateUtil;
import com.chocopepper.chococam.util.ImageLoader;
import com.chocopepper.chococam.util.ImageLoader.OnLoadFinishedListener;
import com.chocopepper.chococam.util.ImageViewRounded;
import com.chocopepper.chococam.util.Logger;
import com.chocopepper.chococam.util.MyProgressDialog;
import com.chocopepper.chococam.util.Utils;

public class ReplyActivity extends Activity implements OnItemClickListener,
		OnClickListener, OnFocusChangeListener {
	
	// 댓글 액티비트를 호출하는 어댑터가 친구와 인기 두 곳에서 호출 하기 때문에 호출 하는 뷰를 분기로 나눠서 데이터를 가져오기위한 플래그
	boolean isFeedsListAdapter = false;
	
	SocialServerApis.ChocoPost post = null;
	SocialServerApis.ChocoComment resultComment = null;
	
	// 실제 어댑터에 적용될 리스트
	private List<SocialServerApis.ChocoComment> commentList = new ArrayList<SocialServerApis.ChocoComment>();
	
	// 데이터 임시 보관용 리스트
	private List<SocialServerApis.ChocoComment> mCommentList = new ArrayList<SocialServerApis.ChocoComment>();
	
	public ImageLoader imageLoaderUser;
	public ImageLoader imageLoaderGame;
	private ImageLoader imageLoader_screenshot;
	private SocialServerApis.ChocoUser mMyUserInfo;
	LayoutInflater mInflater;

	private static final String TAG = Logger.makeLogTag(ReplyActivity.class);

	private int pageCount = 1;

	// 리스트 설정
	ReplyAdapter mAdatper;		
	SocialServerApis mSocialconnection = null;

	// 위젯 설정
	EditText m_etxtMessage;
	Button m_btnSend;
	PullToRefresh m_replyListView;
	TextView m_txtUserName;
	TextView m_txtTitle;
	TextView m_txtDate;
	ImageViewRounded m_imgUser;	
	ImageView m_img_screenshot;
	TextView m_textViewDescription;
	ScrollView mScrollView;	

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reply);

		mInflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				
		// 12-10-12 nerine
		// 댓글 액티비트를 호출하는 어댑터가 친구와 인기 두 곳에서 호출 하기 때문에 호출 하는 뷰를 분기로 나눠서 데이터를 가져온다.		
		// 테스트 시 포스트 아이디가 같은지 로그를 확인해야한다.
		Intent i = getIntent();
		isFeedsListAdapter = i.getBooleanExtra("isFeedsList", false);
		postDataUpdate();
		
	
		imageLoaderUser = new ImageLoader(ReplyActivity.this.getApplicationContext());
		imageLoaderGame = new ImageLoader(ReplyActivity.this.getApplicationContext());
		
		imageLoaderUser.setStubImageResourceId(R.drawable.default_user);
		imageLoaderGame.setStubImageResourceId(R.drawable.game_icon_gray);
		
		imageLoader_screenshot = new ImageLoader(
				ReplyActivity.this.getApplicationContext());
		imageLoader_screenshot.setAutomaticRotateImage(false);
		imageLoader_screenshot.setImageResize(true);
		
		/*
		 * 2012-08-18 brucewang
		 * image loading이 완료되면 ScrollView 의 사이즈를 변경하도록 한다.
		 */
		imageLoader_screenshot.setOnLoadFinishedListener(new OnLoadFinishedListener() {
			@Override
			public void whenLoadFinished(ImageView imageView, Bitmap bitmap) {
				Display display = getWindowManager().getDefaultDisplay();
				int w_height = display.getHeight();
				
				int height = mScrollView.getHeight() + bitmap.getHeight();
				if( height > w_height/2 ){
					height = w_height/2;
					LayoutParams param = mScrollView.getLayoutParams();
					param.height = height;
					//mScrollView.setLayoutParams(param);
				}
			}
		});
		

		TextView txtTitle = (TextView) findViewById(R.id.title);
		m_txtUserName = (TextView) findViewById(R.id.TextViewName);	
		m_txtTitle = (TextView) findViewById(R.id.TextViewTitle);
		m_txtDate = (TextView) findViewById(R.id.TextViewDate);
		m_imgUser = (ImageViewRounded) findViewById(R.id.imgUser);		
		
		

		Resources res = getResources();
		
		txtTitle.setText(res.getString(R.string.reply));
		
		m_etxtMessage = (EditText) findViewById(R.id.etxtMessage);
		m_etxtMessage.setInputType(0);
		m_etxtMessage.setOnClickListener(this);
		m_etxtMessage.setOnFocusChangeListener(this);
		
		
		m_btnSend = (Button) findViewById(R.id.btnSend);
		m_btnSend.setOnClickListener(this);

		mSocialconnection = SocialServerApis.getInstance(this);
		m_replyListView = (PullToRefresh) findViewById(R.id.replyListView);
		mAdatper = new ReplyAdapter(this, commentList);
		m_replyListView.setAdapter(mAdatper);
		m_replyListView.setOnItemClickListener(this);
		
		
		m_img_screenshot = (ImageView)findViewById(R.id.imgScreenShot);
		m_textViewDescription = (TextView)findViewById(R.id.textViewDescription);
		mScrollView = (ScrollView)findViewById(R.id.scrollView1);
				
		// 최초에 한번 실행했을때 서버에서 다음 페이지가 있는지 확인되면 both로 변경
		// m_replyListView.setMode(lib.pulltorefresh.PullToRefreshBase.Mode.BOTH);
		((PullToRefresh) m_replyListView).setOnRefreshListener(new OnRefreshListener() {
			public void onRefresh() {
				// Do work to refresh the list here.
				
				if(m_replyListView.getCurrentMode() == lib.pulltorefresh.PullToRefreshBase.Mode.PULL_UP_TO_REFRESH )
				{		
					pageCount++;
					new GetFeedDetailTask().execute(post._id, String.format("%d", pageCount));
				}
				else if(m_replyListView.getCurrentMode() == lib.pulltorefresh.PullToRefreshBase.Mode.PULL_DOWN_TO_REFRESH)
				{
					pageCount = 1;
					//기존 헤더쪽 refresh
					new GetFeedDetailTask().execute(post._id, String.format("%d", pageCount));					
				}
			}
		});

		// 자신의 정보를 불러옴.
		MyUserInfo.getInstance().getMyUserInfo(ReplyActivity.this, false,
				new MyUserInfoListener() {
					@Override
					public void whenMyInfoIsReceived(SocialServerApis.ChocoUser myinfo) {
						if (myinfo != null) {
							mMyUserInfo = myinfo;							
//							new GetFeedBasicTask().execute(mFeedId);
							new GetFeedDetailTask().execute(post._id, String.format("%d", pageCount));						
							updateBasicFeedUi();							
						}
					}
				});
	}
	
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		switch (v.getId()) {
		case R.id.etxtMessage:
			m_etxtMessage.setInputType(1);
			InputMethodManager imm1 = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
			imm1.showSoftInput(m_etxtMessage, InputMethodManager.SHOW_IMPLICIT);
			break;
		default:
			break;
		}
		
	
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnSend:
			// 20120528_khan - 빈 글이 전송됨[[
			String replyMsg = m_etxtMessage.getText().toString();
			if (replyMsg != null && replyMsg.trim().length() > 0) {
				sendReplyToServer(post._id, m_etxtMessage.getText().toString(), null);
			}
			// ]]
			
			// 20120522_arisu717 - 친구: 댓글 전송 후 edit에 글이 남아 있음 [[
			m_etxtMessage.setText("");
			
			/**
			 * nerine 12-05-30
			 * 댓글 전송 후 키보드 내림
			 */
			InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(m_etxtMessage.getWindowToken(), 0);
			
			
			m_replyListView.postDelayed(new Runnable() {
				@Override
				public void run() {
					
					//Logger.e(TAG, "onClick edit Message..." + mAdatper.getCount());
					
					m_replyListView.setSelection(mAdatper.getCount());
				}
			}, 1000);
			// ]]
			break;
		case R.id.etxtMessage:
			m_etxtMessage.setInputType(1);
			InputMethodManager imm1 = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
			imm1.showSoftInput(m_etxtMessage, InputMethodManager.SHOW_IMPLICIT);			
			break;
		default:
			break;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		//Logger.e(TAG, String.format("%d", arg2));
	}

	private void sendReplyToServer(String post_id,
			String description, String reply_to_id) {
		_ServerCommTaskData param = new _ServerCommTaskData();
		param.description = description;
		param.post_id = post_id;
		param.reply_to_id = reply_to_id;	
		new SendReplyTask().execute(param);
	}
	
	private void updateBasicFeedUi(){		
		String str_user_name = post.user.name;
		String str_title = post.movie.name;
		String str_user_photo = post.user.avatar_url.thumbnail;
		String str_feed_date = post.created_at;
	
		String strDescription = post.description;		
		String strScreenshotUrl = post.movie.file_url.thumbnail;
		if ((strScreenshotUrl != null && strScreenshotUrl.length() > 0)
				|| (strDescription != null && strDescription.length() > 0)) {
			if (strDescription != null) {				
				m_textViewDescription.setText(strDescription);				
			}
			if (strScreenshotUrl != null) {
				m_textViewDescription.setVisibility(View.VISIBLE);
				m_img_screenshot.setVisibility(View.VISIBLE);				
				imageLoader_screenshot.DisplayImage(
						Utils.getServerImageUrl(strScreenshotUrl),
						m_img_screenshot,
						this);
			}			
		} else {
			m_textViewDescription.setVisibility(View.GONE);
			m_img_screenshot.setVisibility(View.GONE);			
		}
		
		m_txtUserName.setText(str_user_name);
		m_txtTitle.setText(str_title);
		
		String strDateFromServer = str_feed_date;
		strDateFromServer = DateUtil.getDifferentTime(strDateFromServer);
		m_txtDate.setText(strDateFromServer);

		imageLoaderUser.DisplayImage(Utils.getServerImageUrl(str_user_photo),m_imgUser);

	}
	
	class _ServerCommTaskData {
		String post_id;
		String description;
		String reply_to_id;
	}
	
	// 댓글 달기 
	class SendReplyTask extends AsyncTask<_ServerCommTaskData, String, Boolean> {
		private MyProgressDialog mProgress = null;
		private _ServerCommTaskData mParam = null;

		@Override
		protected Boolean doInBackground(_ServerCommTaskData... params) {
			boolean bOk = false;
			mParam = params[0];

			resultComment = mSocialconnection.chocoCommentWriteToPost(
					mParam.description, 
					mParam.post_id, 
					mParam.reply_to_id);  
							
			return bOk;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (!ReplyActivity.this.isFinishing()) {
				mProgress = MyProgressDialog.show(ReplyActivity.this, "", "");
			}
		}

		protected void onProgressUpdate(String... progress) {
		}

		protected void onPostExecute(Boolean result) {
			if (mProgress != null) {
				mProgress.dismiss();
			}
			
			if (resultComment != null) {
				mAdatper.setFeedData(commentList);
				mAdatper.notifyDataSetChanged();
				// 모델 별로 메모리에 할당해야함
				SocialServerApis.ChocoComment feed = new SocialServerApis.ChocoComment();
				feed.user = new SocialServerApis.ChocoUser();				
				feed.user.avatar_url = new SocialServerApis.ChocoAvatarUrl();
				
				feed.description = mParam.description;
				feed._id = mMyUserInfo._id;
				feed.user.name = mMyUserInfo.name;
				feed.user.avatar_url.thumbnail = mMyUserInfo.avatar_url.thumbnail;
				feed.created_at = DateUtil.getCurrentTimeWithoutNoon();

				/**
				 * nerine 12-05-30 추가된것은 맨 아래로 들어가도록 수정 aca : mGameFeeds.add(0,
				 * feed);
				 */
				commentList.add(feed);
				mAdatper.notifyDataSetChanged();
			} else {
				Resources res = getResources();
				Toast.makeText(ReplyActivity.this,
						res.getString(R.string.server_connect_error),
						Constants.DEFAULT_TOAST_DURATION).show();
			}
		}
	}
	
	// 댓글 정보 가져오기
	class GetFeedDetailTask extends AsyncTask<String, String, Boolean> {
		private MyProgressDialog mProgress = null;

		@Override
		protected Boolean doInBackground(String... params) {
			boolean result = false;
			String str_post_id = params[0];
			String str_feed_page = params[1];
			int feed_page = Integer.parseInt(str_feed_page);
			
			mCommentList = mSocialconnection.chocoCommentListForPost(str_post_id, feed_page);
			
			// TODO
			// 테스트용 로그 추후 삭제 필요
			Logger.e(TAG, "page count = " + str_feed_page);
			Logger.e(TAG, "mCommentList count = " + mCommentList.size());
			
			return result;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if( !ReplyActivity.this.isFinishing() ){
				mProgress = MyProgressDialog.show(ReplyActivity.this, "", "");
			}
		}

		protected void onProgressUpdate(String... progress) {
		}

		protected void onPostExecute(Boolean result) {
			if (mProgress != null) {
				mProgress.dismiss();
			}			
			
			if (mCommentList != null
					&& mCommentList.size() > 0) {
				
				if(pageCount == 1)
				{					
					commentList = mCommentList;
				}
				else
				{
					List<SocialServerApis.ChocoComment> tempArray = new ArrayList<SocialServerApis.ChocoComment>();
					tempArray = mCommentList;
					
					for(int i = 0; i < tempArray.size();i++)
					{
						//데이터를 넣는다.
						commentList.add(tempArray.get(i));
					}
				}
				
				mAdatper.setFeedData(commentList);
				mAdatper.notifyDataSetChanged();
			}
			

			((PullToRefresh) m_replyListView).onRefreshComplete();
			if(mProgress!=null){ mProgress.dismiss(); }
			
			
			m_replyListView.setMode(lib.pulltorefresh.PullToRefreshBase.Mode.BOTH);
			m_replyListView.postDelayed(new Runnable() {
				@Override
				public void run() {										
					m_replyListView.setSelection(mAdatper.getCount());
				}
			}, 1000);
			
		}
	}
	
	// 포스트 데이터를 업데이트한다.
	private void postDataUpdate()
	{
		if(isFeedsListAdapter)
		{
			post = FeedsListAdapter.getFeedData().post;
		}
		else
		{
			post = PopularPostsListAdapter.getPostData();
		}		
		
		if(post != null)
		{
			Logger.e(TAG, "post id = " + post._id);			
		}			
	}
}