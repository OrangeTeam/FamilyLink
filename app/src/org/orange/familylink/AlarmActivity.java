/**
 *
 */
package org.orange.familylink;

import org.orange.familylink.data.UrgentMessageBody;
import org.orange.familylink.database.Contract;
import org.orange.familylink.navigation.StartNavigation;
import org.orange.familylink.util.ConvertUtil;
import org.orange.familylink.util.Network;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

/**
 * 警报{@link Activity}
 * @author Team Orange
 */
public class AlarmActivity extends BaseActivity {
	/**
	 * 警告消息的ID
	 * <p>
	 * Type: long
	 */
	public static final String EXTRA_ID = AlarmActivity.class.getName() + ".extra.ID";

	private MessageWrapper mUrgentMessage;
	private TextView mTextViewAlarmNotification;
	private TextView mTextViewPosition;
	private Button mButtonCallBack;
	private Button mButtonNavigate;

	private void setUrgentMessage(MessageWrapper message) {
		mUrgentMessage = message;
		if(message == null)
			return;
		// 设置 通告内容和回电（call back）按钮的内容
		Integer notificationResId = null;
		if(message.body.getType() == UrgentMessageBody.Type.SEEK_HELP)
			notificationResId = R.string.seek_help_alarm_notification;
		else if(message.body.getType() == UrgentMessageBody.Type.FALL_DOWN_ALARM)
			notificationResId = R.string.fall_down_alarm_notification;
		if(message.contact_name != null) {
			mTextViewAlarmNotification.setText(getString(notificationResId, message.contact_name));
			mButtonCallBack.setText(getString(R.string.call_to_somebody, message.contact_name));
		} else {
			mTextViewAlarmNotification.setText(getString(notificationResId, message.address));
			mButtonCallBack.setText(R.string.call_back);
		}
		// 显示 发送方当前位置
		if(message.body.containsPosition()) {
			String resultAddress = null;
			if(Network.isConnected(this)){
				resultAddress = ConvertUtil.getAddress(message.body.getPositionLongitude(),
						message.body.getPositionLatitude());
				mTextViewPosition.setText(resultAddress);
			} else {
				mTextViewPosition.setText(message.body.getPositionLatitude() + "," +
						message.body.getPositionLongitude());
			}
			mButtonNavigate.setVisibility(View.VISIBLE);
		} else {
			mTextViewPosition.setText(R.string.unknown);
			mButtonNavigate.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm);
		mTextViewAlarmNotification = (TextView) findViewById(R.id.alarm_notification);
		mTextViewPosition = (TextView) findViewById(R.id.position);
		mButtonCallBack = (Button) findViewById(R.id.call_to_somebody);
		mButtonNavigate = (Button) findViewById(R.id.navigate_to_this_position);

		Bundle extras = getIntent().getExtras();
		if(extras == null || !extras.containsKey(EXTRA_ID))
			throw new IllegalStateException("You must put extra: EXTRA_ID(fell down alarm message's ID)");
		long messageId = extras.getLong(EXTRA_ID);
		new ShowUrgentMessage().execute(messageId);
	}

	/**
	 * 当 导航到此地点 按钮被点击时，调用此方法
	 * @param button 被点击的按钮被点击的按钮
	 */
	public void onClickNavigateToThisPosition(View button) {
		try{
			StartNavigation.toStartNavigationApp(this,
					getFragmentManager(),
					mUrgentMessage.body.getPositionLatitude(),
					mUrgentMessage.body.getPositionLongitude());
		} catch(NullPointerException e) {
			// do nothing
		}
	}
	/**
	 * 当回电按钮被点击时，调用此方法
	 * @param button 被点击的按钮
	 */
	public void onClickCallBack(View button) {
		if(mUrgentMessage == null || mUrgentMessage.address == null)
			return;
		Intent intent = new Intent(Intent.ACTION_CALL,
				Uri.parse("tel:" + mUrgentMessage.address));
		startActivity(intent);
	}


	private static class MessageWrapper {
		public String contact_name;
		public String address;
		public UrgentMessageBody body;
	}
	private class ShowUrgentMessage extends AsyncTask<Long, Void, MessageWrapper> {
		private final String[] PROJECTION_MESSAGE = {
			Contract.Messages.COLUMN_NAME_ADDRESS,
			Contract.Messages.COLUMN_NAME_BODY,
			Contract.Messages.COLUMN_NAME_CONTACT_ID};
		private final String[] PROJECTION_CONTACT = {Contract.Contacts.COLUMN_NAME_NAME};

		@Override
		protected MessageWrapper doInBackground(Long... params) {
			ContentResolver contentResolver = getContentResolver();
			long messageId = params[0];
			MessageWrapper result = new MessageWrapper();
			Uri uri = ContentUris.withAppendedId(Contract.Messages.MESSAGES_ID_URI, messageId);
			Cursor cursor = contentResolver.query(uri, PROJECTION_MESSAGE, null, null, null);
			if(!cursor.moveToFirst()) //if the cursor is empty
				return null;
			long contactId = cursor.getLong(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_CONTACT_ID));
			result.address = cursor.getString(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_ADDRESS));
			String body = cursor.getString(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_BODY));
			result.body = new Gson().fromJson(body, UrgentMessageBody.class);
			cursor.close();

			uri = ContentUris.withAppendedId(Contract.Contacts.CONTACTS_ID_URI, contactId);
			cursor = contentResolver.query(uri, PROJECTION_CONTACT, null, null, null);
			if(cursor.moveToFirst()) {
				result.contact_name = cursor.getString(cursor.getColumnIndex(Contract.Contacts.COLUMN_NAME_NAME));
			}
			cursor.close();
			return result;
		}

		@Override
		protected void onPostExecute(MessageWrapper result) {
			if(result != null)
				setUrgentMessage(result);
		}
	}

}
