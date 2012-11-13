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
package com.chocopepper.chococam.activity.friends;

import java.util.ArrayList;

import lib.pulltorefresh.PullToRefresh;
import lib.pulltorefresh.PullToRefreshBase.OnRefreshListener;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chocopepper.chococam.R;
import com.chocopepper.chococam.activity.GFragment;
import com.chocopepper.chococam.activity.OnPullToRefreshTouchListener;
import com.chocopepper.chococam.activity.friends.manage.FriendsActivity;
import com.chocopepper.chococam.dao.UserService;
import com.chocopepper.chococam.network.SocialServerApis;
import com.chocopepper.chococam.util.Constants;
import com.chocopepper.chococam.util.ImageViewRounded;
import com.chocopepper.chococam.util.Logger;
import com.chocopepper.chococam.util.MyProgressDialog;

public class FriendsFeedActivity extends GFragment implements OnItemClickListener,  OnClickListener, OnPullToRefreshTouchListener{
	
	public static Activity gfContext = null;
	
	private boolean isMsgCheck = false;//메세지를 보여야 할 지 여부
	
	private static final String TAG = Logger
			.makeLogTag(FriendsFeedActivity.class);
	
//	private boolean nextPageCheck = false;
	private int pageCount = 1;

	LinearLayout llmsg;
	TextView txtContent;
	ImageViewRounded imgUser;
	
	private PullToRefresh refreshListView = null;
	FeedsListAdapter mAdatper;
	ArrayList<SocialServerApis.ChocoFeed> mFeeds = new ArrayList<SocialServerApis.ChocoFeed>();
	
	SocialServerApis mSocialconnection = null;
	
	GetDataTask getDataTask;
	
	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
            return null;
        }
		View v = inflater.inflate(R.layout.gamefeeds, container, false);
		refreshListView = (PullToRefresh) v.findViewById(R.id.refreshListView);
		llmsg = (LinearLayout)v.findViewById(R.id.llmsg);
		txtContent = (TextView)v.findViewById(R.id.txtContent);
		imgUser = (ImageViewRounded)v.findViewById(R.id.imgUser);
		return v;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
		
		gfContext = this.getActivity();
		
		mSocialconnection = SocialServerApis.getInstance(gfContext);

		llmsg.bringToFront();
		llmsg.setOnClickListener(this);
		llmsg.setVisibility(View.GONE);
		
		mAdatper = new FeedsListAdapter(gfContext, mFeeds);
		refreshListView.setAdapter(mAdatper);
		
		
		/*
		 * 2012-08-14 brucewang
		 * '친구가 없습니다' 라는 메시지를 보여주고/숨기기 위한 처리 코드.
		 */
		refreshListView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				FriendsFeedActivity.this.isTouch(false);
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				FriendsFeedActivity.this.isTouch(true);
			}
		});
		
		
		
		
		
		refreshListView.setOnItemClickListener(this);		
		((PullToRefresh) refreshListView).setOnRefreshListener(new OnRefreshListener() {
			public void onRefresh() {
				// Do work to refresh the list here.
				
				if(refreshListView.getCurrentMode() == lib.pulltorefresh.PullToRefreshBase.Mode.PULL_UP_TO_REFRESH )
				{		
					pageCount++;
					getDataTask = new GetDataTask();
					getDataTask.execute(String.format("%d", pageCount));
//					//아래쪽 새로고침 추가
//					if(nextPageCheck)
//					{
//						
//					}				
//					else
//					{
//						((PullToRefresh) refreshListView).onRefreshComplete();
//					}
					
					Logger.e(TAG, "page count = " + pageCount);
				}
				else if(refreshListView.getCurrentMode() == lib.pulltorefresh.PullToRefreshBase.Mode.PULL_DOWN_TO_REFRESH)
				{					
					pageCount = 1;
					//기존 헤더쪽 refresh
					getDataTask = new GetDataTask();
					getDataTask.execute(String.format("%d", pageCount));
				}
			}
		});

		
		getDataTask = new GetDataTask();
		getDataTask.execute(String.format("%d", pageCount));
		
	}
	
    

//	@Override
//	protected void onResume() {
//		
//		Logger.e(TAG, "onResume...");
//		
//		getDataTask = new GetDataTask();
//		getDataTask.execute(String.format("%d", pageCount));
//
//		super.onResume();		
//	}
	

