package com.chocopepper.chococam.activity.friends.manage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import lib.pulltorefresh.PullToRefresh;
import lib.pulltorefresh.PullToRefreshBase.OnRefreshListener;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chocopepper.chococam.R;
import com.chocopepper.chococam.model.MyFriendsList;
import com.chocopepper.chococam.model.MyFriendsList.MyFriendsInfoListener;
import com.chocopepper.chococam.network.SocialServerApis;
import com.chocopepper.chococam.network.SocialServerApis.ChocoUser;
import com.chocopepper.chococam.util.ImageViewRounded;
import com.chocopepper.chococam.util.Logger;







public class FriendsList extends Fragment implements OnItemClickListener  {
	
	
	LinearLayout llmsg;
	TextView txtContent;
	ImageViewRounded imgUser;
	
	PullToRefresh friendsList;
	FriendsCurrentListAdapter mAdatper;
	List<SocialServerApis.ChocoUser> mLocalUsers = new CopyOnWriteArrayList<SocialServerApis.ChocoUser>();
	SocialServerApis mSocialconnection = null;
	private static final String TAG = Logger.makeLogTag(FriendsList.class);
	
	private int pageCount = 1;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {

            return null;
        }
		View v = inflater.inflate(R.layout.friends_current_list_layout, container, false);
		
		friendsList = (PullToRefresh)v.findViewById(R.id.ngame_list);
		friendsList.setMode(lib.pulltorefresh.PullToRefreshBase.Mode.BOTH);
		
		/**
		 * nerine 12-07-13
		 * 친구가 없을때 친구 초청 메시지를 보여주고 메시지 터치 시 친구 추천으로 이동시킨다.
		 */
		llmsg = (LinearLayout)v.findViewById(R.id.llmsg);
		txtContent = (TextView)v.findViewById(R.id.txtContent);
		imgUser = (ImageViewRounded)v.findViewById(R.id.imgUser);
		llmsg.bringToFront();		
		llmsg.setVisibility(View.GONE);
		llmsg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Logger.e(TAG, "msg click!!");
				Activity par = FriendsList.this.getActivity();
				
				if(par != null)
				{
					if (par instanceof FriendsActivity) {
						FriendsActivity friends = (FriendsActivity) par;					
//						friends.onPageSelected(FriendsActivity.VIEW_RECOMMENDED_FRIENDS);
						friends.finish();
						
						Intent i = new Intent(friends, FriendsActivity.class);					
						i.putExtra(FriendsActivity.PAGER_INDEX, FriendsActivity.VIEW_RECOMMENDED_FRIENDS);
						startActivity(i);
					}
				}
			}
		});
		
		return v;
	}
	
	
	private void getMyFriendsList(){		
		// 12-10-16 nerine
		// 싱글톤으로 생성 시 새로운 데이터로 갱신이 되지 않아 그때 마다 생성하도록 변경
		MyFriendsList mFriendsList = new MyFriendsList();
		mFriendsList.getMyFriendsList(FriendsList.this.getActivity(),
				false,
				new MyFriendsInfoListener() {
					
					@Override
					public void whenMyFriendsListIsReceived(List<ChocoUser> friendslist) {
						mLocalUsers = friendslist;
						mAdatper.setFriendsList(mLocalUsers);
						mAdatper.notifyDataSetChanged();
					}
				});
				
//		MyFriendsList.getInstance().getMyFriendsList(
//				FriendsList.this.getActivity(), 
//				false, 
//				new MyFriendsInfoListener() {
//					
//					@Override
//					public void whenMyFriendsListIsReceived(List<ChocoUser> friendslist) {
//						mLocalUsers = friendslist;
//						mAdatper.setFriendsList(mLocalUsers);
//						mAdatper.notifyDataSetChanged();
//					}
//				});
		((PullToRefresh) friendsList).onRefreshComplete();
	}
	


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        
        mSocialconnection = SocialServerApis.getInstance(this.getActivity().getApplicationContext());
        
        if(friendsList!=null){
	        mAdatper = new FriendsCurrentListAdapter(getActivity(), mLocalUsers);
	        friendsList.setAdapter(mAdatper);
	        friendsList.setOnItemClickListener( this );
//	        friendsList.setSelector(R.drawable.selector_empty);
	        
        }
        else{
        	Logger.e("TAG", "LISTVIEW IS NULL!!!" );
        }
        
        friendsList.setOnRefreshListener(new OnRefreshListener() {
			public void onRefresh() {
				// Do work to refresh the list here.
				
				if(friendsList.getCurrentMode() == lib.pulltorefresh.PullToRefreshBase.Mode.PULL_UP_TO_REFRESH )
				{		
					pageCount++;
					getMyFriendsList();					
				}
				else if(friendsList.getCurrentMode() == lib.pulltorefresh.PullToRefreshBase.Mode.PULL_DOWN_TO_REFRESH)
				{					
					pageCount = 1;
					//기존 헤더쪽 refresh									
					getMyFriendsList();
				}
			}
		});
        getMyFriendsList();
    }
    
    
    
    // 2012-05-23 brucewang
    // 친구 편집 모드와 일반 모드로 뷰 전환.
    public void switchEditMode(){
    	mAdatper.switchEditMode();
    }
    
   	public void onItemClick(AdapterView<?> parent, View v, int position, long id) 
   	{
   		Logger.e(TAG, String.format("%d", id));
   			
   	}
   	
   	
   	public static String getTitle(Context context) {
		Resources res = context.getResources();
		
		return res.getString(R.string.fragment_title_friends_manage_current);
	}
   	
//   	private static boolean bGetDataTaskRunning = false;

   	
   	public DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {

		@Override
		public void onCancel(DialogInterface dialog) {
			Logger.i(TAG, "FriendsList Task Cancel");
			getMyFriendsList();
		}
	};
	
	
	
	
	
	
//	class GetDataTask extends AsyncTask<Integer, String, Boolean> {
//		private MyProgressDialog mProgress = null;
//
//		@Override
//		protected Boolean doInBackground(Integer... params) {
//			boolean result = false;
//			
//			int page = params[0];
//			Context ctx = FriendsList.this.getActivity();
//
//			SocialServerApis socialserver = SocialServerApis.getInstance(ctx);
//			
//			
//			if(mLocalUsers!=null && mLocalUsers.size()>0){
//				return false;
//			}
//
//			// Following list
//			List<SocialServerApis.ChocoFollowInfo> list_following = socialserver.chocoFollowGetFollowings();
//			for( SocialServerApis.ChocoFollowInfo follow : list_following ){
//				follow.user.follow_accepted = follow.accepted;
//				follow.user.i_am_following = true;
//				mLocalUsers.add( follow.user );
//			}
//			
//			// Follower list
//			List<SocialServerApis.ChocoFollowInfo> list_followers = socialserver.chocoFollowGetFollowers();
//			for( SocialServerApis.ChocoFollowInfo follow : list_followers ){
//				if( follow.accepted ){
//					follow.user.is_follower = true;
//					mLocalUsers.add( follow.user );
//				}
//			}
//			
//
//			return true;
//		}
//
//		@Override
//		protected void onPreExecute() {
//			super.onPreExecute();
//			Activity ctx = FriendsList.this.getActivity();
//			mProgress = MyProgressDialog.show(ctx, "", "");
//		}
//
//		protected void onProgressUpdate(String... progress) {
//		}
//
//		protected void onPostExecute(Boolean result) {
//			if (mProgress != null) {
//				mProgress.dismiss();
//			}
//			if(result){
//				
//			}
//		}
//	}
}
