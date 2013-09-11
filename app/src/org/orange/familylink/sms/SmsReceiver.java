package org.orange.familylink.sms;

import org.orange.familylink.data.Message.Code;
import org.orange.familylink.data.Settings;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 接收短信
 * @author Orange Team
 *
 */
public class SmsReceiver extends BroadcastReceiver {
	static Context mContext;
	Intent mIntent;

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		mContext = arg0;
		mIntent = arg1;
		if(mIntent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
			//在收件箱中注册监听
			mContext.getContentResolver().registerContentObserver(
					Uri.parse("content://sms/inbox"),
					true,
					new SmsObserver(new HandlerOfMessage()));
		}
	}

	/**
	 * 处理接收到的信息
	 * @author Orange Team
	 *
	 */
	public static class HandlerOfMessage extends Handler{
		public HandlerOfMessage(){}

		@Override
		public void handleMessage(Message msg){
			super.handleMessage(msg);
			Bundle bundle = msg.getData();
			String bodyResult = bundle.getString("bodyResult");
			String addressResult = bundle.getString("addressResult");
			SmsMessage localMessage = new SmsMessage();
			//对接收的信息进行处理且存储
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
			String bodyResult = null;
			String idResult = null;
			String addressResult = null;
			ContentResolver contentResolver = mContext.getContentResolver();
			Uri uri = Uri.parse("content://sms/inbox");
			//查询收件箱中的短信，看此短信是否是设置中设置的号码发过来的短信
			Cursor cursor = contentResolver.query(
					uri,
					new String[] {"_id", "address", "body"},
					formWhere() + " and read = 0",
					getAddressOfSetting(),
					"date DESC");
			if(cursor.getCount() > 0 && cursor.moveToFirst()){
				bodyResult = cursor.getString(cursor.getColumnIndex("body"));

				//此时这个短信已经符合是设置中设置的号码发过来的，如果短信中到 Code不正确说明不是本应用发出的，结束
				if(!getCodeFromBody(bodyResult))
					return;

				idResult = cursor.getString(cursor.getColumnIndex("_id"));
				addressResult = cursor.getString(cursor.getColumnIndex("address"));
			}

			cursor.close();

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
			bundle.putString("bodyResult", bodyResult);
			bundle.putString("addressResult", addressResult);
			msg.setData(bundle);
			mHandler.sendMessage(msg);
		}

		public long getThreadId(){
			long threadId = -1;
			String SORT_ORDER = "date DESC";
			Cursor cursor = mContext.getContentResolver().query(
					Uri.parse("content://sms/inbox"),
					new String[] {"_id", "thread_id", "address", "date"},
					formWhere(), getAddressOfSetting(), SORT_ORDER);
			if(cursor.getCount() > 0 && cursor.moveToFirst()){
				threadId = cursor.getLong(cursor.getColumnIndex("thread_id"));
			}
			cursor.close();
			return threadId;
		}

		/**
		 * 此方法用于查看短信中的code是否正确
		 * @param body
		 * @return
		 */
		protected boolean getCodeFromBody(String body){
			SmsMessage mMessage = new SmsMessage();
			mMessage.receive(body, Settings.getPassword(mContext));
			if(Code.isLegalCode(mMessage.getCode()))
				return true;
			else
				return false;
		}

		/**
		 * 此方法用于获取设置中的联系人的个数然后生成字符串，如"address = ? or address = ?"
		 * @return
		 */
		protected String formWhere(){
			return null;
		}

		/**
		 * 此方法用于获取设置中的联系人的号码然后生成字符串数组
		 * @return
		 */
		protected String[] getAddressOfSetting(){
			return null;
		}
	}

}
