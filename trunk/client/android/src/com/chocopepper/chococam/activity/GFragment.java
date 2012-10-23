package com.chocopepper.chococam.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;



public class GFragment extends Fragment {

//	private Typeface font;
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		
//		font = FontUtil.getInstance(getActivity().getApplicationContext()).getDefaultFont();
		
		ViewGroup group = (ViewGroup)view;
		int childCnt = group.getChildCount();
		for(int i=0; i < childCnt; i++){
			View v = group.getChildAt(i);
			if(v instanceof TextView){
//				((TextView)v).setTypeface(font);
			}
		}
		return view;
	}
	
	public void OnMyResume(){
		
	}
	
	
	protected boolean mbActivityCreated=false;
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mbActivityCreated = true;
	}
}
