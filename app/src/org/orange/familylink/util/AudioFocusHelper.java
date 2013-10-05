/**
 *
 */
package org.orange.familylink.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;

/**
 * @author Team Orange
 *
 */
public abstract class AudioFocusHelper implements OnAudioFocusChangeListener {
	AudioManager mAudioManager;

	public AudioFocusHelper(Context context) {
		this.mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	}

	public boolean requestFocus() {
		return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
			mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
											AudioManager.AUDIOFOCUS_GAIN);
	}

	public boolean abandonFocus() {
		return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
			mAudioManager.abandonAudioFocus(this);
	}


}
