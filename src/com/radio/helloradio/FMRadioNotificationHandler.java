/**
 * FMRadioNotificationHandler.java
 * Original source code: http://code.google.com/p/galaxy-s-radio-widget/
 * Modified by Albert Wan and Michael Chen
 * CS 194-22, UC Berkeley Spring 2012
 */

package com.radio.helloradio;

import android.os.Handler;
import android.os.Message;

public class FMRadioNotificationHandler extends Handler {

	private static final int EVENT_AF_RECEIVED = 0xe;

	private static final int EVENT_AF_STARTED = 0xd;

	private static final int EVENT_CHANNEL_FOUND = 0x1;

	private static final int EVENT_EAR_PHONE_CONNECT = 0x8;

	private static final int EVENT_EAR_PHONE_DISCONNECT = 0x9;

	private static final int EVENT_OFF = 0x6;

	private static final int EVENT_ON = 0x5;

	private static final int EVENT_RDS_DISABLED = 0xc;

	private static final int EVENT_RDS_ENABLED = 0xb;

	private static final int EVENT_RDS_EVENT = 0xa;

	private static final int EVENT_SCAN_FINISHED = 0x3;

	private static final int EVENT_SCAN_STARTED = 0x2;

	private static final int EVENT_SCAN_STOPPED = 0x4;

	private static final int EVENT_TUNE = 0x7;
	
	private IFMRadioNotification receiver;
	
	public FMRadioNotificationHandler(IFMRadioNotification notificationReceiver) {
		this.receiver = notificationReceiver;
	}
	
	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case EVENT_EAR_PHONE_CONNECT:
			receiver.onEarPhoneConnected();
			break;
		case EVENT_EAR_PHONE_DISCONNECT:
			receiver.onEarPhoneDisconnected();
			break;
		case EVENT_RDS_EVENT:
		{
			Object[] data = (Object[]) msg.obj;
			
			long freq = (Long) data[0];
			String stationName = (String) data[1];
			String radioText = (String) data[2];
			
			receiver.onRDSReceived(freq, stationName, radioText);
			break;
		}

		}
	}
}
