/**
 *
 */
package org.orange.familylink;

import java.util.List;

import org.orange.familylink.alarm.AccelerometerListener;
import org.orange.familylink.alarm.AccelerometerListener.OnFallListener;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * 显示应用的当前状态
 * @author Team Orange
 */
public class StatusActivity extends Activity {
	private TextView mMainTextView;
	private GraphView mStatusGraphView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupActionBar();
		setupContentView();

		showSensorInformation();
	}
	/**
	 * 初始化配置{@link ActionBar}
	 */
	protected void setupActionBar() {
		ActionBar actionBar = getActionBar();
		// Show the Up button in the action bar.
		actionBar.setDisplayHomeAsUpEnabled(true);
	}
	/**
	 * 初始化配置主界面
	 * @see #setContentView(View)
	 */
	protected void setupContentView() {
		LinearLayout mainContainer = new LinearLayout(this);
		mainContainer.setOrientation(LinearLayout.VERTICAL);
		setContentView(mainContainer);

		mStatusGraphView = new GraphView(this);
		mainContainer.addView(mStatusGraphView, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				0, 6));

		ScrollView scroll = new ScrollView(this);
		mMainTextView = new TextView(this);
		scroll.addView(mMainTextView);
		mainContainer.addView(scroll, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				0, 4));
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(mAccelerometerListener.register(this, SensorManager.SENSOR_DELAY_NORMAL))
			mMainTextView.append("register ok\n");
		else
			mMainTextView.append("register failure\n");
