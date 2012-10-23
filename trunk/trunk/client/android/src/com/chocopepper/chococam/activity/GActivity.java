package com.chocopepper.chococam.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;



public class GActivity extends Activity {

//	protected Typeface font = null;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		font = FontUtil.getInstance(getApplicationContext()).getDefaultFont();
	}

	@Override
	public void setContentView(int viewId) {
		View view = LayoutInflater.from(this).inflate(viewId, null);
		ViewGroup group = (ViewGroup)view;
		int childCnt = group.getChildCount();
		for(int i=0; i < childCnt; i++){
			View v = group.getChildAt(i);
			if ((v instanceof TextView) || (v instanceof Button) || (v instanceof EditText)) {
//				((TextView)v).setTypeface(font);
			}
		}
		super.setContentView(view);
	}



}
