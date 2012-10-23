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

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chocopepper.chococam.R;
import com.chocopepper.chococam.activity.MoviePlayer;
import com.chocopepper.chococam.activity.friends.manage.ReplyActivity;
import com.chocopepper.chococam.network.SocialServerApis;
import com.chocopepper.chococam.network.SocialServerApis.ChocoPost;
import com.chocopepper.chococam.util.Constants;
import com.chocopepper.chococam.util.DateUtil;
import com.chocopepper.chococam.util.ImageLoader;
import com.chocopepper.chococam.util.ImageViewRounded;
import com.chocopepper.chococam.util.Logger;
import com.chocopepper.chococam.util.Utils;

/**
 * This is an ChocoTalk client demo application.
 * 
 * Chat room adding friends list adapter.
 * 
 * @author Bruce Wang (brucewang@chocopepper.com)
 */

public class PopularPostsListAdapter extends BaseAdapter {

	private static ChocoPost postData;
	
	boolean ischeck = false;
	boolean isEmpty = false;
	Activity mContext;
	LayoutInflater mInflater;
	List<SocialServerApis.ChocoPost> mFeeds;
	private ImageLoader imageLoader_user;	
	private ImageLoader imageLoader_screenshot;
	private static final String TAG = Logger
			.makeLogTag(PopularPostsListAdapter.class);
	// private int list_position = -1;
	// private int emoticon_id = -1;

	public PopularPostsListAdapter(Activity context, List<SocialServerApis.ChocoPost> feed) {
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mFeeds = feed;
		imageLoader_user = new ImageLoader(context.getApplicationContext());		
		imageLoader_screenshot = new ImageLoader(
				context.getApplicationContext());
		imageLoader_screenshot.setAutomaticRotateImage(false);
		imageLoader_screenshot.setImageResize(true);

		imageLoader_user.setStubImageResourceId(R.drawable.default_user);
		
	}

	public void setFeedData(List<SocialServerApis.ChocoPost> feed) {
		mFeeds = feed;
	}

	public int getCount() {
		return mFeeds.size();
	}

	public Object getItem(int position) {
		return mFeeds.get(position);
	}

	public long getItemId(int position) {
		return position;
	}
	
	// 12-10-12 nerine
	// 리플 액티비티 feed 전달용 함수 
	public static void setPostData(ChocoPost feed)
	{
		postData = feed;		
	}
	public static ChocoPost getPostData()
	{		
		return postData; 
	}

	class ViewHolder {
		TextView txtTitle;
		TextView txtSubTitle;
		ImageViewRounded imgUser;		
		ImageView imgEmo;
		LinearLayout linearLayoutCommentAndScreenshot;
		LinearLayout linearLayoutGameInfo;
		TextView textDescription;
		ImageView imgScreenshot;
		Button btn_smile;
		Button btn_reply;
		TextView txtDate;		
	}

	public void updateMyView(final int position, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();

		final SocialServerApis.ChocoPost feed = mFeeds.get(position);
		
		// 12-10-11 nerine
		// 동영상의 url을 넣는다.
		SocialServerApis.ChocoFileUrl mUrl = feed.movie.file_url;		
		final String movie_url = mUrl.mp4;

//		/**
//		 * 디바이스마다 다르게 설정 한다. 개선의 여지가 필요한 코드
//		 */
//		int devicePixelWidth = DeviceUtils.getDeviceSizeWidth(mContext);
//
//		if (devicePixelWidth == 720) {
//			holder.txtTitle.setMaxWidth(230);
//		} else if (devicePixelWidth == 800) {
//			holder.txtTitle.setMaxWidth(350);
//		} else if (devicePixelWidth == 480) {
//			holder.txtTitle.setMaxWidth(120);
//		} else {
//			holder.txtTitle.setMaxWidth(100);
//		}
		
		
		holder.txtTitle.setText(  feed.user.name );
		imageLoader_user.DisplayImage(
				Utils.getServerImageUrl(feed.user.avatar_url.thumbnail),
				holder.imgUser,
				mContext);

		String strDescription = feed.description;
		String strScreenshotUrl = feed.movie.file_url.thumbnail;
		if ((strScreenshotUrl != null && strScreenshotUrl.length() > 0)
				|| (strDescription != null && strDescription.length() > 0)) {
			if (strDescription != null) {
				holder.textDescription.setText(strDescription);
			}
			if (strScreenshotUrl != null) {
				imageLoader_screenshot.DisplayImage(
						Utils.getServerImageUrl(strScreenshotUrl),
						holder.imgScreenshot,
						mContext);
			}
//			holder.linearLayoutGameInfo.setVisibility(View.GONE);
			holder.linearLayoutCommentAndScreenshot.setVisibility(View.VISIBLE);
		} else {
//			holder.linearLayoutGameInfo.setVisibility(View.VISIBLE);
			holder.linearLayoutCommentAndScreenshot.setVisibility(View.GONE);
		}

		holder.btn_smile.setTag(feed);
		holder.btn_reply.setTag(feed);
		holder.imgScreenshot.setTag(feed);

		holder.btn_smile.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v.isPressed()) {
					ischeck = false;
				}

