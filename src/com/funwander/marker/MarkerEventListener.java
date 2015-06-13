package com.funwander.marker;

/**
 * If class must have possibilities of getting events from MarkerController it
 * must implement this interface and register itself in MarkerController
 * 
 * @author nickolas
 * 
 */
public interface MarkerEventListener {

	public enum EventType {
		MARKER_SELECTED
	}

	public void onMarkerSelected();

}
