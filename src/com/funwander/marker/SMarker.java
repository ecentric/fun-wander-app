package com.funwander.marker;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.funwander.R;
import com.funwander.shared.exch.SignPoint.SignType;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Represent "smart" (with type and rating) marker
 * 
 * @author nickolas
 * 
 */
public class SMarker implements Comparable<SMarker> {

	static final int TILE_COUNT = 5;

	private LatLng position;
	private double rating;
	private SignType type;

	/**
	 * True if pressed
	 */
	private boolean active;

	/**
	 * Map marker
	 */
	private Marker marker;

	/**
	 * 
	 * @param type
	 * @param pos
	 * @param rate
	 *            - from 0 to 1
	 * @param active
	 */
	public SMarker(SignType type, LatLng pos, double rate, boolean active) {
		this.position = pos;
		this.rating = rate;
		this.type = type;
		this.active = active;
	}

	/**
	 * 
	 * @param type
	 * @param pos
	 * @param rate
	 *            - from 0 to 1
	 */
	public SMarker(SignType type, LatLng pos, double rate) {
		this(type, pos, rate, false);
	}

	/**
	 * Return options for creation marker on map
	 * 
	 * @param res
	 * @return
	 */
	public MarkerOptions getOptions(Resources res) {
		return new MarkerOptions().position(this.position).icon(getBitmap(res))
				.anchor((float) 0.5, (float) 0.5);
	}

	@Override
	public int hashCode() {
		return (int) (position.latitude + position.longitude) * 1000000;
	}

	@Override
	public boolean equals(Object o) {
		SMarker marker = (SMarker) o;
		return this.position.equals(marker.getPosition());
	}

	/**
	 * Calculates Bitmap for marker based on it rating and type
	 * TODO: create one bitmap 
	 * @param res
	 * @return
	 */
	private BitmapDescriptor getBitmap(Resources res) {
		Bitmap icon;
		switch (this.type) {
		case IPLACE:
			icon = BitmapFactory.decodeResource(res, R.drawable.iplace);
			break;
		case THEATER:
			icon = BitmapFactory.decodeResource(res, R.drawable.theater);
			break;
		case MUSEUM:
			icon = BitmapFactory.decodeResource(res, R.drawable.museum);
			break;
		default:
			throw new IllegalStateException("Unsupported sign type");
		}
		int x = 0;
		if (this.active == true) {
			x = (TILE_COUNT - 1) * icon.getWidth() / TILE_COUNT;
		} else {
			x = (int) Math.round(this.rating * (TILE_COUNT - 2))
					* icon.getWidth() / TILE_COUNT;
			System.err.println(Math.round(this.rating * (TILE_COUNT - 2)));
		}
		icon = Bitmap.createBitmap(icon, x, 0, icon.getWidth() / TILE_COUNT,
				icon.getHeight());
		return BitmapDescriptorFactory.fromBitmap(icon);
	}

	public SignType getType() {
		return this.type;
	}

	public LatLng getPosition() {
		return this.position;
	}

	public boolean isActive() {
		return this.active;
	}

	public double getRate() {
		return this.rating;
	}

	public void setPosition(LatLng position) {
		this.position = position;
	}

	public Marker getMarker() {
		return this.marker;
	}

	public void setMarker(Marker marker) {
		this.marker = marker;
	}

	@Override
	public int compareTo(SMarker another) {
		if (this.rating > another.rating) return -1;
		else if (this.rating < another.rating) return 1;
		else return 0;
	}
}
