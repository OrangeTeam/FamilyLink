/**
 *
 */
package org.orange.familylink;

import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * 显示应用的当前状态
 * @author Team Orange
 */
public class StatusActivity extends ActionBarActivity {
	private static final String TAG = StatusActivity.class.getSimpleName();
	private TextView mMainTextView;

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupActionBar();
		setupContentView();

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		if(mAccelerometer == null)
			finish();
	}
	/**
	 * 初始化配置{@link ActionBar}
	 */
	protected void setupActionBar() {
		ActionBar actionBar = getSupportActionBar();
		// Show the Up button in the action bar.
		actionBar.setDisplayHomeAsUpEnabled(true);
	}
	/**
	 * 初始化配置主界面
	 * @see #setContentView(android.view.View)
	 */
	protected void setupContentView() {
		ScrollView scroll = new ScrollView(this);
		mMainTextView = new TextView(this);
		scroll.addView(mMainTextView);
		setContentView(scroll);
	}

	@Override
	protected void onStart() {
		super.onStart();

		StringBuilder sb = new StringBuilder();
		sb.append("Sensor List:\n");
		List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
		for(Sensor sensor : deviceSensors)
			sb.append(sensor.getName()).append(" ")
				.append(sensor.getType()).append(" ")
				.append(sensor.getVendor()).append(" ")
				.append(sensor.getVersion()).append(" ")
				.append(sensor.getPower()).append(" ")
				.append("\n\n");
		mMainTextView.setText(sb.toString());
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(mAccelerometerListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
//		mSensorManager.registerListener(mSensorEventListener, mAccelerometer, 1000000);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(mAccelerometerListener);
	}

	/**
	 * 处理加速度传感器的事件，检测用户是否摔倒
	 */
	protected SensorEventListener mAccelerometerListener = new SensorEventListener() {
		float[] gravity = null;

		@Override
		public void onSensorChanged(SensorEvent event) {
			long start;
			if(BuildConfig.DEBUG) start = System.currentTimeMillis();
			if(event.sensor.getType() != Sensor.TYPE_ACCELEROMETER
					|| event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
				return;
			// In this example, alpha is calculated as t / (t + dT),
			// where t is the low-pass filter's time-constant and
			// dT is the event delivery rate.
			final float alpha = 0.8f;
			if(gravity != null) {
				float[] linear_acceleration = new float[3];

				// Isolate the force of gravity with the low-pass filter.
				gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
				gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
				gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

				// Remove the gravity contribution with the high-pass filter.
				linear_acceleration[0] = event.values[0] - gravity[0];
				linear_acceleration[1] = event.values[1] - gravity[1];
				linear_acceleration[2] = event.values[2] - gravity[2];

				if(event.values[0]*event.values[0]+event.values[1]*event.values[1]+event.values[2]*event.values[2] <= 1) {
					String info = "[" + event.timestamp+" "+event.accuracy+"] "+"\n"
						+event.values[0]+" "+event.values[1]+" "+event.values[2]+"\n"
						+gravity[0]+" "+gravity[1]+" "+gravity[2]+"\n\n";
					mMainTextView.setText(mMainTextView.getText()+info);
				}
				String infor = "g:\t"+gravity[0]+" "+gravity[1]+" "+gravity[2]+"\n"
						+linear_acceleration[0]+" "+linear_acceleration[1]+" "+linear_acceleration[2]+"\n"
						+event.values[0]+" "+event.values[1]+" "+event.values[2]+"\n";
				Log.d(TAG, infor);
			} else {
				gravity = new float[3];
				gravity[0] = event.values[0];
				gravity[1] = event.values[1];
				gravity[2] = event.values[2];
			}
			if(BuildConfig.DEBUG) {
				Log.i(TAG, "use " + (System.currentTimeMillis()-start)
						+" milliseconds in onSensorChanged");
			}
		}
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	};

}
