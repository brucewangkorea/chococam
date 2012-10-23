package com.chocopepper.chococam.activity.friends.manage;

import java.util.ArrayList;
import java.util.List;

import lib.viewpagerindicator.actionbar.TitleProvider;
import lib.viewpagerindicator.normal.ViewPagerIndicator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class FriendsAdapter extends FragmentPagerAdapter implements ViewPagerIndicator.PageInfoProvider, TitleProvider {

	private List<Fragment> fragments;
	
	// 2012-05-07 brucewang
	// 각 서브 탭들의 타이틀을 관리.
	private ArrayList<String> titles = new ArrayList<String>();
	
	public FriendsAdapter(FragmentManager fm, List<Fragment> fragments) {
		super(fm);
		this.fragments = fragments;
	}
	


	public void addTitle(String str){
		titles.add(str);
	}
	

	@Override
	public String getTitle(int position) {
		String str = titles.get(position);
		return str;
	}
	
	@Override
	public Fragment getItem(int position) {
		return this.fragments.get(position);
	}

	@Override
	public int getCount() {
		return this.fragments.size();
	}
}