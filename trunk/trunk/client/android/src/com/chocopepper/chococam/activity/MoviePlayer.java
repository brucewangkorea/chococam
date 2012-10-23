package com.chocopepper.chococam.activity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import com.chocopepper.chococam.R;
import com.chocopepper.chococam.util.Constants;
import com.chocopepper.chococam.util.Logger;

/**
 * 12-10-11 
 * @author nerine
 * 동영상 재생 액티비티
 */
public class MoviePlayer extends Activity {
	
	private static final String TAG = Logger.makeLogTag(MoviePlayer.class);
	
	@Override
	public void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.movie_player);
		 
		// 동영상의 주소를 인탠트로 전달 받는다.
		Intent intent = getIntent();		
		String movie_url = Constants.SOCIAL_SERVER_URL + intent.getStringExtra(Constants.MOVIE_URL);
//		String movie_url = intent.getStringExtra(Constants.MOVIE_URL);
		
		if(movie_url == null || movie_url.trim().length() < 1)
		{
			Logger.e(TAG, "movie_url is NULL!!!");			
			return;
		}
		
		VideoView videoView = (VideoView) findViewById(R.id.VideoView);
		MediaController mediaController = new MediaController(this);
		mediaController.setAnchorView(videoView);

		Uri video = Uri.parse(movie_url);

		videoView.setMediaController(mediaController);
		videoView.setVideoURI(video);
		videoView.requestFocus();
//		videoView.start();
		// 비디오 재생 시작 리스너
		videoView.setOnPreparedListener(new OnPreparedListener()
        {
            @Override
            public void onPrepared(MediaPlayer mp)
            {            	
                mp.start();
            }
        });
		
		// 비디오 재생 에러 리스터
		videoView.setOnErrorListener(new OnErrorListener() {			
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) 
			{
				Logger.e(TAG, "Video Play Error = " + what + "//" + extra);
				
				// TODO
				// 비디오 재생 에러 시 액티비티를 종료 시킨다. 
				// 추후에 더 좋은 처리 방법으로 변경이 필요하다.
				finish();				
				return true;
			}
		});
	}
}
