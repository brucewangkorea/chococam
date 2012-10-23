package com.chocopepper.chococam.activity.friends.manage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import lib.pulltorefresh.PullToRefresh;
import lib.pulltorefresh.PullToRefreshBase.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.chocopepper.chococam.R;
import com.chocopepper.chococam.model.FacebookUser;
import com.chocopepper.chococam.model.MyFriendsList;
import com.chocopepper.chococam.model.MyFriendsList.MyFriendsInfoListener;
import com.chocopepper.chococam.network.SocialServerApis;
import com.chocopepper.chococam.network.SocialServerApis.ChocoUser;
import com.chocopepper.chococam.util.Constants;
import com.chocopepper.chococam.util.ImageLoader;
import com.chocopepper.chococam.util.ImageViewRounded;
import com.chocopepper.chococam.util.Logger;
import com.chocopepper.chococam.util.MyProgressDialog;
import com.chocopepper.lib.facebook.BaseRequestListener;
import com.chocopepper.lib.facebook.ChocoFacebook;
import com.facebook.android.FacebookError;

public class RecommendedFriends extends Fragment implements OnItemClickListener
{
	private ChocoFacebook mChocoFacebook;
	private ArrayList<FacebookUser> mFacebookFriends = new ArrayList<FacebookUser>();
	private Handler mFacebookHandler;
	private FacebookFriendsListAdapter mAdapter;
	private SocialServerApis mSocialconnection; 
	private String TAG = Logger.makeLogTag(RecommendedFriends.class);
	private PullToRefresh mListView;
	private ImageLoader imageLoader;	
	/* 
	 * Facebook 친구들의 목록 정보가 반환된 경우에 호출되는 Listener
	 * 
	 */
	public class FacebookFriendsRequestListener extends BaseRequestListener {
		@Override
		public void onComplete(final String response, final Object state) {
			JSONArray jsonArray;
			try {
				jsonArray = new JSONObject(response).getJSONArray("data");

				JSONObject jsonObject = null;
				int length = jsonArray.length();
				for (int i = 0; i < length; i++) {
					try {
						jsonObject = jsonArray.getJSONObject(i);
						final String strId = jsonObject.getString("id");
						
						// 2012-10-12 brucewang
						// 이미 친구인 경우는 화면상에 보여주지 않음.
						boolean bIsFriend = false;
						for( SocialServerApis.ChocoUser fr : mCurrentFrieds ){
							if( fr.fb_user_id.equals(strId) ){
								bIsFriend = true;
								break;
							}
						}
						
						if(bIsFriend){
							Logger.e(TAG, "Skipping");
							continue;
						}
						
						
						
						
						
						// Facebook 사용자 이미지 정보 형식이 변경되었음.
						JSONObject j;
						String img_url = jsonObject.getString("picture");
						try {
							j = new JSONObject(img_url);
							String str = j.getString("data");
							j = new JSONObject(str);
							img_url = j.getString("url");
						} catch (Exception e) {
							//Logger.e(TAG, e.toString());
						}

						final String picURL = img_url;
						final String name = jsonObject.getString("name");
						
						mFacebookFriends.add(new FacebookUser(strId, name,
								picURL));						
					} catch (JSONException e1) {
						e1.printStackTrace();
					}
				}

				// Main UI thread 에서 뭔가 작업이 필요할 때.
				mFacebookHandler.post(new Runnable() {
					@Override
					public void run() {
						whenFriendsListRecievedOk();
					}
				});

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			super.onFacebookError(e, state);
			whenFriendsListRecievedFail();
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			super.onFileNotFoundException(e, state);
			whenFriendsListRecievedFail();
		}

		public void onIOException(IOException e, Object state) {
			super.onIOException(e, state);
			whenFriendsListRecievedFail();
		}

		@Override
		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			super.onMalformedURLException(e, state);
			whenFriendsListRecievedFail();
		}
	}// end of 'FacebookFriendsRequestListener'
	
	
	private void whenFriendsListRecievedOk() {
		new FindChocoFacebookFriendsTask().execute();
		
		// 12-10-17 nerine		
		// FindChocoFacebookFriendsTask에서 데이터를 변경하기때문에 여기서 리스트를 변경할 필요 없음
		// mAdapter.notifyDataSetChanged();
	}

	private void whenFriendsListRecievedFail() {

	}

	
	
	
	
	
	
