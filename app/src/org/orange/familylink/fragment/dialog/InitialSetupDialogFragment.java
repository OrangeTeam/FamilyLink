/**
 *
 */
package org.orange.familylink.fragment.dialog;

import org.orange.familylink.R;
import org.orange.familylink.data.Settings;
import org.orange.familylink.data.Settings.Role;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * 初始化设置对话框。
 * @author Team Orange
 */
public class InitialSetupDialogFragment extends DialogFragment {
	private OnClickListener mListener;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		setCancelable(false);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// inflate dialog view
		View dialogView = getActivity().getLayoutInflater()
				.inflate(R.layout.dialog_fragment_initial_setup_dialog_view, null);
		final EditText passwordInput = (EditText) dialogView.findViewById(R.id.password);
		final Spinner roleInput = (Spinner) dialogView.findViewById(R.id.role);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				getActivity(), R.array.pref_role_titles, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		roleInput.setAdapter(adapter);
		// 默认选择“受顾者”，照顾用户心理，避免用户自己选择“受顾者”
		roleInput.setSelection(1);

		builder.setView(dialogView)
			.setTitle(R.string.initial_Setup)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					int position = roleInput.getSelectedItemPosition();
					Role role = null;
					switch(position){
					case 0:
						role = Role.CARER;
						break;
					case 1:
						role = Role.CAREE;
						break;
					default:
						throw new IllegalStateException("illegal position :"+position);
					}
					Settings.setRole(getActivity(), role);

					String password = passwordInput.getText().toString();
					PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
							.putString(Settings.PREF_KEY_PASSWORD, password).commit();
					if(mListener != null)
						mListener.onClickPositiveButton(InitialSetupDialogFragment.this, role, password);
				}
			});
		Dialog dialog = builder.create();
		// 自动打开软键盘
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		return dialog;
	}

	/**
	 * 设置{@link OnClickListener}
	 * @param listener 新的{@link OnClickListener}；可以设置为null，来取消之前的设置
	 */
	public void setOnClickListener(OnClickListener listener) {
		mListener = listener;
	}

	/**
	 * 用来接收 用户点击对话框事件 的回调接口
	 * @author Team Orange
	 */
	public static interface OnClickListener {
		/**
		 * 当用户点击确认时调用此方法
		 * @param dialog 发生确认点击事件的{@link DialogFragment}
		 * @param newRole 用户设置的角色
		 * @param newPassword 用户设置的密码
		 */
		public void onClickPositiveButton(InitialSetupDialogFragment dialog,
				Role newRole, String newPassword);
	}
}
