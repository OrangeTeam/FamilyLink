package org.orange.familylink;

import org.orange.familylink.database.Contract;
import org.orange.familylink.database.Contract.Contacts;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

/**设置联默认系人
 * @author Team Orange
 */
public class ContactDetailActivity extends BaseActivity {
	private static String[] projection = {
			Contract.Contacts._ID,
			Contract.Contacts.COLUMN_NAME_NAME,
			Contract.Contacts.COLUMN_NAME_PHONE_NUMBER};

	private Animation mAnimationShake;
	private EditText mEditTextPhone = null;
	private EditText mEditTextName = null;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAnimationShake = AnimationUtils.loadAnimation(this, R.anim.shake);
		setContentView(R.layout.activity_contact_detial);
		mEditTextPhone = (EditText) findViewById(R.id.phone_input);
		mEditTextName = (EditText) findViewById(R.id.name_input);

		Contact contact = getDefaultContact(this);
		mEditTextName.setText(contact.name);
		mEditTextPhone.setText(contact.phone);
	}

	@Override
	protected void onStop() {
		super.onStop();
		final String name = mEditTextName.getText().toString();
		final String phone = mEditTextPhone.getText().toString();
		if(TextUtils.isEmpty(phone) || TextUtils.isEmpty(name)) {
			return;
		}
		new AsyncContactSaver(getContentResolver()).execute(name, phone);
	}

	@Override
	public void onBackPressed() {
		// 检查输入有效性
		boolean inputValid = true;
		final String name = mEditTextName.getText().toString();
		final String phone = mEditTextPhone.getText().toString();
		if(TextUtils.isEmpty(phone)) {
			mEditTextPhone.requestFocus();
			mEditTextPhone.startAnimation(mAnimationShake);
			inputValid = false;
		}
		if(TextUtils.isEmpty(name)) {
			mEditTextName.requestFocus();
			mEditTextName.startAnimation(mAnimationShake);
			inputValid = false;
		}
		if(!inputValid)
			return;
		// 在onStop保存联系人信息
		super.onBackPressed();	//因为目前没有使用Fragment，暂时没未考虑Fragment的问题
	}

	/**
	 * 取得默认联系人
	 */
	public static Contact getDefaultContact(Context context) {
		Cursor cursor = context.getContentResolver().query(Contract.Contacts.CONTACTS_URI,
				projection, null, null, null);
		Long id = null;
		String phone = null;
		String name = null;
		if(cursor.moveToLast()) { // if the cursor isn't empty
			int idIndex = cursor.getColumnIndex(Contract.Contacts._ID);
			if(!cursor.isNull(idIndex))
				id = cursor.getLong(idIndex);
			phone = cursor.getString(
					cursor.getColumnIndex(Contract.Contacts.COLUMN_NAME_PHONE_NUMBER));
			name = cursor.getString(
					cursor.getColumnIndex(Contract.Contacts.COLUMN_NAME_NAME));
		}
		cursor.close();
		return new Contact(id, name, phone);
	}

	/**
	 * 联系人存储类，请直接读取其字段。
	 * @author Team Orange
	 */
	public static class Contact {
		public final Long id;
		public final String name;
		public final String phone;
		public Contact(Long id, String name, String phone) {
			this.id = id;
			this.name = name;
			this.phone = phone;
		}
	}


	/**
	 * {@link AsyncContactSaver#execute(String...)}的顺序为
	 * <code>execute(contactName, contactPhone)</code>
	 * <p>如：<code>new AsyncContactSaver(getContentResolver()).execute(name, phone);</code></p>
	 * @author Team Orange
	 */
	private static class AsyncContactSaver extends AsyncTask<String, Void, Void> {
		private ContentResolver mContentResolver;

		/**
		 * @see AsyncContactSaver
		 */
		public AsyncContactSaver(ContentResolver contentResolver) {
			mContentResolver = contentResolver;
		}

		@Override
		protected Void doInBackground(String... params) {
			//TODO 同步，完善多个AsyncContactSaver同时运行时的情况
			final String name = params[0], phone = params[1];
			mContentResolver.delete(Contract.Contacts.CONTACTS_URI, null, null);
			ContentValues contact = new ContentValues(2);
			contact.put(Contacts.COLUMN_NAME_NAME, name);
			contact.put(Contacts.COLUMN_NAME_PHONE_NUMBER, phone);
			mContentResolver.insert(Contract.Contacts.CONTACTS_URI, contact);
			return null;
		}
	}
}
