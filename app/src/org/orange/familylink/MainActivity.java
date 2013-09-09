package org.orange.familylink;

import org.orange.familylink.data.Settings;
import org.orange.familylink.data.Settings.Role;
import org.orange.familylink.database.Contract;
import org.orange.familylink.fragment.LogFragment;
import org.orange.familylink.fragment.NavigateFragment;
import org.orange.familylink.fragment.SeekHelpFragment;
import org.orange.familylink.fragment.dialog.InitialSetupDialogFragment;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Window;

/**
 * 主{@link Activity}。应用的默认{@link Activity}
 * @author Team Orange
 */
public class MainActivity extends BaseActivity {
	/**
	 * 应当显示的消息的ID列表
	 * <p>
	 * Type: long[]
	 */
	public static final String EXTRA_IDS = MainActivity.class.getName() + ".extra.IDS";
	/**
	 * 意图设置的消息状态筛选条件，用R.string.*表示
	 * <p>
	 * Type: int
	 */
	public static final String EXTRA_STATUS = MainActivity.class.getName() + ".extra.STATUS";
	/**
	 * 意图设置的消息代码筛选条件，用R.string.*表示
	 * <p>
	 * Type: int
	 */
	public static final String EXTRA_CODE = MainActivity.class.getName() + ".extra.CODE";
	/**
	 * 意图设置的消息联系人筛选条件，用联系人ID表示
	 * <p>
	 * Type: long
	 */
	public static final String EXTRA_CONTACT_ID =
			MainActivity.class.getName() + ".extra.CONTACT_ID";

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
		setProgressBarIndeterminateVisibility(false);
		mViewPager = (ViewPager) findViewById(R.id.pager);

		setup();
		handleIntent();
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
			dialog.show(getFragmentManager(), null);
		}
		else
			throw new IllegalArgumentException("ilegal role: " + role);
		mRole = role;
	}
	/**
	 * 取得指定pager的位置
	 * @param pagerId 要查找位置的Pager的Id。用R.string.*表示
	 * @return 指定Pager的位置；如果无指定Pager的位置，返回-1
	 * @see #mPagersOrder
	 */
	protected int getPagerPosition(int pagerId) {
		for(int position = 0 ; position < mPagersOrder.length ; position++)
			if(mPagersOrder[position] == pagerId)
				return position;
		return -1;
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
		ActionBar actionBar = getActionBar();
		// Specify that the Home/Up button should not be enabled, since there is no hierarchical parent.
		actionBar.setHomeButtonEnabled(false);
		// Specify that tabs should be displayed in the action bar.
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// Create a tab listener that is called when the user changes tabs.
		ActionBar.TabListener tabListener = new ActionBar.TabListener() {
			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				mViewPager.setCurrentItem(tab.getPosition(), true);
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
		mViewPager.setAdapter(new AppSectionsPagerAdapter(getFragmentManager()));
		mViewPager.setOnPageChangeListener(new OnPageChangeListener(){
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
			@Override
			public void onPageSelected(int position) {
				getActionBar().setSelectedNavigationItem(position);
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
			getActionBar().getTabAt(i).setText(mPagersOrder[i]);
		// 通知ViewPager数据集有变化
		mViewPager.getAdapter().notifyDataSetChanged();
		return true;
	}

	/**
	 * 取得启动本{@link Activity}的意图（{@link Intent}）Pager
	 * @return 意图（{@link Intent}）打开的Pager，用R.string.*表示；如果没有指定，返回null
	 * @see #EXTRA_IDS
	 * @see #getIntent()
	 */
	protected Integer getIntentPager() {
		Intent intent = getIntent();
		Integer fragmentId = null;
		if(Contract.Messages.MESSAGES_TYPE.equals(intent.getType())) {
			fragmentId = R.string.log;
		}
		return fragmentId;
	}
	/**
	 * 根据{@link Intent}，创建{@link Fragment}的参数
	 * @return 创建的参数；如果{@link Intent}没有指定Pager或参数，返回null
	 * @see #getIntent()
	 * @see #getIntentPager()
	 */
	protected Bundle buildFragmentArgumentsByIntent() {
		Integer pagerId = getIntentPager();
		if(pagerId == null)
			return null;
		Bundle extra = getIntent().getExtras();
		if(extra == null)
			return null;
		Integer status = null, code = null;
		Long contactId = null;
		long[] ids = extra.getLongArray(EXTRA_IDS);
		if(extra.containsKey(EXTRA_STATUS))
			status = extra.getInt(EXTRA_STATUS);
		if(extra.containsKey(EXTRA_CODE))
			code = extra.getInt(EXTRA_CODE);
		if(extra.containsKey(EXTRA_CONTACT_ID))
			contactId = extra.getLong(EXTRA_CONTACT_ID);
		if(ids == null && status == null && code == null && contactId == null)
			return null;
		Bundle args = new Bundle();
		switch(pagerId) {
		case R.string.log:
			if(ids != null)
				args.putLongArray(LogFragment.ARGUMENT_KEY_IDS, ids);
			if(status != null)
				args.putInt(LogFragment.ARGUMENT_KEY_STATUS, status);
			if(code != null)
				args.putInt(LogFragment.ARGUMENT_KEY_CODE, code);
			if(contactId != null)
				args.putLong(LogFragment.ARGUMENT_KEY_CONTACT_ID, contactId);
			break;
		default:
			throw new UnsupportedOperationException("unsupported pager");
		}
		return args;
	}

	/**
	 * 处理{@link Intent}，跳转到意图的页面。
	 * @see #getIntent()
	 */
	protected void handleIntent() {
		Integer fragmentId = getIntentPager();
		if(fragmentId != null) {
			int position = getPagerPosition(fragmentId);
			mViewPager.setCurrentItem(position, true);
		}
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
			Integer intentPager = getIntentPager();
			Bundle args = buildFragmentArgumentsByIntent();
			switch(mPagersOrder[position]){
			case R.string.seek_help:
				return new SeekHelpFragment();
			case R.string.log:
				Fragment logFragment = new LogFragment();
				if(intentPager != null && intentPager == R.string.log)
					logFragment.setArguments(args);
				return logFragment;
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

			int position = getPagerPosition(id);
			if(position >= 0)
				return position;
			else
				throw new IllegalStateException("Unknown pager or position");
		}

		@Override
		public int getCount() {
			return mPagersOrder.length;
		}
	}
}
