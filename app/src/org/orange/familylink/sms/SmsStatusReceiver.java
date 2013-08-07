/**
 *
 */
package org.orange.familylink.sms;

import org.orange.familylink.BuildConfig;
import org.orange.familylink.data.MessageLogRecord.Status;
import org.orange.familylink.database.Contract.Messages;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
			Toast.makeText(context, "sent:"+intent.getDataString(), Toast.LENGTH_LONG).show();
			return;
		case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
			//TODO 待处理
			break;
		case SmsManager.RESULT_ERROR_NO_SERVICE:
			//TODO 待处理
			break;
		case SmsManager.RESULT_ERROR_NULL_PDU:
			//TODO 待处理
			break;
		case SmsManager.RESULT_ERROR_RADIO_OFF:
			//TODO 待处理
			break;
		}
		ContentValues updateValues = new ContentValues();
		updateValues.put(Messages.COLUMN_NAME_TIME, System.currentTimeMillis());
		updateValues.put(Messages.COLUMN_NAME_STATUS, Status.FAILED_TO_SEND.name());
		contentResolver.update(intent.getData(), updateValues, null, null);
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
		Toast.makeText(context, "delivered:"+intent.getDataString(), Toast.LENGTH_LONG).show();
	}

}
