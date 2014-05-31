package org.orange.familylink.sms;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.orange.familylink.AlarmActivity;
import org.orange.familylink.ContactDetailActivity;
import org.orange.familylink.MainActivity;
import org.orange.familylink.R;
import org.orange.familylink.ContactDetailActivity.Contact;
import org.orange.familylink.data.CommandMessageBody;
import org.orange.familylink.data.Location;
import org.orange.familylink.data.Message.Code;
import org.orange.familylink.database.Contract;
import org.orange.familylink.data.ResponseMessageBody;
import org.orange.familylink.data.Settings;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * 短信接收的service,这个服务是前台服务
 * @author Orange Team
 *
 */
public class SmsReceiverService extends Service {
	public static final String ACTION_FOREGROUND = "org.orange.familylink.FOREGROUND";
	public static final String ACTION_BACKGROUND = "org.orange.familylink.BACKGROUND";

	private static final Class<?>[] mSetForegroundSignature = new Class[] {
	    boolean.class};
	private static final Class<?>[] mStartForegroundSignature = new Class[] {
        int.class, Notification.class};
    private static final Class<?>[] mStopForegroundSignature = new Class[] {
        boolean.class};

    private NotificationManager mNM;
    private Method mSetForeground;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mSetForegroundArgs = new Object[1];
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];

	static Context mContext;
	SmsObserver smsObserver;

	/**
	 * 存储接收到的信息，调用SmsMessage中的receiveAndSave()方法
	 * @author Orange Team
	 *
	 */
	public static class HandlerOfMessage extends Handler{
		private HandlerOfMessage(){}

		@Override
		public void handleMessage(Message msg){
			super.handleMessage(msg);
			Bundle bundle = msg.getData();
			//获取信息中的body
			String bodyResult = bundle.getString("bodyResult");
			//获取信息中的电话号码
			String addressResult = bundle.getString("addressResult");

			SmsMessage localMessage = new SmsMessage();
			//对接收的信息进行存储
			Uri uri = localMessage.receiveAndSave(mContext, bodyResult, addressResult);

			localMessage.receive(bodyResult, Settings.getPassword(mContext));
			if (localMessage.getCode() == null) return;
			final int code = localMessage.getCode();

			if (Code.isInform(code)) {
				if (Code.Extra.Inform.hasSetUrgent(code)) {
					//监护方接收到紧急消息，启动AlarmActivity
					startAlarm(uri);
				} else if (Code.Extra.Inform.hasSetRespond(code)) {
					//监护方接收到受顾方返回的响应消息
					onReceiveRespondMessage(addressResult, localMessage, code);
				}
			} else if (Code.isCommand(code)) {
				//受顾方接收监护方的命令消息
				onReceiveCommandMessage(addressResult, localMessage, code);
			}
		}

		private void onReceiveRespondMessage(String messageAddress, SmsMessage message, int code) {
			final Gson gson = new Gson();
			final ResponseMessageBody messageBody = gson.fromJson(
							message.getBody(),
							ResponseMessageBody.class);
			// 查询此消息响应的命令
			if (messageBody.getId() == null) throw new NullPointerException("responseMessageBody's id is null");
			final String[] projection = {
					Contract.Messages.COLUMN_NAME_ADDRESS,
					Contract.Messages.COLUMN_NAME_CODE};
			Uri uri = ContentUris.withAppendedId(
					Contract.Messages.MESSAGES_ID_URI, messageBody.getId());
			Cursor cursor = mContext.getContentResolver().query(
					uri, projection, null, null, null);
			if (cursor.getCount() != 1) {
				if (cursor.getCount() == 0) {
					throw new IllegalStateException(
						"Can't find command to whom this message respond");
				} else {
					throw new RuntimeException();
				}
			}
			// 检查此消息的发送者和命令的接受者 是否一致
			cursor.moveToFirst();
			String commanded = cursor.getString(cursor
					.getColumnIndex(Contract.Messages.COLUMN_NAME_ADDRESS));
			if (!messageAddress.equals(commanded)) {
				throw new IllegalStateException("The sender of response " +
						"message isn't the receiver of the command message");
			}
			// 确认此消息应答的消息，是个命令消息
			int codeIndex = cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_CODE);
			if (cursor.isNull(codeIndex)) {
				throw new IllegalStateException("The message to whom this " +
						"message respond isn't a command message");
			}
			final int commandCode = cursor.getInt(codeIndex);
			cursor.close();
			if (!Code.isCommand(commandCode)) {
				throw new IllegalStateException("The message to whom this " +
						"message respond isn't a command message");
			}
			// 根据命令类型，处理此响应消息
			if (Code.Extra.Command.hasSetLocateNow(commandCode)) {
				//监护方接收到受顾方返回的现在定位结果
				startMap(gson.fromJson(messageBody.getContent(), Location.class));
			}
		}

		/**
		 * 监护方接收到本应用发出的短信，之后通过这个方法分析短信的内容是否为紧急消息，如果是就会启动警告界面来提供帮助
		 * @param uri
		 */
		private void startAlarm(Uri uri){
			long messageId = ContentUris.parseId(uri);
			Intent mIntent = new Intent(mContext, AlarmActivity.class);
			mIntent.putExtra(AlarmActivity.EXTRA_ID, messageId);
			mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(mIntent);
		}

		/**
		 * 接收到命令消息时被调用
		 */
		private void onReceiveCommandMessage(String messageAddress, SmsMessage localMessage, int code){
			final Gson gson = new Gson();
			CommandMessageBody messageBody = gson.fromJson(
							localMessage.getBody(),
							CommandMessageBody.class);
			ResponseMessageBody response = new ResponseMessageBody();
			response.setId(messageBody.getId());
			if (Code.Extra.Command.hasSetLocateNow(code)) {
//				Location location;
//				location = new Location();//TODO get current location
//				response.setContent(gson.toJson(location));
			}
			final SmsMessage message = new SmsMessage();
			message.setCode(Code.INFORM | Code.Extra.Inform.RESPONSE);
			message.setBody(response.toJson());
			// 发送消息
			Contact contact = ContactDetailActivity.getDefaultContact(mContext);
			if(!contact.phone.equals(messageAddress)) {
				throw new IllegalStateException(
						"Message's sender isn't the default contact");
			}
			message.sendAndSave(mContext, contact.id, contact.phone);
		}

		/**
		 * 监护方接收到受顾方返回的现在定位结果
		 * @param location 应答现在定位命令的位置信息
		 */
		private void startMap(Location location){
			//MapActivity
		}

	}

	/**
	 * 监听器类，监听到收件箱中有新的短信来之后看是否为本应用发出的短信，如果是就截获到本应用中，且删除收件箱中的这条短信
	 * @author Orange Team
	 *
	 */
	public final class SmsObserver extends ContentObserver{
		private Handler mHandler;

		public SmsObserver(Handler handler){
			super(handler);
			mHandler = handler;
		}

		@Override
		public void onChange(boolean selfChange){
			//信息中的body
			String bodyResult = null;
			//信息中的电话号码
			String addressResult = null;
			//信息中的ID
			long idResult = 0;

			ContentResolver contentResolver = mContext.getContentResolver();
			Uri uri = Uri.parse("content://sms/inbox");
			//查询收件箱中的短信，看此短信是否是设置中设置的号码发过来的短信
			Cursor cursor = contentResolver.query(
					uri,
					new String[] {"_id", "address", "body"},
					"address like ? and read = 0",
					getAddressOfSetting(),
					"date DESC");

			if(cursor.moveToFirst()){ // if the cursor isn't empty
				bodyResult = cursor.getString(cursor.getColumnIndex("body"));

				//此时这个短信已经符合是设置中设置的号码发过来的，如果短信不是SmsMessage说明不是本应用发出的，结束
				if(!isSmsMessage(bodyResult)) {
					cursor.close();
					return;
				}

				addressResult = cursor.getString(cursor.getColumnIndex("address"));
				idResult = cursor.getLong(cursor.getColumnIndex("_id"));
				cursor.close();
			}else{
				cursor.close();
				return;
			}

			if(idResult != 0){
				//删除收件箱中的短信
				contentResolver.delete(
						Uri.parse("content://sms/conversations/" + getThreadId()),
						"read = 0 and _id = ?",
						new String[] {String.valueOf(idResult)});
			}

			Message msg = new Message();
			Bundle bundle = new Bundle();
			//数据封装到bundle中
			bundle.putString("bodyResult", bodyResult);
			bundle.putString("addressResult", addressResult);
			msg.setData(bundle);
			mHandler.sendMessage(msg);
		}

		/**
		 * 获取对话的id
		 * @return ThreadId long类型
		 */
		public long getThreadId(){
			long threadId = -1;
			String SORT_ORDER = "date DESC";
			Cursor cursor = mContext.getContentResolver().query(
					Uri.parse("content://sms/inbox"),
					new String[] {"_id", "thread_id", "address", "date"},
					"address like ?", getAddressOfSetting(), SORT_ORDER);
			if(cursor.moveToFirst()){
				threadId = cursor.getLong(cursor.getColumnIndex("thread_id"));
			}
			cursor.close();
			return threadId;
		}

		/**
		 * 检查指定内容是否是本应用发送的{@link SmsMessage}
		 * @param body 需检查的短信的内容
		 * @return 如果是本应用发送的{@link SmsMessage}，返回true；否则返回false
		 */
		protected boolean isSmsMessage(String body){
			try{
				new SmsMessage().receive(body, Settings.getPassword(mContext));
				return true;
			} catch(JsonSyntaxException e) {
				return false;
			}
		}

		/**
		 * 此方法用于获取设置中的联系人的号码然后生成字符串数组
		 * @return
		 */
		protected String[] getAddressOfSetting(){
			final int TYPES_OF_PHONE = 1;
			String[] phones = new String[TYPES_OF_PHONE];
			phones[0] = "%" + ContactDetailActivity.getDefaultContact(mContext).phone;
			return phones;
		}
	}

	/**
	 * bindService时会调用这个方法
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate(){

		mContext = SmsReceiverService.this;
		smsObserver = new SmsObserver(new HandlerOfMessage());

		//对系统的收件箱注册监听
		mContext.getContentResolver().registerContentObserver(
				Uri.parse("content://sms"),
				true,
				smsObserver);

		//获取通知的管理类
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		try {
			mStartForeground = getClass().getMethod("startForeground",
					mStartForegroundSignature);
			mStopForeground = getClass().getMethod("stopForeground",
					mStopForegroundSignature);
			return;
		} catch (NoSuchMethodException e) {
			// 运行旧版本
			mStartForeground = mStopForeground = null;
		}

		try {
			mSetForeground = getClass().getMethod("setForeground",
					mSetForegroundSignature);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(
					"OS doesn't have Service.startForeground OR Service.setForeground!");
		}
	}

	/**
	 * 
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		handleCommand(intent);

		return START_STICKY;
	}

	/**
	 * 处理notification显示的内容，且调用启动前台服务的方法
	 * @param intent 启动这个服务时的Intent,且这个intent要设置ACTION
	 */
	void handleCommand(Intent intent) {
		//启动这个服务时设置的ACTION这个ACTION设置为ACTION_FOREGROUND说明为启用前台服务
		if (ACTION_FOREGROUND.equals(intent.getAction())) {
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle(getText(R.string.SMS_receiver_service_title))
			.setContentText(getText(R.string.SMS_receiver_service_content));

			Intent resultIntent = new Intent(this, MainActivity.class);
			//制造一个回退activity栈，使得从notification进入应用后退出时是退到home
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
			stackBuilder.addParentStack(MainActivity.class);
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent =stackBuilder.getPendingIntent(
					0,
					PendingIntent.FLAG_UPDATE_CURRENT
					);
			mBuilder.setContentIntent(resultPendingIntent);

			//启动前台服务方法的调用
			startForegroundCompat(R.string.SMS_receiver_service_title, mBuilder.build());
		} else if (ACTION_BACKGROUND.equals(intent.getAction())) {
			stopForegroundCompat(R.string.SMS_receiver_service_title);
		}
	}

	void invokeMethod(Method method, Object[] args) {
		try {
			method.invoke(this, args);
		} catch (InvocationTargetException e) {
			Log.w("ForegroundService", "Unable to invoke method", e);
		} catch (IllegalAccessException e) {
			Log.w("ForegroundService", "Unable to invoke method", e);
		}
	}

	/**
	 * 启动前台服务
	 * @param id notification显示的标题字符的id
	 * @param notification
	 */
	void startForegroundCompat(int id, Notification notification) {
		// API为2。0以后的版本使用startForeground
		if (mStartForeground != null) {
			mStartForegroundArgs[0] = Integer.valueOf(id);
			mStartForegroundArgs[1] = notification;
			invokeMethod(mStartForeground, mStartForegroundArgs);
			return;
		}

		// API小于2.0的版本使用setForeground
		mSetForegroundArgs[0] = Boolean.TRUE;
		invokeMethod(mSetForeground, mSetForegroundArgs);
		mNM.notify(id, notification);
	}

	/**
	 * 结束前台服务
	 * @param id
	 */
	void stopForegroundCompat(int id) {
		// API为2。0以后的版本使用stopForeground
		if (mStopForeground != null) {
			mStopForegroundArgs[0] = Boolean.TRUE;
			invokeMethod(mStopForeground, mStopForegroundArgs);
			return;
		}

		// API小于2.0的版本使用setForeground
		mNM.cancel(id);
		mSetForegroundArgs[0] = Boolean.FALSE;
		invokeMethod(mSetForeground, mSetForegroundArgs);
	}

	/**
	 * 
	 */
	@Override
	public void onDestroy(){
		//注销对系统收件箱的监听
		mContext.getContentResolver().unregisterContentObserver(smsObserver);
		//停止前台服务
		stopForegroundCompat(R.string.SMS_receiver_service_title);
	}

}
