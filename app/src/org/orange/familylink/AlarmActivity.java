/**
 *
 */
package org.orange.familylink;

import java.io.IOException;

import org.orange.familylink.data.UrgentMessageBody;
import org.orange.familylink.database.Contract;
import org.orange.familylink.navigation.StartNavigation;
import org.orange.familylink.util.AudioFocusHelper;
import org.orange.familylink.util.ConvertUtil;
import org.orange.familylink.util.Network;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.google.gson.Gson;

/**
 * 警报{@link Activity}
 * 
 * @author Team Orange
 */
public class AlarmActivity extends BaseActivity {
	/**
	 * 警告消息的ID
	 * <p>
	 * Type: long
	 */
	public static final String EXTRA_ID = AlarmActivity.class.getName()
			+ ".extra.ID";

	private MessageWrapper mUrgentMessage;
	private AsyncPositionTranslator mAsyncPositionTranslator;
	// 用于UI状态控制
	private TextView mTextViewAlarmNotification;
	private TextView mTextViewPosition;
	private Button mButtonCallBack;
	private Button mButtonNavigate;
	// 用于警报蜂鸣声
	private AudioFocusHelper mAudioFocusHelper;
	private MediaPlayer mMediaPlayer;

	// 高德地图对象的操作方法与接口
	private AMap aMap;
	// 地图的容器
	private MapView mapView;

	private void setUrgentMessage(MessageWrapper message) {
		mUrgentMessage = message;
		if (message == null)
			return;
		// 设置 通告内容和回电（call back）按钮的内容
		Integer notificationResId = null;
		if (message.body.getType() == UrgentMessageBody.Type.SEEK_HELP) {
			getActionBar().setTitle(R.string.seek_help_alarm);
			findViewById(R.id.main_container).setBackgroundColor(
					getResources().getColor(R.color.urgent));
			notificationResId = R.string.seek_help_alarm_notification;
		} else if (message.body.getType() == UrgentMessageBody.Type.FALL_DOWN_ALARM) {
			getActionBar().setTitle(R.string.fall_down_alarm);
			findViewById(R.id.main_container).setBackgroundColor(
					getResources().getColor(R.color.command));
			notificationResId = R.string.fall_down_alarm_notification;
		}
		if (message.contact_name != null) {
			mTextViewAlarmNotification.setText(getString(notificationResId,
					message.contact_name));
			mButtonCallBack.setText(getString(R.string.call_to_somebody,
					message.contact_name));
		} else {
			mTextViewAlarmNotification.setText(getString(notificationResId,
					message.address));
			mButtonCallBack.setText(R.string.call_back);
		}
		// 显示 发送方当前位置
		if (message.body.containsPosition()) {
			String location = message.body.getPositionLatitude() + ","
					+ message.body.getPositionLongitude();
			setPosition(location);
			mButtonNavigate.setVisibility(View.VISIBLE);
			// 异步把location翻译为人类可读的地点
			if (mAsyncPositionTranslator != null)
				mAsyncPositionTranslator.cancel(true);
			mAsyncPositionTranslator = new AsyncPositionTranslator();
			mAsyncPositionTranslator.execute((Void) null);
		} else {
			mTextViewPosition.setText(R.string.unknown);
			mButtonNavigate.setVisibility(View.INVISIBLE);
		}
	}

	private void setPosition(String text) {
		if (text != null) {
			if (mUrgentMessage.body.containsPosition()) {
				// 设置在地图上显示的超链接
				SpannableString ss = new SpannableString(text);
				URLSpan span = new URLSpan("geo:"
						+ mUrgentMessage.body.getPositionLatitude() + ","
						+ mUrgentMessage.body.getPositionLongitude());
				ss.setSpan(span, 0, ss.length(),
						SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
				mTextViewPosition.setText(ss);
				mTextViewPosition.setMovementMethod(LinkMovementMethod
						.getInstance());
			} else
				// !mUrgentMessage.body.containsPosition()
				mTextViewPosition.setText(text);
		} else
			// text == null
			mTextViewPosition.setText(R.string.unknown);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm);
		mTextViewAlarmNotification = (TextView) findViewById(R.id.alarm_notification);
		mTextViewPosition = (TextView) findViewById(R.id.position);
		mButtonCallBack = (Button) findViewById(R.id.call_to_somebody);
		mButtonNavigate = (Button) findViewById(R.id.navigate_to_this_position);

		mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);// 必须要写

		init();

		Bundle extras = getIntent().getExtras();
		if (extras == null || !extras.containsKey(EXTRA_ID))
			throw new IllegalStateException(
					"You must put extra: EXTRA_ID(fell down alarm message's ID)");
		long messageId = extras.getLong(EXTRA_ID);
		new ShowUrgentMessage().execute(messageId);

