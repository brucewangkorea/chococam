package com.chocopepper.chococam.util;

import android.os.Environment;


public class Constants {
	
	public static final int FACEBOOK_AUTHORIZE_ACTIVITY_RESULT_CODE = 0;
	public static final String  FACEBOOK_OPENGRAPH_LIKEACTION_URL_FORMAT = "http://www.gamchen.com:3000/games/%d";
	
	public static final int PICK_FROM_CAMERA = 9990;
	public static final int PICK_FROM_ALBUM = 9991;
	public static final int CROP_FROM_CAMERA = 9992;
	
	
	public static final String MOBILE_WEB_URL = "http://www.gamchen.com/";
	public static final String APP_INSTALL_URL = "http://projectG-web-1464147646.ap-northeast-1.elb.amazonaws.com/store";//"http://social.gamchen.com/store";
	public static final String URL_TERMS = "http://projectG-web-1464147646.ap-northeast-1.elb.amazonaws.com/service_policy";//"http://social.gamchen.com/service_policy";
	public static final String URL_TERMS_PRIVACY = "http://projectG-web-1464147646.ap-northeast-1.elb.amazonaws.com/privacy_policy";//"http://social.gamchen.com/privacy_policy";
	public static final String APP_NAME = "g";
	public static final String OS_NAME = "Android"; 
	public static final String APP_PACKAGE_NAME = "com.chocopepper.chococam";
	
	public static final int  DEFAULT_TOAST_DURATION = 4000;
	
	public static final String SHARED_PREFERENCE_NAME = "xmpp_preference";
	
    
    // file transfer
    public static final String SD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String IN_MEMORY_PATH =  Environment.getDataDirectory().getAbsolutePath();
    public static final String SD_DATA_PATH = SD_PATH + "/Android/data";
    public static final String DATA_PATH = "/data/data";
    public static final String PROJECT_PATH = "/com.chocopepper.chococam";
    
    // Account
    public static final String RESULT_PHONE_NUMBER = "pnumber";
    public static final String RESULT_COUNTRY_CODE_NUMBER = "ccnumber";
    public static final String RESULT_TERMS_CHECK = "termscheck";
    public static final String RESULT_INFO_CHECK = "infocheck";
    public static final String RESULT_VERIFY_CODE_NUMBER = "vcnumber";
    public static final String RESULT_START_MAINACTIVITY = "startmain";
    public static final String RESULT_SMS_MSG = "smsmsg";
    public static String SMS_RECEIVE_ACTIVITY = "VerifyCodeActivity";

    public static final String LOGIN_RESULT = "login_result";
    public static final String LOGIN_INFO = "login_info";
    
    public static final String RESULT_SUCCESS_CODE = "200";
    public static final String RESULT_DEVICE_DUPLICATE_CODE = "201";
    public static final String RESULT_EMERGENCY_CODE = "202";
    public static final String RESULT_UPDATE_CODE = "203";
    
    // Profile
    public static final String RESULT_ACIVITY_MSG = "activitymsg";
    public static final String RESULT_USER_NAME = "user_name";
    public static final String PREFS_USER_ACTIVITY_MESSAGE = "user_activity_msg";
   
	
    // 2012-05-11 brucewang 
    // Facebook
    //public static final String PREFS_FACEBOOK = "com.google.android.facebook";
    public static final String PREFS_FACEBOOK_MY_ID = "my_facebook_id";
    public static final String PREFS_CONTACT_IGNORE_LIST_JSON = "contactlist_ignore";
    public static final String PREFS_SETTNIG_CHAT_AWAKE_PUSH_CHECK = "setting_chat_awake_push_check";
    
    
    // image cache
    public static final String IMAGE_CACHE_PATH = "/Android/data/g";
    public static final String PROFILE_IMAGE = "imgprofile.png";
    public static final String UPLOAD_IMAGE = "imgupload.png";
    public static final String PROFILE_IMAGE_PATH = SD_PATH + IMAGE_CACHE_PATH + "/" + PROFILE_IMAGE;
    public static final String PREFS_PROFILE_IMAGE_PATH = "profile_image_path";
    
    
    
    public static final int MAX_PHOTOUPLOAD_IMG_WIDTH = 1280;
    public static final int MAX_PHOTOUPLOAD_IMG_HEIGHT = 720;
    
    
    // server image path
    // TEST SERVER
	//public final static String SOCIAL_SERVER_URL = "http://123.212.42.41:8094";

    // REAL SERVER
	//public final static String SOCIAL_SERVER_URL = "http://projectG-web-1464147646.ap-northeast-1.elb.amazonaws.com";//"http://social.gamchen.com"; // social Domain
	
    // 2012-09-21 brucewang
    // New Hostway Server (Loadbalancer)
	//public final static String SOCIAL_SERVER_URL = "http://64.23.79.32";
    
    // 2012-10-04 brucewang
    // test서버.
    //public final static String SOCIAL_SERVER_DOMAINORIP = "218.36.25.68";//
    // STA test 서버.
    //public final static String SOCIAL_SERVER_DOMAINORIP = "123.212.195.24";//
    //public final static String SOCIAL_SERVER_DOMAINORIP = "192.168.1.17";//
    
    // 호스트웨이 서버
    public final static String SOCIAL_SERVER_DOMAINORIP = "64.23.68.147";//
    
	public final static String SOCIAL_SERVER_URL = "http://"+SOCIAL_SERVER_DOMAINORIP+":3000";
    
	public final static long MAXIMUM_UPLAODFILE_SIZE = (10*1024*1024);//
    
	
	public static final String SERVER_IMAGE_PATH = SOCIAL_SERVER_URL;
    
	
	// 2012-10-04 brucewang
	// 
	public static final String PREFS_AUTH_TOKEN = "auth_token";
    
    // user id
    public static final String PREFS_USER_ID = "user_id";
    
    // uuid 
    //public static final String PREFS_FILE = "device_id.xml";
    public static final String PREFS_DEVICE_ID = "device_id";
    
    // friends
    public static String TARGET_USER_ID = "target_user_id";
    
    //movie
    public static String MOVIE_URL = "movie_url";
    
    //etc
    public static String FRIENDS_FEED_LIST_CHECK = "friends_feed_check";
    
    public static boolean RESETUP_APP = false;
}

