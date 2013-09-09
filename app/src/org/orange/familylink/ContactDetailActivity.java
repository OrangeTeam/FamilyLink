package org.orange.familylink;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

public class ContactDetailActivity extends BaseActivity {
	private EditText editText2 = null;
	private EditText editText1 = null;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_detial);
		Intent intent = getIntent();
		String name = intent.getStringExtra("name");
		String number = intent.getStringExtra("number");
		editText2 = (EditText)findViewById(R.id.edit2);
		editText1 = (EditText) findViewById(R.id.edit1);
		editText1.setText(name);
		editText2.setText(number);
	}

	public EditText getEditText2() {
		return editText2;
	}

	public void setEditText2(EditText editText2) {
		this.editText2 = editText2;
	}

	public EditText getEditText1() {
		return editText1;
	}

	public void setEditText1(EditText editText1) {
		this.editText1 = editText1;
	}


}