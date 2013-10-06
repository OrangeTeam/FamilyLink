package org.orange.familylink.util;

import org.orange.familylink.data.Settings;
import org.orange.familylink.location.LocationService;
import org.orange.familylink.sms.SmsReceiverService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

/**
 * 这个广播接收器用于开机启动Service
 * @author Orange Team
 *
 */
public class StartServiceReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		if(arg1.getAction().equals(Intent.ACTION_BOOT_COMPLETED)
				|| arg1.getAction().equals(Intent.ACTION_REBOOT)){

			PreferenceManager.getDefaultSharedPreferences(arg0).edit()
			.putBoolean(Settings.PREF_KEY_START_LOCATION_SERVICE, true).commit();
			//启动locationService
			Intent locationIntent = new Intent(arg0, LocationService.class);
			arg0.startService(locationIntent);
			//启动SmsReceiverservice
			Intent smsIntent = new Intent(arg0, SmsReceiverService.class);
			smsIntent.setAction(SmsReceiverService.ACTION_FOREGROUND);
			arg0.startService(smsIntent);
		}
	}

}
