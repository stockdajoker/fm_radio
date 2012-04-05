package com.radio.helloradio;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

public class FMPlayerServiceWrapper {

	static final String TAG = "GalaxySRadioWidget";
	
	private Map<IFMRadioNotification, Object> listenersMap = new HashMap<IFMRadioNotification, Object>();
	
	private Object internalServiceObject;
	private Class<?> internalServiceClass;
	private Class<?> listenerClass;
	
	private Method setListenerMethod;
	private Method removeListenerMethod;
	private Method onMethod;
	private Method offMethod;
	private Method isOnMethod;
	private Method isScanningMethod;
	private Method setBandMethod;
	private Method setChannelSpacingMethod;
	private Method setDEConstantMethod;
	private Method setSpeakerOnMethod;
	private Method seekUpMethod;
	private Method seekDownMethod;
	private Method scanMethod;
	private Method cancelScanMethod;
	private Method tuneMethod;
	private Method getLastScanResultMethod;
	private Method getCurrentChannelMethod;
	private Method disableRDSMethod;
	private Method enableRDSMethod;
	private Method disableAFMethod;
	private Method enableAFMethod;
	private Method isHeadsetPluggedMethod;
	
	public FMPlayerServiceWrapper(Object serviceObject) {
		this.internalServiceObject = serviceObject;
		
		setUpInnerMethods();
	}
	
	private void setUpInnerMethods() {
		internalServiceClass = internalServiceObject.getClass();
		
		try {
			listenerClass = Class.forName("com.samsung.media.fmradio.FMEventListener");
			
			onMethod = internalServiceClass.getMethod("on");
			offMethod = internalServiceClass.getMethod("off");
			isOnMethod = internalServiceClass.getMethod("isOn");
			isScanningMethod = internalServiceClass.getMethod("isScanning");
			setBandMethod = internalServiceClass.getMethod("setBand", int.class);
			setChannelSpacingMethod = internalServiceClass.getMethod("setChannelSpacing", int.class);
			setDEConstantMethod = internalServiceClass.getMethod("setDEConstant", long.class);
			setSpeakerOnMethod = internalServiceClass.getMethod("setSpeakerOn", boolean.class);
			seekUpMethod = internalServiceClass.getMethod("seekUp");
			seekDownMethod = internalServiceClass.getMethod("seekDown");
			scanMethod = internalServiceClass.getMethod("scan");
			cancelScanMethod = internalServiceClass.getMethod("cancelScan");
			tuneMethod = internalServiceClass.getMethod("tune", long.class);
			getCurrentChannelMethod = internalServiceClass.getMethod("getCurrentChannel");
			disableRDSMethod = internalServiceClass.getMethod("disableRDS");
			enableRDSMethod = internalServiceClass.getMethod("enableRDS");
			disableAFMethod = internalServiceClass.getMethod("disableAF");
			enableAFMethod = internalServiceClass.getMethod("enableAF");
			getLastScanResultMethod = internalServiceClass.getMethod("getLastScanResult");
			isHeadsetPluggedMethod = internalServiceClass.getMethod("isHeadsetPlugged");
			
			setListenerMethod = isHeadsetPluggedMethod = internalServiceClass.getMethod("setListener", listenerClass);
			removeListenerMethod = isHeadsetPluggedMethod = internalServiceClass.getMethod("removeListener", listenerClass);
		} catch (SecurityException e) {
			log("FMPlayerServiceWrapper - setUpInnerMethods - SecurityException: " + e.getMessage());
		} catch (NoSuchMethodException e) {
			log("FMPlayerServiceWrapper - setUpInnerMethods - NoSuchMethodException: " + e.getMessage());
		} catch (ClassNotFoundException e) {
			log("FMPlayerServiceWrapper - setUpInnerMethods - ClassNotFoundException: " + e.getMessage());
		}
	}
	
