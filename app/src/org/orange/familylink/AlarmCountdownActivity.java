package org.orange.familylink;

import org.orange.familylink.ContactDetailActivity.Contact;
import org.orange.familylink.data.Message.Code;
import org.orange.familylink.data.UrgentMessageBody;
import org.orange.familylink.sms.SmsMessage;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;
import de.passsy.holocircularprogressbar.HoloCircularProgressBar;

public class AlarmCountdownActivity extends Activity {
	private long mExitTime;
	private ObjectAnimator objectAnimator;		 //获取ObjectAnimator对象的一个引用

	private static final int NUM_SHOW_TIME = 20000;	 //倒计时的时间
	private static final float[] POSTION_TIME = {1f, 0f};  //动画的起止点，从0开始，绕一圈结束
	private HoloCircularProgressBar progress;
	private TextView mTextView;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm_countdown);
		getActionBar().setTitle(getString(R.string.fall_down_alarm)
				+ " (" + getString(R.string.counting_down) + ")");

		mTextView = (TextView) super.findViewById(R.id.holoTimeText);
		//Animation实例化
		progress = (HoloCircularProgressBar) findViewById(R.id.holoCircularProgressBar1);
		animate(progress, new AnimatorListener() {
			@Override
			public void onAnimationCancel(final Animator animation) {
			}
			@Override
			public void onAnimationEnd(final Animator animation) {
				getActionBar().setTitle(R.string.fall_down_alarm);
				//TODO 在此启动摔倒警报
				new Thread() {
					@Override
					public void run() {
						sendAlarmMessage();
					}
				}.start();
			}
			@Override
			public void onAnimationRepeat(final Animator animation) {
			}
			@Override
			public void onAnimationStart(final Animator animation) {
			}
		});
	}

	/**
	 * Animate.
	 * @param progressBar the progress bar
	 * @param listener the listener
	 */
	private void animate(final HoloCircularProgressBar progressBar, final AnimatorListener listener) {
		//设置animation的起，止点位置
		final float[] progresses = POSTION_TIME ;
		final float markerProgress = 1f;
		final ObjectAnimator progressBarAnimator = ObjectAnimator.ofFloat(progressBar, "progress", progresses);

		setObjectAnimation(progressBarAnimator);		  //set ObjectAnimation对象
		progressBarAnimator.setDuration(NUM_SHOW_TIME);   //设置Animation的时间
		if(listener != null)
			progressBarAnimator.addListener(listener);

		//当animation更新时回调
		progressBarAnimator.addUpdateListener(new AnimatorUpdateListener() {
			/**
			 * 上次更新倒计时器的时间。单位：秒
			 */
			private int mLastPlayTimeSecond = -1;
			@Override
			public void onAnimationUpdate(final ValueAnimator animation) {
				progressBar.setProgress((Float) animation.getAnimatedValue());
				// 每过一秒，更新一次倒计时器
				int currentPlayTimeSecond = (int) (animation.getCurrentPlayTime() / 1000);
				if(currentPlayTimeSecond != mLastPlayTimeSecond) {
					mLastPlayTimeSecond = currentPlayTimeSecond;
					mTextView.setText(
							String.valueOf(NUM_SHOW_TIME / 1000 - currentPlayTimeSecond));
				}
			}
		});
		progressBar.setMarkerProgress(markerProgress);
		progressBarAnimator.start();
	}

	/**
	 * 设置和取得ObjectAnimator的引用
	 */
	private ObjectAnimator getObjectAnimation(){
		return this.objectAnimator;
	}
	private void setObjectAnimation(ObjectAnimator obj){
		this.objectAnimator = obj;
	}

	/**
	 * 覆写onTouchEvent， 要在两秒内点击两次屏幕才退出警报
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_UP)
		{
			//如果两次点击屏幕的时间大于两秒
			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				Toast.makeText(this, R.string.touch_again_to_cancel, Toast.LENGTH_SHORT).show();
				mExitTime = System.currentTimeMillis();
			} else {
				//获得ObjectAnimation的引用，取消Animation
				getObjectAnimation().cancel();
				//关闭Activity
				this.finish();
			}
			return true;
		}
		return super.onTouchEvent(event);
	}

	private void sendAlarmMessage() {
		// 构造消息
		SmsMessage message = new SmsMessage();
		message.setCode(Code.INFORM | Code.Extra.Inform.URGENT);
		UrgentMessageBody messageBody = new UrgentMessageBody();
		messageBody.setType(UrgentMessageBody.Type.FALL_DOWN_ALARM);
		//TODO 在下一行取得当前位置坐标，允许监护人导航过来
//		messageBody.setContent(当前位置经纬度);
		message.setBody(messageBody.toJson());
		// 发送消息
		Contact contact = ContactDetailActivity.getDefaultContact(this);
		message.sendAndSave(this, contact.id, contact.phone);
	}
}
