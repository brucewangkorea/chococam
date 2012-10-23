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
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chocopepper.chococam.R;
import com.chocopepper.chococam.activity.MoviePlayer;
import com.chocopepper.chococam.activity.friends.manage.ReplyActivity;
import com.chocopepper.chococam.network.SocialServerApis;
import com.chocopepper.chococam.network.SocialServerApis.ChocoFeed;
import com.chocopepper.chococam.util.Constants;
import com.chocopepper.chococam.util.DateUtil;
import com.chocopepper.chococam.util.ImageLoader;
import com.chocopepper.chococam.util.ImageLoader.OnLoadFinishedListener;
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

public class FeedsListAdapter extends BaseAdapter {

	
	private static ChocoFeed feedData;
	
	boolean ischeck = false;
	boolean isEmpty = false;
	Activity mContext;
	LayoutInflater mInflater;
	List<SocialServerApis.ChocoFeed> mFeeds;	
	private ImageLoader imageLoader_user;	
	private ImageLoader imageLoader_screenshot;
	private static final String TAG = Logger
			.makeLogTag(FeedsListAdapter.class);
	// private int list_position = -1;
	// private int emoticon_id = -1;

	public FeedsListAdapter(Activity context, List<SocialServerApis.ChocoFeed> feed) {
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mFeeds = feed;
		imageLoader_user = new ImageLoader(context.getApplicationContext());		
		imageLoader_screenshot = new ImageLoader(
				context.getApplicationContext());
		imageLoader_screenshot.setAutomaticRotateImage(false);
		imageLoader_screenshot.setImageResize(true);
		

		// 12-10-18 nerine
		// 섬네일 이미지 로딩이 끝나면 보이도록 한다.
		imageLoader_screenshot.setOnLoadFinishedListener(new OnLoadFinishedListener() {
			@Override
			public void whenLoadFinished(ImageView imageView, Bitmap bitmap) {
				Logger.e(TAG, "loader finish");				
								
			}
		});
		

		imageLoader_user.setStubImageResourceId(R.drawable.default_user);		

	}

