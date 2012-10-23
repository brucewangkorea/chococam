package com.chocopepper.chococam.util;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.chocopepper.chococam.R;

public class AccountDialog extends Dialog {
	
		
	boolean singleButtonCheck = false;
	
	Context mContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();    
		lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		lpWindow.dimAmount = 0.8f;
		getWindow().setAttributes(lpWindow);
		
		setContentView(R.layout.account_dialog);
		
		//싱글 버튼 
		if(singleButtonCheck)
		{
			setSingleButtonLayout();
			setTitle(mTitle);
			setContent(mContent);			
			setClickListener(mLeftClickListener , mRightClickListener);
		}
		else
		{
			setLayout();
			setTitle(mTitle);
			setContent(mContent);
			setRightButtonName(mRightButtonText);
			setClickListener(mLeftClickListener , mRightClickListener);
		}
		
	}
	
	public AccountDialog(Context context) {
		// Dialog 배경을 투명 처리 해준다.
		super(context , android.R.style.Theme_Translucent_NoTitleBar);
	}
	
	public AccountDialog(Context context , String title , String content, Boolean btnCheck,
			View.OnClickListener singleListener) {
		super(context , android.R.style.Theme_Translucent_NoTitleBar);
		mContext = context;
		singleButtonCheck = btnCheck;
		this.mTitle = title;
		this.mContent = content;
		this.mLeftClickListener = singleListener;
	}
	
	public AccountDialog(Context context , String title , String content , String rightbuttontext,
			View.OnClickListener leftListener ,	View.OnClickListener rightListener) {
		super(context , android.R.style.Theme_Translucent_NoTitleBar);
		mContext = context;
		this.mTitle = title;
		this.mContent = content;
		this.mRightButtonText = rightbuttontext;
		this.mLeftClickListener = leftListener;
		this.mRightClickListener = rightListener;
	}
	
	private void setTitle(String title){
		mTitleView.setText(title);
	}
	
	private void setContent(String content){
		mContentView.setText(content);
	}
	
	private void setLeftButtonName(String leftName)
	{
		mLeftButton.setText(leftName);
	}
	
	private void setRightButtonName(String rightName)
	{
		mRightButton.setText(rightName);
	}
	
	private void setClickListener(View.OnClickListener left , View.OnClickListener right){
		if(left!=null && right!=null){
			mLeftButton.setOnClickListener(left);
			mRightButton.setOnClickListener(right);
		}else if(left!=null && right==null){
			mLeftButton.setOnClickListener(left);
		}else {
			
		}
	}
	
	@Override
	public void onBackPressed()
	{
		super.onBackPressed();		
	}
	
	/*
	 * Layout
	 */
	private TextView mTitleView;
	private TextView mContentView;
	private Button mLeftButton;
	private Button mRightButton;
	private String mTitle;
	private String mContent;
	private String mLeftButtonText;
	private String mRightButtonText;
	
	
	private View.OnClickListener mLeftClickListener;
	private View.OnClickListener mRightClickListener;
	
	/*
	 * Layout
	 */
	private void setLayout(){
		mTitleView = (TextView) findViewById(R.id.tv_title);
		mContentView = (TextView) findViewById(R.id.tv_content);
		mLeftButton = (Button) findViewById(R.id.btnOK);
		mRightButton = (Button) findViewById(R.id.btnCancel);
	}
	
	/*
	 * Layout
	 */
	private void setSingleButtonLayout(){
		mTitleView = (TextView) findViewById(R.id.tv_title);
		mContentView = (TextView) findViewById(R.id.tv_content);
		mLeftButton = (Button) findViewById(R.id.btnOK);
		mRightButton = (Button) findViewById(R.id.btnCancel);
		mRightButton.setVisibility(View.GONE);		
	}
	
}









