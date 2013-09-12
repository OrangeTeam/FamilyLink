/**
 *
 */
package org.orange.familylink.data;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;

/**
 * @author Team Orange
 *
 */
public class Settings {
	/**
	 * 设置项key：密码
	 * <p>
	 * Type：String
	 */
	public static final String PREF_KEY_PASSWORD = "password";
	/**
	 * 设置项key：角色
	 * <p>
	 * Type：String
	 * <p>
	 * "1" for Carer, "0" for Caree
	 */
	public static final String PREF_KEY_ROLE = "role";
	/**
	 * 设置项key：同步频率
	 * <p>
	 * Type：String
	 * <p>
	 * 同步间隔值，单位：分钟。如“180”表示每3小时更新一次
	 */
	public static final String PREF_KEY_SYNC_FREQUENCY ="sync_frequency";
	/**
	 * 设置项key：启动定位服务
	 * Type: String
	 * <p>
	 * true 就启动这个服务
	 */
	public static final String PREF_KEY_START_LOCATION_SERVICE = "start_location_service";
	/**
	 * 设置项key：定位频率
	 * <p>
	 * Type: String
	 * <p>
	 * 定位的时间间隔值，单位：分钟。如“180”表示每3小时定位一次
	 */
	public static final String PREF_KEY_LOCATE_FREQUENCY = "location_frequency";
	/**
	 * 设置项key：新消息通知
	 * <p>
	 * Type：boolean
	 * <p>
	 * true for enabled
	 */
	public static final String PREF_KEY_NOTIFICATIONS_NEW_MESSAGE
								= "notifications_new_message";
	/**
	 * 设置项key：新消息铃声
	 * <p>
	 * Type：String
	 * <p>
	 * 所选铃声的URI
	 * @see RingtonePreference
	 */
	public static final String PREF_KEY_NOTIFICATIONS_NEW_MESSAGE_RINGTONE
								= "notifications_new_message_ringtone";
	/**
	 * 设置项key：新消息振动
	 * <p>
	 * Type：boolean
	 * <p>
	 * true for enabled
	 */
	public static final String PREF_KEY_NOTIFICATIONS_NEW_MESSAGE_VIBRATE
								= "notifications_new_message_vibrate";

	/**
	 * 用户角色。
	 * @author Team Orange
	 */
	public enum Role {
		/** 照料着 */
		CARER,
		/** 受顾者 */
		CAREE
	}

	/**
	 * 获取 密码
	 * @param context 应用全局信息
	 * @return 密码；null表示尚未设置密码
	 */
	public static String getPassword(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString(PREF_KEY_PASSWORD, null);
	}
	/**
	 * 获取 用户角色
	 * @param context 上下文环境
	 * @return 用户角色；null表示未设置
	 */
	public static Role getRole(Context context) {
		String role = PreferenceManager.getDefaultSharedPreferences(context)
				.getString(PREF_KEY_ROLE, null);
		if("1".equals(role))
			return Role.CARER;
		else if("0".equals(role))
			return Role.CAREE;
		else if(role == null)
			return null;
		else
			throw new IllegalStateException("Illegal role value: "+role);
	}
	/**
	 * 设置用户角色
	 * @param context 应用全局信息
	 * @param role 用户角色
	 * @return 如果成功保存了新值，返回true
	 * @see Editor#commit()
	 */
	public static boolean setRole(Context context, Role role) {
		String value = null;
		switch(role){
		case CARER:
			value = "1";
			break;
		case CAREE:
			value = "0";
			break;
		}
		return setRole(context, value);
	}
	/**
	 * 设置用户角色
	 * @param context
	 * @param value 用户角色。见{@link #PREF_KEY_ROLE}
	 * @return 如果成功保存了新值，返回true
	 */
	public static boolean setRole(Context context, String value){
		if(!"0".equals(value) && !"1".equals(value))
			throw new IllegalArgumentException("Illegal Argument: "+value);
		return PreferenceManager.getDefaultSharedPreferences(context)
				.edit()
				.putString(PREF_KEY_ROLE, value)
				.commit();
	}
	/**
	 * 取得同步频率。用更新间隔表示，单位微秒。如3 600 000表示每1小时更新一次。
	 * @param context 上下文环境
	 * @return 以微秒为单位的更新间隔；-1表示不自动更新（never）；null表示未设置
	 */
	public static Long getSyncFrequency(Context context) {
		String freq = PreferenceManager.getDefaultSharedPreferences(context)
				.getString(PREF_KEY_SYNC_FREQUENCY, null);
		if(freq == null)
			return null;
		else if("-1".equals(freq))
			return -1L;
		else
			return Long.parseLong(freq) * 60 * 1000;
	}

	/**
	 * 获取‘开启定位服务’项是否被选中
	 * @param context
	 * @return 返回true则为需要开启定位服务
	 */
	public static boolean getStartLocationService(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(PREF_KEY_START_LOCATION_SERVICE, false);
	}

	/**
	 * 获取定位频率
	 * @param context
	 * @return 返回定位频率，定位频率为两次定位之间的时间间隔
	 */
	public static Long getLocateFrequency(Context context){
		String locationFrequency = PreferenceManager.getDefaultSharedPreferences(context)
				.getString(PREF_KEY_LOCATE_FREQUENCY, null);
		if(locationFrequency == null)
			return null;
		else
			return Long.parseLong(locationFrequency) * 60 * 1000;
	}
	/**
	 * 设置定位频率
	 * @param context
	 * @param value 新定位频率
	 * @return 当成功保存新值时，返回true
	 * @see #PREF_KEY_LOCATE_FREQUENCY
	 */
	public static boolean setLocateFrequency(Context context, String value) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.edit()
				.putString(PREF_KEY_LOCATE_FREQUENCY, value)
				.commit();
	}
}
