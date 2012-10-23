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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.chocopepper.chococam.R;
import com.chocopepper.chococam.network.SocialServerApis;
import com.chocopepper.chococam.util.DateUtil;
import com.chocopepper.chococam.util.ImageLoader;
import com.chocopepper.chococam.util.ImageViewRounded;
import com.chocopepper.chococam.util.Utils;

/**
 * This is an ChocoTalk client demo application.
 * 
 * Chat room adding friends list adapter.
 * 
 * @author Kyle Ahn (khan4257@gmail.com)
 */

public class ReplyAdapter extends BaseAdapter {

	Context mContext;
	LayoutInflater mInflater;
	List<SocialServerApis.ChocoComment> mCommentList;
	public ImageLoader imageLoader;
	public ArrayList<View> mViews = new ArrayList<View>();

	public ReplyAdapter(Context context, List<SocialServerApis.ChocoComment> feed) {
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mCommentList = feed;
		imageLoader = new ImageLoader(context.getApplicationContext());
	}

	public void setFeedData(List<SocialServerApis.ChocoComment> feed) {
		mCommentList = feed;
		mViews.clear();
	}

	public int getCount() {
		return mCommentList.size();
	}

	public Object getItem(int position) {
		return mCommentList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	class ViewHolder {
		ImageViewRounded imgUser;
		TextView txtTitle;
		TextView txtMessage;
		TextView txtDate;
	}
	public void updateMyView(int position, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();

		SocialServerApis.ChocoComment feed = mCommentList.get(position);		
		
		holder.txtTitle.setText(feed.user.name);

		holder.txtMessage.setText(feed.description);
		// 20120531_arisu717 - 시간표현 가독성 향상 [[
		// txtDate.setText(feed.reg_date);
		String strDateFromServer = feed.created_at;
		strDateFromServer = DateUtil.getDifferentTime(strDateFromServer);
		holder.txtDate.setText(strDateFromServer);
		// ]]
		
		// 12-10-18 nerine
		// 댓글에 사진 표시
		if(feed.user.avatar_url.thumbnail != null)
		{
			imageLoader.DisplayImage(Utils.getServerImageUrl(feed.user.avatar_url.thumbnail),holder.imgUser);
		}				
	}

	public View createView(int position, ViewGroup parent) {
		View convertView = mInflater.inflate(R.layout.reply_feed_listitem,
				parent, false);
		return convertView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.reply_feed_listitem,parent, false);
			holder = new ViewHolder();
			
			holder.imgUser = (ImageViewRounded) convertView.findViewById(R.id.imgUser);
			holder.txtTitle = (TextView) convertView.findViewById(R.id.TextViewTitle);
			holder.txtMessage = (TextView) convertView.findViewById(R.id.TextViewMessage);
			holder.txtDate = (TextView) convertView.findViewById(R.id.TextViewDate);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		updateMyView(position, convertView);
		return convertView;
	}
}
