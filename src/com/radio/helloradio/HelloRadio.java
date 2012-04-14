package com.radio.helloradio;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.media.audiofx.Visualizer;
//import ca.gc.crc.libfmrds.FMinterface;
import android.media.MediaRecorder;
import android.os.Environment;

import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.lang.Math;
import java.util.Arrays;

public class HelloRadio extends Activity implements View.OnClickListener {
	//private FMinterface FMLibrary = new FMinterface();

	private String TAG = "HelloRadio";
	private Thread myTimer = null;
    private MediaRecorder mRecorder = null;
    private static String mFileName = null;
    private MediaPlayer mMediaPlayer;
    private Visualizer mVisualizer;
    private byte[] mFft;
    private boolean runOnce = false;
    
    public native void DoFFT(double[] data, int size);
  
    
    private FMPlayerServiceWrapper mFmRadioServiceWrapper;
	private IFMRadioNotification mRadioNotification = new GalaxyRadioNotification();
	private AudioManager aManager = null;
	private String mFrequencyData = "";
	private String mStationData = "";
	private String mInfoData = "";
	private Handler mRefreshHandler = new RefreshHandler();
	private boolean isTuned;
	
	private static final int RADIO_AUDIO_STREAM = 0xa;
	private static final String FM_RADIO_SERVICE_NAME = "FMPlayer";
    
	private final static int RATE = 8000;
    private final static int CHANNEL_MODE = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private final static int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private final static int BUFFER = 6144;
	
    private final static int BUFFER_SIZE_IN_MS = 3000;
    private final static int CHUNK_SIZE_IN_SAMPLES = 4096;
    
    private final static int CHUNK_SIZE_IN_MS = 1000 * CHUNK_SIZE_IN_SAMPLES / RATE;
    private final static int BUFFER_SIZE_IN_BYTES = RATE * BUFFER_SIZE_IN_MS / 1000 * 2;
    private final static int CHUNK_SIZE_IN_BYTES = RATE * CHUNK_SIZE_IN_MS / 1000 * 2;
    private final static int MIN_FREQUENCY = 50;
    private final static int MAX_FREQUENCY = 600;
    private final static int DRAW_FREQUENCY_STEP = 5;
    
    private void log(String msg) {
		Log.d(TAG, msg);
	}
    

    
	public void recordStart() {
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
				log("error");
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
	
    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    	mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
    	mFileName += "/audiorecordtest.3gp";


    	log("working");
    	System.loadLibrary("fft-jni");
    	log("still working");
    	aManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
    	mFmRadioServiceWrapper = new FMPlayerServiceWrapper(getSystemService(FM_RADIO_SERVICE_NAME));
    	mFmRadioServiceWrapper.setListener(mRadioNotification);
       
        
        ((Button)findViewById(R.id.button1)).setOnClickListener(this);
        ((Button)findViewById(R.id.button2)).setOnClickListener(this);
    }
    
    public void TimerMethod() {
    	this.runOnUiThread(Timer_tick);
    }
    
    
    private Runnable Timer_tick = new Runnable() {
    	public void run() {
    		
    	}
    };
    
    public static int byteArrayToInt(byte[] b) {
    	return (b[0] << 24) 
    			+ ((b[1] & 0xFF) << 16)
    			+ ((b[2] & 0xFF) << 8)
    			+(b[3] & 0xFF);
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
    				log("max: " + max * samplingRate / bytes.length);
    				
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
    	switch(v.getId()) {
    	case R.id.button1:
    		//start
    		recordStart();

    		String temp = ((EditText)findViewById(R.id.editText1)).getText().toString();
    		int currentFreq = Integer.parseInt(temp); 
    		((TextView)findViewById(R.id.textView8)).setText(Double.toString((double) currentFreq / 1000));
    		if (isRadioSupported()) {
    		    if (turnRadioOn()) {
    		    	enableRDS();
    		    	setFrequency(currentFreq);
    		    	setRadioVolume();

    		    	myTimer = new Thread(Timer_tick);
    		    	myTimer.start();
    		    	
    		    }
    		}
    		break;
    	case R.id.button2:
    		//stop
    		if (myTimer != null) {
    			 

	        	/*
		    	String PI = Integer.toHexString(FMLibrary.getPI());
		    	String PS = FMLibrary.getPS();
		    	String RT = FMLibrary.getRT();
		                
		    	((TextView)findViewById(R.id.textView3)).setText(PI);
		    	((TextView)findViewById(R.id.textView5)).setText(PS);
		    	((TextView)findViewById(R.id.textView7)).setText(RT);
		    	*/
    			log("before stopped");
    			recordStop();	
    			log("stopped");
		    	turnRadioOff();
		    	analyze();
		    	log("radio turned off");
    		}
		    break;
    	default:
    		break;
    	
    	}
    }
    
    public class GalaxyRadioNotification implements IFMRadioNotification {

		@Override
		public void onRDSReceived(long freq, String stationName,
				String radioText) {
			mStationData = stationName;
			mInfoData = radioText;

		}

	}
    
    public class RefreshHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
		}
	}
}