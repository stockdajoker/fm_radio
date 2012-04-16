package com.radio.helloradio;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.media.audiofx.Visualizer;
import android.media.MediaRecorder;
import android.os.Environment;

import java.io.IOException;
import java.lang.Math;

public class HelloRadio extends Activity implements View.OnClickListener {

	private String TAG = "HelloRadio";
	private Thread myTimer = null;
    private MediaRecorder mRecorder = null;
    private static String mFileName = null;
    private MediaPlayer mMediaPlayer;
    private Visualizer mVisualizer;
    private Toast toast;
    
    private FMPlayerServiceWrapper mFmRadioServiceWrapper;
	private IFMRadioNotification mRadioNotification = new GalaxyRadioNotification();
	private AudioManager aManager = null;
	private String mStationData = "";
	private String mInfoData = "";
	
	private static final int RADIO_AUDIO_STREAM = 0xa;
	private static final String FM_RADIO_SERVICE_NAME = "FMPlayer";
    
    private void log(String msg) {
		Log.d(TAG, msg);
	}
   
	public void recordStart() {
		if (mRecorder == null) {
			mRecorder = new MediaRecorder();
			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
			mRecorder.setOutputFile(mFileName);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			log("mRecorder: " + mRecorder.toString());
			try {
				mRecorder.prepare();
				mRecorder.start();
			} catch (IOException e) {
				System.err.println("prepare() failed");
			}
		}
	}
	
	public void recordStop() {
		if (mRecorder != null) {
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;
		}
	}
	
	public boolean isRadioSupported() {
    	try {
			Class.forName("com.samsung.media.fmradio.FMEventListener");
			log("class found!");
			return true;
		} catch (ClassNotFoundException e) {
			log("class not found");
			return false;
		}
	}
	
	public boolean turnRadioOn() {
		if (!mFmRadioServiceWrapper.isOn()) {
			try {
				mFmRadioServiceWrapper.on();
				return true;
			} catch (FMPlayerException e) {
				toast = Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT);
				switch (e.getCode()) {
				case FMPlayerException.AIRPLANE_MODE:
					toast.setText("Airplane Mode is on");
					break;
				case FMPlayerException.HEAD_SET_IS_NOT_PLUGGED:
					toast.setText("No earphones connected.");
					break;
				}
				toast.show();
				return false;
			}
		}
		log("whee");
		return false;
	}
	
	public void turnRadioOff() {
		if (mFmRadioServiceWrapper.isOn()) {
			mFmRadioServiceWrapper.off();
		}
	}
	
	public boolean isRadioOn() {
		return mFmRadioServiceWrapper.isOn();
	}
	
	public void setFrequency(int frequency) {
		mFmRadioServiceWrapper.tune(frequency);
	}
	
	public void enableRDS() {
		mFmRadioServiceWrapper.enableRDS();
		mFmRadioServiceWrapper.enableAF();
	}
	
	public void setRadioVolume() {
		mFmRadioServiceWrapper.setSpeakerOn(false);
		aManager.setStreamVolume(RADIO_AUDIO_STREAM, aManager.getStreamVolume(RADIO_AUDIO_STREAM), 0x0);
	}
	
	public void TimerMethod() {
		this.runOnUiThread(Timer_tick);
	}
	
	private Runnable Timer_tick = new Runnable() {
		public void run() {

		}
	};
	
    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    	mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
    	mFileName += "/audiorecordtest.3gp";

    	aManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
    	mFmRadioServiceWrapper = new FMPlayerServiceWrapper(getSystemService(FM_RADIO_SERVICE_NAME));
    	mFmRadioServiceWrapper.setListener(mRadioNotification);
      
        ((Button)findViewById(R.id.button1)).setOnClickListener(this);
        ((Button)findViewById(R.id.button2)).setOnClickListener(this);
    }
    
    private static int findMaxIndex(double[] array) {
    	double max = array[0];
    	int maxIndex = 0;
    	for (int i = 1; i < array.length; i++) {
    		if (array[i] > max) {
    			max = array[i];
    			maxIndex = i;
    		}
    	}
    	return maxIndex;
    }
    
    private void analyze() {
    	try {
    		mMediaPlayer = new MediaPlayer();
    		mMediaPlayer.setDataSource(mFileName);
    		
    		mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
    			public void onCompletion(MediaPlayer mediaPlayer) {
    				mVisualizer.setEnabled(false);
    			}
    		});

    		int Id = mMediaPlayer.getAudioSessionId();

    		mVisualizer = new Visualizer(Id);
    		mVisualizer.setEnabled(false);
    		mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
    		mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
    			public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
    				samplingRate = samplingRate / 1000;
    				double[] magnitude = new double[bytes.length / 2];
    				
    				for (int i = 2, j = 0; i < bytes.length; i += 2, j++) {
    					magnitude[j] = Math.pow((double) (bytes[i] * bytes[i] + bytes[i + 1] * bytes[i + 1]), 0.5);
    				}
    				double max = findMaxIndex(magnitude);  				
    				String output = "frequency: " + max * samplingRate / bytes.length + " Hz";
    				log(output);
    				toast = Toast.makeText(getApplicationContext(), output, Toast.LENGTH_SHORT);
    				toast.show();
    			}
    			public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
    				boolean periodFound = false;
    				if (visualizer == mVisualizer) {
    					if (periodFound) {
    						
    					}
    				}
    			}
    		}, Visualizer.getMaxCaptureRate() / 2, true, true);
    		mVisualizer.setEnabled(true);
    		mMediaPlayer.prepare();
    		mMediaPlayer.start();
    	} catch (Exception e) {
    		log("error: " + e);
    	}	
    }
    
    public void onClick(View v) {
		toast = Toast.makeText(getApplicationContext(), "nothing", Toast.LENGTH_SHORT);
    	switch(v.getId()) {
    	case R.id.button1:
    		//start
    		String temp = ((EditText)findViewById(R.id.editText1)).getText().toString();
    		int currentFreq = Integer.parseInt(temp); 
    		((TextView)findViewById(R.id.textView8)).setText(Double.toString((double) currentFreq / 1000));
    		if (isRadioSupported()) {
    		    if (turnRadioOn()) {
    	    		recordStart();
    		    	enableRDS();
    		    	setFrequency(currentFreq);
    		    	setRadioVolume();
    		    	toast.setText("Radio on");
    		    	log("radio on");
    		    	
    		    	myTimer = new Thread(Timer_tick);
    		    	myTimer.start();
    		    }
    		}
    		break;
    	case R.id.button2:
    		//stop
    		if (isRadioOn()) {
    			recordStop();	
    			turnRadioOff();
    			analyze();
    			toast.setText("Radio off");
    			log("radio off");
    		}
		    break;
    	default:
    		break;
    	}
    	toast.show();
    }
    
    public class GalaxyRadioNotification implements IFMRadioNotification {

    	@Override
    	public void onEarPhoneConnected() {}
    	
    	@Override
    	public void onEarPhoneDisconnected() {
    		
    	}
		@Override
		public void onRDSReceived(long freq, String stationName,
				String radioText) {
			mStationData = stationName;
			mInfoData = radioText;
			((TextView) findViewById(R.id.textView3)).setText(mStationData);
			((TextView) findViewById(R.id.textView7)).setText(mInfoData);

		}

	}
    
    public class RefreshHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
		}
	}
}