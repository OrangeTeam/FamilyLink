/**
 *
 */
package org.orange.familylink;

import android.support.v7.app.ActionBarActivity;
import android.view.Menu;

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
}
