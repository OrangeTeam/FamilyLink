package org.orange.familylink;

import org.apache.commons.lang3.ArrayUtils;
import org.orange.familylink.R.drawable;
import org.orange.familylink.R.string;
import org.orange.familylink.data.MessageLogRecord.Direction;
import org.orange.familylink.data.Settings;
import org.orange.familylink.data.Settings.Role;
import org.orange.familylink.fragment.dialog.InitialSetupDialogFragment;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

/**
 * 主{@link Activity}。应用的默认{@link Activity}
 * @author Team Orange
 */
public class MainActivity extends BaseActivity {
	private GridView mMainMenuGridView;
	private Role mRole;
	private Function[] mFunctions;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mMainMenuGridView = (GridView) findViewById(R.id.main_menu);

		setup();
	}

	@Override
	protected void onStart() {
		super.onStart();
		changeMainMenuIfNecessary();
	}

	/**
	 * 根据用户角色，设置主菜单内容。
	 * @param role 当前用户角色
	 */
	protected void setMainMenuContent(Role role) {
		if(role == Role.CAREE)
			mFunctions = FUNCTIONS_CAREE;
		else if(role == Role.CARER)
			mFunctions = FUNCTIONS_CARER;
		else if(role == null) {
			// 还没有配置用户角色， 现在配置
			mFunctions = FUNCTIONS_GENERAL;
			// 弹出对话框
			InitialSetupDialogFragment dialog = new InitialSetupDialogFragment();
			dialog.setOnClickListener(new InitialSetupDialogFragment.OnClickListener() {
				@Override
				public void onClickPositiveButton(InitialSetupDialogFragment dialog,
						Role newRole, String newPassword) {
					changeMainMenuIfNecessary();
				}
			});
			dialog.show(getFragmentManager(), null);
		}
		else
			throw new IllegalArgumentException("ilegal role: " + role);
		mRole = role;
	}

	/**
	 * 初始化各项配置。典型情况下在{@link #onCreate(Bundle)}调用
	 */
	protected void setup() {
		setMainMenuContent(Settings.getRole(this));
		setupMainMenu();
		setupActionBar();
	}
	/**
	 * 配置{@link ActionBar}，典型情况下在{@link #onCreate(Bundle)}调用
	 */
	protected void setupActionBar() {
		ActionBar actionBar = getActionBar();
		// Specify that the Home/Up button should not be enabled, since there is no hierarchical parent.
		actionBar.setHomeButtonEnabled(false);
	}
	/**
	 * 配置主菜单，典型情况下在{@link #onCreate(Bundle)}调用
	 */
	protected void setupMainMenu() {
		mMainMenuGridView.setAdapter(new MainMenuAdapter(this));
		mMainMenuGridView.setOnItemClickListener(new OnMenuItemClickListener());
	}

	/**
	 * 如果需要，改变主菜单内容。例如改变用户角色时
	 * @return 如果改变了，返回true；如果无需改变，返回false
	 */
	protected boolean changeMainMenuIfNecessary() {
		Role role = Settings.getRole(this);
		if(mRole == role)
			return false;
		// 更新Pagers的顺序设置
		setMainMenuContent(role);
		// 通知ViewPager数据集有变化
		((MainMenuAdapter) mMainMenuGridView.getAdapter()).notifyDataSetChanged();
		return true;
	}

	private class MainMenuAdapter extends BaseAdapter {
		private Context mContext;

		public MainMenuAdapter(Context context) {
			super();
			mContext = context;
		}
		@Override
		public int getCount() {
			return mFunctions.length;
		}
		@Override
		public Object getItem(int position) {
			return mFunctions[position];
		}
		public int getItemPosition(Function function) {
			for(int i = 0 ; i < mFunctions.length ; i++)
				if(mFunctions[i] == function)
					return i;
			return -1;
		}
		@Override
		public long getItemId(int position) {
			return mFunctions[position].ordinal();
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView item = null;
			if (convertView == null) {  // if it's not recycled, initialize some attributes
				item = new TextView(mContext);
				item.setGravity(Gravity.CENTER);
				item.setTextAppearance(mContext, android.R.style.TextAppearance_Large);
			} else {
				item = (TextView) convertView;
			}
			Function function = mFunctions[position];
			item.setText(function.getTitleResourceId());
			item.setCompoundDrawablesWithIntrinsicBounds(0, function.getIconResourceId(), 0, 0);
			return item;
		}
	}

	private class OnMenuItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Function function = Function.values()[(int)id];
			switch (function) {
			//----------------------- 受顾方 -----------------------
			case MASTER_SWITCH:
				boolean isOn = view.isActivated();
				isOn = !isOn;	//改变状态
				view.setActivated(isOn);
				MainMenuAdapter adapter = (MainMenuAdapter) mMainMenuGridView.getAdapter();
				setLocateService(mMainMenuGridView.getChildAt(
						adapter.getItemPosition(Function.LOCATE_SERVICE)), isOn);
				setFallDownAlarmService(mMainMenuGridView.getChildAt(
						adapter.getItemPosition(Function.FALL_DOWN_ALARM_SERVICE)), isOn);
				if(!isOn)
					finish();
				break;
			case LOCATE_SERVICE:
				setLocateService(view, !view.isActivated());
				break;
			case LOCATE_FREQUENCY:
				break;
			case FALL_DOWN_ALARM_SERVICE:
				setFallDownAlarmService(view, !view.isActivated());
				break;
			case SEEK_HELP:
				break;
			//----------------------- 监护方 -----------------------
			case LOCATE_NOW:
				break;
			//----------------------- 通用 -----------------------
			case GIVE_A_CALL:
				break;
			case OUTBOX: {
				Intent intent = new Intent(MainActivity.this, MessagesActivity.class);
				intent.putExtra(MessagesActivity.EXTRA_DIRECTION, Direction.SEND);
				startActivity(intent);
				break;
			}
			case RESPONSE_MESSAGE: {
				Intent intent = new Intent(MainActivity.this, MessagesActivity.class);
				intent.putExtra(MessagesActivity.EXTRA_DIRECTION, Direction.RECEIVE);
				startActivity(intent);
				break;
			}
			case CONTACTS_SETTING:
				startActivity(new Intent(MainActivity.this, ContactsActivity.class));
				break;
			case ROLE_SETTING:
				break;

			default:
				throw new IllegalStateException("unsupport function: " + function.name());
			}
		}
		private void setLocateService(View locateSwitch, boolean isOn) {
			locateSwitch.setActivated(isOn);
		}
		private void setFallDownAlarmService(View alarmSwitch, boolean isOn) {
			alarmSwitch.setActivated(isOn);
		}
	}

	/**
	 * 本应用的功能。在主功能菜单中作为菜单项使用。
	 * @author Team Orange
	 */
	private static enum Function {
		//----------------------- 受顾方 -----------------------
		/** 总开关 */
		MASTER_SWITCH(R.string.master_switch, R.drawable.ic_main_menu_sample_switch),
		/** 定位服务（开关） */
		LOCATE_SERVICE(R.string.locate_service, R.drawable.ic_main_menu_sample_switch),
		/** 定位频率 */
		LOCATE_FREQUENCY(R.string.locate_frequency, R.drawable.ic_main_menu_sample),
		/** 摔倒检测服务（开关） */
		FALL_DOWN_ALARM_SERVICE(R.string.fall_down_alarm_service, R.drawable.ic_main_menu_sample_switch),
		/** 求助 */
		SEEK_HELP(R.string.seek_help, R.drawable.ic_main_menu_sample),
		//----------------------- 监护方 -----------------------
		/** 现在获取对方地点 */
		LOCATE_NOW(R.string.locate_now, R.drawable.ic_main_menu_sample),
		//----------------------- 通用 -----------------------
		/** 给对方打电话 */
		GIVE_A_CALL(R.string.call, R.drawable.ic_main_menu_sample),
		/** 发件箱（已发消息） */
		OUTBOX(R.string.outbox, R.drawable.ic_main_menu_sample),
		/** 回馈信息（相应消息） */
		RESPONSE_MESSAGE(R.string.response_message, R.drawable.ic_main_menu_sample),
		/** 联系人设置 */
		CONTACTS_SETTING(R.string.contacts_setting, R.drawable.ic_main_menu_sample),
		/** 角色设置 */
		ROLE_SETTING(R.string.role_setting, R.drawable.ic_main_menu_sample);

		private final int mIconResourceId;
		private final int mTitleResourceId;

		/**
		 * @param titleResId 标题 {@link string}资源ID
		 * @param iconResId 图标  {@link drawable}资源ID
		 */
		private Function(int titleResId, int iconResId) {
			mIconResourceId = iconResId;
			mTitleResourceId = titleResId;
		}
		public int getIconResourceId() {
			return mIconResourceId;
		}
		public int getTitleResourceId() {
			return mTitleResourceId;
		}
	}

	private static final Function[] FUNCTIONS_GENERAL =
	{Function.GIVE_A_CALL, Function.OUTBOX, Function.RESPONSE_MESSAGE,
	Function.CONTACTS_SETTING, Function.ROLE_SETTING};
	/** 受顾者的功能及其顺序 */
	private static final Function[] FUNCTIONS_CAREE = ArrayUtils.addAll(
			new Function[]{Function.MASTER_SWITCH, Function.LOCATE_SERVICE,
					Function.LOCATE_FREQUENCY, Function.FALL_DOWN_ALARM_SERVICE,
					Function.SEEK_HELP},
			FUNCTIONS_GENERAL);
	/** 照料者的功能及其顺序 */
	private static final Function[] FUNCTIONS_CARER = ArrayUtils.addAll(
			new Function[]{Function.LOCATE_NOW},
			FUNCTIONS_GENERAL);
}
