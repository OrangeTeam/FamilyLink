/**
 *
 */
package org.orange.familylink.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.RelativeLayout;

/**
 * @author Team Orange
 */
public class CheckableRelativeLayout extends RelativeLayout implements Checkable {
	private static final int[] CHECKED_STATE_SET = 
		{android.R.attr.state_checked, android.R.attr.state_checkable};

	private boolean mChecked = false;

	public CheckableRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public CheckableRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CheckableRelativeLayout(Context context) {
		super(context);
	}

	@Override
	public void setChecked(boolean checked) {
		mChecked = checked;
		refreshDrawableState();
	}

	@Override
	public boolean isChecked() {
		return mChecked;
	}

	@Override
	public void toggle() {
		setChecked(!mChecked);
	}

	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + CHECKED_STATE_SET.length);
		if (isChecked()) {
			mergeDrawableStates(drawableState, CHECKED_STATE_SET);
		}
		return drawableState;
	}

}