		mAudioFocusHelper = new AudioFocusHelper(this) {
			@Override
			public void onAudioFocusChange(int focusChange) {
			}
		};
		mMediaPlayer = MediaPlayer.create(this, R.raw.alarm);
		mMediaPlayer.setLooping(true);
		startAlarm();
	}

	/**
	 * 初始化AMap对象
	 */
	private void init() {
		if (aMap == null) {
			aMap = mapView.getMap();
		}
	}

	/**
	 * 绘制系统默认的1种marker背景图片
	 */
	public void drawMarkers() {
		Marker marker = aMap.addMarker(new MarkerOptions()
				.position(
						new LatLng(mUrgentMessage.body.getPositionLatitude(),
								mUrgentMessage.body.getPositionLongitude()))
				.title("好好学习")
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
				.perspective(true).draggable(true));
		marker.setRotateAngle(0);// 设置marker旋转90度
		marker.showInfoWindow();// 设置默认显示一个infowinfow
		aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
				new LatLng(mUrgentMessage.body.getPositionLatitude(),
						mUrgentMessage.body.getPositionLongitude()), 15));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
		if (mMediaPlayer != null) {
			stopAlarm();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}

	/**
	 * 当 导航到此地点 按钮被点击时，调用此方法
	 * 
	 * @param button
	 *            被点击的按钮被点击的按钮
	 */
	public void onClickNavigateToThisPosition(View button) {
		try {
			StartNavigation.toStartNavigationApp(this, getFragmentManager(),
					mUrgentMessage.body.getPositionLatitude(),
					mUrgentMessage.body.getPositionLongitude());
		} catch (NullPointerException e) {
			// do nothing
		}
	}

	/**
	 * 当回电按钮被点击时，调用此方法
	 * 
	 * @param button
	 *            被点击的按钮
	 */
	public void onClickCallBack(View button) {
		if (mUrgentMessage == null || mUrgentMessage.address == null)
			return;
		Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
				+ mUrgentMessage.address));
		startActivity(intent);
	}

	/**
	 * 当停止警报声钮被点击时，调用此方法
	 * 
	 * @param button
	 *            被点击的按钮
	 */
	public void onClickStopAlarm(View button) {
		stopAlarm();
	}

	/**
	 * 开始播放警报声
	 * 
	 * @return 成功时，返回true；失败时，返回false
	 */
	private boolean startAlarm() {
		if (mAudioFocusHelper.requestFocus()) {
			try {
				mMediaPlayer.prepare();
			} catch (IllegalStateException e) {
				// may be Prepared, do nothing
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			mMediaPlayer.start();
			return true;
		} else
			return false;
	}

	/**
	 * 停止播放警报声
	 */
	private void stopAlarm() {
		try {
			mMediaPlayer.stop();
		} catch (IllegalStateException e) {
			// do nothing
		}
		mAudioFocusHelper.abandonFocus();
	}

	private static class MessageWrapper {
		public String contact_name;
		public String address;
		public UrgentMessageBody body;
	}

	private class ShowUrgentMessage extends
			AsyncTask<Long, Void, MessageWrapper> {
		private final String[] PROJECTION_MESSAGE = {
				Contract.Messages.COLUMN_NAME_ADDRESS,
				Contract.Messages.COLUMN_NAME_BODY,
				Contract.Messages.COLUMN_NAME_CONTACT_ID };
		private final String[] PROJECTION_CONTACT = { Contract.Contacts.COLUMN_NAME_NAME };

		@Override
		protected MessageWrapper doInBackground(Long... params) {
			ContentResolver contentResolver = getContentResolver();
			long messageId = params[0];
			MessageWrapper result = new MessageWrapper();
			Uri uri = ContentUris.withAppendedId(
					Contract.Messages.MESSAGES_ID_URI, messageId);
			Cursor cursor = contentResolver.query(uri, PROJECTION_MESSAGE,
					null, null, null);
			if (!cursor.moveToFirst()) // if the cursor is empty
				return null;
			long contactId = cursor.getLong(cursor
					.getColumnIndex(Contract.Messages.COLUMN_NAME_CONTACT_ID));
			result.address = cursor.getString(cursor
					.getColumnIndex(Contract.Messages.COLUMN_NAME_ADDRESS));
			String body = cursor.getString(cursor
					.getColumnIndex(Contract.Messages.COLUMN_NAME_BODY));
			result.body = new Gson().fromJson(body, UrgentMessageBody.class);
			cursor.close();

			uri = ContentUris.withAppendedId(Contract.Contacts.CONTACTS_ID_URI,
					contactId);
			cursor = contentResolver.query(uri, PROJECTION_CONTACT, null, null,
					null);
			if (cursor.moveToFirst()) {
				result.contact_name = cursor.getString(cursor
						.getColumnIndex(Contract.Contacts.COLUMN_NAME_NAME));
			}
			cursor.close();
			return result;
		}

		@Override
		protected void onPostExecute(MessageWrapper result) {
			if (result != null){
				setUrgentMessage(result);
				drawMarkers();
			}
		}
	}

	private class AsyncPositionTranslator extends AsyncTask<Void, Void, String> {
		@Override
		protected String doInBackground(Void... params) {
			String resultAddress = null;
			if (!mUrgentMessage.body.containsPosition())
				return null;
			final double latitude = mUrgentMessage.body.getPositionLatitude();
			final double longitude = mUrgentMessage.body.getPositionLongitude();
			if (!Network.isConnected(AlarmActivity.this))
				return null;
			resultAddress = ConvertUtil.getAddress(longitude, latitude);
			if (resultAddress != null) {
				if (resultAddress.isEmpty())
					resultAddress = null;
				else
					resultAddress += "(" + latitude + "," + longitude + ")";
			}
			return resultAddress;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null)
				setPosition(result);
			if (mAsyncPositionTranslator == this)
				mAsyncPositionTranslator = null;
		}
	}

}
