/**
 *
 */
package org.orange.familylink.fragment;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.TextView;
import org.orange.familylink.R;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

/**
 * 导航{@link Fragment}
 * @author Team Orange
 */

public class NavigateFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		TextView textView = new TextView(getActivity());
		textView.setText(R.string.navigate);
		return textView;
	}

}
