package com.dataexpo.rna;

import java.io.IOException;
import java.util.HashMap;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;

import com.idata.fastscandemo.R;


public class SoundManager {
	private Context mContext;
	private SoundPool soundpool;
	private HashMap<Integer, Integer> spMap;
	protected MediaPlayer mediaPlayer = null;

	public SoundManager(Context context) {
		this.mContext = context;
	}

	public void initSound() {
			initSoundPool();
	}

	public void playSoundAndVibrate(boolean m_key_beep, boolean m_key_vibrate) {
	
			playPoolSoundAndVibrate(m_key_beep, m_key_vibrate);
		

	}

	private void initSoundPool() {
		soundpool = new SoundPool(1,
				AudioManager.STREAM_RING,
				0
		);
		spMap = new HashMap<Integer, Integer>();
		
			spMap.put(1, soundpool.load(mContext, R.raw.beep, 1));
		
	}

	private void playPoolSoundAndVibrate(boolean m_key_beep,
			boolean m_key_vibrate) {

		if (m_key_beep) {
			soundpool.play(spMap.get(1), 1, 1, 1, 0, 1);
		}
	}

	private void initBeepSound() {
		if (mediaPlayer == null) {
			mediaPlayer = new MediaPlayer();
		
			AssetFileDescriptor file=null;
			
				 file = mContext.getResources()
							.openRawResourceFd(R.raw.beep);
			
			try {
				if (mediaPlayer != null) {
					mediaPlayer.setDataSource(file.getFileDescriptor(),
							file.getStartOffset(), file.getLength());
					file.close();

					mediaPlayer.prepare();

				}
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private void playBeepSoundAndVibrate(boolean m_key_beep,
			boolean m_key_vibrate) {
		if (m_key_beep && (mediaPlayer != null)) {
			mediaPlayer.start();
		}
	}

}