//	@Override
//	public void onStart() {
////		getDataTask = new GetDataTask();
////		getDataTask.execute(String.format("%d", pageCount));
//		
//		super.onStart();
//	}
	
	@Override
	public void OnMyResume(){
		if(mbActivityCreated){
			getDataTask = new GetDataTask();
			getDataTask.execute(String.format("%d", pageCount));
		}
	}


	class GetDataTask extends AsyncTask<String, String, Boolean> {
		private MyProgressDialog mProgress=null;
		
		@Override
		protected Boolean doInBackground(String... params) {
			boolean result = false;
			
			String str_page_count = params[0];
			
			int page_count = Integer.parseInt(str_page_count);
			
			ArrayList<SocialServerApis.ChocoFeed> responseList = mSocialconnection.chocoGetFeeds(page_count);
			
			// 12-10-15 nerine
			// 다음 페이지가 있는지 체크하는 리스트를 넣는다.
//			mNextList = mSocialconnection.chocoGetFeeds(page_count + 1);
//			
//			if(mNextList.size() <= 0)
//			{
//				nextPageCheck = false;
//			}
//			else
//			{
//				nextPageCheck = true;
//			}
			
			if (responseList == null) {				
				mFeeds = new ArrayList<SocialServerApis.ChocoFeed>();				
			}else{				
				if(page_count == 1)
				{
					mFeeds = new ArrayList<SocialServerApis.ChocoFeed>();					
					for(int i = 0; i < responseList.size();i++)
					{
						SocialServerApis.ChocoFeed feed = responseList.get(i);
						
						//데이터를 넣는다.
//						if(feed.getGame_id()>0){
//						if(feed.post!=null){				
//							mFeeds.add(feed);
//						}
						
						
						if(feed.action.action_type != 2 || feed.action.action_type != 3)
						{
							
							if(feed.action.action_type == 1 && feed.like == null)
							{
								//일단 null체크 이사님께 말씀드려서 잘못된 피드 삭제 요청
							}
							else
							{
								mFeeds.add(feed);
							}
						}
					}
				}
				else
				{
					ArrayList<SocialServerApis.ChocoFeed> tempArray = new ArrayList<SocialServerApis.ChocoFeed>();
					tempArray = responseList;
					
					for(int i = 0; i < tempArray.size();i++)
					{
						SocialServerApis.ChocoFeed feed = tempArray.get(i);
						//데이터를 넣는다.
//						if(feed.post!=null){
//							mFeeds.add(feed);
//						}
							
						if (feed.action.action_type != 2 || feed.action.action_type != 3) 
						{
							if(feed.action.action_type == 1 && feed.like == null)
							{
								//일단 null체크 이사님께 말씀드려서 잘못된 피드 삭제 요청
							}
							else
							{
								mFeeds.add(feed);
							}
						}
					}
				}
			}
			
			
			// TODO
			// 테스트용 로그 추후 삭제 필요
			Logger.e(TAG, "page count = " + page_count);
			Logger.e(TAG, "responseList count = " + responseList.size());
			Logger.e("","count = "+mFeeds.size());
			
			return result;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgress = MyProgressDialog.show(FriendsFeedActivity.this.getActivity(), "", "");
			llmsg.setVisibility(View.GONE);
			
			mProgress.setCancelable(true);
			mProgress.setOnCancelListener(cancelListener);
		}

		protected void onProgressUpdate(String... progress) {
		}

		protected void onPostExecute(Boolean result) {
			if(mProgress!=null){ mProgress.dismiss(); }
			
			/**
			 * nerine 12-07-13
			 * 친구 탭에 보여줄 메시지 정리
			 * 
			 * 1. 친구 피드도 없고 내 피드도 없다.(친구 초청 메세지)
			 * 2. 내 피드는 있고 친구피드는 없다.(친구 초청 메세지)
			 * 3. 친구 피드는 있으나 내 피드는 없다.(메세지 필요 없음)
			 * 4. 친구 피드가 있었으나 친구와의 관계가 끊어져(탈퇴 포함) 친구 피드가 없다.(친구 초청 메세지)
			 * 5. 내 피드 또는 친구피드가 있었으나 외부 요인으로 데이터가 없다.(네트워크 오류 메세지)
			 */
			
			if(mFeeds.size() == 0)//1번 또는 5번의 경우
			{				
				if(getFriendsList())//5번 일 때 
				{
					txtContent.setText(getResources().getString(R.string.reload_msg));
					imgUser.setVisibility(View.GONE);
				}
				else//1번 일 때
				{
					txtContent.setText(getResources().getString(R.string.gamefeeds_empty_msg));
					imgUser.setVisibility(View.VISIBLE);
				}
				
				llmsg.setVisibility(View.VISIBLE);
				isMsgCheck = false;
			}
			else if(mFeeds.size() == 1)//2번 또는 3번의 경우
			{
				// mFeeds.get(0).post.user_id를  mFeeds.get(0)._id로 변경
				// post가 null로 들어오는 경우가 있기 때문에 feed의 id로 변경 한다.
				if(mFeeds.get(0).post!=null && mFeeds.get(0)._id.equals(String.format("%d", UserService.getDefaultUserId(gfContext))))//2번의 경우
				{
					llmsg.setVisibility(View.VISIBLE);
					setFriendsList();
					isMsgCheck = true;
				}
				else//3번의 경우
				{
					llmsg.setVisibility(View.GONE);
					isMsgCheck = false;
				}
			}
			else//나머지 경우는 전부 숨긴다.
			{
				llmsg.setVisibility(View.GONE);
				isMsgCheck = false;
			}
			

			// 피드가 갱신되었다는것을 알려주면서 곧바로 알아서 'notifyDataSetChanged'
			// 를 호출하도록 바꾸었습니다. 필요한 View를 미리 만들어버리게...
			mAdatper.setFeedData(mFeeds);			
			mAdatper.notifyDataSetChanged();
			
			refreshListView.setMode(lib.pulltorefresh.PullToRefreshBase.Mode.BOTH);
			
//			// 다음 페이지가 있다면
//			if(nextPageCheck)
//			{
//				
//			}
//			else
//			{
//				refreshListView.setMode(lib.pulltorefresh.PullToRefreshBase.Mode.PULL_DOWN_TO_REFRESH);
//			}
			
			// Call onRefreshComplete when the list has been refreshed.
			((PullToRefresh) refreshListView).onRefreshComplete();
			
			
		}
	}

	public DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {

		@Override
		public void onCancel(DialogInterface dialog) {
			Logger.i(TAG, "GameFeed Task Cancel");
			
			if (getDataTask != null) {	
				// Call onRefreshComplete when the list has been refreshed.
				((PullToRefresh) refreshListView).onRefreshComplete();
				
				getDataTask.cancel(true);
			}
			
		}
	};
	
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Logger.e(TAG, String.format("%d", arg2));
		
	}
	
	/**
	 * 이모티콘이 팝업에서 변경 될 시 호출하여 UI를 업데이트 한다. 
	 * @param result
	 * @param position
	 * @param emoticon_id
	 */
	public void uiUpdate(Boolean result, String strSvrMsg, int position)
	{
//		Logger.e(TAG, "emo send result = " + result + "//" + "positon = " + position);
		
		//서버에서 내려온 결과가 성공이라면
		if(result)
		{
			//mAdatper.setEmoticon(position, emoticon_id);
			mAdatper.notifyDataSetChanged();
			
			getDataTask = new GetDataTask();
			getDataTask.execute(String.format("%d", pageCount));
		}
		else
		{
			Toast.makeText(gfContext, strSvrMsg, Constants.DEFAULT_TOAST_DURATION).show();
		}
		
	}




	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llmsg:
			Intent i = new Intent(gfContext, FriendsActivity.class);
			i.putExtra(FriendsActivity.PAGER_INDEX, FriendsActivity.VIEW_RECOMMENDED_FRIENDS);
			startActivity(i);
			break;

		default:
			break;
		}
	}
	
	
	public void setFriendsList() {
		final SharedPreferences prefs = gfContext.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		prefs.edit().putBoolean(Constants.FRIENDS_FEED_LIST_CHECK, true).commit();
	}
	
	public boolean getFriendsList(){
		boolean result = false;
		final SharedPreferences prefs = gfContext.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		result = prefs.getBoolean(Constants.FRIENDS_FEED_LIST_CHECK, false);		
		return result;
	}

	@Override
	public void isTouch(boolean isTouching) {
		if(llmsg != null)
		{
			if(isMsgCheck)//true면 보이는 상태
			{
				if(isTouching)
				{
					llmsg.setVisibility(View.GONE);
				}
				else
				{
					llmsg.setVisibility(View.VISIBLE);
				}
			}
		}
	}


}