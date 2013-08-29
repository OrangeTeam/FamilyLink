/**
 *
 */
package org.orange.familylink.fragment;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.app.ListFragment;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.Toast;
import org.orange.familylink.ContactDetailActivity;
import org.orange.familylink.R;
import org.orange.familylink.database.Contract;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * 求助{@link Fragment}
 * @author Team Orange
 */
public class SeekHelpFragment extends ListFragment {
	private static final int LOADER_ID_CONTACTS = 1;
	/** 用于显示联系人的{@link ListView}的{@link ListAdapter} */
	private CursorAdapter mAdapterForContactList;

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Give some text to display if there is no data.
		setEmptyText(getResources().getText(R.string.no_contact));

		// We have a menu item to show in action bar.
		setHasOptionsMenu(true);

		// Create an empty adapter we will use to display the loaded data.
		mAdapterForContactList = new ContactListAdapter(getActivity(), null, 0);
		setListAdapter(mAdapterForContactList);

		// Start out with a progress indicator.
		setListShown(false);

		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(LOADER_ID_CONTACTS, null,
				mLoaderCallbacksForContacts);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_seek_help, menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch(item.getItemId()) {
		case R.id.add_contact:
			Toast.makeText(getActivity(), item.getTitle(), Toast.LENGTH_LONG).show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}


	protected final LoaderCallbacks<Cursor> mLoaderCallbacksForContacts = new LoaderCallbacks<Cursor>() {
		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			// One callback only to one loader, so we don't care about the ID.
			Uri baseUri = Contract.Contacts.CONTACTS_URI;
			String[] projection = { Contract.Contacts._ID,
					Contract.Contacts.COLUMN_NAME_NAME,
					Contract.Contacts.COLUMN_NAME_PHONE_NUMBER };
			return new CursorLoader(getActivity(), baseUri, projection, null,
					null, null);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			mAdapterForContactList.swapCursor(data);
			if (isResumed()) {
				setListShown(true);
			} else {
				setListShownNoAnimation(true);
			}
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			mAdapterForContactList.swapCursor(null);
		}
	};
	private class ContactListAdapter extends CursorAdapter {
		private final LayoutInflater mInflater;

		public ContactListAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View rootView = mInflater.inflate(R.layout.fragment_seek_help,
					parent, false);
			ViewHolder holder = new ViewHolder();
			holder.name = (TextView) rootView.findViewById(R.id.name);
			holder.sms = (Button) rootView.findViewById(R.id.OvalButtonsms);
			holder.phone = (Button) rootView.findViewById(R.id.OvalButtonphone);
			holder.people = (RelativeLayout) rootView.findViewById(R.id.people);
			rootView.setTag(holder);
			return rootView;
		}

		public void bindView(View view, Context context, Cursor cursor) {
			final String phone_number = cursor
					.getString(cursor
							.getColumnIndex(Contract.Contacts.COLUMN_NAME_PHONE_NUMBER));
			final String name = cursor.getString(cursor
					.getColumnIndex(Contract.Contacts.COLUMN_NAME_NAME));
			final ViewHolder holder = (ViewHolder) view.getTag();
			holder.sms.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Uri smsToUri = Uri.parse("smsto:" + phone_number);
					Intent mIntent = new Intent(
							android.content.Intent.ACTION_SENDTO, smsToUri);
					startActivity(mIntent);
				}
			});
			holder.name.setText(name);
			holder.phone.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_CALL, Uri
							.parse("tel:" + phone_number));
					startActivity(intent);
				}
			});
			holder.people.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), ContactDetailActivity.class);
					intent.putExtra("name", (String) holder.name.getText());
					intent.putExtra("number", phone_number);
					startActivity(intent);
				}
			});
		}

		private class ViewHolder {
			Button phone;
			Button sms;
			TextView name;
			RelativeLayout people;
		}
	}

}
