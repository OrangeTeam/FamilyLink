/**
 *
 */
package org.orange.familylink.data;

import android.content.Context;
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
}
