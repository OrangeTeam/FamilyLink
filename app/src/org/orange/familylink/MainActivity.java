package org.orange.familylink;

import org.orange.familylink.data.Settings;
import org.orange.familylink.data.Settings.Role;
import org.orange.familylink.fragment.LogFragment;
import org.orange.familylink.fragment.NavigateFragment;
import org.orange.familylink.fragment.SeekHelpFragment;

import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;

/**
 * @author Team Orange
 */
public class MainActivity extends BaseActivity {
	private ViewPager mViewPager;
	private int[] mPagersOrder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mViewPager = (ViewPager) findViewById(R.id.pager);

		setup();
	}

	protected void setup() {
		Role role = Settings.getRole(this);
		if(role == Role.CARER)
			mPagersOrder = new int[]{R.string.log, R.string.seek_help, R.string.navigate};
		else if(role == Role.CAREE)
			mPagersOrder = new int[]{R.string.seek_help, R.string.log, R.string.navigate};
		setupViewPager(mViewPager);
		setupActionBar();
	}
	/**
	 * 配置{@link ActionBar}，典型情况下在{@link #onCreate(Bundle)}调用
	 */
	protected void setupActionBar() {
		ActionBar actionBar = getSupportActionBar();
		// Specify that the Home/Up button should not be enabled, since there is no hierarchical parent.
		actionBar.setHomeButtonEnabled(false);
		if(getResources().getConfiguration().orientation ==
				Configuration.ORIENTATION_PORTRAIT) {
			//Let navigation tabs to collapse into the main action bar
			actionBar.setDisplayShowHomeEnabled(false);
			actionBar.setDisplayShowTitleEnabled(false);
		}
		// Specify that tabs should be displayed in the action bar.
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// Create a tab listener that is called when the user changes tabs.
		ActionBar.TabListener tabListener = new ActionBar.TabListener() {
			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				mViewPager.setCurrentItem(tab.getPosition());
			}
			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {}
			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {}
		};
		// Add tabs, specifying the tab's text and TabListener
		for(int i = 0 ;i < mPagersOrder.length ; i++)
			actionBar.addTab(actionBar.newTab().setText(mPagersOrder[i]).setTabListener(tabListener));
	}
	/**
	 * 配置{@link ViewPager}，典型情况下在{@link #onCreate(Bundle)}调用
	 */
	protected void setupViewPager(ViewPager viewPager) {
		mViewPager.setAdapter(new AppSectionsPagerAdapter(getSupportFragmentManager()));
		mViewPager.setOnPageChangeListener(new OnPageChangeListener(){
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
			@Override
			public void onPageSelected(int position) {
				getSupportActionBar().setSelectedNavigationItem(position);
			}
			@Override
			public void onPageScrollStateChanged(int state) {}
		});
	}

	/**
	 * {@link MainActivity}中{@link ViewPager}的{@link PagerAdapter}，
	 * 为{@link MainActivity}提供内容{@link Fragment}。
	 * @see FragmentPagerAdapter
	 * @author Team Orange
	 */
	protected class AppSectionsPagerAdapter extends FragmentPagerAdapter {
		public AppSectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch(mPagersOrder[position]){
			case R.string.seek_help:
				return new SeekHelpFragment();
			case R.string.log:
				return new LogFragment();
			case R.string.navigate:
				return new NavigateFragment();
			}
			throw new IllegalArgumentException("illegal position: " + position);
		}

		@Override
		public int getCount() {
			return mPagersOrder.length;
		}
	}
}
