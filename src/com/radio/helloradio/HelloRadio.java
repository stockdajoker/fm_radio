package com.radio.helloradio;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
//import ca.gc.crc.libfmrds.FMinterface;
import android.media.MediaRecorder;
import android.os.Environment;

import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class HelloRadio extends Activity implements View.OnClickListener {
	//private FMinterface FMLibrary = new FMinterface();

	private String TAG = "HelloRadio";
	private Thread myTimer = null;
    private MediaRecorder mRecorder = null;
    private static String mFileName = null;
    
    public native void DoFFT(double[] data, int size);
  
    
    private FMPlayerServiceWrapper mFmRadioServiceWrapper;
	private IFMRadioNotification mRadioNotification = new GalaxyRadioNotification();
	private AudioManager aManager = null;
	private AudioRecord recorder = null;
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
    
    public void initRecorderParameters(int[] sampleRates){

        for (int i = 0; i < sampleRates.length; ++i){
            try {
                log("Indexing "+sampleRates[i]+"Hz Sample Rate");
                int tmpBufferSize = AudioRecord.getMinBufferSize(sampleRates[i], 
                                AudioFormat.CHANNEL_IN_MONO,
                                AudioFormat.ENCODING_PCM_16BIT);

                // Test the minimum allowed buffer size with this configuration on this device.
                if(tmpBufferSize != AudioRecord.ERROR_BAD_VALUE){
                    // Seems like we have ourself the optimum AudioRecord parameter for this device.
                    recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 
                                                            sampleRates[i], 
                                                            AudioFormat.CHANNEL_IN_MONO,
                                                            AudioFormat.ENCODING_PCM_16BIT,
                                                            tmpBufferSize);
                    // Test if an AudioRecord instance can be initialized with the given parameters.
                    if(recorder.getState() == AudioRecord.STATE_INITIALIZED){
                        String configResume = "initRecorderParameters(sRates) has found recorder settings supported by the device:"  
                                            + "\nSource   = MICROPHONE"
                                            + "\nsRate    = "+sampleRates[i]+"Hz"
                                            + "\nChannel  = MONO"
                                            + "\nEncoding = 16BIT";
                        log(configResume);

                        /*//+++Release temporary recorder resources and leave.
                        tmpRecoder.release();
                        tmpRecoder = null;
						*/
                        return;
                    }                 
                }else{
                    log( "Incorrect buffer size. Continue sweeping Sampling Rate...");
                }
            } catch (IllegalArgumentException e) {
                log( "The "+sampleRates[i]+"Hz Sampling Rate is not supported on this device");
            }
        }
    }
    
	public void recordStart() {
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
		mRecorder.setOutputFile(mFileName);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		
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
    		short[] audioData = new short[BUFFER];
    		int bufferSize = BUFFER;
    		int numCrossing;
    		while (isRadioOn()) {
    			/*log("state: " + recorder.getRecordingState());
    			if (recorder.getRecordingState() == android.media.AudioRecord.RECORDSTATE_STOPPED) {
    				recorder.startRecording();
    				log("state after : " + recorder.getRecordingState());
    			} else {
    				log("y u never come here");
    				recorder.read(audioData, 0, BUFFER);
    				
    				numCrossing = 0;
    				for (int p=0;p<bufferSize/4;p+=4) {
    					if (audioData[p]>0 && audioData[p+1]<=0) numCrossing++;
    		            if (audioData[p]<0 && audioData[p+1]>=0) numCrossing++;
    		            if (audioData[p+1]>0 && audioData[p+2]<=0) numCrossing++;
    		            if (audioData[p+1]<0 && audioData[p+2]>=0) numCrossing++;
    		            if (audioData[p+2]>0 && audioData[p+3]<=0) numCrossing++;
    		            if (audioData[p+2]<0 && audioData[p+3]>=0) numCrossing++;
    		            if (audioData[p+3]>0 && audioData[p+4]<=0) numCrossing++;
    		            if (audioData[p+3]<0 && audioData[p+4]>=0) numCrossing++;
    				}//for p
    		       
    				for (int p=(bufferSize/4)*4;p<bufferSize-1;p++) {
    					if (audioData[p]>0 && audioData[p+1]<=0) numCrossing++;
    					if (audioData[p]<0 && audioData[p+1]>=0) numCrossing++;
    				}
    				log("frequency: " + ((8000 / bufferSize) * numCrossing / 2));
    			}*/
    		}
    		if (recorder.getState() == android.media.AudioRecord.RECORDSTATE_RECORDING) {
    			recorder.stop();

    		}

    	}
    };
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
    		    	if (recorder == null) {
    		    		int[] sampleRates = {8000, 11025, 22050, 44100, 48000};
    		    		initRecorderParameters(sampleRates); 	
    		    	}
    		    	enableRDS();
    		    	setFrequency(currentFreq);
    		    	setRadioVolume();

    		    	log("state: " + recorder.getRecordingState());
        			if (recorder.getRecordingState() == android.media.AudioRecord.RECORDSTATE_STOPPED) {
        				recorder.startRecording();
        				log("state after: " + recorder.getRecordingState());
        			}
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
		    	log("radio turned off");
		    	if (recorder != null) {
		    		recorder.release();
		    		recorder = null;
		    	}
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