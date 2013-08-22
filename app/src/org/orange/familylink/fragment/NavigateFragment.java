/**
 *
 */
package org.orange.familylink.fragment;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.Button;
import org.orange.familylink.location.LocationTracker;
import org.orange.familylink.navigation.StartNavigation;

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
		LocationTracker mTracker = new LocationTracker(getActivity());
		final double latitude = mTracker.getLatitude();
		final double longitude = mTracker.getLongitude();
		Button button = new Button(getActivity());
		button.setTextSize(15);
		button.setText("to navigation");
		button.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				StartNavigation.toStartNavigationApp(getActivity(), getActivity(), latitude, longitude);
			}
		});
		return button;
	}

}
