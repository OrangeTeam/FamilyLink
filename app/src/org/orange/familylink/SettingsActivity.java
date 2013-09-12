package org.orange.familylink;

import org.orange.familylink.data.Settings;
import org.orange.familylink.location.LocationService;

import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setupSimplePreferencesScreen();
	}

	/**
	 * Shows the simplified settings UI
	 */
	private void setupSimplePreferencesScreen() {
		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.

		// Add 'general' preferences.
		addPreferencesFromResource(R.xml.preferences);

		// Bind the summaries of EditText/List/Dialog/Ringtone preferences to
		// their values. When their values change, their summaries are updated
		// to reflect the new value, per the Android Design guidelines.
		bindPreferenceSummaryToValue(findPreference(Settings.PREF_KEY_ROLE));
		bindPreferenceSummaryToValue(findPreference(Settings.PREF_KEY_SYNC_FREQUENCY));
		bindPreferenceSummaryToValue(findPreference(Settings.PREF_KEY_START_LOCATION_SERVICE));
		bindPreferenceSummaryToValue(findPreference(Settings.PREF_KEY_LOCATE_FREQUENCY));
		bindPreferenceSummaryToValue(findPreference(Settings.PREF_KEY_NOTIFICATIONS_NEW_MESSAGE_RINGTONE));
	}

	private void onRoleChanged(Preference preference, String value) {
		Preference sync = findPreference(Settings.PREF_KEY_SYNC_FREQUENCY);
		if("1".equals(value))
			sync.setEnabled(false);
		else if("0".equals(value))
			sync.setEnabled(true);
		else
			throw new IllegalArgumentException("Illegal role value");
	}

	/**
	 * 调用此方法用于开启后台定位服务
	 * @param context
	 */
	private void toStartLocationService(Context context){
		Intent intent = new Intent(context, LocationService.class);
		context.startService(intent);
	}

	/**
	 * 调用此方法停止后台定位服务
	 * @param context
	 */
	private void toEndLocationService(Context context){
		Intent intent = new Intent(context, LocationService.class);
		context.stopService(intent);
	}

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();

			if (preference instanceof ListPreference) {
				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);

				// Set the summary to reflect the new value.
				preference
						.setSummary(index >= 0 ? listPreference.getEntries()[index]
								: null);

				if(Settings.PREF_KEY_ROLE.equals(listPreference.getKey()))
					onRoleChanged(listPreference, stringValue);

				//如果用户更改了定位的频率，就会重新启动定位服务
				if(Settings.PREF_KEY_LOCATE_FREQUENCY.equals(listPreference.getKey())){
					//这里是一旦用户更改了定位的时间间隔，就要从新更改服务中的定位的时间计划任务，也就是也先停用服务在开启
					//这样避免了计划任务再次被调用而出现异常，这里不要判断服务是否被开启就可以停用这个服务，是因为
					//stopService方法，这个方法如果服务没被启动，那么停止它将不会有任何影响
					toEndLocationService(preference.getContext());
					toStartLocationService(preference.getContext());
				}

			} else if(preference instanceof CheckBoxPreference){

				CheckBoxPreference checkBoxPreference = (CheckBoxPreference)preference;

				//判断是否开启了后台定位服务，如果选择了开启就启动后台定位服务
				if(Settings.PREF_KEY_START_LOCATION_SERVICE.equals(checkBoxPreference.getKey())){
					if("true".equals(stringValue))
						toStartLocationService(preference.getContext());
					if("false".equals(stringValue))
						toEndLocationService(preference.getContext());
				}
			}else if (preference instanceof RingtonePreference) {
				// For ringtone preferences, look up the correct display value
				// using RingtoneManager.
				if (TextUtils.isEmpty(stringValue)) {
					// Empty values correspond to 'silent' (no ringtone).
					preference.setSummary(R.string.pref_ringtone_silent);

				} else {
					Ringtone ringtone = RingtoneManager.getRingtone(
							preference.getContext(), Uri.parse(stringValue));

					if (ringtone == null) {
						// Clear the summary if there was a lookup error.
						preference.setSummary(null);
					} else {
						// Set the summary to reflect the new ringtone display
						// name.
						String name = ringtone
								.getTitle(preference.getContext());
						preference.setSummary(name);
					}
				}

			} else {
				// For all other preferences, set the summary to the value's
				// simple string representation.
				preference.setSummary(stringValue);
			}
			return true;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 *
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference
				.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		// Trigger the listener immediately with the preference's
		// current value.
		if(preference instanceof CheckBoxPreference){
			sBindPreferenceSummaryToValueListener.onPreferenceChange(
					preference,
					PreferenceManager.getDefaultSharedPreferences(
							preference.getContext()).getBoolean(preference.getKey(),
							false));
		}else{
			sBindPreferenceSummaryToValueListener.onPreferenceChange(
					preference,
					PreferenceManager.getDefaultSharedPreferences(
							preference.getContext()).getString(preference.getKey(),
							""));
		}
	}
}
