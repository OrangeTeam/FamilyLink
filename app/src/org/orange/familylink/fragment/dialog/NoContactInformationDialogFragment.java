/**
 *
 */
package org.orange.familylink.fragment.dialog;

import org.orange.familylink.ContactDetailActivity;
import org.orange.familylink.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

/**
 * 尚未设置联系方式提示{@link DialogFragment}
 * @author Team Orange
 */
public class NoContactInformationDialogFragment extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.no_contact_information)
				.setMessage(R.string.no_contact_information_message)
				.setNegativeButton(android.R.string.cancel, null)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startActivity(new Intent(getActivity(), ContactDetailActivity.class));
					}
				});
		return builder.create();
	}

}
