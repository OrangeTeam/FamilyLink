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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
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
	private Button mButtonEdit = null;
	private Button mButtonSave = null;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAnimationShake = AnimationUtils.loadAnimation(this, R.anim.shake);
		setContentView(R.layout.activity_contact_detial);
		mEditTextPhone = (EditText) findViewById(R.id.phone_input);
		mEditTextName = (EditText) findViewById(R.id.name_input);
		mButtonEdit = (Button) findViewById(R.id.button_edit);
		mButtonSave = (Button) findViewById(R.id.button_save);
		mEditTextName.setFocusable(false);
		mEditTextPhone.setFocusable(false);

		Contact contact = getDefaultContact(this);
		mEditTextName.setText(contact.name);
		mEditTextPhone.setText(contact.phone);
		/**
		 * 解锁获取焦点
		 */
		mButtonEdit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mEditTextPhone.setFocusable(true);
				mEditTextPhone.setFocusableInTouchMode(true);
				mEditTextName.setFocusable(true);
				mEditTextName.setFocusableInTouchMode(true);
				mEditTextName.requestFocus();
				mEditTextName.setSelection(mEditTextName.length());
				mButtonEdit.setVisibility(View.GONE);
				mButtonSave.setVisibility(View.VISIBLE);
			}
		});
		/**先删除上一个后保存下一个
		 * 保存之后继续取消焦点
		 */
		mButtonSave.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
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
				// 保存联系人信息
				new AsyncContactSaver(getContentResolver()).execute(name, phone);
				// 退出编辑状态
				mEditTextName.setFocusable(false);
				mEditTextPhone.setFocusable(false);
				mButtonSave.setVisibility(View.GONE);
				mButtonEdit.setVisibility(View.VISIBLE);
			}
		});
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
