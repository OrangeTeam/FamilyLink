/**
 *
 */
package org.orange.familylink;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * @author Team Orange
 */
public abstract class BaseActivity extends ActionBarActivity {

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
