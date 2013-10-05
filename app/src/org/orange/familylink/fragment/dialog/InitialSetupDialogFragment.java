/**
 *
 */
package org.orange.familylink.fragment.dialog;

import org.orange.familylink.R;
import org.orange.familylink.data.Settings;
import org.orange.familylink.data.Settings.Role;
import org.orange.familylink.database.Contract;
import org.orange.familylink.database.Contract.Contacts;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * 初始化设置对话框。
 * @author Team Orange
 */
public class InitialSetupDialogFragment extends DialogFragment {
	private OnClickListener mListener;

	private Animation mAnimationShake;
	private EditText mEditTextContactName;
	private EditText mEditTextContactAddress;
	private Spinner mSpinnerRole;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAnimationShake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		setCancelable(false);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// inflate dialog view
		final View dialogView = getActivity().getLayoutInflater()
				.inflate(R.layout.dialog_fragment_initial_setup_dialog_view, null);
		mEditTextContactName = (EditText) dialogView.findViewById(R.id.contact_name_input);
		mEditTextContactAddress = (EditText) dialogView.findViewById(R.id.contact_address_input);
		mSpinnerRole = (Spinner) dialogView.findViewById(R.id.role);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				getActivity(), R.array.pref_role_titles, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinnerRole.setAdapter(adapter);
		mSpinnerRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			private final String[] mRoles = getResources().getStringArray(R.array.pref_role_titles);
			private final TextView mContactNameLable = (TextView) dialogView.findViewById(R.id.contact_name_title);
			private final TextView mContactAddressLable = (TextView) dialogView.findViewById(R.id.contact_address_title);
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String currentRole;
				if(position == 1)
					currentRole = mRoles[0];
				else if(position == 0)
					currentRole = mRoles[1];
				else
					throw new UnsupportedOperationException("Unsupported roles selected:"+position);
				mContactNameLable.setText(getString(R.string.initial_setup_dialog_contact_name_label, currentRole));
				mContactAddressLable.setText(getString(R.string.initial_setup_dialog_contact_phone_label, currentRole));
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
		});
		// 默认选择“受顾者”，照顾用户心理，避免用户自己选择“受顾者”
		mSpinnerRole.setSelection(1);

		builder.setView(dialogView)
			.setTitle(R.string.initial_Setup)
			.setPositiveButton(android.R.string.ok, null);
		Dialog dialog = builder.create();
		// 自动打开软键盘
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		return dialog;
	}

	@Override
	public void onStart() {
		super.onStart();
		//super.onStart() is where dialog.show() is actually called on the underlying dialog,
		//so we have to do it after this point
		AlertDialog dialog = (AlertDialog) getDialog();
		if(dialog != null) {
			Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
			positiveButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					boolean inputValid = true;
					// 保存联系人信息
					String name = mEditTextContactName.getText().toString();
					String address = mEditTextContactAddress.getText().toString();
					if(TextUtils.isEmpty(address)) {
						mEditTextContactAddress.requestFocus();
						mEditTextContactAddress.startAnimation(mAnimationShake);
						inputValid = false;
					}
					if(TextUtils.isEmpty(name)) {
						mEditTextContactName.requestFocus();
						mEditTextContactName.startAnimation(mAnimationShake);
						inputValid = false;
					}
					if(!inputValid)
						return;
					new ContactUpdater(getActivity().getContentResolver()).execute(name, address);
					// 保存角色信息
					int position = mSpinnerRole.getSelectedItemPosition();
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
					// 通知监视器
					if(mListener != null)
						mListener.onClickPositiveButton(InitialSetupDialogFragment.this);
					dismiss();
				}
			});
		}
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
		 */
		public void onClickPositiveButton(InitialSetupDialogFragment dialog);
	}

	private static class ContactUpdater extends AsyncTask<String, Void, Void> {
		private final ContentResolver mContentResolver;
		public ContactUpdater(ContentResolver contentResolver) {
			mContentResolver = contentResolver;
		}
		@Override
		protected Void doInBackground(String... params) {
			String contactName = params[0];
			String contactAddress = params[1];
			mContentResolver.delete(Contract.Contacts.CONTACTS_URI, null, null);
			ContentValues contact = new ContentValues(2);
			contact.put(Contacts.COLUMN_NAME_NAME, contactName);
			contact.put(Contacts.COLUMN_NAME_PHONE_NUMBER, contactAddress);
			mContentResolver.insert(Contract.Contacts.CONTACTS_URI, contact);
			return null;
		}
	}
}
