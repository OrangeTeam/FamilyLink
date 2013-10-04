/**
 *
 */
package org.orange.familylink.sms;

import java.util.ArrayList;

import org.orange.familylink.BuildConfig;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * 短信发送Helper类
 * @author Team Orange
 */
public class SmsSender {
	private static final String TAG = SmsSender.class.getSimpleName();

	/**
	 * 发送消息
	 * @param context 上下文环境
	 * @param messageUri 消息{@link Uri}
	 * @param message 要发动的消息内容
	 * @param dest 目的地址，如手机号
	 * @throws IllegalArgumentException 当message或dest为空时
	 */
	public static void sendMessage(Context context, Uri messageUri, String message, String dest) {
		if(TextUtils.isEmpty(message) || TextUtils.isEmpty(dest))
			throw new IllegalArgumentException("message or dest address shouldn't be empty.");
		// remove spaces and dashes from destination number
		// (e.g. "801 555 1212" -> "8015551212")
		// (e.g. "+8211-123-4567" -> "+82111234567")
		dest = PhoneNumberUtils.stripSeparators(dest);
		SmsManager smsManager = SmsManager.getDefault();
		ArrayList<String> messages = smsManager.divideMessage(message);
		int messagesCount = messages.size();
		ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>(messagesCount);
		ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>(messagesCount);

		//最后一条短信发送后才改变消息状态
		for(int i = 0 ; i < (messagesCount - 1) ; i++) {
			sentIntents.add(null);
			deliveryIntents.add(null);
		}
		Intent intent = new Intent(SmsIntent.MESSAGE_SENT_ACTION, messageUri,
				context, SmsStatusReceiver.class);
		sentIntents.add(PendingIntent.getBroadcast(context, 0, intent, 0));
		intent = new Intent(SmsIntent.MESSAGE_DELIVERED_ACTION, messageUri,
				context, SmsStatusReceiver.class);
		deliveryIntents.add(PendingIntent.getBroadcast(context, 0, intent, 0));
		if(BuildConfig.DEBUG)
			Log.v(TAG, "Sending message in " + messages.size() + " parts");
		try {
			smsManager.sendMultipartTextMessage(dest, null, messages, sentIntents, deliveryIntents);
		} catch (Exception ex) {
			Log.e(TAG, "SmsSender.sendMessage: caught", ex);
			throw new RuntimeException("SmsSender.sendMessage: caught " + ex +
					" from SmsManager.sendMultipartTextMessage()", ex);
		}
	}
}