				if (!ischeck) {
					Activity parent = (Activity) mContext;
					String callActivity = "PopularFeedsActivity";
					Utils.PopShow(v, parent, position, callActivity);
					ischeck = true;
				}

			}
		});
		
		holder.imgScreenshot.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext.getApplicationContext(), MoviePlayer.class);
				intent.putExtra(Constants.MOVIE_URL, movie_url);
				mContext.startActivity(intent);
			}
		});

		holder.btn_reply.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				GameFeed _feed = (GameFeed) v.getTag();
				Intent intent = new Intent(mContext.getApplicationContext(),
						ReplyActivity.class);
				
				Logger.e(TAG, "post id = " + feed._id);
				
				intent.putExtra("isFeedsList", false);
				
				setPostData(feed);

				mContext.startActivity(intent);
			}
		});

		//holder.txtTitle.setText(feed.getUser_name());
		
		holder.txtSubTitle.setText(feed.movie.name);

		// 20120531_arisu717 - 시간표현 가독성 향상 [[
		// txtDate.setText( feed.getReg_date() );
		String strDateFromServer = feed.created_at;
		if (strDateFromServer == null) {
			Logger.e(TAG, "strDateFromServer is NULL");
			strDateFromServer = "...";
		} else {
			strDateFromServer = DateUtil.getDifferentTime(strDateFromServer);
		}
		holder.txtDate.setText(strDateFromServer);
		// ]]
		
		long my_emoticon_count = feed.like_count;
		long my_comment_count = feed.comment_count;
		
		holder.btn_smile.setText(String.format("%d", my_emoticon_count));
		holder.btn_reply.setText(String.format("%d", my_comment_count));


//		// 12-09-24 nerine
//		// 섬네일 이미지가 비어있으면 직접 이미지 뷰에 기본 이미지를 삽입한다.
//		if(feed.getUser_thum_img().equals(""))
//		{			
//			holder.imgUser.setImageResource(R.drawable.default_user);
//		}
//		else
//		{
//			imageLoader_user.DisplayImage(Utils.getServerImageUrl(feed.getUser_thum_img()), holder.imgUser);
//		}
		

//		holder.imgUser.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Utils.openUserInfoPage(mContext,
//						Long.parseLong(feed.getUser_id()));
//			}
//		});
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.gamefeeds_listitem,parent, false);
			holder = new ViewHolder();
			holder.txtTitle = (TextView) convertView.findViewById(R.id.TextViewTitle);
			holder.txtSubTitle = (TextView) convertView.findViewById(R.id.TextViewSubTitle);
			holder.imgUser = (ImageViewRounded) convertView.findViewById(R.id.imgUser);			
			holder.imgEmo = (ImageView) convertView.findViewById(R.id.imgEmo);
			holder.linearLayoutCommentAndScreenshot = (LinearLayout) convertView.findViewById(R.id.linearImgAndText);			
			holder.linearLayoutGameInfo = (LinearLayout) convertView.findViewById(R.id.linearGameInfo);			
			holder.textDescription = (TextView) convertView.findViewById(R.id.TextComment);
			holder.imgScreenshot = (ImageView) convertView.findViewById(R.id.imgScreenShot);
			holder.btn_smile = (Button) convertView.findViewById(R.id.btn_smile);
			holder.btn_reply = (Button) convertView.findViewById(R.id.btn_reply);
			holder.txtDate = (TextView) convertView.findViewById(R.id.TextViewDate);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		updateMyView(position, convertView);
		return convertView;
	}
}
