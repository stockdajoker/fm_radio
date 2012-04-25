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
    private int matched = 0;
    private double matchedFreq = 0;
    
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
    				double frequency = max * samplingRate / bytes.length;
    				//String output = "frequency: " + max * samplingRate / bytes.length + " Hz";
    				//log(output);
    				if (frequency == matchedFreq) {
    					matchedFreq = frequency;
      					matched++;
    					if (matched == 3) {
    						if (matchedFreq == 43.06640625) {
    							log("!");
    						} else if (matchedFreq == 86.1328125) {
    							log("\"");
    						} else if (matchedFreq == 129.19921875) {
    							log("#");
    						} else if (matchedFreq == 172.265625) {
    							log("$");
    						} else if (matchedFreq == 215.33203125) {
    							log("%");
    						} else if (matchedFreq == 258.3984375) {
    							log("&");
    						} else if (matchedFreq == 301.46484375) {
    							log("'");
    						} else if (matchedFreq == 344.53125) {
    							log("(");
    						} else if (matchedFreq == 387.59765625) {
    							log(")");
    						} else if (matchedFreq == 430.6640625) {
    							log("*");
    						} else if (matchedFreq == 473.73046875) {
    							log("+");
    						} else if (matchedFreq == 516.796875) {
    							log(",");
    						} else if (matchedFreq == 559.86328125) {
    							log("-");
    						} else if (matchedFreq == 602.9296875) {
    							log(".");
    						} else if (matchedFreq == 645.99609375) {
    							log("/");
    						} else if (matchedFreq == 689.0625) {
    							log("0");
    						} else if (matchedFreq == 732.12890625) {
    							log("1");
    						} else if (matchedFreq == 775.1953125) {
    							log("2");
    						} else if (matchedFreq == 818.26171875) {
    							log("3");
    						} else if (matchedFreq == 861.328125) {
    							log("4");
    						} else if (matchedFreq == 904.39453125) {
    							log("5");
    						} else if (matchedFreq == 947.4609375) {
    							log("6");
    						} else if (matchedFreq == 990.52734375) {
    							log("7");
    						} else if (matchedFreq == 1033.59375) {
    							log("8");
    						} else if (matchedFreq == 1076.66015625) {
    							log("9");
    						} else if (matchedFreq == 1119.7265625) {
    							log(":");
    						} else if (matchedFreq == 1162.79296875) {
    							log(";");
    						} else if (matchedFreq == 1205.859375) {
    							log("<");
    						} else if (matchedFreq == 1248.92578125) {
    							log("=");
    						} else if (matchedFreq == 1291.9921875) {
    							log(">");
    						} else if (matchedFreq == 1335.05859375) {
    							log("?");
    						} else if (matchedFreq == 1378.125) {
    							log("@");
    						} else if (matchedFreq == 1421.19140625) {
    							log("A");
    						} else if (matchedFreq == 1464.2578125) {
    							log("B");
    						} else if (matchedFreq == 1507.32421875) {
    							log("C");
    						} else if (matchedFreq == 1550.390625) {
    							log("D");
    						} else if (matchedFreq == 1593.45703125) {
    							log("E");
    						} else if (matchedFreq == 1636.5234375) {
    							log("F");
    						} else if (matchedFreq == 1679.58984375) {
    							log("G");
    						} else if (matchedFreq == 1722.65625) {
    							log("H");
    						} else if (matchedFreq == 1765.72265625) {
    							log("I");
    						} else if (matchedFreq == 1808.7890625) {
    							log("J");
    						} else if (matchedFreq == 1851.85546875) {
    							log("K");
    						} else if (matchedFreq == 1894.921875) {
    							log("L");
    						} else if (matchedFreq == 1937.98828125) {
    							log("M");
    						} else if (matchedFreq == 1981.0546875) {
    							log("N");
    						} else if (matchedFreq == 2024.12109375) {
    							log("O");
    						} else if (matchedFreq == 2067.1875) {
    							log("P");
    						} else if (matchedFreq == 2110.25390625) {
    							log("Q");
    						} else if (matchedFreq == 2153.3203125) {
    							log("R");
    						} else if (matchedFreq == 2196.38671875) {
    							log("S");
    						} else if (matchedFreq == 2239.453125) {
    							log("T");
    						} else if (matchedFreq == 2282.51953125) {
    							log("U");
    						} else if (matchedFreq == 2325.5859375) {
    							log("V");
    						} else if (matchedFreq == 2368.65234375) {
    							log("W");
    						} else if (matchedFreq == 2411.71875) {
    							log("X");
    						} else if (matchedFreq == 2454.78515625) {
    							log("Y");
    						} else if (matchedFreq == 2540.91796875) {
    							log("Z");
    						} else if (matchedFreq == 2583.984375) {
    							log("[");
    						} else if (matchedFreq == 2627.05078125) {
    							log("\\");
    						} else if (matchedFreq == 2670.1171875) {
    							log("]");
    						} else if (matchedFreq == 2713.18359375) {
    							log("^");
    						} else if (matchedFreq == 2756.25) {
    							log("_");
    						} else if (matchedFreq == 2799.31640625) {
    							log("`");
    						} else if (matchedFreq == 2842.3828125) {
    							log("a");
    						} else if (matchedFreq == 2885.44921875) {
    							log("b");
    						} else if (matchedFreq == 2928.515625) {
    							log("c");
    						} else if (matchedFreq == 2971.58203125) {
    							log("d");
    						} else if (matchedFreq == 3014.6484375) {
    							log("e");
    						} else if (matchedFreq == 3057.71484375) {
    							log("f");
    						} else if (matchedFreq == 3100.78125) {
    							log("g");
    						} else if (matchedFreq == 3186.9140625) {
    							log("h");
    						} else if (matchedFreq == 3229.98046875) {
    							log("i");
    						} else if (matchedFreq == 3273.046875) {
    							log("j");
    						} else if (matchedFreq == 3445.3125) {
    							log("k");
    						} else if (matchedFreq == 3488.37890625) {
    							log("l");
    						} else if (matchedFreq == 3617.578125) {
    							log("m");
    						} else if (matchedFreq == 3660.64453125) {
    							log("n");
    						} else if (matchedFreq == 3746.77734375) {
    							log("o");
    						}
    						matched = 0;
    					}
    				} else {
    					matchedFreq = frequency;
    					//log("logged");
    				}
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