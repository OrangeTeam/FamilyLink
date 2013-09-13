package org.orange.familylink;

import org.orange.familylink.database.Contract;
import org.orange.familylink.database.Contract.Contacts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
/**设置联默认系人
 * @author Team Orange
 */
public class ContactDetailActivity extends BaseActivity {
	private static String[] projection = { Contract.Contacts.COLUMN_NAME_NAME,
			Contract.Contacts.COLUMN_NAME_PHONE_NUMBER};

	private EditText mEditTextPhone = null;
	private EditText mEditTextName = null;
	private Button mButtonEdit = null;
	private Button mButtonSave = null;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_detial);
		mEditTextPhone = (EditText) findViewById(R.id.phone_input);
		mEditTextName = (EditText) findViewById(R.id.name_input);
		mButtonEdit = (Button) findViewById(R.id.button_edit);
		mButtonSave = (Button) findViewById(R.id.button_save);
		mEditTextName.setFocusable(false);
		mEditTextPhone.setFocusable(false);

		Contact contact = getContact(this);
		mEditTextName.setText(contact.name);
		mEditTextPhone.setText(contact.phone);
		/**
		 * 解锁获取焦点
		 */
		mButtonEdit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mEditTextPhone.setFocusable(true);
				mEditTextPhone.setFocusableInTouchMode(true);
				mEditTextPhone.requestFocus();
				mEditTextName.setFocusable(true);
				mEditTextName.setFocusableInTouchMode(true);
				mEditTextName.requestFocus();
				mEditTextName.setSelection(mEditTextName.length());
			}
		});
		/**先删除上一个后保存下一个
		 * 保存之后继续取消焦点
		 */
		mButtonSave.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				getContentResolver().delete(Contract.Contacts.CONTACTS_URI,
							null, null);
				ContentValues contact = new ContentValues(2);
				contact.put(Contacts.COLUMN_NAME_NAME, getInputName());
				contact.put(Contacts.COLUMN_NAME_PHONE_NUMBER, getInputPhone());
				getContentResolver().insert(Contract.Contacts.CONTACTS_URI,
						contact);
				mEditTextName.setFocusable(false);
				mEditTextPhone.setFocusable(false);
			}
		});
	}

	private String getInputPhone() {
		return mEditTextPhone.getText().toString();
	}
	private String getInputName() {
		return mEditTextName.getText().toString();
	}

	public static Contact getContact(Context context) {
		Cursor cursor = context.getContentResolver().query(Contract.Contacts.CONTACTS_URI,
				projection, null, null, null);
		String phone = null;
		String name = null;
		if (cursor.moveToLast()) {
			phone = cursor.getString(
					cursor.getColumnIndex(Contract.Contacts.COLUMN_NAME_PHONE_NUMBER));
			name = cursor.getString(
					cursor.getColumnIndex(Contract.Contacts.COLUMN_NAME_NAME));
		}
		return new Contact(name, phone);
	}

	public static class Contact {
		public final String name;
		public final String phone;
		public Contact(String name, String phone) {
			this.name = name;
			this.phone = phone;
		}
	}
}
