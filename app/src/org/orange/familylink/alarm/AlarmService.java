package org.orange.familylink.alarm;

import android.app.Service;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.IBinder;

/**
 * 摔倒检测{@link Service}
 * @author Team Orange
 */
public class AlarmService extends Service {
	//实例化传感器
	private AccelerometerListener mAccelerometerListener;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * 在Service启动时执行
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		//进行摔倒检测
		mAccelerometerListener = new AccelerometerListener(this);
		//加速器注册
		mAccelerometerListener.register(this, SensorManager.SENSOR_DELAY_NORMAL) ;
	}

	/**
	 * 在Service结束时执行
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		mAccelerometerListener.unregister(this);
	}

}
