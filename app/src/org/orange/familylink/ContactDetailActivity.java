package org.orange.familylink;

import org.orange.familylink.database.Contract;
import org.orange.familylink.database.Contract.Contacts;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
/**设置联默认系人
 * 
 * @author Dell
 *
 */
public class ContactDetailActivity extends BaseActivity {
	private EditText mEditTextPhone = null;
	private EditText mEditTextName = null;
	private Button mButtonEdit = null;
	private Button mButtonSave = null;
	private String nameString = "";
	private String phoneString = "";
	private Cursor cursor;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_detial);
		mEditTextPhone = (EditText) findViewById(R.id.phone_input);
		mEditTextName = (EditText) findViewById(R.id.name_input);
		mButtonEdit = (Button) findViewById(R.id.button_edit);
		mButtonSave = (Button) findViewById(R.id.button_save);
		mEditTextName.setFocusable(false);
		mEditTextPhone.setFocusable(false);
		String[] projection = { Contract.Contacts.COLUMN_NAME_NAME,
				Contract.Contacts.COLUMN_NAME_PHONE_NUMBER };
		cursor = getContentResolver().query(Contract.Contacts.CONTACTS_URI,
				projection, null, null, null);
		if (cursor.moveToLast()) {
			phoneString = cursor
					.getString(cursor
							.getColumnIndex(Contract.Contacts.COLUMN_NAME_PHONE_NUMBER));
			nameString = cursor.getString(cursor
					.getColumnIndex(Contract.Contacts.COLUMN_NAME_NAME));
		}
		mEditTextName.setText(nameString);
		mEditTextPhone.setText(phoneString);
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

				if (cursor.getCount() != 0) {
					getContentResolver().delete(Contract.Contacts.CONTACTS_URI,
							null, null);
				}
				ContentValues contact = new ContentValues(2);
				contact.put(Contacts.COLUMN_NAME_NAME, getName());
				contact.put(Contacts.COLUMN_NAME_PHONE_NUMBER, getNumber());
				getContentResolver().insert(Contract.Contacts.CONTACTS_URI,
						contact);
				mEditTextName.setFocusable(false);
				mEditTextPhone.setFocusable(false);
			}
		});
	}

	public String getNumber() {
		return mEditTextPhone.getText().toString();
	}

	public String getName() {
		return mEditTextName.getText().toString();
	}
}
