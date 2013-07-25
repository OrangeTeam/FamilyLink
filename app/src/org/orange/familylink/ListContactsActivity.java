/**
 *
 */
package org.orange.familylink;

import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;

/**
 * @author Team Orange
 */
public class ListContactsActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_contacts);
		setupActionBar();
	}

	/**
	 * Set up the {@link ActionBar}.
	 */
	protected void setupActionBar() {
		ActionBar actionBar = getSupportActionBar();
		// Show the Up button in the action bar.
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

}
