package org.orange.familylink;

import java.io.IOException;

import org.orange.familylink.ContactDetailActivity.Contact;
import org.orange.familylink.data.Message.Code;
import org.orange.familylink.data.UrgentMessageBody;
import org.orange.familylink.fragment.dialog.NoContactInformationDialogFragment;
import org.orange.familylink.location.LocationTracker;
import org.orange.familylink.sms.SmsMessage;
import org.orange.familylink.util.AudioFocusHelper;

import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.media.MediaPlayer;
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

	private LocationTracker mLocationTracker;
	private AudioFocusHelper mAudioFocusHelper;
	private MediaPlayer mMediaPlayer;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm_countdown);
		getActionBar().setTitle(getString(R.string.fall_down_alarm)
				+ " (" + getString(R.string.counting_down) + ")");

		mTextView = (TextView) super.findViewById(R.id.holoTimeText);
		//Animation实例化
		progress = (HoloCircularProgressBar) findViewById(R.id.holoCircularProgressBar1);
		animate(progress, null);

		mAudioFocusHelper = new AudioFocusHelper(this) {
			@Override
			public void onAudioFocusChange(int focusChange) {
			}
		};
		mMediaPlayer = MediaPlayer.create(this, R.raw.alarm);
		mMediaPlayer.setLooping(true);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mLocationTracker = new LocationTracker(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		mLocationTracker.stopUsingGPS();
		mLocationTracker = null;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mMediaPlayer != null) {
			stopAlarm();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
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
					int countDownTime = NUM_SHOW_TIME / 1000 - currentPlayTimeSecond;
					mTextView.setText(String.valueOf(countDownTime));
					if(countDownTime == NUM_SHOW_TIME / 2000) {
						startAlarm();
					} else if(countDownTime == 0) {
						getActionBar().setTitle(R.string.fall_down_alarm);
						sendAlarmMessage();
					}
				}
			}
		});
		progressBar.setMarkerProgress(markerProgress);
		progressBarAnimator.start();
	}

	/**
	 * 开始播放警报声
	 * @return 成功时，返回true；失败时，返回false
	 */
	private boolean startAlarm() {
		if(mAudioFocusHelper.requestFocus()) {
			try{
				mMediaPlayer.prepare();
			} catch(IllegalStateException e) {
				// may be Prepared, do nothing
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			mMediaPlayer.start();
			return true;
		} else
			return false;
	}
	/**
	 * 停止播放警报声
	 */
	private void stopAlarm() {
		try{
			mMediaPlayer.stop();
		} catch(IllegalStateException e) {
			// do nothing
		}
		mAudioFocusHelper.abandonFocus();
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
		final SmsMessage message = new SmsMessage();
		message.setCode(Code.INFORM | Code.Extra.Inform.URGENT);
		UrgentMessageBody messageBody = new UrgentMessageBody();
		messageBody.setType(UrgentMessageBody.Type.FALL_DOWN_ALARM);
		if(mLocationTracker != null && mLocationTracker.canGetLocation())
			messageBody.setPosition(mLocationTracker.getLatitude(), mLocationTracker.getLongitude());
		message.setBody(messageBody.toJson());
		// 发送消息
		final Contact contact = ContactDetailActivity.getDefaultContact(this);
		if(contact.phone != null && !contact.phone.isEmpty())
			new Thread() {
				@Override
				public void run() {
					message.sendAndSave(AlarmCountdownActivity.this, contact.id, contact.phone);
				}
			}.start();
		else
			new NoContactInformationDialogFragment().show(getFragmentManager(), "no_contact_info");
	}
}
