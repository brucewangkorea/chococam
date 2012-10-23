package com.chocopepper.chococam.activity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.chocopepper.chococam.util.Logger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;

public class ActivityUtils {
	private static final String TAG = Logger.makeLogTag(ActivityUtils.class);
	// 앱 종료시에 통지할 리스너
	private static List<FinishApplicationListener> finishAppListeners = null;

    /**
     * 앱 종료시 통지받기 위한 listener.
     * @param listener
     */
	public static void addFinishApplicationListener(FinishApplicationListener listener) {
		if (finishAppListeners == null) {
			finishAppListeners = new CopyOnWriteArrayList<FinishApplicationListener>();
		}
		if (!finishAppListeners.contains(listener)) {
			finishAppListeners.add(listener);
		}
	}
	
	public static void notifyFinish() {
		if (finishAppListeners != null && !finishAppListeners.isEmpty()) {
			for (FinishApplicationListener listener : finishAppListeners) {
				listener.finish();
			}
			finishAppListeners.clear();
			finishAppListeners = null;
		}
	}
	
	/**
	 * 시작시의 Intent의 Extra를 받아 다음 Intent에 추가시킨다.
	 * @param activity  현 Activity
	 * @param intent	Extra 데이터를 추가시킬 Intent
	 */
	public static void setStartIntent(Activity activity, Intent intent) {
		Bundle extras = getStartExtras(activity);
		if (extras != null)
			intent.putExtras(extras);
	}
	
	private static Bundle getStartExtras(Activity activity) {
		Intent startIntent = activity.getIntent();
		return startIntent.getExtras();
	}
	
	/**
	 * Back View Change Animation. 
	 * @param view		애니메이션을 넣을 뷰
	 * @param listener	애니메이션 리스너
	 */
	public static void setHistoryBackAnimation(View view, AnimationListener listener) {
		TranslateAnimation animation = new TranslateAnimation ( 
				Animation.RELATIVE_TO_SELF, -1,
				Animation.RELATIVE_TO_SELF, 0, 
				Animation.RELATIVE_TO_SELF, 0, 
				Animation.RELATIVE_TO_SELF, 0
		);
		
		if (listener != null)
			animation.setAnimationListener(listener);
		
		animation.setDuration(300);
		animation.setFillAfter(true);
		view.setAnimation(animation);
		animation.start();
	}
	
	/**
	 * Next View Change Animation
	 * @param view		애니메이션을 넣을 뷰
	 * @param listener	애니메이션 리스너
	 */
	public static void setHistoryNextAnimation(View view, AnimationListener listener) {
		TranslateAnimation animation = new TranslateAnimation ( 
				Animation.RELATIVE_TO_SELF, 1,
				Animation.RELATIVE_TO_SELF, 0, 
				Animation.RELATIVE_TO_SELF, 0, 
				Animation.RELATIVE_TO_SELF, 0
		);
		
		if (listener != null)
			animation.setAnimationListener(listener);
		
		animation.setDuration(300);
		animation.setFillAfter(true);
		view.setAnimation(animation);
		animation.start();
	}
	
	/**
	 * Title View Change Animation
	 * @param view		애니메이션을 넣을 뷰
	 * @param listener	애니메이션 리스너
	 */
	public static void setTitleContentShowAnimation(View view, AnimationListener listener) {
		TranslateAnimation animation = new TranslateAnimation ( 
				Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 0, 
				Animation.RELATIVE_TO_SELF, -1, 
				Animation.RELATIVE_TO_SELF, 0
		);
		
		if (listener != null)
			animation.setAnimationListener(listener);
		
		animation.setDuration(300);
		animation.setFillAfter(true);
		view.setAnimation(animation);
		animation.start();
	}
	
	/**
	 * Title back View Change Animation
	 * @param view		애니메이션을 넣을 뷰
	 * @param listener	애니메이션 리스너
	 */
	public static void setTitleContentHideAnimation(View view, AnimationListener listener) {
		TranslateAnimation animation = new TranslateAnimation ( 
				Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 0, 
				Animation.RELATIVE_TO_SELF, 0, 
				Animation.RELATIVE_TO_SELF, -1
		);
		
		if (listener != null)
			animation.setAnimationListener(listener);
		
		animation.setDuration(300);
		animation.setFillAfter(true);
		view.setAnimation(animation);
		animation.start();
	}
}
