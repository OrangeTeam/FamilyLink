package org.orange.familylink;

import org.apache.commons.lang3.ArrayUtils;
import org.orange.familylink.ContactDetailActivity.Contact;
import org.orange.familylink.R.drawable;
import org.orange.familylink.R.string;
import org.orange.familylink.alarm.AlarmService;
import org.orange.familylink.data.Message;
import org.orange.familylink.data.Message.Code;
import org.orange.familylink.data.MessageLogRecord.Direction;
import org.orange.familylink.data.Settings;
import org.orange.familylink.data.Settings.Role;
import org.orange.familylink.data.UrgentMessageBody;
import org.orange.familylink.fragment.dialog.InitialSetupDialogFragment;
import org.orange.familylink.fragment.dialog.LocateFrequencyDialogFragment;
import org.orange.familylink.fragment.dialog.NoContactInformationDialogFragment;
import org.orange.familylink.fragment.dialog.RoleDialogFragment;
import org.orange.familylink.fragment.dialog.RoleDialogFragment.OnRoleChangeListener;
import org.orange.familylink.location.LocationService;
import org.orange.familylink.location.LocationTracker;
import org.orange.familylink.sms.SmsMessage;
import org.orange.familylink.sms.SmsReceiverService;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.Toast;

/**
 * 主{@link Activity}。应用的默认{@link Activity}
 * @author Team Orange
 */
public class MainActivity extends BaseActivity {
	private static enum DialogType {
		DIALOG_NO_CONTACT_INFORMATION;
	}
	private GridView mMainMenuGridView;
	private Role mRole;
	private Function[] mFunctions;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mMainMenuGridView = (GridView) findViewById(R.id.main_menu);

		//开启接收短信服务
		Intent smsIntent = new Intent(this, SmsReceiverService.class);
		smsIntent.setAction(SmsReceiverService.ACTION_FOREGROUND);
		startService(smsIntent);

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
				public void onClickPositiveButton(InitialSetupDialogFragment dialog) {
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
		else {
			onRoleChanged(role);
			return true;
		}
	}
	/**
	 * 当 用户角色 改变时，调用此方法
	 * @param newRole 新的用户角色
	 */
	private void onRoleChanged(Role newRole) {
		MainMenuAdapter adapter = (MainMenuAdapter) mMainMenuGridView.getAdapter();
		// 如果角色不再是 受顾者，关闭受顾者才使用的服务
		if(mRole == Role.CAREE) {
			View view = null;
			view = mMainMenuGridView.getChildAt(
					adapter.getItemPosition(Function.LOCATE_SERVICE));
			if(view.isActivated())
				setLocateService(view, false);
			view = mMainMenuGridView.getChildAt(
					adapter.getItemPosition(Function.FALL_DOWN_ALARM_SERVICE));
			if(view.isActivated())
				setFallDownAlarmService(view, false);
		}
		// 更新Pagers的顺序设置
		setMainMenuContent(newRole);
		// 通知ViewPager数据集有变化
		adapter.notifyDataSetChanged();
	}

	private void showDialogFragment(DialogType dialogType) {
		switch(dialogType) {
		case DIALOG_NO_CONTACT_INFORMATION:
			new NoContactInformationDialogFragment().show(getFragmentManager(), dialogType.name());
			break;
		default:
			throw new UnsupportedOperationException("unsupported dialog:" + dialogType);
		}
	}

	/**
	 * 检查指定服务是否已打开
	 * @param serviceClass 待检测的服务的{@link Class}
	 * @return 如果已打开，返回true
	 */
	private boolean isServiceRunning(Class<? extends Service> serviceClass) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
	/**
	 * 开启或关闭 {@link LocationService} 服务
	 * @param locateSwitch 本服务的开关按钮
	 * @param isOn 如果要打开服务，设为true；如果要关闭服务，设为false
	 */
	private void setLocateService(View locateSwitch, boolean isOn) {
		locateSwitch.setActivated(isOn);
		PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit()
		.putBoolean(Settings.PREF_KEY_START_LOCATION_SERVICE, isOn).commit();
		final Intent intent = new Intent(this, LocationService.class);
		if(isOn){
			startService(intent);
		}else{
			stopService(intent);
		}
	}
	/**
	 * 开启或关闭 {@link AlarmService} 服务
	 * @param locateSwitch 本服务的开关按钮
	 * @param isOn 如果要打开服务，设为true；如果要关闭服务，设为false
	 */
	private void setFallDownAlarmService(View alarmSwitch, boolean isOn) {
		alarmSwitch.setActivated(isOn);
		final Intent intent = new Intent(this, AlarmService.class);
		if(isOn) {
			startService(intent);
		} else {
			stopService(intent);
		}
	}

