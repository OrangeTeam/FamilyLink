package org.orange.familylink;

import org.orange.familylink.fragment.LogFragment;
import org.orange.familylink.fragment.NavigateFragment;
import org.orange.familylink.fragment.SeekHelpFragment;

import android.os.Bundle;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mViewPager = (ViewPager) findViewById(R.id.pager);

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
		// Add 3 tabs, specifying the tab's text and TabListener
		actionBar.addTab(actionBar.newTab().setText(R.string.seek_help).setTabListener(tabListener));
		actionBar.addTab(actionBar.newTab().setText(R.string.log).setTabListener(tabListener));
		actionBar.addTab(actionBar.newTab().setText(R.string.navigate).setTabListener(tabListener));
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
	public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {
		public static final int PAGE_COUNT = 3;

		public AppSectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch(position){
			case 0:
				return new SeekHelpFragment();
			case 1:
				return new LogFragment();
			case 2:
				return new NavigateFragment();
			default:
				throw new IllegalArgumentException("position should be lower than "+PAGE_COUNT);
			}
		}

		@Override
		public int getCount() {
			return PAGE_COUNT;
		}
	}
}
