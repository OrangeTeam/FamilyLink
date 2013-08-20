package org.orange.familylink.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 这个广播接收器用于开机启动LocationService
 * @author Orange Team
 *
 */
public class BootBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		if(arg1.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
			Intent intent = new Intent(arg0, LocationService.class);
			arg0.startService(intent);
		}
	}

}
