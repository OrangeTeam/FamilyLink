/**
 *
 */
package org.orange.familylink.sms;

import android.content.Intent;

/**
 * 短信相关{@link Intent}常量
 * @author Team Orange
 */
public interface SmsIntent {
	/** 消息已送达 */
	public static final String MESSAGE_DELIVERED_ACTION =
			SmsIntent.class.getName() + ".MESSAGE_DELIVEREDD";
	/** 消息已发送 */
	public static final String MESSAGE_SENT_ACTION =
			SmsIntent.class.getName() + ".MESSAGE_SENT";
}
