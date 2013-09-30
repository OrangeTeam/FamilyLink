package org.orange.familylink.location;

import java.util.Timer;
import java.util.TimerTask;

import org.orange.familylink.ContactDetailActivity;
import org.orange.familylink.R;
import org.orange.familylink.ContactDetailActivity.Contact;
import org.orange.familylink.data.Message.Code;
import org.orange.familylink.data.Settings;
import org.orange.familylink.sms.SmsMessage;
import org.orange.familylink.util.ConvertUtil;
import org.orange.familylink.util.Network;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.widget.Toast;

/**
 * 定位服务
 * @author Orange Team
 *
 */
public class LocationService extends Service {
	static Context mContext;

	//调用一个定位类
	static LocationTracker mLocationTracker;

	private Looper mServiceLooper;

	//服务帮助的一个内部类
	private ServiceHandler mServiceHandler;

	//短信发送时间计划控制
	private SmsSenderController senderController;

	//本应用中编写的一个Message类
	static private org.orange.familylink.data.Message localMessage;

	/**
	 * 服务中的一个内部类，继承了Handler类，用于一些功能上的操作
	 * @author Orange Team
	 *
	 */
	private static final class ServiceHandler extends Handler {

		public ServiceHandler(Looper looper){
			super(looper);
		}

		/**
		 * 在这里获取位置信息且转换为可读的地理位置，存储到数据库中，通过短信发送定位信息给监护人
		 */
		@Override
		public void handleMessage(Message msg){
			String resultAddress = null;

			if(Network.isConnected(mContext)){
				//根据经纬度获取地址位置信息
				resultAddress = ConvertUtil.getAddress(mLocationTracker.getLongitude(),
						mLocationTracker.getLatitude());
			}else{
				Toast.makeText(mContext, R.string.unconnection, Toast.LENGTH_LONG).show();
				return;
			}

			if(mLocationTracker.canGetLocation()){
				localMessage = new SmsMessage();
				localMessage.setBody(resultAddress);
				localMessage.setCode(Code.INFORM | Code.Extra.Inform.PULSE);
				Contact contact = ContactDetailActivity.getDefaultContact(mContext);
				localMessage.sendAndSave(mContext, contact.id, contact.phone);
			}
		}
	}

	/**
	 * 一个内部类，用于短信发送的时间计划任务
	 * @author Orange Team
	 *
	 */
	private class SmsSenderController extends TimerTask{
		private Timer timer = new Timer();

		/**
		 * 在构造方法中初始化ServiceHandler
		 * @param looper
		 */
		public SmsSenderController(Looper looper){
			mServiceLooper = looper;
			mServiceHandler = new ServiceHandler(mServiceLooper);
		}

		/**
		 * 在计划的时间到达指定的时间时系统会调用这个方法之后调用handleMessage
		 */
		@Override
		public void run() {
			Message message = new Message();
			mServiceHandler.sendMessage(message);
		}
	}

	/**
	 * bindService时会调用这个方法
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	/**
	 * 不管是bindService还是startService都会调用这个方法，这个方法只被执行一次，所以一些初始化的工作在这里
	 * 如果是先startService那么bindService时就会跳过这个方法
	 */
	@Override
	public void onCreate(){
		//因为服务被启动时是在一个线程中，为了不中断UI线程，这里启动一个新的线程
		HandlerThread thread = new HandlerThread("ServiceStartArguments",
				Process.THREAD_PRIORITY_BACKGROUND);

		thread.start();

		mContext = LocationService.this;

		//实例化定位操作
		mLocationTracker = new LocationTracker(LocationService.this);

		//实例化短信发送的时间计划控制操作
		senderController = new SmsSenderController(thread.getLooper());
	}

	/**
	 * 
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){

		if(Settings.getStartLocationService(LocationService.this)){
			//启动时间计划进行发送定位信息短信
			senderController.timer.schedule(senderController, 2*1000,
					Settings.getLocateFrequency(LocationService.this));
		}else{
			stopSelf();
		}

		return START_STICKY;
	}

	/**
	 * 
	 */
	@Override
	public void onDestroy(){
		//当服务被退出时移除定位监听，也就是不再定位了
		mLocationTracker.stopUsingGPS();
		//当服务被退出时移除时间任务计划
		senderController.timer.cancel();
	}

}
