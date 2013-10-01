package org.orange.familylink.sms;

import org.orange.familylink.ContactDetailActivity;
import org.orange.familylink.data.Settings;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.google.gson.JsonSyntaxException;

/**
 * 短信接收的service
 * @author Orange Team
 *
 */
public class SmsReceiverService extends Service {
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
			Log.w("smsfamily", "sms4");
			//对接收的信息进行存储
			localMessage.receiveAndSave(mContext, bodyResult, addressResult);
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
			Log.w("smsfamily", "sms1");
			//信息中的body
			String bodyResult = null;
			//信息中的id
			String idResult = null;
			//信息中的电话号码
			String addressResult = null;

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
				Log.w("smsfamily", "sms2");

				//此时这个短信已经符合是设置中设置的号码发过来的，如果短信不是SmsMessage说明不是本应用发出的，结束
				if(!isSmsMessage(bodyResult)) {
					cursor.close();
					return;
				}

				Log.w("smsfamily", "sms3");
				idResult = cursor.getString(cursor.getColumnIndex("_id"));
				addressResult = cursor.getString(cursor.getColumnIndex("address"));
				cursor.close();
			}else{
				cursor.close();
				return;
			}

			//删除收件箱中的短信
			int row = contentResolver.delete(
					Uri.parse("content://sms/conversations/" + getThreadId()),
					"read = 0",
					null);
			Log.w("row", row + "");
			Log.w("id", idResult + "");
			Log.w("address", addressResult + "");

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
	}

	/**
	 * 
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){

		return START_STICKY;
	}

	/**
	 * 
	 */
	@Override
	public void onDestroy(){
		//注销对系统收件箱的监听
		mContext.getContentResolver().unregisterContentObserver(smsObserver);
	}

}
