/**
 *
 */
package org.orange.familylink.fragment.dialog;

import org.orange.familylink.R;
import org.orange.familylink.data.Settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;

/**
 * 角色设置{@link DialogFragment}
 * @author Team Orange
 */
public class RoleDialogFragment extends DialogFragment {
	private String[] values;
	private OnRoleChangeListener mListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		values = getResources().getStringArray(R.array.pref_role_values);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		String currentValue = PreferenceManager.getDefaultSharedPreferences(getActivity())
				.getString(Settings.PREF_KEY_ROLE, null);
		final int currentPosition = getItemPosition(currentValue);
		builder
			.setTitle(R.string.pref_title_role)
			.setSingleChoiceItems(R.array.pref_role_titles, currentPosition,
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which != currentPosition) {
					String value = values[which];
					Settings.setRole(getActivity(), value);
					if(mListener != null)
						mListener.onRoleChange(RoleDialogFragment.this);
				}
				dismiss();
			}
		});
		return builder.create();
	}

	/**
	 * 取得{@link Settings#PREF_KEY_ROLE}值在其选择列表中的位置
	 * @param value 待查{@link Settings#PREF_KEY_ROLE}值
	 * @return 如果查到了指定值，返回其位置；如果查不到，返回-1
	 */
	private int getItemPosition(String value) {
		for(int i = 0 ; i < values.length ; i++)
			if(values[i].equals(value))
				return i;
		return -1;
	}

	/**
	 * 设置{@link OnRoleChangeListener}
	 * @param mListener 新的{@link OnRoleChangeListener}；可以设置为null，来取消之前的设置
	 */
	public void setOnRoleChangeListener(OnRoleChangeListener mListener) {
		this.mListener = mListener;
	}

	/**
	 * 用来接收 改变用户角色事件 的回调接口
	 * @author Team Orange
	 */
	public static interface OnRoleChangeListener {
		/**
		 * 当用户角色改变时调用此方法
		 * @param dialog 发生此事件的{@link DialogFragment}
		 */
		public void onRoleChange(RoleDialogFragment dialog);
	}
}
