package org.orange.familylink;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;
import de.passsy.holocircularprogressbar.HoloCircularProgressBar;

public class AlarmCountdownActivity extends Activity {

	private long mExitTime;
	private Bundle bundle = new Bundle() ;
	private Handler myHandler;
	private ObjectAnimator objectAnimator;         //获取ObjectAnimator对象的一个引用
	
	private static final int COUNT_DOWM_TIME = 2000;    //Toast提示语显示的时间
	private static final int NUM_SHOW_TIME = 60000;     //倒计时的时间
	private static final float[] POSTION_TIME = {(float)0 ,(float) 1} ;  //动画的起止点，从0开始，绕一圈结束
	private static final String BUNDLE_KEY = "timeShow" ;  //bundle传消息用的key
	private HoloCircularProgressBar progress ;
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm_countdown);
		//开始启动Activity时，提示检测到摔倒，是否取消警报
		Toast.makeText(this, "检测到摔倒，两次触摸屏幕以取消警报倒计时",  COUNT_DOWM_TIME+ 2000).show();
		
		final TextView textView = (TextView) super.findViewById(R.id.holoTimeText);
		//Animation实例化
	    progress = (HoloCircularProgressBar) findViewById(R.id.holoCircularProgressBar1);
		animate(progress, new AnimatorListener() {
			
			@Override
			public void onAnimationCancel(final Animator animation) {
				
			}
			
			@Override
			public void onAnimationEnd(final Animator animation) {
				//在此启动摔倒警报
			}
			
			@Override
			public void onAnimationRepeat(final Animator animation) {
				
			}
			
			@Override
			public void onAnimationStart(final Animator animation) {
				
			}
		});
		
		/**
		 * 
		 * 通过handle更新倒计时数字
		 */
		
		myHandler = new Handler(){
			@Override
			public void handleMessage(Message msg){
				//如果是由animation发出的
				if(msg.what == 0x1234)
				{
					//通过bundle获得目前的倒计时间
					int timeShow = bundle.getInt(BUNDLE_KEY);
					textView.setText(timeShow+"");
				}
			}
		};
	}

	/**
	 * Animate.
	 * 
	 * @param progressBar
	 *            the progress bar
	 * @param listener
	 *            the listener
	 */
	private void animate(final HoloCircularProgressBar progressBar, final AnimatorListener listener) {
		
		//设置animation的起，止点位置
		final float[] progresses = POSTION_TIME ;
		final float progress = (1);
		final ObjectAnimator progressBarAnimator = ObjectAnimator.ofFloat(progressBar, "progress", progresses);
		
		
		setObjectAnimation(progressBarAnimator);          //set ObjectAnimation对象
		progressBarAnimator.setDuration(NUM_SHOW_TIME);   //设置Animation的时间
		progressBarAnimator.addListener(new AnimatorListener() { //添加Listener

			@Override
			public void onAnimationCancel(final Animator animation) {
				
			}

			@Override
			public void onAnimationEnd(final Animator animation) {
				//在此启动摔倒警报
			}

			@Override
			public void onAnimationRepeat(final Animator animation) {
			}

			@Override
			public void onAnimationStart(final Animator animation) {
			}
		});
		progressBarAnimator.addListener(listener);
		
		//当animation更新时回调
		progressBarAnimator.addUpdateListener(new AnimatorUpdateListener() {
			
			/**
			 * 每过一秒，发送一个Message
			 * 
			 */
			private int timeShow = 0;
			@Override
			public void onAnimationUpdate(final ValueAnimator animation) {
				if((int)animation.getCurrentPlayTime() / 1000 != timeShow)
				{
					timeShow = (int)animation.getCurrentPlayTime() / 1000 ;
					Message msg = new Message();
					msg.what = 0x1234 ;
					bundle.putInt(BUNDLE_KEY, timeShow) ;
					myHandler.sendMessage(msg);
				}
				progressBar.setProgress((Float) animation.getAnimatedValue());
			}
		});
		progressBar.setMarkerProgress(progress);
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
	 * 
	 * 覆写onTouchEvent， 要在两秒内点击两次屏幕才退出警报
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_UP)
		{
			 //如果两次点击屏幕的时间大于两秒
			 if ((System.currentTimeMillis() - mExitTime) > 2000) {
                 Toast.makeText(this, "请再次触摸屏幕，退出警报", COUNT_DOWM_TIME).show();
                 mExitTime = System.currentTimeMillis();
             }else{
            	 //获得ObjectAnimation的引用，取消Animation
            	 getObjectAnimation().cancel();
            	 //关闭Activity
            	 this.finish();
             }
			 return true;
		}
		return super.onTouchEvent(event);
	}

}
