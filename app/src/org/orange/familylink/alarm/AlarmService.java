package org.orange.familylink.alarm;

import org.orange.familylink.alarm.AccelerometerListener.OnFallListener;

import android.app.Service;
import android.content.Intent;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.IBinder;

public class AlarmService extends Service {
	
	/**
	 * 摔倒检测Service
	 * @author orange Team
	 */
	//实例化传感器
	private AccelerometerListener mAccelerometerListener ;
	@Override
	
	public IBinder onBind(Intent arg0) {
		return null;
	}

	/**
	 * 在Service启动时执行
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		//进行摔倒检测
		mAccelerometerListener = new AccelerometerListener(this) {
			@Override
			public void onSensorChanged(SensorEvent event) {
				super.onSensorChanged(event);
			}
		}.setOnFallListener(new OnFallListener() {
			@Override
			public void onFall(AccelerometerListener eventSource, float[] raw,
					float[] gravity, float[] linearAcceleration) {
				//如果摔倒启动倒计时Activity
				mAccelerometerListener.setAutoAlarm(AlarmService.this) ;
			}
		});
		//加速器注册
		mAccelerometerListener.register(this, SensorManager.SENSOR_DELAY_NORMAL) ;
	}

	/**
	 * 在Service结束时执行
	 */
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mAccelerometerListener.unregister(this);
	}
	
}
