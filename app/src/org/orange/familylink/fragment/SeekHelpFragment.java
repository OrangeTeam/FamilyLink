/**
 *
 */
package org.orange.familylink.fragment;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.orange.familylink.R;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

/**
 * 求助{@link Fragment}
 * @author Team Orange
 */
public class SeekHelpFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_seek_help, container, false);
		return rootView;
	}

}
