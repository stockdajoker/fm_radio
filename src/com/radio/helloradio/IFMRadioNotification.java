package com.radio.helloradio;

public interface IFMRadioNotification {
	
	public void onRDSReceived(long freq, String stationName, String radioText);
	
	public void onEarPhoneConnected();
	
	public void onEarPhoneDisconnected();
	
}
