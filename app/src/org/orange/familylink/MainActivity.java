package org.orange.familylink;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.widget.ViewPager;
import org.orange.familylink.data.Settings;
import org.orange.familylink.data.Settings.Role;
import org.orange.familylink.fragment.LogFragment;
import org.orange.familylink.fragment.NavigateFragment;
import org.orange.familylink.fragment.SeekHelpFragment;
import org.orange.familylink.fragment.dialog.InitialSetupDialogFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.Window;

/**
 * 主{@link Activity}。应用的默认{@link Activity}
 * @author Team Orange
 */
public class MainActivity extends BaseActivity {
	// 用string ID表示页面及其顺序
	/** 照料者的页面及其顺序 */
	private static final int[] PAGERS_ORDER_CARER =
			new int[]{R.string.log, R.string.seek_help, R.string.navigate};
	/** 受顾者的页面及其顺序 */
	private static final int[] PAGERS_ORDER_CAREE =
			new int[]{R.string.seek_help, R.string.log, R.string.navigate};

	private ViewPager mViewPager;
	private Role mRole;
	private int[] mPagersOrder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		// LogFragment在删除消息时使用
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setSupportProgressBarIndeterminateVisibility(false);
		mViewPager = (ViewPager) findViewById(R.id.pager);

		setup();
	}

	@Override
	protected void onStart() {
		super.onStart();
		changePagersOrderIfNecessary();
	}

	/**
	 * 根据用户角色，更改Pagers的顺序配置。
	 * @param role 当前用户角色
	 */
	protected void setPagersOrder(Role role) {
		if(role == Role.CARER)
			mPagersOrder = PAGERS_ORDER_CARER;
		else if(role == Role.CAREE)
			mPagersOrder = PAGERS_ORDER_CAREE;
		else if(role == null) {
			// 还没有配置用户角色， 现在配置
			mPagersOrder = PAGERS_ORDER_CAREE;	// 设置默认的临时页面顺序
			// 弹出对话框
			InitialSetupDialogFragment dialog = new InitialSetupDialogFragment();
			dialog.setOnClickListener(new InitialSetupDialogFragment.OnClickListener() {
				@Override
				public void onClickPositiveButton(InitialSetupDialogFragment dialog,
						Role newRole, String newPassword) {
					changePagersOrderIfNecessary();
				}
			});
			dialog.show(getSupportFragmentManager());
		}
		else
			throw new IllegalArgumentException("ilegal role: " + role);
		mRole = role;
	}

	/**
	 * 初始化各项配置。典型情况下在{@link #onCreate(Bundle)}调用
	 */
	protected void setup() {
		setPagersOrder(Settings.getRole(this));
		setupViewPager();
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
		// Add tabs, specifying the tab's text and TabListener
		for(int i = 0 ;i < mPagersOrder.length ; i++)
			actionBar.addTab(actionBar.newTab().setText(mPagersOrder[i]).setTabListener(tabListener));
	}
	/**
	 * 配置{@link ViewPager}，典型情况下在{@link #onCreate(Bundle)}调用
	 */
	protected void setupViewPager() {
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
	 * 如果需要，改变Pagers的顺序。例如改变用户角色时
	 * @return 如果Pagers的顺序改变了，返回true；如果无需改变，返回false
	 */
	protected boolean changePagersOrderIfNecessary() {
		Role role = Settings.getRole(this);
		if(mRole == role)
			return false;
		// 更新Pagers的顺序设置
		setPagersOrder(role);
		// 更新ActionBar的Tabs的顺序
		for(int i = 0 ; i < mPagersOrder.length ; i++)
			getSupportActionBar().getTabAt(i).setText(mPagersOrder[i]);
		// 通知ViewPager数据集有变化
		mViewPager.getAdapter().notifyDataSetChanged();
		return true;
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
		public long getItemId(int position) {
			return mPagersOrder[position];
		}

		@Override
		public int getItemPosition(Object object) {
			int id = -1;
			if(object instanceof SeekHelpFragment)
				id = R.string.seek_help;
			else if(object instanceof LogFragment)
				id = R.string.log;
			else if(object instanceof NavigateFragment)
				id = R.string.navigate;
			else
				throw new IllegalStateException("encounter unknown item");
			for(int position = 0 ; position < mPagersOrder.length ; position++)
				if(mPagersOrder[position] == id)
					return position;
			if(id == -1)
				throw new IllegalStateException("this method is bad.");
			return PagerAdapter.POSITION_NONE;
		}

		@Override
		public int getCount() {
			return mPagersOrder.length;
		}
	}
}
