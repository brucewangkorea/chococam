package com.chocopepper.chococam.activity.friends.manage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import lib.pulltorefresh.PullToRefresh;
import lib.pulltorefresh.PullToRefreshBase.OnRefreshListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.chocopepper.chococam.R;
import com.chocopepper.chococam.network.SocialServerApis;
import com.chocopepper.chococam.util.Logger;
import com.chocopepper.chococam.util.MyProgressDialog;

public class ReceivedRequests extends Fragment implements OnItemClickListener {
	PullToRefresh friendsList;
	FriendsReceivedRequestsListAdapter mAdatper;
	List<SocialServerApis.ChocoUser> mLocalUsers = new CopyOnWriteArrayList<SocialServerApis.ChocoUser>();
	SocialServerApis mSocialconnection = null;
	private static final String TAG = Logger.makeLogTag(ReceivedRequests.class);

//	private boolean nextPageCheck = false;
	private int pageCount = 1;

	GetDataTask getDataTask;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {

			return null;
		}
		View v = inflater.inflate(R.layout.friends_current_list_layout,
				container, false);

		friendsList = (PullToRefresh) v.findViewById(R.id.ngame_list);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mSocialconnection = SocialServerApis.getInstance(this.getActivity()
				.getApplicationContext());

		if (friendsList != null) {
			mAdatper = new FriendsReceivedRequestsListAdapter(getActivity(),
					mLocalUsers);
			friendsList.setAdapter(mAdatper);
			// friendsList.setOnItemClickListener( this );
			// friendsList.setSelector(R.drawable.selector_empty);
		} else {
			Logger.e("TAG", "LISTVIEW IS NULL!!!");
		}

		((PullToRefresh) friendsList)
				.setOnRefreshListener(new OnRefreshListener() {
					public void onRefresh() {
						// Do work to refresh the list here.

						if (friendsList.getCurrentMode() == lib.pulltorefresh.PullToRefreshBase.Mode.PULL_UP_TO_REFRESH) {
							// 아래쪽 새로고침 추가
							pageCount++;

							getDataTask = new GetDataTask();
							getDataTask.execute(String.format("%d", pageCount));
							Logger.e(TAG, "page count = " + pageCount);
						} else if (friendsList.getCurrentMode() == lib.pulltorefresh.PullToRefreshBase.Mode.PULL_DOWN_TO_REFRESH) {
							pageCount = 1;

							getDataTask = new GetDataTask();
							getDataTask.execute(String.format("%d", pageCount));
						}
					}
				});

		if (mLocalUsers != null && mLocalUsers.size() < 1) {
			 getDataTask = new GetDataTask();
			 getDataTask.execute(String.format("%d", pageCount));
		}
	}

	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		Logger.e(TAG, String.format("%d", id));
	}

	public static String getTitle(Context context) {
		Resources res = context.getResources();

		return res
				.getString(R.string.fragment_title_friends_manage_recieved_request);
	}

	private static boolean bGetDataTaskRunning = false;
	class GetDataTask extends AsyncTask<String, String, Boolean> {
		private MyProgressDialog mProgress = null;
		private List<SocialServerApis.ChocoUser> users = new CopyOnWriteArrayList<SocialServerApis.ChocoUser>();

		@Override
		protected Boolean doInBackground(String... params) {
			boolean result = false;
			if (bGetDataTaskRunning == true) {
				return false;
			}
			bGetDataTaskRunning = true;

			Context ctx = ReceivedRequests.this.getActivity()
					.getApplicationContext();

			String str_page_count = params[0];

			int page_count = Integer.parseInt(str_page_count);

			users.clear();

			users = mSocialconnection.chocoFollowGetFollowerRequests();
			
			return result;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgress = MyProgressDialog.show(
					ReceivedRequests.this.getActivity(), "", "");

			mProgress.setCancelable(true);
			mProgress.setOnCancelListener(cancelListener);
		}

		protected void onProgressUpdate(String... progress) {
		}

		protected void onPostExecute(Boolean result) {
			try {
				if (mProgress != null) {
					mProgress.dismiss();
				}
			} catch (Exception e) {
				Logger.e(TAG, e.toString());
			}

			friendsList.onRefreshComplete();

			mLocalUsers.clear();
			mLocalUsers.addAll(users);

			mAdatper.notifyDataSetChanged();

			friendsList.setMode(lib.pulltorefresh.PullToRefreshBase.Mode.BOTH);
			
			bGetDataTaskRunning = false;

			Logger.i(TAG, "ReceivedRequest Task End");
		}
	}

	public DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {

		@Override
		public void onCancel(DialogInterface dialog) {
			Logger.e(TAG, "ReceivedRequests Task Cancel");
		}
	};
}
