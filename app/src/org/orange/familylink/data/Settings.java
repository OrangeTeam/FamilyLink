/**
 *
 */
package org.orange.familylink.data;

import android.preference.RingtonePreference;

/**
 * @author Team Orange
 *
 */
public class Settings {
	/**
	 * 设置项key：角色
	 * <p>
	 * Type：Sting
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


}
