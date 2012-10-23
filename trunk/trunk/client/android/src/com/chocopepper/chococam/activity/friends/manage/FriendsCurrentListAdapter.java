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

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.chocopepper.chococam.R;
import com.chocopepper.chococam.network.SocialServerApis;
import com.chocopepper.chococam.util.Constants;
import com.chocopepper.chococam.util.ImageLoader;
import com.chocopepper.chococam.util.ImageViewRounded;
import com.chocopepper.chococam.util.MyProgressDialog;
import com.chocopepper.chococam.util.Utils;

/** 
 * This is an ChocoTalk client demo application.
 * 
 * Chat room adding friends list adapter.
 * 
 * @author Bruce Wang (brucewang@chocopepper.com)
 */

public class FriendsCurrentListAdapter extends BaseAdapter {

	
//	FriendsCurrentListViewHolder fHolder;
	
	Activity mContext;
	LayoutInflater mInflater;
	List<SocialServerApis.ChocoUser> mLocalUsers;
	private ImageLoader imageLoader;
	private boolean mInEditMode=false;
	
	
	// 2012-05-23 brucewang
    // 친구 편집 모드와 일반 모드로 뷰 전환.
    public void switchEditMode(){    	
    	mInEditMode = !mInEditMode;    	
    	this.notifyDataSetChanged();
    }
    
    public void setFriendsList(List<SocialServerApis.ChocoUser> users){
    	mLocalUsers = users;
    }

	public FriendsCurrentListAdapter(Activity context, List<SocialServerApis.ChocoUser> users) {
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mLocalUsers = users;
        imageLoader = new ImageLoader(mContext);
        imageLoader.setStubImageResourceId(R.drawable.default_user);
	}


	public int getCount() {
		return mLocalUsers.size();
	}

	public Object getItem(int position) {
		return mLocalUsers.get(position);
	}

	public long getItemId(int position) {
		return position;
	}
	
	
	public void updateMyView(int position, View convertView){
		TextView txtUserName = (TextView)convertView.findViewById(R.id.txtUserName);
		TextView txtFollowFollowing = (TextView)convertView.findViewById(R.id.txtFollowFollowing);

		ImageViewRounded imgUser = (ImageViewRounded)convertView.findViewById(R.id.imgUser);
		
		Button btn = (Button)convertView.findViewById(R.id.buttonRequestFriendship);
				
		final SocialServerApis.ChocoUser user = mLocalUsers.get(position);
		
		txtUserName.setText(user.name);
		
		if( user.i_am_following ){
			txtFollowFollowing.setText( "following" );
		}
		else if( user.is_follower ){
			txtFollowFollowing.setText( "follower" );
		}
		
		String strUserImg = user.avatar_url.thumbnail;
		
		if (strUserImg == null || strUserImg.trim().length() < 1 || strUserImg.equals(Constants.SERVER_IMAGE_PATH)) {
			imgUser.setImageResource(R.drawable.default_user);
		} else {
			imageLoader.DisplayImage( 
					Utils.getServerImageUrl(strUserImg), 
					imgUser);
		}
		
		
		
		
//		btn.setTypeface(font);
		if(mInEditMode){			
			btn.setVisibility(View.VISIBLE);
			txtFollowFollowing.setVisibility(View.GONE);
			if( user.i_am_following ){
				btn.setText("Unfollow");
			}else{
				btn.setText("Reject");
			}
		}else{
			btn.setVisibility(View.GONE);
			txtFollowFollowing.setVisibility(View.VISIBLE);			
		}
		btn.setTag(user);
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 2012-05-23 brucewang
				// 친구관계 끊기 선택시...				
				SocialServerApis.ChocoUser _user = (SocialServerApis.ChocoUser)v.getTag();
				new DeleteFriendshipTask().setContext(mContext).execute(_user);
			}
		});
		
		imgUser.setTag(user);
		imgUser.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {				
				SocialServerApis.ChocoUser user = (SocialServerApis.ChocoUser)v.getTag();
//				Utils.openUserInfoPage(mContext, user.getUser_id());				
			}
		});
	}
	
	public View createView(int position, ViewGroup parent){
		View convertView = mInflater.inflate(R.layout.friends_current_list_item, parent, false);
		return convertView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final int pos = position;

		View view2return = convertView;
		if (convertView == null) {
			view2return = createView(pos, parent);
			updateMyView(position, view2return);
		} else {
			updateMyView(position, view2return);
		}

		return view2return;
	}
	
	class DeleteFriendshipTask extends AsyncTask<SocialServerApis.ChocoUser, String, Boolean> {
		private MyProgressDialog mProgress=null;
		private Context mCtx;
		private String mErrorMsg = "";
		
		public DeleteFriendshipTask setContext(Context ctx){
			mCtx = ctx;
			return this;
		}

		@Override
		protected Boolean doInBackground(SocialServerApis.ChocoUser... params) {
			boolean result = false;
			SocialServerApis.ChocoUser user = (SocialServerApis.ChocoUser)params[0];

			//mPosts
			SocialServerApis socialconnection = SocialServerApis.getInstance(mCtx);
			
			if( user.i_am_following ){
				result = socialconnection.chocoFollowUnfollow(user._id);
			}else{
				result = socialconnection.chocoFollowRejectFollow(user._id);
			}
			
			if(result)
			{
				// 목록에서 지움.
				int foundPosition=-1;
				for( int position=0; position<mLocalUsers.size(); position++ ){
					SocialServerApis.ChocoUser _usertmp = mLocalUsers.get(position);
					if( user._id.equals(_usertmp._id) ){
						foundPosition = position;
						break;
					}
				}
				if(foundPosition>=0){
					mLocalUsers.remove(foundPosition);
				}
			}

			return result;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgress = MyProgressDialog.show(mContext, "", "");
		}

		protected void onProgressUpdate(String... progress) {
		}

		protected void onPostExecute(Boolean result) {
			if(mProgress!=null){ mProgress.dismiss(); }
			//
			if( result==false ){
				Toast.makeText(mContext, mErrorMsg, Constants.DEFAULT_TOAST_DURATION).show();
			}else{
				FriendsCurrentListAdapter.this.notifyDataSetChanged();
			}
		}
	}
}

