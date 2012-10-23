package com.chocopepper.chococam.network;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.chocopepper.chococam.util.Constants;
import com.chocopepper.chococam.util.Logger;
import com.chocopepper.lib.RestfulClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SocialServerApis {
	private static final String TAG = Logger.makeLogTag(SocialServerApis.class);
	private static SocialServerApis mInstance = null;
	public final static String mServerUrl = Constants.SOCIAL_SERVER_URL;

	private final static String TEST_RESPONSE = "{\"head\":{\"code\":\"200\",\"code_msg\":\"정상\"},\"body\":{\"info\":{\"total_count\":\"5\",\"page\":\"1\"},\"list\":[{\"user_id\":\"132\",\"user_name\":\"김태희\"},{\"user_id\":\"14\",\"user_name\":\"원빈\"}]}}";

	private Context mContext = null;
	
	


	/*
	 * --------------------------------------------------------------------
	 * Singleton 관련.
	 * --------------------------------------------------------------------
	 */
	public static synchronized SocialServerApis getInstance(Context ctx) {
		if (null == mInstance) {
			mInstance = new SocialServerApis(ctx);
		}

		return mInstance;
	}

	public SocialServerApis(Context ctx) {
		mContext = ctx;
	}

	// For test
	@SuppressWarnings("unused")
	private void justsleep() {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}



	public String parseJsonInfoStringValue(String json, String fieldname) {
		String str = "";

		JSONObject j;
		try {
			j = new JSONObject(json);
			str = j.getString(fieldname);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return str;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	public String retrieveDataFromJson(
			String strResponse) {
		JSONObject j;
		String data="";
		try {
			j = new JSONObject(strResponse);
			data = j.getString("data");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
	
	public static class ChocoAvatarUrl{
		public String thumbnail;
		public String original;
	}
	public static class ChocoUser{
		public String email;
		public String fb_user_id;
		public String name;
		public String _id;
		public ChocoAvatarUrl avatar_url;
		
		//　현재 사용자가 following 하고 있는 사용자 인가?
		public boolean i_am_following;
		// 현재 사용자를 follow 하는 사용자인가?
		public boolean is_follower;
		// 이 사용자가 현재 사용자의 follow 요청을 accept 하였는가?
		public boolean follow_accepted;
		
		public void copyFromOther(ChocoUser user) {
			this.email=user.email;
			this.fb_user_id=user.fb_user_id;
			this.name=user.fb_user_id;
			this._id=user._id;
			if(avatar_url!=null){
				this.avatar_url = new ChocoAvatarUrl();
				this.avatar_url.original=user.avatar_url.original;
				this.avatar_url.thumbnail=user.avatar_url.thumbnail;
			}
			
			this.i_am_following = user.i_am_following;
			this.is_follower = user.is_follower;
			this.follow_accepted = user.follow_accepted;
		}
	}
	
	// Rreturn : Can be NULL
	public ChocoUser chocoRegisterUser(String facebook_id, String name, String profileImageUrl) {
		String strResponse = TEST_RESPONSE;
		ChocoUser user = null;
		RestfulClient restfulClient = new RestfulClient(mContext);

		restfulClient.AddParam("facebook_id", facebook_id);
		restfulClient.AddParam("name", name);
		restfulClient.AddParam("image_url", profileImageUrl);
		int responseCode = restfulClient.sendHttpRequest(
				RestfulClient.HttpReqType.HTTP_POST, mServerUrl
						+ "/api/v1/users.json");

		if (responseCode == 200) {
			strResponse = restfulClient.getResponse();
			String data = retrieveDataFromJson(strResponse);
			
			GsonBuilder gsonb = new GsonBuilder();
			Gson gson = gsonb.create();
			user = gson.fromJson(data, ChocoUser.class);
		}

		return user;
	}
	
	
	public static String mAuthToken=null;
	
	// Rreturn : Can be NULL
	public String chocoGetAuthToken(String facebook_id){
		String strResponse = TEST_RESPONSE;
		
		RestfulClient restfulClient = new RestfulClient(mContext);
		restfulClient.AddParam("facebook_id", facebook_id);
		int responseCode = restfulClient.sendHttpRequest(
				RestfulClient.HttpReqType.HTTP_POST, mServerUrl
						+ "/api/v1/tokens.json");
		if (responseCode == 200) {
			strResponse = restfulClient.getResponse();
			JSONObject j;
			try {
				j = new JSONObject(strResponse);
				mAuthToken = j.getString("token");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mAuthToken;
	}
	
	public boolean chocoLogout(){
		boolean result=false;
		RestfulClient restfulClient = new RestfulClient(mContext);
		int responseCode = restfulClient.sendHttpRequest(
				RestfulClient.HttpReqType.HTTP_GET, mServerUrl
						+ "/logout");
		if (responseCode == 200) {
			result = true;
		}else{
			String strResponse = restfulClient.getResponse();
			String data = retrieveDataFromJson(strResponse);
			Logger.e(TAG, data);
		}
		return result;
	}
	
	
	
	public static final int ACT_TYPE_COMMENT   = 0;// 0: 'Comment'
	public static final int ACT_TYPE_LIKE      = 1;// 1: 'Like/Dislike'
	public static final int ACT_TYPE_FOLLOW    = 2;// # 2: 'Follow request' (수락/거절/블록 등의 action은 피드로 나타날 필요가 없음)
	public static final int ACT_TYPE_PLAYMOVIE = 3;// # 3: 'Play movie'
	public static final int ACT_TYPE_POSTMOVIE = 4;// # 4: 'Post Movie'
	//
	public static final int TARGET_TYPE_POST = 0;// # Post
	public static final int TARGET_TYPE_COMMENT = 1;// # Comment
	public static class ChocoAction{
		public String user_id;
		public int action_type;
		public int target_type;
		public String target_id;
	}
	
	public static class ChocoFileUrl{
		public String mp4;
		public String flv;
		public String thumbnail;
	}
	
	public static class ChocoMovie{
		public String _id;// "506e942e6a8745fb1400001a",
		public String created_at;// "2012-10-05T08:02:54Z",
		public String updated_at;// "2012-10-05T08:02:59Z",
		public String file_content_type;// "video/mp4",
		public String file_file_name;// "a.mp4",
		public long file_file_size;// 1654641,
		public String file_updated_at;// "2012-10-05T08:02:54+00:00",
		public String name;// "adfadfadf",
		public long play_count;// 0,
		public String post_id;// "506e942e6a8745fb14000019",
		public boolean processing;// false,
		public String remote_source;// "",
		public String remote_url;// "",
		public List<String> thumbnails;// ["http://www.a.com/a.png"],
        public ChocoFileUrl file_url;//
	}
	
	
	public static class ChocoPost{
		public String _id;
		public String created_at;
		public String updated_at;
		public String description;
		public List<String> recipients;
		public long like_count;
		public long comment_count;
		public long view_count;
		public List<String> referred_users;
		public String scope;
		public String user_id;
		public ChocoMovie movie;
		public ChocoUser user;
	}
	
	public static class ChocoLike{
		public String _id;
		public String created_at;
		public String updated_at;
		public String user_id;
		public ChocoUser user;
	}
	
	public static class ChocoFeed{
		public String _id;
		public String created_at;
		public String updated_at;
		
		public String reader_id;
		public ChocoAction action;
		public ChocoPost post;
		public ChocoLike like;
		public ChocoComment comment;
	}
	
	public ArrayList<ChocoFeed> chocoGetFeeds(int page){
		ArrayList<ChocoFeed> result = new ArrayList<SocialServerApis.ChocoFeed>();
		//
		RestfulClient restfulClient = new RestfulClient(mContext);
		restfulClient.AddParam("auth_token", mAuthToken);
		restfulClient.AddParam("page", String.valueOf(page));
		int responseCode = restfulClient.sendHttpRequest(
				RestfulClient.HttpReqType.HTTP_GET, mServerUrl
						+ "/api/v1/action_feeds.json");
		
		String strResponse = restfulClient.getResponse();
		String data = retrieveDataFromJson(strResponse);
		
		GsonBuilder gsonb = new GsonBuilder();
		Gson gson = gsonb.create();
		
		if (responseCode == 200) {
			JSONArray jarray;
			try {
				jarray = new JSONArray(data);
				int len = jarray.length();
				for(int i=0; i<len; i++){
					String str = jarray.getString(i);
					ChocoFeed feed = gson.fromJson(str, ChocoFeed.class);
					result.add(feed);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			Logger.e(TAG, data);
		}
		return result;
	}
	
	
	public ArrayList<ChocoPost> chocoGetPopularPosts(int page){
		ArrayList<ChocoPost> result = new ArrayList<SocialServerApis.ChocoPost>();
		//
		RestfulClient restfulClient = new RestfulClient(mContext);
		restfulClient.AddParam("auth_token", mAuthToken);
		restfulClient.AddParam("page", String.valueOf(page));
		int responseCode = restfulClient.sendHttpRequest(
				RestfulClient.HttpReqType.HTTP_GET, mServerUrl
						+ "/api/v1/posts/popular.json");
		
		String strResponse = restfulClient.getResponse();
		String data = retrieveDataFromJson(strResponse);
		
		GsonBuilder gsonb = new GsonBuilder();
		Gson gson = gsonb.create();
		
		if (responseCode == 200) {
			JSONArray jarray;
			try {
				jarray = new JSONArray(data);
				int len = jarray.length();
				for(int i=0; i<len; i++){
					String str = jarray.getString(i);
					ChocoPost feed = gson.fromJson(str, ChocoPost.class);
					result.add(feed);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			Logger.e(TAG, data);
		}
		return result;
	}
	
	public ChocoPost chocoUploadMovie(
			String filepath, 
			String description,
			String name,
			String scope,
			List<String> recipients){
		ChocoPost post = null;

		// String strResponse = TEST_RESPONSE;
		RestfulClient restfulClient = new RestfulClient(mContext);
		restfulClient.AddParam("auth_token", mAuthToken);
		restfulClient.AddParam("post[description]", description);
		restfulClient.AddParam("post[movie_attributes][name]", name);
		if(scope==null){
			scope = "public";
		}
		restfulClient.AddParam("post[scope]", scope);
		if(recipients!=null){
			int index=0;
			for(String recp : recipients){
				restfulClient.AddParam("post[recipients][]", recp);
			}
		}
		
		int responseCode = restfulClient.HttpFileUpload(mServerUrl
				+ "/api/v1/posts", "post[movie_attributes][file]", filepath);

		if (responseCode == 200) {
			String strResponse = restfulClient.getResponse();
			String data = retrieveDataFromJson(strResponse);
			GsonBuilder gsonb = new GsonBuilder();
			Gson gson = gsonb.create();
			post = gson.fromJson(data, ChocoPost.class);
		}

		return post;
	}
	
	
	//String myUserId = UserService.getMyUserId2(getApplicationContext());
	public ChocoUser chocoUpdateUser(
			String user_id,
			String name,
			String email,
			String avatar_filepath){
		ChocoUser user = null;

		// String strResponse = TEST_RESPONSE;
		RestfulClient restfulClient = new RestfulClient(mContext);
		restfulClient.AddParam("auth_token", mAuthToken);
		if(name!=null) restfulClient.AddParam("user[name]", name);
		if(email!=null) restfulClient.AddParam("user[email]", email);
		int responseCode = restfulClient.HttpFileUpload(
				mServerUrl + "/api/v1/users/" + user_id, 
				"user[avatar]", 
				avatar_filepath,
				"PUT");

		if (responseCode == 200) {
			String strResponse = restfulClient.getResponse();
			String data = retrieveDataFromJson(strResponse);
			GsonBuilder gsonb = new GsonBuilder();
			Gson gson = gsonb.create();
			user = gson.fromJson(data, ChocoUser.class);
		}

		return user;
	}
	
	public ChocoUser chocoGetUserInfo( String user_id ){
		ChocoUser user = null;
		
		
		RestfulClient restfulClient = new RestfulClient(mContext);
		restfulClient.AddParam("auth_token", mAuthToken);
		int responseCode = restfulClient.sendHttpRequest(
				RestfulClient.HttpReqType.HTTP_GET, 
				mServerUrl + "/api/v1/users/" + user_id
		);
		if (responseCode == 200) {
			String strResponse = restfulClient.getResponse();
			String data = retrieveDataFromJson(strResponse);
			GsonBuilder gsonb = new GsonBuilder();
			Gson gson = gsonb.create();
			user = gson.fromJson(data, ChocoUser.class);
		}

		return user;
	}
	
	
	
	public static class ChocoFollowInfo{
		public boolean accepted;
		public ChocoUser user;
	}
	
	
	
	public List<ChocoFollowInfo> chocoFollowGetFollowings(){
		List<ChocoFollowInfo> result = new ArrayList<SocialServerApis.ChocoFollowInfo>();
		
		RestfulClient restfulClient = new RestfulClient(mContext);
		restfulClient.AddParam("auth_token", mAuthToken);
		int responseCode = restfulClient.sendHttpRequest(
				RestfulClient.HttpReqType.HTTP_POST, 
				mServerUrl + "/api/v1/users/following.json"
		);
		String strResponse = restfulClient.getResponse();
		String data = retrieveDataFromJson(strResponse);
		
		GsonBuilder gsonb = new GsonBuilder();
		Gson gson = gsonb.create();
		
		if (responseCode == 200) {
			JSONArray jarray;
			try {
				jarray = new JSONArray(data);
				int len = jarray.length();
				for(int i=0; i<len; i++){
					String str = jarray.getString(i);
					ChocoFollowInfo feed = gson.fromJson(str, ChocoFollowInfo.class);
					result.add(feed);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			Logger.e(TAG, data);
		}
		return result;
	}
	
	
	public List<ChocoFollowInfo> chocoFollowGetFollowers(){
		List<ChocoFollowInfo> result = new ArrayList<SocialServerApis.ChocoFollowInfo>();
		
		RestfulClient restfulClient = new RestfulClient(mContext);
		restfulClient.AddParam("auth_token", mAuthToken);
		int responseCode = restfulClient.sendHttpRequest(
				RestfulClient.HttpReqType.HTTP_POST, 
				mServerUrl + "/api/v1/users/followers.json"
		);
		String strResponse = restfulClient.getResponse();
		String data = retrieveDataFromJson(strResponse);
		
		GsonBuilder gsonb = new GsonBuilder();
		Gson gson = gsonb.create();
		
		if (responseCode == 200) {
			JSONArray jarray;
			try {
				jarray = new JSONArray(data);
				int len = jarray.length();
				for(int i=0; i<len; i++){
					String str = jarray.getString(i);
					ChocoFollowInfo feed = gson.fromJson(str, ChocoFollowInfo.class);
					result.add(feed);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			Logger.e(TAG, data);
		}
		return result;
	}
	
	
	public List<ChocoUser> chocoFollowGetFollowerRequests(){
		List<ChocoUser> result = new ArrayList<SocialServerApis.ChocoUser>();
		
		RestfulClient restfulClient = new RestfulClient(mContext);
		restfulClient.AddParam("auth_token", mAuthToken);
		int responseCode = restfulClient.sendHttpRequest(
				RestfulClient.HttpReqType.HTTP_POST, 
				mServerUrl + "/api/v1/users/follow_requests.json"
		);
		String strResponse = restfulClient.getResponse();
		String data = retrieveDataFromJson(strResponse);
		
		GsonBuilder gsonb = new GsonBuilder();
		Gson gson = gsonb.create();
		
		if (responseCode == 200) {
			JSONArray jarray;
			try {
				jarray = new JSONArray(data);
				int len = jarray.length();
				for(int i=0; i<len; i++){
					String str = jarray.getString(i);
					ChocoUser feed = gson.fromJson(str, ChocoUser.class);
					result.add(feed);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			Logger.e(TAG, data);
		}
		return result;
	}
	
	
	
	
	public boolean chocoFollowRequestFollow(String to){
		boolean result = false;
		
		RestfulClient restfulClient = new RestfulClient(mContext);
		restfulClient.AddParam("auth_token", mAuthToken);
		restfulClient.AddParam("to", to);
		int responseCode = restfulClient.sendHttpRequest(
				RestfulClient.HttpReqType.HTTP_POST, 
				mServerUrl + "/api/v1/users/follow.json"
		);
		String strResponse = restfulClient.getResponse();
		String data = retrieveDataFromJson(strResponse);
		
		GsonBuilder gsonb = new GsonBuilder();
		Gson gson = gsonb.create();
		
		if (responseCode == 200) {
			result = true;
		}else{
			Logger.e(TAG, data);
		}
		return result;
	}
	
	public boolean chocoFollowUnfollow(String to){
		boolean result = false;
		
		RestfulClient restfulClient = new RestfulClient(mContext);
		restfulClient.AddParam("auth_token", mAuthToken);
		restfulClient.AddParam("to", to);
		int responseCode = restfulClient.sendHttpRequest(
				RestfulClient.HttpReqType.HTTP_POST, 
				mServerUrl + "/api/v1/users/unfollow.json"
		);
		String strResponse = restfulClient.getResponse();
		String data = retrieveDataFromJson(strResponse);
		
		GsonBuilder gsonb = new GsonBuilder();
		Gson gson = gsonb.create();
		
		if (responseCode == 200) {
			result = true;
		}else{
			Logger.e(TAG, data);
		}
		return result;
	}
	
	public boolean chocoFollowAcceptFollow(String to){
		boolean result = false;
		
		RestfulClient restfulClient = new RestfulClient(mContext);
		restfulClient.AddParam("auth_token", mAuthToken);
		restfulClient.AddParam("to", to);
		int responseCode = restfulClient.sendHttpRequest(
				RestfulClient.HttpReqType.HTTP_POST, 
				mServerUrl + "/api/v1/users/accept_follow.json"
		);
		String strResponse = restfulClient.getResponse();
		String data = retrieveDataFromJson(strResponse);
		
		GsonBuilder gsonb = new GsonBuilder();
		Gson gson = gsonb.create();
		
		if (responseCode == 200) {
			result = true;
		}else{
			Logger.e(TAG, data);
		}
		return result;
	}
	
	public boolean chocoFollowRejectFollow(String to){
		boolean result = false;
		
		RestfulClient restfulClient = new RestfulClient(mContext);
		restfulClient.AddParam("auth_token", mAuthToken);
		restfulClient.AddParam("to", to);
		int responseCode = restfulClient.sendHttpRequest(
				RestfulClient.HttpReqType.HTTP_POST, 
				mServerUrl + "/api/v1/users/reject_follow.json"
		);
		String strResponse = restfulClient.getResponse();
		String data = retrieveDataFromJson(strResponse);
		
		GsonBuilder gsonb = new GsonBuilder();
		Gson gson = gsonb.create();
		
		if (responseCode == 200) {
			result = true;
		}else{
			Logger.e(TAG, data);
		}
		return result;
	}
	
	public boolean chocoFollowBlockFollow(String to){
		boolean result = false;
		
		RestfulClient restfulClient = new RestfulClient(mContext);
		restfulClient.AddParam("auth_token", mAuthToken);
		restfulClient.AddParam("to", to);
		int responseCode = restfulClient.sendHttpRequest(
				RestfulClient.HttpReqType.HTTP_POST, 
				mServerUrl + "/api/v1/users/block_follow.json"
		);
		String strResponse = restfulClient.getResponse();
		String data = retrieveDataFromJson(strResponse);
		
		GsonBuilder gsonb = new GsonBuilder();
		Gson gson = gsonb.create();
		
		if (responseCode == 200) {
			result = true;
		}else{
			Logger.e(TAG, data);
		}
		return result;
	}
	
	
	public boolean chocoFollowUnblockFollow(String to){
		boolean result = false;
		
		RestfulClient restfulClient = new RestfulClient(mContext);
		restfulClient.AddParam("auth_token", mAuthToken);
		restfulClient.AddParam("to", to);
		int responseCode = restfulClient.sendHttpRequest(
				RestfulClient.HttpReqType.HTTP_POST, 
				mServerUrl + "/api/v1/users/unblock_follow.json"
		);
		String strResponse = restfulClient.getResponse();
		String data = retrieveDataFromJson(strResponse);
		
		GsonBuilder gsonb = new GsonBuilder();
		Gson gson = gsonb.create();
		
		if (responseCode == 200) {
			result = true;
		}else{
			Logger.e(TAG, data);
		}
		return result;
	}
	
	
	
	
	
	public static class ChocoComment{
		public String _id;
		public String user_id;
		public String description;
		public String post_id;
		public String reply_to_id;
		public String created_at;
		public String updated_at;
		public ChocoUser user;
	}
	
	public List<ChocoComment> chocoCommentListForPost(String post_id, int page){
		List<ChocoComment> result = new ArrayList<SocialServerApis.ChocoComment>();
		
		RestfulClient restfulClient = new RestfulClient(mContext);
		restfulClient.AddParam("auth_token", mAuthToken);
		restfulClient.AddParam("id", post_id);
		restfulClient.AddParam("page", String.valueOf(page));
		int responseCode = restfulClient.sendHttpRequest(
				RestfulClient.HttpReqType.HTTP_POST, 
				mServerUrl + "/api/v1/posts/comments.json"
		);
		String strResponse = restfulClient.getResponse();
		String data = retrieveDataFromJson(strResponse);
		
		GsonBuilder gsonb = new GsonBuilder();
		Gson gson = gsonb.create();
		
		if (responseCode == 200) {
			JSONArray jarray;
			try {
				jarray = new JSONArray(data);
				int len = jarray.length();
				for(int i=0; i<len; i++){
					String str = jarray.getString(i);
					ChocoComment feed = gson.fromJson(str, ChocoComment.class);
					result.add(feed);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			Logger.e(TAG, data);
		}
		return result;
	}
	
	public ChocoComment chocoCommentWriteToPost(String description, String post_id, String reply_to_id){
		ChocoComment result = null;
		
		RestfulClient restfulClient = new RestfulClient(mContext);
		restfulClient.AddParam("auth_token", mAuthToken);
		restfulClient.AddParam("post_id", post_id);
		if(reply_to_id!=null){
			restfulClient.AddParam("reply_to_id", reply_to_id);
		}
		restfulClient.AddParam("description", description);
		int responseCode = restfulClient.sendHttpRequest(
				RestfulClient.HttpReqType.HTTP_POST, 
				mServerUrl + "/api/v1/posts/create_comment.json"
		);
		String strResponse = restfulClient.getResponse();
		String data = retrieveDataFromJson(strResponse);
		
		GsonBuilder gsonb = new GsonBuilder();
		Gson gson = gsonb.create();
		
		if (responseCode == 200) {
			result = gson.fromJson(data, ChocoComment.class);
		}else{
			Logger.e(TAG, data);
		}
		return result;
	}
	
	
	public boolean chocoCommentDelete(String comment_id){
		boolean result = false;
		
		RestfulClient restfulClient = new RestfulClient(mContext);
		restfulClient.AddParam("auth_token", mAuthToken);
		restfulClient.AddParam("id", comment_id);
		int responseCode = restfulClient.sendHttpRequest(
				RestfulClient.HttpReqType.HTTP_POST, 
				mServerUrl + "/api/v1/posts/delete_comment.json"
		);
		String strResponse = restfulClient.getResponse();
		String data = retrieveDataFromJson(strResponse);
		
		GsonBuilder gsonb = new GsonBuilder();
		Gson gson = gsonb.create();
		
		if (responseCode == 200) {
			result = true;
		}else{
			Logger.e(TAG, data);
		}
		return result;
	}
	
	
	
	
	
	
	
	public boolean chocoLikePost(String post_id){
		boolean result = false;
		
		RestfulClient restfulClient = new RestfulClient(mContext);
		restfulClient.AddParam("auth_token", mAuthToken);
		restfulClient.AddParam("id", post_id);
		int responseCode = restfulClient.sendHttpRequest(
				RestfulClient.HttpReqType.HTTP_POST, 
				mServerUrl + "/api/v1/posts/like_post.json"
		);
		String strResponse = restfulClient.getResponse();
		String data = retrieveDataFromJson(strResponse);
		
		GsonBuilder gsonb = new GsonBuilder();
		Gson gson = gsonb.create();
		
		if (responseCode == 200) {
			result = true;
		}else{
			Logger.e(TAG, data);
		}
		return result;
	}
	
	public boolean chocoLikeComment(String comment_id){
		boolean result = false;
		
		RestfulClient restfulClient = new RestfulClient(mContext);
		restfulClient.AddParam("auth_token", mAuthToken);
		restfulClient.AddParam("id", comment_id);
		int responseCode = restfulClient.sendHttpRequest(
				RestfulClient.HttpReqType.HTTP_POST, 
				mServerUrl + "/api/v1/posts/like_comment.json"
		);
		String strResponse = restfulClient.getResponse();
		String data = retrieveDataFromJson(strResponse);
		
		GsonBuilder gsonb = new GsonBuilder();
		Gson gson = gsonb.create();
		
		if (responseCode == 200) {
			result = true;
		}else{
			Logger.e(TAG, data);
		}
		return result;
	}
	
	// 'type' : "post" 또는 "comment"
	// 'id' : 위 type 파라미터에 따라 포스트또는 코멘트의 id
	public boolean chocoLikeCancel(String type, String id){
		boolean result = false;
		
		RestfulClient restfulClient = new RestfulClient(mContext);
		restfulClient.AddParam("auth_token", mAuthToken);
		restfulClient.AddParam("type", type);
		restfulClient.AddParam("id", id);
		int responseCode = restfulClient.sendHttpRequest(
				RestfulClient.HttpReqType.HTTP_POST, 
				mServerUrl + "/api/v1/posts/unlike.json"
		);
		String strResponse = restfulClient.getResponse();
		String data = retrieveDataFromJson(strResponse);
		
		GsonBuilder gsonb = new GsonBuilder();
		Gson gson = gsonb.create();
		
		if (responseCode == 200) {
			result = true;
		}else{
			Logger.e(TAG, data);
		}
		return result;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	public List<ChocoUser> chocoFindUsersByFacebookId(List<String> facebook_ids){
		List<ChocoUser> result = new ArrayList<SocialServerApis.ChocoUser>();
		
		RestfulClient restfulClient = new RestfulClient(mContext);
		restfulClient.AddParam("auth_token", mAuthToken);
		for( String fbid : facebook_ids ){
			restfulClient.AddParam("fb_ids[]", fbid);
		}
		
		int responseCode = restfulClient.sendHttpRequest(
				RestfulClient.HttpReqType.HTTP_POST, 
				mServerUrl + "/api/v1/users/find_by_fbid.json"
		);
		String strResponse = restfulClient.getResponse();
		String data = retrieveDataFromJson(strResponse);
		
		GsonBuilder gsonb = new GsonBuilder();
		Gson gson = gsonb.create();
		
		if (responseCode == 200) {
			JSONArray jarray;
			try {
				jarray = new JSONArray(data);
				int len = jarray.length();
				for(int i=0; i<len; i++){
					String str = jarray.getString(i);
					ChocoUser feed = gson.fromJson(str, ChocoUser.class);
					result.add(feed);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			Logger.e(TAG, data);
		}
		return result;
	}
}
