package com.chocopepper.chococam.util;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;

import com.chocopepper.chococam.R;

public class MyProgressDialog extends Dialog {
	private static final boolean USE_SEPARATE_ACTIVITY_FOR_PROGRESS = false;
	private static MyProgressActivity mProgressActivityInstance = null;
	private static int progressReferenceCount = 0;
	private static final String TAG = Logger.makeLogTag(MyProgressDialog.class);
	
	@SuppressWarnings("rawtypes")
	private static ArrayList<AsyncTask> m_tasks = new ArrayList<AsyncTask>();
	@SuppressWarnings("rawtypes")
	public void addTask(AsyncTask task) {
		m_tasks.add(task);
	}
	@SuppressWarnings("rawtypes")
	public static void cancelAllTasks() {
		for (AsyncTask task : m_tasks) {
			if (task != null) {
				task.cancel(true);
			}
		}
	}
	public static void setProgressActivityInstance(MyProgressActivity act) {
		mProgressActivityInstance = act;
		m_tasks.clear();
	}
	
	
	
	private Activity mParentActivity = null;
	public static MyProgressDialog show(Activity context, boolean isBlur,
			CharSequence title, CharSequence message) {
		return show(context, title, message, false, false, isBlur);
	}

	public static MyProgressDialog show(Activity context, CharSequence title,
			CharSequence message) {
		return show(context, title, message, false, false, false);
	}

	public static MyProgressDialog show(Activity context, CharSequence title,
			CharSequence message, boolean indeterminate, boolean cancelable,
			boolean isBlur) {
		if (context == null) {
			//context = MainActivity.MainContext;
			Logger.e(TAG, "CONTEXT IS NULL");
			return null;
		}
		
		if( context.isFinishing() ){
			Logger.e(TAG, "ACTIVITY IS FINISHING");
			return null;
		}
		
		
		
		MyProgressDialog dialog = new MyProgressDialog(context, isBlur);
		if (USE_SEPARATE_ACTIVITY_FOR_PROGRESS==false) {
			try {
				dialog.getWindow().setLayout(LayoutParams.FILL_PARENT,
						LayoutParams.FILL_PARENT);
				dialog.setTitle(title);
				dialog.setCancelable(cancelable);
				// dialog.setOnCancelListener(cancelListener);

				dialog.show();
			} catch (Exception e) {
				Logger.e(TAG, e.toString());
			}
		} else {
			progressReferenceCount++;
			if (progressReferenceCount == 1) {
				Intent i = new Intent(context, MyProgressActivity.class);
				context.startActivity(i);
			}
			Logger.e(TAG, String.format("REFCOUNT = %d", progressReferenceCount));
		}

		return dialog;
	}

		@Override
	public void dismiss() {
		if (USE_SEPARATE_ACTIVITY_FOR_PROGRESS==false) {
			try{
				super.dismiss();
			}
			catch(Exception e){
				Logger.e(TAG, e.toString() );
			}
		} else {
			progressReferenceCount--;
			Log.e("TAG", String.format("REFCOUNT = %d", progressReferenceCount));
			if (progressReferenceCount < 1) {
				if (mProgressActivityInstance != null) {
					mProgressActivityInstance.finish();
					mProgressActivityInstance.overridePendingTransition(0, 0);
					mProgressActivityInstance = null;
					Log.e("TAG", "CLOSE ACTIVITY");
				} else {
					Log.e("TAG", "ACTIVITY IS NULL!!!!!!");
				}
			}
		}
	}



		
		
		
	public MyProgressDialog(Activity context) {
		this(context, true);
		mParentActivity = context;
		setContentView(R.layout.progress);
	}


	public MyProgressDialog(Activity context, boolean isBlur) {
		super(context, R.style.MyProgressDialog);
		mParentActivity = context;
		if (isBlur)
			setContentView(R.layout.progress);
		else
			setContentView(R.layout.progress_noblur);
	}
}
