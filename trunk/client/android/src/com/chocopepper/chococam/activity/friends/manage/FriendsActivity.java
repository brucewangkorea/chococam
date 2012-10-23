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

import java.util.List;
import java.util.Vector;

import lib.viewpagerindicator.actionbar.TitlePageIndicator;
import lib.viewpagerindicator.actionbar.TitlePageIndicator.IndicatorStyle;
import lib.viewpagerindicator.normal.ViewPagerIndicator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chocopepper.chococam.R;
import com.chocopepper.chococam.dao.UserService;
import com.chocopepper.chococam.util.Constants;
import com.chocopepper.chococam.util.Logger;


/** 
 * This is an ChocoTalk client demo application.
 * 
 * Friend List Viewer
 * 
 * @author Kyle Ahn (khan4257@gmail.com)
 */

public class FriendsActivity extends FragmentActivity implements ViewPagerIndicator.PageInfoProvider, OnPageChangeListener { 
	
	private static final String TAG = Logger.makeLogTag(FriendsActivity.class);
	public static final String PAGER_INDEX = FriendsActivity.class.getSimpleName() + "_selectedPagerIndex";
	
	public static FriendsActivity faContext = null;
	
	public static final int VIEW_FRIENDLIST = 0;
	public static final int VIEW_RECOMMENDED_FRIENDS = 1;
	public static final int VIEW_RECEIVED_REQUESTS = 2;
	
	private SharedPreferences sharedPref;
	
	private int selectedPagerIndex = VIEW_FRIENDLIST;
	
	private FriendsAdapter mAdapter;
	
	//private PageIndicator mIndicator;
//	private ViewPagerIndicator mIndicator;
	private TitlePageIndicator indicator;
	
	private ViewPager  mViewPager;
	
	RelativeLayout btnEdit;
	
	
	public FriendsList getCurrentFriendsView(){
		return (FriendsList)mAdapter.getItem(0);
	}
	public RecommendedFriends getSuggestedFriendsView(){
		return (RecommendedFriends)mAdapter.getItem(1);
	}
	public ReceivedRequests getReceivedFriendshipRequestView(){
		return (ReceivedRequests)mAdapter.getItem(2);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends);
        
    	sharedPref = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
    	selectedPagerIndex = sharedPref.getInt(PAGER_INDEX, VIEW_FRIENDLIST);
        
    	
        ImageView btnCreatechat = (ImageView) findViewById(R.id.btnCreateChat);
        btnCreatechat.setVisibility(View.GONE);
        
        TextView txt_title = (TextView)findViewById(R.id.title);
        txt_title.setText(R.string.friends_tab_sub_suggestion);
        
        List<Fragment> fragments = new Vector<Fragment>();
        
        final Fragment frFreindsList = Fragment.instantiate(this, FriendsList.class.getName());
		
        Intent i = getIntent();
		long target_user_id = i.getLongExtra(Constants.TARGET_USER_ID, UserService.getMyUserId(FriendsActivity.this));
		Bundle args = new Bundle();
        args.putLong(Constants.TARGET_USER_ID, target_user_id);
        
        selectedPagerIndex = i.getIntExtra(PAGER_INDEX, selectedPagerIndex);
        
        frFreindsList.setArguments(args);
		fragments.add(frFreindsList);
		//fragments.add(Fragment.instantiate(this, FriendsList.class.getName()));
		
		fragments.add(Fragment.instantiate(this, RecommendedFriends.class.getName()));
		fragments.add(Fragment.instantiate(this, ReceivedRequests.class.getName()));
		
		
		btnEdit = (RelativeLayout) findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {				
				// 타이틀 바의 친구목록 편집 버튼 클릭시
				((FriendsList)frFreindsList).switchEditMode();
			}
		});
        
        
        
        
		mAdapter = new FriendsAdapter(getSupportFragmentManager(), fragments);
		mAdapter.addTitle(this.getString(R.string.friends_tab_sub_current_friends));
		mAdapter.addTitle(this.getString(R.string.friends_tab_sub_suggestion));
		//mAdapter.addTitle(this.getString(R.string.friends_tab_sub_invitation));
		mAdapter.addTitle(this.getString(R.string.friends_tab_sub_received_invitation));
		
        mViewPager = (ViewPager)findViewById(R.id.pager);
        mViewPager.setAdapter(mAdapter);
        
        // 초기 위치.
        mViewPager.setCurrentItem(selectedPagerIndex);
        // 2012-05-23 brucewang
        if(selectedPagerIndex==0){
        	btnEdit.setVisibility(View.VISIBLE);
        }
        
        //mIndicator = (TitlePageIndicator)findViewById(R.id.indicator);
//        mIndicator = (ViewPagerIndicator)findViewById(R.id.indicator);
//		mIndicator.addPageChangeListener(this);
//        mViewPager.setOnPageChangeListener(mIndicator);
        
        
        indicator = (TitlePageIndicator)findViewById(R.id.indicator);
        indicator.setFooterIndicatorStyle(IndicatorStyle.None);
        indicator.setViewPager(mViewPager, selectedPagerIndex);
        indicator.setOnPageChangeListener(this);
        mViewPager.setOnPageChangeListener(indicator);
   	
    }
    
    
//    private class OnIndicatorClickListener implements ViewPagerIndicator.OnClickListener{
//		@Override
//		public void onCurrentClicked(View v) {
//			//Toast.makeText(FriendsActivity.this, "Hello", Toast.LENGTH_SHORT).show();
//		}
//		
//		@Override
//		public void onNextClicked(View v) {
//			mViewPager.setCurrentItem(Math.min(mAdapter.getCount() - 1, mIndicator.getCurrentPosition() + 1));
//		}
//
//		@Override
//		public void onPreviousClicked(View v) {
//			mViewPager.setCurrentItem(Math.max(0, mIndicator.getCurrentPosition() - 1));
//		}
//    }
	
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case 1:
			// TODO 차단 유저 관리 모듈 적용
			return true;
		default:
			break;
		}
		return (super.onOptionsItemSelected(item));
	}

	@Override
	public String getTitle(int pos) {
		String name = pos + "";
		switch (pos) {
		case 0:
			name = FriendsList.getTitle(getApplicationContext());
			break;
		case 1:
			name = RecommendedFriends.getTitle(getApplicationContext());
			break;
		case 2:
			name = ReceivedRequests.getTitle(getApplicationContext());
			break;default:
			break;
		}
		
		return name;
	}

	@Override
	public void onPageScrollStateChanged(int arg0) { /* do noting */ }

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) { /* do noting */ }

	@Override
	public void onPageSelected(int position) {
		
		if (position == VIEW_FRIENDLIST)
			btnEdit.setVisibility(View.VISIBLE);
		else
			btnEdit.setVisibility(View.GONE);

		selectedPagerIndex = position;
		Editor editor = sharedPref.edit();
		editor.putInt(PAGER_INDEX, selectedPagerIndex);
		editor.commit();
        
	}
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume() {
		Logger.e(TAG, "onResume....");
		super.onResume();
	}		
}