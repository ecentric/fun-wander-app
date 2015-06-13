package com.funwander.path;

import java.util.ArrayList;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Container of points that represent path on map
 * @author nickolas
 */
public class Path {
	
	private ArrayList<LatLng> points;
	
	/**
	 * Map polyline
	 */
	private Polyline polyline = null;
	
	public Path() {
		this.points = new ArrayList<LatLng>();
	}
	
	public Path(ArrayList<LatLng> points) {
		this.points = points;
	}

	public PolylineOptions getPolylineOptions() {
		return new PolylineOptions().addAll(points).color(Color.parseColor("#ffb64b4e")).width(6);
	}
	
	public Polyline getPolyline() { 
		return this.polyline;
	}
	
	public void setPolyline(Polyline p) {
		this.polyline = p;
	}
	
	public void addPoint(double lan, double lon) {
		points.add(new LatLng(lan, lon));
	}

}
