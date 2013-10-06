/**
 *
 */
package org.orange.familylink.sms;

import org.orange.familylink.BuildConfig;
import org.orange.familylink.MessagesActivity;
import org.orange.familylink.R;
import org.orange.familylink.data.MessageLogRecord.Status;
import org.orange.familylink.database.Contract;
import org.orange.familylink.database.Contract.Messages;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

/**
 * 短信发送状态接收器
 * @author Team Orange
 */
public class SmsStatusReceiver extends BroadcastReceiver {
	private static final String TAG = SmsStatusReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if(SmsIntent.MESSAGE_SENT_ACTION.equals(action)) {
			onMessageSent(context, intent);
		} else if(SmsIntent.MESSAGE_DELIVERED_ACTION.equals(action)) {
			onMessageDelivered(context, intent);
		}
		if(BuildConfig.DEBUG) Log.v(TAG, "receiver broadcase"+action+":"+intent.getDataString());
	}
	/**
	 * 当接收到{@link SmsIntent#MESSAGE_SENT_ACTION}时被调用
	 * @param context 上下文环境
	 * @param intent 接收到的{@link Intent}
	 */
	protected void onMessageSent(Context context, Intent intent) {
		ContentResolver contentResolver = context.getContentResolver();
		switch(getResultCode()) {
		case Activity.RESULT_OK:
			boolean shouldUpdate = false;
			// 如果状态已经变为DELIVERED，则不再改变状态
			Cursor c = contentResolver.query(intent.getData(),
					new String[]{Messages.COLUMN_NAME_STATUS}, null, null, null);
			if(c != null && c.moveToNext()) {
				String status = c.getString(c.getColumnIndex(Messages.COLUMN_NAME_STATUS));
				if(Status.valueOf(status) != Status.DELIVERED)
					shouldUpdate = true;
			} else {
				if(BuildConfig.DEBUG)
					throw new RuntimeException("Can't find message:"+intent.getDataString());
				else
					Log.e(TAG, "Can't find message:"+intent.getDataString());
			}
			if(shouldUpdate) {
				ContentValues updateValues = new ContentValues();
				updateValues.put(Messages.COLUMN_NAME_TIME, System.currentTimeMillis());
				updateValues.put(Messages.COLUMN_NAME_STATUS, Status.SENT.name());
				contentResolver.update(intent.getData(), updateValues, null, null);
			}
			// 提示发送了短信
			String contactName = getReceiverOfMessage(context, intent.getData());
			if(contactName == null)
				contactName = context.getString(R.string.unknown);
			Toast.makeText(context, context.getString(R.string.sms_sent_notification, contactName),
					Toast.LENGTH_LONG).show();
			return;
		default:
			onFailedToSend(context, intent);
			return;
		}
	}

	/**
	 * 当接收到{@link SmsIntent#MESSAGE_SENT_ACTION} 且
	 * {@link #getResultCode()} != {@link Activity#RESULT_OK} 时，被调用
	 * @param context 上下文环境
	 * @param intent 接收到的{@link Intent}
	 */
	//TODO 改进友好性
	protected void onFailedToSend(Context context, Intent intent) {
		Intent resultIntent = new Intent(context, MessagesActivity.class);
		resultIntent.setAction(Intent.ACTION_VIEW);
		resultIntent.setType(Messages.MESSAGES_TYPE);
		long id = ContentUris.parseId(intent.getData());
		if(id >= 1) {
			resultIntent.putExtra(MessagesActivity.EXTRA_IDS, new long[]{id});
			resultIntent.putExtra(MessagesActivity.EXTRA_STATUS, Status.FAILED_TO_SEND);
		}
		PendingIntent resultPendingIntent = PendingIntent.getActivity(
				context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setContentTitle(context.getString(R.string.failed_to_send))
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentIntent(resultPendingIntent)
				.setAutoCancel(true);
		switch(getResultCode()) {
		case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
			Bundle extra = intent.getExtras();
			boolean containErrorCode = false;
			String errorCode = "null";
			if(extra != null && extra.containsKey("errorCode"))
				containErrorCode = true;
			if(containErrorCode)
				errorCode = extra.get("errorCode").toString();
			if(BuildConfig.DEBUG) {
				System.out.println("has errorCode:" + containErrorCode);
				System.out.println("errorCode:" + errorCode);
				// 无话费时，上边是true和21（Integer）
			}
			builder.setContentText(context.getString(
					R.string.failed_to_send_because_of_generic_failure, errorCode));
			break;
		case SmsManager.RESULT_ERROR_NO_SERVICE:
			//TODO 待处理
			break;
		case SmsManager.RESULT_ERROR_NULL_PDU:
			//TODO 待处理
			break;
		case SmsManager.RESULT_ERROR_RADIO_OFF:
			builder.setContentText(context.getString(
					R.string.failed_to_send_because_of_radio_off));
			break;
		}
		ContentValues updateValues = new ContentValues();
		updateValues.put(Messages.COLUMN_NAME_TIME, System.currentTimeMillis());
		updateValues.put(Messages.COLUMN_NAME_STATUS, Status.FAILED_TO_SEND.name());
		context.getContentResolver().update(intent.getData(), updateValues, null, null);
		((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
				.notify(0, builder.build());
		if(BuildConfig.DEBUG) Log.v(TAG, "failed to send: "+getResultCode()+" "+getResultExtras(true));
	}

	/**
	 * 当接收到{@link SmsIntent#MESSAGE_DELIVERED_ACTION}时被调用
	 * @param context 上下文环境
	 * @param intent 接收到的{@link Intent}
	 */
	protected void onMessageDelivered(Context context, Intent intent) {
		ContentValues updateValues = new ContentValues();
		updateValues.put(Messages.COLUMN_NAME_STATUS, Status.DELIVERED.name());
		context.getContentResolver().update(intent.getData(), updateValues, null, null);
		// 提示短息已送达
		String contactName = getReceiverOfMessage(context, intent.getData());
		if(contactName == null)
			contactName = context.getString(R.string.unknown);
		Toast.makeText(context, context.getString(R.string.sms_delivered_notification, contactName),
				Toast.LENGTH_LONG).show();
	}

	private String getReceiverOfMessage(final Context context, final Uri uriOfMessage) {
		final ContentResolver contentResolver = context.getContentResolver();
		Cursor cursor = contentResolver.query(uriOfMessage,
				new String[]{Contract.Messages.COLUMN_NAME_CONTACT_ID}, null, null, null);
		Long contactId = null;
		if(cursor.moveToFirst()) {
			int index = cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_CONTACT_ID);
			if(!cursor.isNull(index))
				contactId = cursor.getLong(index);
		}
		cursor.close();
		if(contactId == null)
			return null;
		final Uri uriOfContact = ContentUris.withAppendedId(
				Contract.Contacts.CONTACTS_ID_URI, contactId);
		cursor = contentResolver.query(uriOfContact,
				new String[]{Contract.Contacts.COLUMN_NAME_NAME}, null, null, null);
		String contactName = null;
		if(cursor.moveToFirst()) {
			int index = cursor.getColumnIndex(Contract.Contacts.COLUMN_NAME_NAME);
			if(!cursor.isNull(index))
				contactName = cursor.getString(index);
		}
		cursor.close();
		return contactName;
	}
}