	public void setFeedData(List<SocialServerApis.ChocoFeed> feed) {
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
	public static void setFeedData(ChocoFeed feed)
	{
		feedData = feed;		
	}
	public static ChocoFeed getFeedData()
	{		
		return feedData; 
	}
	
	class ViewHolder {
		TextView txtTitle;
		TextView txtSubTitle;
		ImageViewRounded imgUser;		
		ImageView imgEmo;
		ImageView imgPlay;
		LinearLayout linearLayoutCommentAndScreenshot;
		LinearLayout linearLayoutGameInfo;
		
		// TODO
		RelativeLayout RelativeLayoutEmoticons;
		
		
		TextView textDescription;
		ImageView imgScreenshot;
		Button btn_smile;
		Button btn_reply;
		TextView txtDate;		
	}

	public void updateMyView(final int position, View convertView) {
		
		final ViewHolder holder = (ViewHolder) convertView.getTag();

		final SocialServerApis.ChocoFeed feed = mFeeds.get(position);
		
		
		

		switch (feed.action.action_type) {
		//'Comment'
		case 0:			
			holder.RelativeLayoutEmoticons.setVisibility(View.INVISIBLE);
			holder.linearLayoutCommentAndScreenshot.setVisibility(View.GONE);
			holder.txtTitle.setVisibility(View.GONE);
			
			imageLoader_user.DisplayImage(
					Utils.getServerImageUrl(feed.comment.user.avatar_url.thumbnail),
					holder.imgUser,
					mContext);
			
			holder.txtSubTitle.setText(feed.comment.user.name + "님이 댓글을 달았습니다.");
			
			// 20120531_arisu717 - 시간표현 가독성 향상 [[
			String strDateFromServer0 = feed.created_at;
			if (strDateFromServer0 == null) {
				Logger.e(TAG, "strDateFromServer is NULL");
				strDateFromServer0 = "...";
			} else {
				strDateFromServer0 = DateUtil.getDifferentTime(strDateFromServer0);
			}
			holder.txtDate.setText(strDateFromServer0);
			// ]]			
			
			break;
		//'Like'
		case 1:			
			holder.RelativeLayoutEmoticons.setVisibility(View.INVISIBLE);
			holder.linearLayoutCommentAndScreenshot.setVisibility(View.GONE);
			holder.txtTitle.setVisibility(View.GONE);
			
			imageLoader_user.DisplayImage(
					Utils.getServerImageUrl(feed.like.user.avatar_url.thumbnail),
					holder.imgUser,
					mContext);
			holder.txtSubTitle.setText(feed.like.user.name + "님이 좋아요를 하셨습니다");
			
			// 20120531_arisu717 - 시간표현 가독성 향상 [[
			String strDateFromServer1 = feed.created_at;
			if (strDateFromServer1 == null) {
				Logger.e(TAG, "strDateFromServer is NULL");
				strDateFromServer1 = "...";
			} else {
				strDateFromServer1 = DateUtil.getDifferentTime(strDateFromServer1);
			}
			holder.txtDate.setText(strDateFromServer1);
			// ]]			

			break;
//		//'Play Movie'
//		case 3:			
//			holder.RelativeLayoutEmoticons.setVisibility(View.INVISIBLE);
//			holder.linearLayoutCommentAndScreenshot.setVisibility(View.GONE);
//			holder.txtTitle.setText(  feed.action.user_id );
//			holder.imgUser.setImageResource(R.drawable.default_user);
//			holder.txtSubTitle.setText("A님이" + "B님의 동영상을 재생했습니다.");
////			holder.txtSubTitle.setText(feed.action.user_id + "님이" + feed.action.target_id + "님의 동영상을 재생했습니다.");
//			
//			// 20120531_arisu717 - 시간표현 가독성 향상 [[
//			String strDateFromServer3 = feed.created_at;
//			if (strDateFromServer3 == null) {
//				Logger.e(TAG, "strDateFromServer is NULL");
//				strDateFromServer3 = "...";
//			} else {
//				strDateFromServer3 = DateUtil.getDifferentTime(strDateFromServer3);
//			}
//			holder.txtDate.setText(strDateFromServer3);
//			// ]]			

//			break;
		//'Post Movie'
		case 4:
			
			// 12-10-11 nerine
			// 동영상의 url을 넣는다.
			SocialServerApis.ChocoFileUrl mUrl = feed.post.movie.file_url;		
			final String movie_url = mUrl.mp4;					
			Logger.e(TAG, "movie_url_MP4 = " + movie_url);
			// 뷰를 재활용 하기 때문에 이전에 속성을 주면 계속 이어짐
			// 예를 들어 GONE속성을 줘서 숨기면 다음 리스트때도 GONE 속성으로 되어있음
			holder.txtTitle.setVisibility(View.VISIBLE);
			holder.txtTitle.setText(  feed.post.user.name );
			imageLoader_user.DisplayImage(
					Utils.getServerImageUrl(feed.post.user.avatar_url.thumbnail),
					holder.imgUser,
					mContext);

			String strDescription = feed.post.description;
			String strScreenshotUrl = feed.post.movie.file_url.thumbnail;
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
				holder.linearLayoutCommentAndScreenshot.setVisibility(View.VISIBLE);
				holder.RelativeLayoutEmoticons.setVisibility(View.VISIBLE);
			} else {
				holder.linearLayoutCommentAndScreenshot.setVisibility(View.GONE);
				holder.RelativeLayoutEmoticons.setVisibility(View.INVISIBLE);
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
						String callActivity = "FriendsFeedActivity";
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
					
					Logger.e(TAG, "post id = " + feed.post._id);
					Intent intent = new Intent(mContext.getApplicationContext(),
							ReplyActivity.class);					
					intent.putExtra("isFeedsList", true);					
					setFeedData(feed);					
					mContext.startActivity(intent);
				}
			});
			
			holder.txtSubTitle.setText(feed.post.movie.name);

			// 20120531_arisu717 - 시간표현 가독성 향상 [[
			String strDateFromServer = feed.created_at;
			if (strDateFromServer == null) {
				Logger.e(TAG, "strDateFromServer is NULL");
				strDateFromServer = "...";
			} else {
				strDateFromServer = DateUtil.getDifferentTime(strDateFromServer);
			}
			holder.txtDate.setText(strDateFromServer);
			// ]]

			long my_emoticon_count = feed.post.like_count;
			long my_comment_count = feed.post.comment_count;
			
			holder.btn_smile.setText(String.format("%d", my_emoticon_count));
			holder.btn_reply.setText(String.format("%d", my_comment_count));
			
			break;

		default:
			break;
		}		
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
			holder.imgPlay = (ImageView) convertView.findViewById(R.id.imgPlay);
			holder.linearLayoutCommentAndScreenshot = (LinearLayout) convertView.findViewById(R.id.linearImgAndText);			
			holder.linearLayoutGameInfo = (LinearLayout) convertView.findViewById(R.id.linearGameInfo);		
			
			holder.RelativeLayoutEmoticons = (RelativeLayout) convertView.findViewById(R.id.RelativeLayoutEmoticons);
			
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
