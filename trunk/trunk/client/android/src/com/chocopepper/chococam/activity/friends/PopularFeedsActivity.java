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
import android.content.DialogInterface;
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
import com.chocopepper.chococam.network.SocialServerApis;
import com.chocopepper.chococam.util.Constants;
import com.chocopepper.chococam.util.ImageViewRounded;
import com.chocopepper.chococam.util.Logger;
import com.chocopepper.chococam.util.MyProgressDialog;

public class PopularFeedsActivity extends GFragment implements OnItemClickListener,  OnClickListener, OnPullToRefreshTouchListener{
	
	public static Activity gfContext = null;
	
	private boolean isMsgCheck = false;//메세지를 보여야 할 지 여부
	
	private static final String TAG = Logger
			.makeLogTag(PopularFeedsActivity.class);

	private int pageCount = 1;

	LinearLayout llmsg;
	TextView txtContent;
	ImageViewRounded imgUser;
	
	private PullToRefresh refreshListView = null;
	PopularPostsListAdapter mAdatper;
	ArrayList<SocialServerApis.ChocoPost> mPosts = new ArrayList<SocialServerApis.ChocoPost>();
	// 다음 페이지 체크용 리스트
//	ArrayList<SocialServerApis.ChocoPost> mNextList = new ArrayList<SocialServerApis.ChocoPost>();
	
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
		
		mAdatper = new PopularPostsListAdapter(gfContext, mPosts);
		refreshListView.setAdapter(mAdatper);
		
		/*
		 * 2012-08-14 brucewang
		 * '친구가 없습니다' 라는 메시지를 보여주고/숨기기 위한 처리 코드.
		 */
		refreshListView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				PopularFeedsActivity.this.isTouch(false);
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				PopularFeedsActivity.this.isTouch(true);
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
					//아래쪽 새로고침 추가
//					if(nextPageCheck)
//					{
//						
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
//		Logger.e(TAG, "OOOOOON START");
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
			
			ArrayList<SocialServerApis.ChocoPost> responseList = mSocialconnection.chocoGetPopularPosts(page_count);
			
//			// 12-10-15 nerine
//			// 다음 페이지가 있는지 체크하는 리스트를 넣는다.
//			mNextList = mSocialconnection.chocoGetPopularPosts(page_count + 1);
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
				mPosts = new ArrayList<SocialServerApis.ChocoPost>();
			}else{
				
				if(page_count == 1)
				{
					mPosts = new ArrayList<SocialServerApis.ChocoPost>();
					for(int i = 0; i < responseList.size(); i++)
					{
						SocialServerApis.ChocoPost feed = responseList.get(i);
						//데이터를 넣는다.
						mPosts.add(feed);
					}
				}
				else
				{
					ArrayList<SocialServerApis.ChocoPost> tempArray = new ArrayList<SocialServerApis.ChocoPost>();
					tempArray = responseList;
					
					for(int i = 0; i < tempArray.size();i++)
					{
						SocialServerApis.ChocoPost feed = tempArray.get(i);
						//데이터를 넣는다.
						mPosts.add(feed);
					}
				}
			}
			
			// TODO
			// 테스트용 로그 추후 삭제 필요
			Logger.e(TAG, "page count = " + page_count);
			Logger.e(TAG, "responseList count = " + responseList.size());
			Logger.e("","count = "+mPosts.size());
			
			return result;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgress = MyProgressDialog.show(PopularFeedsActivity.this.getActivity(), "", "");
			llmsg.setVisibility(View.GONE);
			
			mProgress.setCancelable(true);
			mProgress.setOnCancelListener(cancelListener);
		}

		protected void onProgressUpdate(String... progress) {
		}

		protected void onPostExecute(Boolean result) {
			if(mProgress!=null){ mProgress.dismiss(); }
			
			if(mPosts.size() == 0)
			{				
				txtContent.setText(getResources().getString(R.string.popularfeeds_empty_msg));
					imgUser.setVisibility(View.VISIBLE);

				llmsg.setVisibility(View.VISIBLE);
				isMsgCheck = false;
			}
			else//나머지 경우는 전부 숨긴다.
			{
				llmsg.setVisibility(View.GONE);
				isMsgCheck = false;
			}
			

			// 피드가 갱신되었다는것을 알려주면서 곧바로 알아서 'notifyDataSetChanged'
			// 를 호출하도록 바꾸었습니다. 필요한 View를 미리 만들어버리게...
			mAdatper.setFeedData(mPosts);
			
			mAdatper.notifyDataSetChanged();
			
			refreshListView.setMode(lib.pulltorefresh.PullToRefreshBase.Mode.BOTH);
			
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
	 */
	public void uiUpdate(Boolean result, String strSvrMsg, int position)
	{
		Logger.e(TAG, "emo send result = " + result + "//" + "positon = " + position);
		
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
			break;

		default:
			break;
		}
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