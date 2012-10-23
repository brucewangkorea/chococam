package com.chocopepper.chococam.activity.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chocopepper.chococam.R;
import com.chocopepper.chococam.util.Constants;
import com.chocopepper.chococam.util.Logger;
import com.chocopepper.chococam.util.Utils;

public class UserNameActivity extends Activity implements View.OnClickListener {
	
	
	private String length;
	private int count;
	
	//위젯 설정
	EditText custom_edit;
	TextView txtLength;
	TextView txtTitle;
	Button btnOk;
	
	private TextWatcher watcher;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_name);

//		FontUtil fontutil = FontUtil.getInstance(this.getApplicationContext());
//		Typeface font = fontutil.getDefaultFont();
		
		custom_edit = (EditText) findViewById(R.id.activity_message_edit);
//		custom_edit.setTypeface(font);
		txtLength = (TextView) findViewById(R.id.txtLength_info);
//		txtLength.setTypeface(font);
		txtTitle = (TextView) findViewById(R.id.textView3);
//		txtTitle.setTypeface(font);
		btnOk = (Button) findViewById(R.id.custom_btn);
//		btnOk.setTypeface(font);
		btnOk.setOnClickListener(this);
		btnOk.setOnTouchListener(
			    new View.OnTouchListener() {
			    public boolean onTouch(View v, MotionEvent event) {
			                  switch(event.getAction()){
			                  case MotionEvent.ACTION_DOWN:
			                	  btnOk.setBackgroundResource(R.drawable.btn_mail_invite_pre);  //버튼이 선택되었을때 이미지를 교체
			                      break;
			                  case MotionEvent.ACTION_UP:
			                	  btnOk.setBackgroundResource(R.drawable.btn_mail_invite_nor); //버튼에서 손을 떼었을때 이미지를 복구
			                      break;
			                  }
			                  return false;
			              }
			          }
			      ); 

		Intent i = getIntent();
		String userName = i.getStringExtra(Constants.RESULT_USER_NAME);
		
		//이전 뷰에 있던 값을 넣어준다
		if(userName.length() <= 0)
		{
			custom_edit.setText("");
		}
		else
		{
			custom_edit.setText(userName);
		}
		
		length = custom_edit.getText().toString();
		count = length.length();
		txtLength.setText(count + "/20");
		
		watcher = new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {}

			@Override
			public void afterTextChanged(Editable s) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				length = custom_edit.getText().toString();
				count = length.length();
				txtLength.setText(count + "/20");
			}
		};
			
		custom_edit.addTextChangedListener(watcher);
		
		custom_edit.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					return true;
				}
				return false;
			}
		});

	}

	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.custom_btn:
			
			if (custom_edit.getText().toString().length() <= 0) {
				Toast.makeText(this, getResources().getString(R.string.nameEmptyError), Constants.DEFAULT_TOAST_DURATION).show();
			}
//			else if(isChosung(custom_edit.getText().toString()))
//			{
//				Toast.makeText(this, getResources().getString(R.string.name_exp_error), 3000).show();
//			}
			else if(Utils.isChosung(custom_edit.getText().toString()))
			{
				Toast.makeText(this, getResources().getString(R.string.name_exp_error), Constants.DEFAULT_TOAST_DURATION).show();
			}
			else
			{
				Logger.e("UserNameActivity", "user Name = " + custom_edit.getText().toString());
				Intent i = new Intent();
				i.putExtra(Constants.RESULT_USER_NAME, custom_edit.getText().toString());
				this.setResult(RESULT_OK, i);
				finish();
			}
			
			break;
		}
	}

	public void onBackPressed() {
		finish();
	}
	
//	private boolean isChosung(String userName)
//	{	
//		CharSequence temp = userName;
//		// 자음 또는 모음만으로 된 경우.
//		String patternRegex = "[ㄱ-ㅎㅏ-ㅣ]";
//		Pattern pattern = Pattern.compile(patternRegex);
//		Matcher match = pattern.matcher(temp);
//		boolean bFound = match.find();
//		return bFound;
//	}
	
}
