/**
 *
 */
package org.orange.familylink.alarm;

import org.orange.familylink.AlarmCountdownActivity;
import org.orange.familylink.BuildConfig;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * 用于 <em>检测摔倒</em> 的加速度传感器（{@link Sensor}）的{@link SensorEventListener}。
 * @author Team Orange
 */
public class AccelerometerListener implements SensorEventListener {
	private static final String TAG = AccelerometerListener.class.getSimpleName();

	private final Context mContext;
	private final Intent mAlarmIntent;
	private OnFallListener mOnFallListener = null;
	private SensorEvent mSensorEvent = null;
	private float[] gravity = null;
	private float[] linear_acceleration = null;

	/**
	 * 构造方法
	 * <p>
	 * Tips：可以链式调用，如
	 * <code>new AccelerometerListener(context).setOnFallListener(listener)</code>
	 * @param context 上下文信息；如果设置为null，就不自动启动{@link AlarmCountdownActivity}
	 */
	public AccelerometerListener(Context context) {
		super();
		mContext = context;
		if(mContext != null)
			mAlarmIntent = new Intent(context, AlarmCountdownActivity.class);
		else
			mAlarmIntent = null;
	}

	/**
	 * 设置接收摔倒通知的{@link OnFallListener}
	 * @param listener 接收摔倒通知的{@link OnFallListener}；可以设为null，来取消之前的设置
	 * @return 用于链式调用的本对象的引用：this
	 */
	public AccelerometerListener setOnFallListener(OnFallListener listener) {
		this.mOnFallListener = listener;
		return this;
	}

	/**
	 * @return 最新的传感器值变动事件{@link SensorEvent}
	 */
	public SensorEvent getSensorEvent() {
		return mSensorEvent;
	}

	/**
	 * @return 最新的传感器原始数据{@link SensorEvent#values}
	 */
	public float[] getRaw() {
		return mSensorEvent != null ? mSensorEvent.values : null;
	}

	/**
	 * @return 最新的计算得到的重力数据
	 */
	public float[] getGravity() {
		return gravity;
	}

	/**
	 * @return 最新的计算得到的线性加速度数据
	 */
	public float[] getLinearAcceleration() {
		return linear_acceleration;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		long start;
		if(BuildConfig.DEBUG) start = System.currentTimeMillis();
		if(event.sensor.getType() != Sensor.TYPE_ACCELEROMETER
				|| event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
			return;
		mSensorEvent = event;

		// In this example, alpha is calculated as t / (t + dT),
		// where t is the low-pass filter's time-constant and
		// dT is the event delivery rate.
		final float alpha = 0.3f;
		if(gravity != null) {
			linear_acceleration = new float[3];

			// Isolate the force of gravity with the low-pass filter.
			gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
			gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
			gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

			// Remove the gravity contribution with the high-pass filter.
			linear_acceleration[0] = event.values[0] - gravity[0];
			linear_acceleration[1] = event.values[1] - gravity[1];
			linear_acceleration[2] = event.values[2] - gravity[2];

			if(gravity[0]*gravity[0]+gravity[1]*gravity[1]+gravity[2]*gravity[2] <= 5) {
				if(mContext != null)
					mContext.startActivity(mAlarmIntent);
				if(mOnFallListener != null)
					mOnFallListener.onFall(this, event.values, gravity, linear_acceleration);
			}

//			String infor = "g:\t"+gravity[0]+" "+gravity[1]+" "+gravity[2]+"\n"
//					+linear_acceleration[0]+" "+linear_acceleration[1]+" "+linear_acceleration[2]+"\n"
//					+event.values[0]+" "+event.values[1]+" "+event.values[2]+"\n";
//			Log.d(TAG, infor);
		} else {
			gravity = new float[3];
			gravity[0] = event.values[0];
			gravity[1] = event.values[1];
			gravity[2] = event.values[2];
			linear_acceleration = new float[3];
			linear_acceleration[0] = linear_acceleration[1] = linear_acceleration[2] = 0;
		}
		if(BuildConfig.DEBUG) {
			Log.i(TAG, "use " + (System.currentTimeMillis()-start)
					+" milliseconds in onSensorChanged");
		}
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	/**
	 * 用于当检测到摔倒时，接收来自{@link AccelerometerListener}的通知
	 * @author Team Orange
	 */
	public static interface OnFallListener {
		/**
		 * 当检测到摔倒时，被调用
		 * @param eventSource 本事件来源{@link AccelerometerListener}
		 * @param raw 传感器原始数据{@link SensorEvent#values}
		 * @param gravity 来源{@link AccelerometerListener}计算得到的重力数据
		 * @param linearAcceleration 来源{@link AccelerometerListener}计算得到的线性加速度数据
		 */
		public void onFall(AccelerometerListener eventSource, float[] raw, float[] gravity, float[] linearAcceleration);
	}
}
