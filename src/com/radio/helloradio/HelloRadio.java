package com.radio.helloradio;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import ca.gc.crc.libfmrds.FMinterface;

public class HelloRadio extends Activity implements View.OnClickListener {
	private FMinterface FMLibrary = new FMinterface();
	private AudioManager aManager;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        aManager = (AudioManager)getSystemService(this.AUDIO_SERVICE);
        
        ((Button)findViewById(R.id.button1)).setOnClickListener(this);
        ((Button)findViewById(R.id.button2)).setOnClickListener(this);
    }
    
    public void onClick(View v) {
    	switch(v.getId()) {
    	case R.id.button1:
    		//start
    		
    		String temp = ((EditText)findViewById(R.id.editText1)).getText().toString();
    		int currentFreq = Integer.parseInt(temp); 
    		((TextView)findViewById(R.id.textView8)).setText(Double.toString((double) currentFreq / 1000));
    		if (FMLibrary.radioIsSupported()) {
    		    FMLibrary.openRadio(aManager);
    		    FMLibrary.setBand(FMLibrary.BAND_87500_108000_kHz);
    		    FMLibrary.setSpacing(FMLibrary.CHAN_SPACING_100_kHz);
    		    FMLibrary.setEmphasis(FMLibrary.DE_TIME_CONSTANT_75);
    		    FMLibrary.setChannel(currentFreq);
		        FMLibrary.syncMediaVolume(aManager);    		    
    		    /*int i = 0;
    		    while (i < 20){
    		        try {
    		            Thread.sleep(250);
    		        } catch (InterruptedException e) {
    		        }
    		        FMLibrary.processRDS();
    		        FMLibrary.syncMediaVolume(aManager);
    		        i++;
    		    }
    		    String PI = Integer.toHexString(FMLibrary.getPI());
    		    String PS = FMLibrary.getPS();
    		    String RT = FMLibrary.getRT();
    		                
    		    ((TextView)findViewById(R.id.textView3)).setText(PI);
    		    ((TextView)findViewById(R.id.textView5)).setText(PS);
    		    ((TextView)findViewById(R.id.textView7)).setText(RT);
    		    
    		    FMLibrary.closeRadio(aManager); 
    		    */
    		}
    		break;
    	case R.id.button2:
    		//stop
	        FMLibrary.processRDS();
		    String PI = Integer.toHexString(FMLibrary.getPI());
		    String PS = FMLibrary.getPS();
		    String RT = FMLibrary.getRT();
		                
		    ((TextView)findViewById(R.id.textView3)).setText(PI);
		    ((TextView)findViewById(R.id.textView5)).setText(PS);
		    ((TextView)findViewById(R.id.textView7)).setText(RT);
		    
		    FMLibrary.closeRadio(aManager); 
    		break;
    	default:
    		break;
    	
    	}
    }
}