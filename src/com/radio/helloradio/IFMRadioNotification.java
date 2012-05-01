/**
 * IFMRadioNotification.java
 * Original source code: http://code.google.com/p/galaxy-s-radio-widget/
 * Modified by Albert Wan and Michael Chen
 * CS 194-22, UC Berkeley Spring 2012
 */

package com.radio.helloradio;

public interface IFMRadioNotification {
	
	public void onRDSReceived(long freq, String stationName, String radioText);
	
	public void onEarPhoneConnected();
	
	public void onEarPhoneDisconnected();
	
}
