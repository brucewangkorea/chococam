package com.chocopepper.chococam.activity.friends;

import java.util.List;
import java.util.Vector;

import lib.viewpagerindicator.actionbar.TitlePageIndicator;
import lib.viewpagerindicator.actionbar.TitlePageIndicator.IndicatorStyle;
import lib.viewpagerindicator.actionbar.TitleProvider;
import lib.viewpagerindicator.normal.ViewPagerIndicator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;

import com.chocopepper.chococam.R;
import com.chocopepper.chococam.activity.GFragment;
import com.chocopepper.chococam.util.Logger;

public class FeedsFragmentActivity extends FragmentActivity {

	public class GameFeedsPagerAdapter extends FragmentPagerAdapter implements ViewPagerIndicator.PageInfoProvider, TitleProvider {
		private List<Fragment> fragments;
		private Context mContext;

		public GameFeedsPagerAdapter(FragmentManager fm, List<Fragment> fragments, Context context) {
			super(fm);
			this.mContext = context;
			this.fragments = fragments;
		}

		@Override
		public Fragment getItem(int position) {
			return this.fragments.get(position);
		}

		@Override
		public int getCount() {
			return this.fragments.size();
		}
		
		@Override
		public String getTitle(int pos) {		
			String name = Integer.toString(pos);
			switch (pos) {
			case 0:
				name = mContext.getResources().getString(R.string.subtab_my_friends_feed);
				break;
			case 1:
				name = mContext.getResources().getString(R.string.subtab_popular_feed);
				break;
			default :
				break;
			}
			return name;
		}
	}
	
	
	private GameFeedsPagerAdapter mPagerAdapter;
	private ViewPager  mViewPager;
//	private ViewPagerIndicator mIndicator;
	private TitlePageIndicator indicator;
	List<Fragment> fragments;
	private static final String TAG = Logger.makeLogTag(FeedsFragmentActivity.class);
//	private Typeface font;
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);
        
        fragments = new Vector<Fragment>();
		fragments.add(Fragment.instantiate(this, FriendsFeedActivity.class.getName()));
		fragments.add(Fragment.instantiate(this, PopularFeedsActivity.class.getName()));
		
        
        // Create our custom adapter to supply pages to the viewpager.
        mPagerAdapter = new GameFeedsPagerAdapter(getSupportFragmentManager(), fragments, this);
        mViewPager = (ViewPager)findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);
        
        // Start at a custom position
        mViewPager.setCurrentItem(0);

        // Find the indicator from the layout
        indicator = (TitlePageIndicator)findViewById(R.id.indicator);
        indicator.setFooterIndicatorStyle(IndicatorStyle.None);
        indicator.setViewPager(mViewPager, 0);

        mViewPager.setOnPageChangeListener(indicator);
    }
    
	@Override
	protected void onResume() {
		refreshSubTabs();
		
		super.onResume();
	}
	
	
	public void refreshSubTabs(){
		int i = mViewPager.getCurrentItem();
		Fragment frg = fragments.get(i);
		GFragment gfrg = (GFragment)frg;
		gfrg.OnMyResume();
//		for( Fragment frg : fragments ){
//			GFragment gfrg = (GFragment)frg;
//			gfrg.OnMyResume();
//		}
	}
	
	public void uiUpdateSubPage(Boolean result, String strSvrMsg, int position){
		int i = mViewPager.getCurrentItem();
		Fragment frg = fragments.get(i);
		//GFragment gfrg = (GFragment)frg;
		if( frg.getClass().getSimpleName().equals(FriendsFeedActivity.class.getSimpleName()) ){
			FriendsFeedActivity gfrg = (FriendsFeedActivity)frg;
			gfrg.uiUpdate(result, strSvrMsg, position);
		}else if( frg.getClass().getSimpleName().equals(PopularFeedsActivity.class.getSimpleName()) ){
			PopularFeedsActivity gfrg = (PopularFeedsActivity)frg;
			gfrg.uiUpdate(result, strSvrMsg, position);
		}
		
	}

	private final DialogInterface.OnClickListener mlistenerExitYes = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dlg, int nWhich) {
			FeedsFragmentActivity.this.finish();			
		}
	};

	private final DialogInterface.OnClickListener mlistenerExitNo = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dlg, int nWhich) {
			dlg.cancel();
		}
	};

	@Override
	public boolean onKeyDown(int nKeyCode, KeyEvent event) {
		boolean fHandled = false;

		switch (nKeyCode) {
		case KeyEvent.KEYCODE_BACK:
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder
				.setCancelable(false)
				.setTitle(R.string.dlg_exit_title)
//				.setIcon(R.drawable.ic_launcher)
				.setMessage(R.string.dlg_exit_msg)
				.setPositiveButton(R.string.dlg_exit_yes, mlistenerExitYes)
				.setNegativeButton(R.string.dlg_exit_no, mlistenerExitNo);
			final Dialog dlg = builder.create();
			dlg.show();
			fHandled = true;
		}

		return (fHandled) ? fHandled : super.onKeyDown(nKeyCode, event);
	}
	// ]]	
}