//		mAccelerometerListener.register(this, 1000000);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mAccelerometerListener.unregister(this);
	}

	/**
	 * 显示使用的传感器的信息
	 */
	protected void showSensorInformation() {
		SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		StringBuilder sb = new StringBuilder();
		sb.append("Accelerometers on Device:\n");
		List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		for(Sensor sensor : deviceSensors)
			sb.append(sensor.getName()).append(" ")
				.append(sensor.getType()).append(" ")
				.append(sensor.getVendor()).append(" ")
				.append(sensor.getVersion()).append(" ")
				.append(sensor.getPower()).append(" ")
				.append("\n\n");
		sb.append("Default Accelerometer:\n");
		Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		if(accelerometer != null)
			sb.append(accelerometer.getName()).append(" ")
				.append(accelerometer.getType()).append(" ")
				.append(accelerometer.getVendor()).append(" ")
				.append(accelerometer.getVersion()).append(" ")
				.append(accelerometer.getPower()).append(" ")
				.append("\n\n");
		else
			sb.append("None(null)").append("\n\n");;
		mMainTextView.setText(sb.toString());
	}

	/**
	 * 处理加速度传感器的事件，检测用户是否摔倒
	 */
	protected AccelerometerListener mAccelerometerListener = new AccelerometerListener(null) {
		@Override
		public void onSensorChanged(SensorEvent event) {
			super.onSensorChanged(event);
			mStatusGraphView.update(GraphView.DataType.GRAVITY, getGravity());
			mStatusGraphView.update(GraphView.DataType.LINEAR_ACCELERATION, getLinearAcceleration());
		}
	}.setOnFallListener(new OnFallListener() {
		@Override
		public void onFall(AccelerometerListener eventSource, float[] raw,
				float[] gravity, float[] linearAcceleration) {
			SensorEvent event = eventSource.getSensorEvent();
			String info = "[" + event.timestamp+" "+event.accuracy+"] "+"\n"
					+"raw :"+event.values[0]+" "+event.values[1]+" "+event.values[2]+"\n"
					+"gavi:"+gravity[0]+" "+gravity[1]+" "+gravity[2]+"\n"
					+"liac:"+linearAcceleration[0]+" "+linearAcceleration[1]+" "+linearAcceleration[2]+"\n\n";
			mMainTextView.append(info);
		}
	});

	/* copy from Android sample src/com.example.android.apis/os/Sensors.java */
	private static class GraphView extends View {
		public enum DataType{
			GRAVITY,
			LINEAR_ACCELERATION
		}

		private Bitmap  mBitmap;
		private Paint   mPaint = new Paint();
		private Canvas  mCanvas = new Canvas();
		private Path	mPath = new Path();
		private RectF   mRect = new RectF();
		private float   mLastValues[] = new float[3*2];
		private float   mOrientationValues[] = new float[3];
		private int		mColors[] = new int[3*2];
		private float   mLastX;
		private float   mScale[] = new float[2];
		private float   mYOffset;
		private float   mMaxX;
		private float   mSpeed = 1.0f;
		private float   mWidth;
		private float   mHeight;

		public GraphView(Context context) {
			super(context);
//			mColors[0] = Color.argb(192, 255, 64, 64);
//			mColors[1] = Color.argb(192, 64, 128, 64);
//			mColors[2] = Color.argb(192, 64, 64, 255);
//			mColors[3] = Color.argb(192, 64, 255, 255);
//			mColors[4] = Color.argb(192, 128, 64, 128);
//			mColors[5] = Color.argb(192, 255, 255, 64);
			mColors[0] = Color.rgb(64, 64, 255);
			mColors[1] = mColors[0];
			mColors[2] = mColors[0];
			mColors[3] = Color.rgb(128, 64, 128);
			mColors[4] = mColors[3];
			mColors[5] = mColors[3];

			mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
			mRect.set(-0.5f, -0.5f, 0.5f, 0.5f);
			mPath.arcTo(mRect, 0, 180);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
			mCanvas.setBitmap(mBitmap);
			mCanvas.drawColor(0xFFFFFFFF);
			mYOffset = h * 0.5f;
			mScale[0] = - (h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
			mScale[1] = - (h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
			mWidth = w;
			mHeight = h;
			if (mWidth < mHeight) {
				mMaxX = w;
			} else {
				mMaxX = w-50;
			}
			mLastX = mMaxX;
			super.onSizeChanged(w, h, oldw, oldh);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			synchronized (this) {
				if (mBitmap != null) {
					final Paint paint = mPaint;
					final Path path = mPath;
					final int outer = 0xFFC0C0C0;
					final int inner = 0xFFff7010;

					if (mLastX >= mMaxX) {
						mLastX = 0;
						final Canvas cavas = mCanvas;
						final float yoffset = mYOffset;
						final float maxx = mMaxX;
						final float oneG = SensorManager.STANDARD_GRAVITY * mScale[0];
						paint.setColor(0xFFAAAAAA);
						cavas.drawColor(0xFFFFFFFF);
						cavas.drawLine(0, yoffset,	  maxx, yoffset,	  paint);
						cavas.drawLine(0, yoffset+oneG, maxx, yoffset+oneG, paint);
						cavas.drawLine(0, yoffset-oneG, maxx, yoffset-oneG, paint);
					}
					canvas.drawBitmap(mBitmap, 0, 0, null);

					float[] values = mOrientationValues;
					if (mWidth < mHeight) {
						float w0 = mWidth * 0.333333f;
						float w  = w0 - 32;
						float x = w0*0.5f;
						for (int i=0 ; i<3 ; i++) {
							canvas.save(Canvas.MATRIX_SAVE_FLAG);
							canvas.translate(x, w*0.5f + 4.0f);
							canvas.save(Canvas.MATRIX_SAVE_FLAG);
							paint.setColor(outer);
							canvas.scale(w, w);
							canvas.drawOval(mRect, paint);
							canvas.restore();
							canvas.scale(w-5, w-5);
							paint.setColor(inner);
							canvas.rotate(-values[i]);
							canvas.drawPath(path, paint);
							canvas.restore();
							x += w0;
						}
					} else {
						float h0 = mHeight * 0.333333f;
						float h  = h0 - 32;
						float y = h0*0.5f;
						for (int i=0 ; i<3 ; i++) {
							canvas.save(Canvas.MATRIX_SAVE_FLAG);
							canvas.translate(mWidth - (h*0.5f + 4.0f), y);
							canvas.save(Canvas.MATRIX_SAVE_FLAG);
							paint.setColor(outer);
							canvas.scale(h, h);
							canvas.drawOval(mRect, paint);
							canvas.restore();
							canvas.scale(h-5, h-5);
							paint.setColor(inner);
							canvas.rotate(-values[i]);
							canvas.drawPath(path, paint);
							canvas.restore();
							y += h0;
						}
					}

				}
			}
		}

		public void update(DataType type, float[] values) {
			synchronized (this) {
				if(mBitmap == null)
					return;

				final Canvas canvas = mCanvas;
				final Paint paint = mPaint;
//				if (type == Sensor.TYPE_ORIENTATION) {
//					for (int i=0 ; i<3 ; i++) {
//						mOrientationValues[i] = values[i];
//					}
//				} else {
					float deltaX = mSpeed;
					float newX = mLastX + deltaX;

					int j = (type == DataType.GRAVITY) ? 1 : 0;
					for (int i=0 ; i<3 ; i++) {
						int k = i+j*3;
						final float v = mYOffset + values[i] * mScale[j];
						paint.setColor(mColors[k]);
						canvas.drawLine(mLastX, mLastValues[k], newX, v, paint);
						mLastValues[k] = v;
					}
					if (type == DataType.LINEAR_ACCELERATION)
						mLastX += mSpeed;
//				}
				invalidate();
			}
		}
	}

}
