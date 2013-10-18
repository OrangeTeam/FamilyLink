/**
 *
 */
package org.orange.familylink.fragment.dialog;

import org.orange.familylink.R;
import org.orange.familylink.data.Settings;
import org.orange.familylink.location.LocationService;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

/**
 * 定位频率设置{@link DialogFragment}
 * @author Team Orange
 */
public class LocateFrequencyDialogFragment extends DialogFragment {
	private String[] values;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		values = getResources().getStringArray(R.array.pref_location_frequency_values);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		String currentValue = PreferenceManager.getDefaultSharedPreferences(getActivity())
				.getString(Settings.PREF_KEY_LOCATE_FREQUENCY, null);
		int currentPosition = getItemPosition(currentValue);
		builder
			.setTitle(R.string.pref_title_location_frequency)
			.setSingleChoiceItems(R.array.pref_location_frequency_titles, currentPosition,
				new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String value = values[which];
					Settings.setLocateFrequency(getActivity(), value);
					Intent intent = new Intent(getActivity(), LocationService.class);
					getActivity().stopService(intent);
					getActivity().startService(intent);
					dismiss();
				}
			})
			.setPositiveButton(android.R.string.cancel, null);
		return builder.create();
	}

	/**
	 * 取得LocateFrequency值在其选择列表中的位置
	 * @param value 待查LocateFrequency值
	 * @return 如果查到了指定值，返回其位置；如果查不到，返回-1
	 */
	private int getItemPosition(String value) {
		for(int i = 0 ; i < values.length ; i++)
			if(values[i].equals(value))
				return i;
		return -1;
	}
}