	private List<ChocoUser> mCurrentFrieds = null;
	private void getMyFriendsList(){		
		
		// 12-10-17 nerine
		// 싱글톤으로 생성 시 새로운 데이터로 갱신이 되지 않아 그때 마다 생성하도록 변경
		MyFriendsList mFriendsList = new MyFriendsList();
		mFriendsList.getMyFriendsList(RecommendedFriends.this.getActivity(),
				false, new MyFriendsInfoListener() {

					@Override
					public void whenMyFriendsListIsReceived(
							List<ChocoUser> friendslist) {												
						mCurrentFrieds = friendslist;
						
						// 다시 로드 할 땐 리스트를 초기화 시킨다.
						mFacebookFriends.clear();
						mChocoFacebook.getMyFriendsList(new FacebookFriendsRequestListener());
						
					}
				});
		
		
//		MyFriendsList.getInstance().getMyFriendsList(
//				RecommendedFriends.this.getActivity(), 
//				false, 
//				new MyFriendsInfoListener() {
//					
//					@Override
//					public void whenMyFriendsListIsReceived(List<ChocoUser> friendslist) {
//						mCurrentFrieds = friendslist;						
//						mChocoFacebook.getMyFriendsList(new FacebookFriendsRequestListener());
//					}
//				});
		
		((PullToRefresh) mListView).onRefreshComplete();
		
		
	}
	
	
	
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {

			return null;
		}
		View v = inflater.inflate(R.layout.friends_recommend_layout,
				container, false);
		mListView = (PullToRefresh) v.findViewById(R.id.ngame_list);
		mListView.setMode(lib.pulltorefresh.PullToRefreshBase.Mode.PULL_DOWN_TO_REFRESH);
		
		mSocialconnection = SocialServerApis.getInstance(this.getActivity().getApplicationContext());
		imageLoader = new ImageLoader(this.getActivity());
		
		
		Logger.e(TAG, "onActivityCreated");
		mFacebookHandler = new Handler();
		mChocoFacebook = ChocoFacebook.getInstance(this.getActivity());
		
		getMyFriendsList();
		
