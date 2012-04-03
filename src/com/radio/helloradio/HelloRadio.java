package com.radio.helloradio;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import ca.gc.crc.libfmrds.FMinterface;
import android.media.MediaRecorder;
import android.os.Environment;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class HelloRadio extends Activity implements View.OnClickListener {
	private FMinterface FMLibrary = new FMinterface();
	private AudioManager aManager;
	private String TAG = "HelloRadio";
	private Timer myTimer;
    private MediaRecorder mRecorder = null;
    private static String mFileName = null;
    
	public void start() {
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setOutputFile(mFileName);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		System.err.println("mRecorder" + mRecorder);
		
		try {
			mRecorder.prepare();
		} catch (IOException e) {
			System.err.println("prepare() failed");
			//System.exit(0);
		}
		mRecorder.start();
	}
	
	public void stop() {
		mRecorder.stop();
		mRecorder.release();
		mRecorder = null;
	}
	
    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    	mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
    	mFileName += "/audiorecordtest.3gp";
    	System.err.println("mFileName " + mFileName);
        
        aManager = (AudioManager)getSystemService(this.AUDIO_SERVICE);
        aManager.setSpeakerphoneOn(true);
       
        
        ((Button)findViewById(R.id.button1)).setOnClickListener(this);
        ((Button)findViewById(R.id.button2)).setOnClickListener(this);
    }
    
    public void TimerMethod() {
    	this.runOnUiThread(Timer_tick);
    }
    
    private Runnable Timer_tick = new Runnable() {
    	public void run() {
    		Log.d(TAG, "woo hoo");
	        FMLibrary.processRDS();
	        String PS = " ";
		    String PI = Integer.toHexString(FMLibrary.getPI());
		    PS = FMLibrary.getPS();
		    String RT = FMLibrary.getRT();
		    Log.d(TAG, "PS: " + PS);
		    Log.d(TAG, "RT: " + RT); 
		    if (PS != null && PS.equals("WiLD 949")) {
		        AlertDialog alertDialog = new AlertDialog.Builder(HelloRadio.this).create();  
		        alertDialog.setTitle("String matched");  
		        alertDialog.setMessage("WiLd 949 matched");  
		        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {  
		          public void onClick(DialogInterface dialog, int which) {  
		            return;  
		        } });   
		        
		        alertDialog.show();
		    }
		    ((TextView)findViewById(R.id.textView3)).setText(PI);
		    ((TextView)findViewById(R.id.textView5)).setText(PS);
		    ((TextView)findViewById(R.id.textView7)).setText(RT);
    	}
    };
    public void onClick(View v) {
    	switch(v.getId()) {
    	case R.id.button1:
    		//start
    		start();

    		String temp = ((EditText)findViewById(R.id.editText1)).getText().toString();
    		int currentFreq = Integer.parseInt(temp); 
    		((TextView)findViewById(R.id.textView8)).setText(Double.toString((double) currentFreq / 1000));
    		if (FMLibrary.radioIsSupported()) {
    		    FMLibrary.openRadio(aManager);
    		    //FMLibrary.setBand(FMLibrary.BAND_87500_108000_kHz);
    		    FMLibrary.setSpacing(FMLibrary.CHAN_SPACING_100_kHz);
    		    FMLibrary.setEmphasis(FMLibrary.DE_TIME_CONSTANT_75);
    		    FMLibrary.setChannel(currentFreq);
		        FMLibrary.syncMediaVolume(aManager);
		        //FMLibrary.setVolume(1);
		        
		        myTimer = new Timer();
		        myTimer.schedule(new TimerTask() {
		        	public void run() {
		        		TimerMethod();
		        	}
		        }, 0, 1000);
    		}
    		break;
    	case R.id.button2:
    		//stop
    		myTimer.cancel();
	        FMLibrary.processRDS();
		    String PI = Integer.toHexString(FMLibrary.getPI());
		    String PS = FMLibrary.getPS();
		    String RT = FMLibrary.getRT();
		                
		    ((TextView)findViewById(R.id.textView3)).setText(PI);
		    ((TextView)findViewById(R.id.textView5)).setText(PS);
		    ((TextView)findViewById(R.id.textView7)).setText(RT);
		    
		    FMLibrary.closeRadio(aManager); 

		    stop();
    		break;
    	default:
    		break;
    	
    	}
    }
}