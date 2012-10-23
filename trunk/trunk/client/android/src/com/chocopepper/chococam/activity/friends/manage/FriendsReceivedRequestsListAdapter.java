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

public class FriendsReceivedRequestsListAdapter extends BaseAdapter {

	Activity mContext;
	LayoutInflater mInflater;
	List<SocialServerApis.ChocoUser> mLocalUsers;
	private ImageLoader imageLoader;

	public FriendsReceivedRequestsListAdapter(Activity context, List<SocialServerApis.ChocoUser> users) {
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageLoader = new ImageLoader(mContext);
		imageLoader.setStubImageResourceId(R.drawable.default_user);

		mLocalUsers = users;
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

	public void updateMyView(int position, View convertView) {
		TextView txtUserName = (TextView) convertView
				.findViewById(R.id.txtUserName);
		ImageViewRounded imgUser = (ImageViewRounded) convertView
				.findViewById(R.id.imgUser);

		Button btnRequestFriendship = (Button) convertView
				.findViewById(R.id.buttonRequestFriendship);

		final SocialServerApis.ChocoUser user = mLocalUsers.get(position);
		btnRequestFriendship.setTag(user);
		
		txtUserName.setText(user.name);
		String userImgPath = Utils.getServerImageUrl(user.avatar_url.thumbnail);
		if (userImgPath != null && userImgPath.trim().length() > 0 && !userImgPath.equals(Constants.SERVER_IMAGE_PATH))
			imageLoader.DisplayImage( Utils.getServerImageUrl(user.avatar_url.thumbnail), imgUser);
		else 
			imgUser.setImageResource(R.drawable.default_user);

		btnRequestFriendship.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SocialServerApis.ChocoUser user = (SocialServerApis.ChocoUser)v.getTag();
				new AcceptFriendshipRequestTask().setContext(mContext).execute(user);
			}
		});

		imgUser.setTag(user);
		imgUser.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SocialServerApis.ChocoUser user = (SocialServerApis.ChocoUser)v.getTag();
				Utils.openUserInfoPage(mContext, user._id);
			}
		});
	}

	public View createView(int position, ViewGroup parent) {
		View convertView = mInflater.inflate(
				R.layout.friends_received_request_list_item, parent, false);
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

	
	
	
	
	
	
	
	
	class AcceptFriendshipRequestTask extends AsyncTask<SocialServerApis.ChocoUser, String, Boolean> {
		private MyProgressDialog mProgress=null;
		private Context mCtx;
		
		
		public AcceptFriendshipRequestTask setContext(Context ctx){
			mCtx = ctx;
			return this;
		}

		@Override
		protected Boolean doInBackground(SocialServerApis.ChocoUser... params) {
			boolean result = false;
			SocialServerApis.ChocoUser user = (SocialServerApis.ChocoUser)params[0];

			//mGameFeeds
			SocialServerApis socialconnection = SocialServerApis.getInstance(mCtx);
			result = socialconnection.chocoFollowAcceptFollow(user._id);
			
			// 2012-05-22 brucewang
			// 친구 요청 수락 완료시, 요청 목록에서 삭제 후
			// 현재 친구 리스트에 추가.
			// 초청을 수락 완료 하였다는 것은 그 친구가 내 친구로 등록되었다는 의미.
			//
			if(result){
				int position_2_delete=-1;
				
				SocialServerApis.ChocoUser _userToAddToFriendList = null;
				for( int pos=0; pos<mLocalUsers.size(); pos++ ){
					SocialServerApis.ChocoUser _usr = mLocalUsers.get(pos);
					if( _usr._id.equals(user._id) ){
						position_2_delete = pos;
						_userToAddToFriendList = _usr;
						break;
					}
				}
				
				// 2012-05-29 brucewang
				// 친구 목록을 static 으로 사용하면서 예상치 않은 오류가 발생함.
				// 어짜피 서버에는 친구로 등록되고,  '현재친구' 리스트는 
				// 매번 서버로부터 데이터를 업데이트 받도록 하였으므로, 일단은
				// 다음 코드는 필요가 없음.
				
//				// 2012-05-29 brucewang
//				// '현재 친구들 목록' 을 보여주는 뷰에도 새 친구가 나타나도록 데이터를 조작함.
//				if( _userToAddToFriendList!=null ){
//					FriendsList.mLocalUsers.add(_userToAddToFriendList);
//				}
				
				if(position_2_delete>=0){
					mLocalUsers.remove(position_2_delete);
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
			// Call onRefreshComplete when the list has been refreshed.
			if(result){
				FriendsReceivedRequestsListAdapter.this.notifyDataSetChanged();
			}
		}
	}
}