	public void on() throws FMPlayerException {
		try {
			onMethod.invoke(internalServiceObject);
		} catch (IllegalArgumentException e) {
			log("FMPlayerServiceWrapper - on - IllegalArgumentException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			log("FMPlayerServiceWrapper - on - IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			log("FMPlayerServiceWrapper - on - InvocationTargetException: " + e.getMessage());
			
			try {
				if (e.getTargetException().getClass().isAssignableFrom(Class.forName("com.samsung.media.fmradio.FMPlayerException"))) {
					Method getCodeMethod = e.getTargetException().getClass().getMethod("getCode");
					int code = (Integer) getCodeMethod.invoke(e.getTargetException());
					
					throw new FMPlayerException(code);
				}
			} catch (ClassNotFoundException e1) {
				log("FMPlayerServiceWrapper - on - ClassNotFoundException: " + e.getMessage());
			} catch (SecurityException e1) {
				log("FMPlayerServiceWrapper - on - SecurityException: " + e.getMessage());
			} catch (NoSuchMethodException e1) {
				log("FMPlayerServiceWrapper - on - NoSuchMethodException: " + e.getMessage());
			} catch (IllegalArgumentException e1) {
				log("FMPlayerServiceWrapper - on - IllegalArgumentException: " + e.getMessage());
			} catch (IllegalAccessException e1) {
				log("FMPlayerServiceWrapper - on - IllegalAccessException: " + e.getMessage());
			} catch (InvocationTargetException e1) {
				log("FMPlayerServiceWrapper - on - InvocationTargetException: " + e.getMessage());
			}
		}
	}
	
	public void off() {
		try {
			offMethod.invoke(internalServiceObject);
		} catch (IllegalArgumentException e) {
			log("FMPlayerServiceWrapper - off - IllegalArgumentException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			log("FMPlayerServiceWrapper - off - IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			log("FMPlayerServiceWrapper - off - InvocationTargetException: " + e.getMessage());
		}
	}
	
	public boolean isOn() {
		try {
			return ((Boolean) isOnMethod.invoke(internalServiceObject)).booleanValue();
		} catch (IllegalArgumentException e) {
			log("FMPlayerServiceWrapper - isOn - IllegalArgumentException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			log("FMPlayerServiceWrapper - isOn - IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			log("FMPlayerServiceWrapper - isOn - InvocationTargetException: " + e.getMessage());
		}
		
		return false;
	}
	
	public boolean isScanning() {		
		try {
			return ((Boolean) isScanningMethod.invoke(internalServiceObject)).booleanValue();
		} catch (IllegalArgumentException e) {
			log("FMPlayerServiceWrapper - isScanning - IllegalArgumentException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			log("FMPlayerServiceWrapper - isScanning - IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			log("FMPlayerServiceWrapper - isScanning - InvocationTargetException: " + e.getMessage());
		}
		
		return false;
	}
	
	public void setBand(int band) {		
		try {
			setBandMethod.invoke(internalServiceObject, band);
		} catch (IllegalArgumentException e) {
			log("FMPlayerServiceWrapper - setBand - IllegalArgumentException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			log("FMPlayerServiceWrapper - setBand - IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			log("FMPlayerServiceWrapper - setBand - InvocationTargetException: " + e.getMessage());
		}
	}
	
	public void setChannelSpacing(int cp) {
		try {
			setChannelSpacingMethod.invoke(internalServiceObject, cp);
		} catch (IllegalArgumentException e) {
			log("FMPlayerServiceWrapper - setChannelSpacing - IllegalArgumentException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			log("FMPlayerServiceWrapper - setChannelSpacing - IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			log("FMPlayerServiceWrapper - setChannelSpacing - InvocationTargetException: " + e.getMessage());
		}
	}
	
	public void setDEConstant(long de) {
		try {
			setDEConstantMethod.invoke(internalServiceObject, de);
		} catch (IllegalArgumentException e) {
			log("FMPlayerServiceWrapper - setDEConstant - IllegalArgumentException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			log("FMPlayerServiceWrapper - setDEConstant - IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			log("FMPlayerServiceWrapper - setDEConstant - InvocationTargetException: " + e.getMessage());
		}
	}
	
	public void setSpeakerOn(boolean mode) {
		try {
			setSpeakerOnMethod.invoke(internalServiceObject, mode);
		} catch (IllegalArgumentException e) {
			log("FMPlayerServiceWrapper - setSpeakerOn - IllegalArgumentException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			log("FMPlayerServiceWrapper - setSpeakerOn - IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			log("FMPlayerServiceWrapper - setSpeakerOn - InvocationTargetException: " + e.getMessage());
		}
	}
	
	public long seekUp() {
		try {
			return ((Long) seekUpMethod.invoke(internalServiceObject)).longValue();
		} catch (IllegalArgumentException e) {
			log("FMPlayerServiceWrapper - seekUp - IllegalArgumentException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			log("FMPlayerServiceWrapper - seekUp - IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			log("FMPlayerServiceWrapper - seekUp - InvocationTargetException: " + e.getMessage());
		}
		
		return -1;
	}
	
	public long seekDown() {
		try {
			return ((Long) seekDownMethod.invoke(internalServiceObject)).longValue();
		} catch (IllegalArgumentException e) {
			log("FMPlayerServiceWrapper - seekDown - IllegalArgumentException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			log("FMPlayerServiceWrapper - seekDown - IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			log("FMPlayerServiceWrapper - seekDown - InvocationTargetException: " + e.getMessage());
		}
		
		return -1;
	}
	
	public void scan() {
		try {
			scanMethod.invoke(internalServiceObject);
		} catch (IllegalArgumentException e) {
			log("FMPlayerServiceWrapper - scan - IllegalArgumentException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			log("FMPlayerServiceWrapper - scan - IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			log("FMPlayerServiceWrapper - scan - InvocationTargetException: " + e.getMessage());
		}
	}
	
	public void stopScan() {
		try {
			cancelScanMethod.invoke(internalServiceObject);
		} catch (IllegalArgumentException e) {
			log("FMPlayerServiceWrapper - stopScan - IllegalArgumentException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			log("FMPlayerServiceWrapper - stopScan - IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			log("FMPlayerServiceWrapper - stopScan - InvocationTargetException: " + e.getMessage());
		}
	}
	
	public void tune(long freq) {
		try {
			tuneMethod.invoke(internalServiceObject, freq);
		} catch (IllegalArgumentException e) {
			log("FMPlayerServiceWrapper - tune - IllegalArgumentException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			log("FMPlayerServiceWrapper - tune - IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			log("FMPlayerServiceWrapper - tune - InvocationTargetException: " + e.getMessage());
		}
	}
	
	public long[] getLastScanResult() {
		try {
			return (long[]) getLastScanResultMethod.invoke(internalServiceObject);
		} catch (IllegalArgumentException e) {
			log("FMPlayerServiceWrapper - getLastScanResult - IllegalArgumentException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			log("FMPlayerServiceWrapper - getLastScanResult - IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			log("FMPlayerServiceWrapper - getLastScanResult - InvocationTargetException: " + e.getMessage());
		}
		
		return null;
	}
	
	public long getCurrentChannel() {
		try {
			return ((Long) getCurrentChannelMethod.invoke(internalServiceObject)).longValue();
		} catch (IllegalArgumentException e) {
			log("FMPlayerServiceWrapper - getCurrentChannel - IllegalArgumentException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			log("FMPlayerServiceWrapper - getCurrentChannel - IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			log("FMPlayerServiceWrapper - getCurrentChannel - InvocationTargetException: " + e.getMessage());
		}
		
		return -1;
	}
	
	public void disableRDS() {
		try {
			disableRDSMethod.invoke(internalServiceObject);
		} catch (IllegalArgumentException e) {
			log("FMPlayerServiceWrapper - disableRDS - IllegalArgumentException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			log("FMPlayerServiceWrapper - disableRDS - IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			log("FMPlayerServiceWrapper - disableRDS - InvocationTargetException: " + e.getMessage());
		}
	}
	
	public void enableRDS() {
		try {
			enableRDSMethod.invoke(internalServiceObject);
		} catch (IllegalArgumentException e) {
			log("FMPlayerServiceWrapper - enableRDS - IllegalArgumentException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			log("FMPlayerServiceWrapper - enableRDS - IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			log("FMPlayerServiceWrapper - enableRDS - InvocationTargetException: " + e.getMessage());
		}
	}
	
	public void disableAF() {
		try {
			disableAFMethod.invoke(internalServiceObject);
		} catch (IllegalArgumentException e) {
			log("FMPlayerServiceWrapper - disableAF - IllegalArgumentException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			log("FMPlayerServiceWrapper - disableAF - IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			log("FMPlayerServiceWrapper - disableAF - InvocationTargetException: " + e.getMessage());
		}
	}
	
	public void enableAF() {
		try {
			enableAFMethod.invoke(internalServiceObject);
		} catch (IllegalArgumentException e) {
			log("FMPlayerServiceWrapper - enableAF - IllegalArgumentException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			log("FMPlayerServiceWrapper - enableAF - IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			log("FMPlayerServiceWrapper - enableAF - InvocationTargetException: " + e.getMessage());
		}
	}
	
	public boolean isHeadsetPlugged() {
		try {
			return ((Boolean) isHeadsetPluggedMethod.invoke(internalServiceObject)).booleanValue();
		} catch (IllegalArgumentException e) {
			log("FMPlayerServiceWrapper - isHeadsetPlugged - IllegalArgumentException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			log("FMPlayerServiceWrapper - isHeadsetPlugged - IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			log("FMPlayerServiceWrapper - isHeadsetPlugged - InvocationTargetException: " + e.getMessage());
		}
		
		return false;
	}
	
	public void setListener(IFMRadioNotification listener) {
		Object fmListener;
		
		try {
			fmListener = listenerClass.newInstance();
			
			Field mHandlerField = fmListener.getClass().getDeclaredField("mHandler");
			mHandlerField.setAccessible(true);
			
			FMRadioNotificationHandler fmHandler = new FMRadioNotificationHandler(listener);
			mHandlerField.set(fmListener, fmHandler);
			
			setListenerMethod.invoke(internalServiceObject, fmListener);
			
			listenersMap.put(listener, fmListener);
		} catch (IllegalAccessException e) {
			log("FMPlayerServiceWrapper - setListener - IllegalAccessException: " + e.getMessage());
		} catch (InstantiationException e) {
			log("FMPlayerServiceWrapper - setListener - InstantiationException: " + e.getMessage());
		} catch (SecurityException e) {
			log("FMPlayerServiceWrapper - setListener - SecurityException: " + e.getMessage());
		} catch (NoSuchFieldException e) {
			log("FMPlayerServiceWrapper - setListener - NoSuchFieldException: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			log("FMPlayerServiceWrapper - setListener - IllegalArgumentException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			log("FMPlayerServiceWrapper - setListener - InvocationTargetException: " + e.getMessage());
		}
	}
	
	public void removeListener(IFMRadioNotification listener) {
		Object fmListener = listenersMap.remove(listener);
		
		if (fmListener != null) {
			try {
				removeListenerMethod.invoke(internalServiceObject, fmListener);
			} catch (IllegalArgumentException e) {
				log("FMPlayerServiceWrapper - removeListener - IllegalArgumentException: " + e.getMessage());
			} catch (IllegalAccessException e) {
				log("FMPlayerServiceWrapper - removeListener - IllegalAccessException: " + e.getMessage());
			} catch (InvocationTargetException e) {
				log("FMPlayerServiceWrapper - removeListener - InvocationTargetException: " + e.getMessage());
			}
		}
	}
	
	private void log(String msg) {
		Log.d(TAG, msg);
	}
}
