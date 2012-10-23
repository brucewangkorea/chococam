package com.chocopepper.chococam.activity;



import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class GFragmentActivity extends FragmentActivity {

//	private Typeface font = null;
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		font = FontUtil.getInstance(getApplicationContext()).getDefaultFont();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#setContentView(int)
	 */
	@Override
	public void setContentView(int layoutResID) {
		View view = LayoutInflater.from(this).inflate(layoutResID, null);
		ViewGroup group = (ViewGroup)view;
		int childCnt = group.getChildCount();
		for(int i=0; i < childCnt; i++){
			View v = group.getChildAt(i);
			if(v instanceof TextView){
//				((TextView)v).setTypeface(font);
			}
		}
		super.setContentView(layoutResID);
	}
	
}
