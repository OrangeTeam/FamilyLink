package org.orange.familylink.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.widget.Toast;

public class ProximityAlertReceiver extends BroadcastReceiver {

	/**
	 * 临近警告receiver
	 * @author orange team
	 */
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// 获取是否进入指定区域 
		boolean isEnter = intent.getBooleanExtra(
				LocationManager.KEY_PROXIMITY_ENTERING, false);
		if(isEnter){
				Toast.makeText(context, "进入指定区域", Toast.LENGTH_LONG).show();
		}else{
				Toast.makeText(context, "离开指定区域", Toast.LENGTH_LONG).show();
		}

	}

}