	private void sendMessage(Function function) {
		// 构造消息
		final Message message = new SmsMessage();
		switch(function) {
		case SEEK_HELP:
			message.setCode(Code.INFORM | Code.Extra.Inform.URGENT);
			UrgentMessageBody body = new UrgentMessageBody();
			body.setType(UrgentMessageBody.Type.SEEK_HELP);
			LocationTracker locationTracker = new LocationTracker(this);
			if(locationTracker.canGetLocation()) {
				body.setPosition(locationTracker.getLatitude(), locationTracker.getLongitude());
			}
			locationTracker.stopUsingGPS();
			message.setBody(body.toJson());
			break;
		case LOCATE_NOW:
			message.setCode(Code.COMMAND | Code.Extra.Command.LOCATE_NOW);
			break;
		default:
			throw new IllegalArgumentException("unsupported function: " + function);
		}
		new Thread() {
			@Override
			public void run() {
				// 发送消息
				Contact contact = ContactDetailActivity.getDefaultContact(MainActivity.this);
				if(contact.phone != null && !contact.phone.isEmpty())
					message.sendAndSave(MainActivity.this, contact.id, contact.phone);
				else
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							showDialogFragment(DialogType.DIALOG_NO_CONTACT_INFORMATION);
						}
					});
			}
		}.start();
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
			if(function == Function.LOCATE_SERVICE)
				item.setActivated(isServiceRunning(LocationService.class));
			else if(function == Function.FALL_DOWN_ALARM_SERVICE)
				item.setActivated(isServiceRunning(AlarmService.class));
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
			case LOCATE_SERVICE: {
				boolean newIsStarted = !view.isActivated();
				setLocateService(view, newIsStarted);
				if(newIsStarted)
					Toast.makeText(MainActivity.this, R.string.have_started_periodically_contact, Toast.LENGTH_LONG).show();
				else
					Toast.makeText(MainActivity.this, R.string.have_stopt_periodically_contact, Toast.LENGTH_LONG).show();
				break;
			}
			case LOCATE_FREQUENCY:
				mLocateFrequencyDialogFragment.show(getFragmentManager(), null);
				break;
			case FALL_DOWN_ALARM_SERVICE:
				setFallDownAlarmService(view, !view.isActivated());
				break;
			case SEEK_HELP:
				sendMessage(Function.SEEK_HELP);
				break;
			//----------------------- 监护方 -----------------------
			case LOCATE_NOW:
				sendMessage(Function.LOCATE_NOW);
				break;
			//----------------------- 通用 -----------------------
			case GIVE_A_CALL: {
				Contact contact = ContactDetailActivity.getDefaultContact(MainActivity.this);
				if(contact.phone != null && !contact.phone.isEmpty()) {
					Intent intent = new Intent(Intent.ACTION_CALL, Uri
							.parse("tel:" + contact.phone));
					startActivity(intent);
				} else {
					showDialogFragment(DialogType.DIALOG_NO_CONTACT_INFORMATION);
				}
				break;
			}
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
				startActivity(new Intent(MainActivity.this, ContactDetailActivity.class));
				break;
			case ROLE_SETTING:
				mRoleDialogFragment.show(getFragmentManager(), null);
				break;

			default:
				throw new IllegalStateException("unsupport function: " + function.name());
			}
		}
	}

	/**
	 * 本应用的功能。在主功能菜单中作为菜单项使用。
	 * @author Team Orange
	 */
	private static enum Function {
		//----------------------- 受顾方 -----------------------
		/** 定位服务（开关） */
		LOCATE_SERVICE(R.string.locate_service, R.drawable.ic_main_menu_locate_service),
		/** 定位频率 */
		LOCATE_FREQUENCY(R.string.locate_frequency, R.drawable.ic_main_menu_locate_frequency),
		/** 摔倒检测服务（开关） */
		FALL_DOWN_ALARM_SERVICE(R.string.fall_down_alarm_service, R.drawable.ic_main_menu_fall_down_alarm_service),
		/** 求助 */
		SEEK_HELP(R.string.seek_help, R.drawable.ic_main_menu_seek_help),
		//----------------------- 监护方 -----------------------
		/** 现在获取对方地点 */
		LOCATE_NOW(R.string.locate_now, R.drawable.ic_main_menu_locate_now),
		//----------------------- 通用 -----------------------
		/** 给对方打电话 */
		GIVE_A_CALL(R.string.call, R.drawable.ic_main_menu_call),
		/** 发件箱（已发消息） */
		OUTBOX(R.string.outbox, R.drawable.ic_main_menu_outbox),
		/** 回馈信息（相应消息） */
		RESPONSE_MESSAGE(R.string.response_message, R.drawable.ic_main_menu_response_message),
		/** 联系人设置 */
		CONTACTS_SETTING(R.string.contacts_setting, R.drawable.ic_main_menu_contacts_setting),
		/** 角色设置 */
		ROLE_SETTING(R.string.role_setting, R.drawable.ic_main_menu_role_setting);

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
			new Function[]{Function.FALL_DOWN_ALARM_SERVICE, Function.LOCATE_SERVICE,
					Function.LOCATE_FREQUENCY, Function.SEEK_HELP},
			FUNCTIONS_GENERAL);
	/** 照料者的功能及其顺序 */
	private static final Function[] FUNCTIONS_CARER = ArrayUtils.addAll(
			new Function[]{Function.LOCATE_NOW},
			FUNCTIONS_GENERAL);

	private final LocateFrequencyDialogFragment mLocateFrequencyDialogFragment =
			new LocateFrequencyDialogFragment();
	private final RoleDialogFragment mRoleDialogFragment = new RoleDialogFragment();
	{
		mRoleDialogFragment.setOnRoleChangeListener(new OnRoleChangeListener() {
			@Override
			public void onRoleChange(RoleDialogFragment dialog) {
				changeMainMenuIfNecessary();
			}
		});
	}
}