		mAdapter = new FacebookFriendsListAdapter();
		mListView.setAdapter(mAdapter);

		
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mListView.setOnRefreshListener(new OnRefreshListener() {
			public void onRefresh() {
				// Do work to refresh the list here.
				getMyFriendsList();				
			}
		});
	}


	public static String getTitle(Context context) {
		Resources res = context.getResources();

		return res.getString(R.string.fragment_title_friends_manage_recommend);
	}
	
	
	
	
	
	
	
	
	

	public DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
		@Override
		public void onCancel(DialogInterface dialog) {
//			if (getDataTask != null) {				
//				getDataTask.cancel(true);
//			}
		}
	};

	



	/**/
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long position ) {
//		RecommendedFriendsListItem item = (RecommendedFriendsListItem)mAdatper.getItem( (int)position);
//		if( item.type==1 ) // 추천 친구.
//		{
//			SocialServerApis.ChocoUser user = (SocialServerApis.ChocoUser)item.data;
//			mAdatper.inviteUser(user);
//		}
//		else if( item.type==2 ) // SMS 초청 친구.
//		{
//			final ContactInfo contact = (ContactInfo)item.data;
//			mAdatper.inviteSmsUser(contact);
//		}
	}
	/**/

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	class FacebookFriendsListAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return mFacebookFriends.size();
		}

		@Override
		public Object getItem(int position) {
			return mFacebookFriends.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view2return = convertView;
			if (convertView == null) {
				view2return = createView(position, parent);
				updateMyView(position, view2return);
			} else {
				updateMyView(position, view2return);
			}
			return view2return;
		}

		public void updateMyView(int position, View convertView) {
			FacebookUser user = mFacebookFriends.get(position);

			// FontUtil fontutil = FontUtil
			// .getInstance(InviteFacebookFriendsActivity.this
			// .getApplicationContext());
			// Typeface font = fontutil.getDefaultFont();

			TextView txtUserName = (TextView) convertView
					.findViewById(R.id.txtUserName);

			ImageViewRounded imgUser = (ImageViewRounded) convertView
					.findViewById(R.id.imgUser);

			Button btnRequestFriendship = (Button) convertView
					.findViewById(R.id.buttonRequestFriendship);
			if(user.choco_user_id!=null && user.choco_user_id.length()>0){
				btnRequestFriendship.setText(R.string.request);
			}
			else{
				btnRequestFriendship.setText(R.string.invite);
			}
			btnRequestFriendship.setTag(user);

			txtUserName.setText(user.name);
			// txtUserName.setTypeface(font);
			imageLoader.DisplayImage(user.picture, imgUser);

			btnRequestFriendship.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					FacebookUser user = (FacebookUser) v.getTag();
					if(user.choco_user_id!=null && user.choco_user_id.length()>0){
						new SendFollowRequestTask().execute(user.choco_user_id);
					}else{
						new SendInvitationTask().execute(user.id);
					}
				}
			});
		}

		public View createView(int position, ViewGroup parent) {
			View convertView = RecommendedFriends.this.getActivity()
					.getLayoutInflater().inflate(
							R.layout.friends_received_request_list_item,
							parent, false);
			return convertView;
		}
	}

	//

	class SendInvitationTask extends AsyncTask<String, String, Boolean> {
		private MyProgressDialog mProgress = null;

		@Override
		protected void onPreExecute() {
			mProgress = MyProgressDialog.show(
					RecommendedFriends.this.getActivity(), "", "");
		}

		protected void onProgressUpdate(String... progress) {
		}

		protected void onPostExecute(Boolean result) {
			if (mProgress != null) {
				mProgress.dismiss();
			}
			if (result) {
				Toast.makeText(RecommendedFriends.this.getActivity(),
						R.string.inivtaion_success,
						Constants.DEFAULT_TOAST_DURATION).show();
				mAdapter.notifyDataSetChanged();
			} else {
				Toast.makeText(RecommendedFriends.this.getActivity(),
						R.string.inivtaion_fail,
						Constants.DEFAULT_TOAST_DURATION).show();
			}
		}

		@Override
		protected Boolean doInBackground(String... params) {
			String friend_facebookid = params[0];
			SocialServerApis socialConnection = SocialServerApis
					.getInstance(RecommendedFriends.this.getActivity());

			// facebook_invitation_message
			mChocoFacebook.postMessageToFriend(friend_facebookid,
					RecommendedFriends.this.getActivity()
							.getString(R.string.facebook_invitation_message),
					Constants.MOBILE_WEB_URL);
			return true;
		}

	}
	
	
	
	
	
	
	class SendFollowRequestTask extends AsyncTask<String, String, Boolean> {
		private MyProgressDialog mProgress = null;

		@Override
		protected void onPreExecute() {
			mProgress = MyProgressDialog.show(
					RecommendedFriends.this.getActivity(), "", "");
		}

		protected void onProgressUpdate(String... progress) {
		}

		protected void onPostExecute(Boolean result) {
			if (mProgress != null) {
				mProgress.dismiss();
			}
			if (result) {
				Toast.makeText(RecommendedFriends.this.getActivity(),
						R.string.inivtaion_success,
						Constants.DEFAULT_TOAST_DURATION).show();				
				mAdapter.notifyDataSetChanged();
				
				// 친구 요청 후에 자동으로 갱신되도록 서버에 리스트 다시 요청
				getMyFriendsList();
			} else {
				Toast.makeText(RecommendedFriends.this.getActivity(),
						R.string.inivtaion_fail,
						Constants.DEFAULT_TOAST_DURATION).show();
			}
		}

		@Override
		protected Boolean doInBackground(String... params) {
			String user_id = params[0];
			SocialServerApis socialConnection = SocialServerApis
					.getInstance(RecommendedFriends.this.getActivity());
			socialConnection.chocoFollowRequestFollow(user_id);
			return true;
		}

	}
	
	
	
	
	
	
	class FindChocoFacebookFriendsTask extends AsyncTask<String, String, Boolean> {
		private MyProgressDialog mProgress = null;

		@Override
		protected void onPreExecute() {
//			mProgress = MyProgressDialog.show(
//					RecommendedFriends.this.getActivity(), "", "");
		}

		protected void onProgressUpdate(String... progress) {
		}

		protected void onPostExecute(Boolean result) {
			if (mProgress != null) {
				mProgress.dismiss();
			}
			mAdapter.notifyDataSetChanged();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			SocialServerApis socialConnection = SocialServerApis
					.getInstance(RecommendedFriends.this.getActivity());
			
			List<String> fb_user_ids = new ArrayList<String>();
			for(FacebookUser fbuser : mFacebookFriends){
				fb_user_ids.add( fbuser.id );
			}
			List<SocialServerApis.ChocoUser> users = socialConnection.chocoFindUsersByFacebookId(fb_user_ids);
			// 받아온 기존 사용자들의 Facebook id 와 지금 받아온 Facebook 친구들의 ID를 비교.
			for( SocialServerApis.ChocoUser user : users ){
				for(FacebookUser fbuser : mFacebookFriends){
					// 같은 Facebook id를 사용한다면 실제 초코캠 서버상의 사용자 아이디 정보를 저장.
					if(fbuser.id.equals(user.fb_user_id)){
						fbuser.choco_user_id = user._id;
						break;
					}
				}
			}
			
			return true;
		}

	}
}
