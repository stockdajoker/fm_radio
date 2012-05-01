/**
 * FMPlayerException.java
 * Original source code: http://code.google.com/p/galaxy-s-radio-widget/
 * Modified by Albert Wan and Michael Chen
 * CS 194-22, UC Berkeley Spring 2012
 */

package com.radio.helloradio;

public class FMPlayerException extends Throwable {
	
	public static final int AIRPLANE_MODE = 0x5;

	public static final int  BATTERY_LOW = 0x6;

	public static final int  HEAD_SET_IS_NOT_PLUGGED = 0x4;

	public static final int  PLAYER_IS_NOT_ON = 0x1;

	public static final int  PLAYER_SCANNING = 0x3;

	public static final int  RADIO_SERVICE_DOWN = 0x2;

	public static final int  TV_OUT_PLUGGED = 0x7;

	private static final long serialVersionUID = 4431144317065869135L;
	
	private int code;
	
	public FMPlayerException(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
}
