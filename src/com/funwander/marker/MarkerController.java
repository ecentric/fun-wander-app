package com.funwander.marker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.res.Resources;

import com.funwander.MapActivity;
import com.funwander.marker.MarkerEventListener.EventType;
import com.funwander.shared.exch.RectOfPoints;
import com.funwander.shared.exch.SignPoint;
import com.funwander.shared.exch.SignPoint.SignType;
import com.funwander.util.AppException;
import com.funwander.util.Configuration;
import com.funwander.util.Logger;
import com.funwander.util.LoggerFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;

/**
 * Class for containing map markers. It control state of markers, can change
 * their state. Class provide facilities of getting points from server and
 * addition it to map.
 * 
 * @author nickolas
 */
public class MarkerController implements OnMarkerClickListener {

	private Logger logger = LoggerFactory.getLogger(MapActivity.class);
	private static Configuration config = Configuration.getInstance();

	private GoogleMap map;
	private Resources resources;
	private int sActiveMarkersAmount;

	private HashMap<LatLng, SMarker> sMarkers;
	private RectOfPoints setOfPoints;

	private MarkerEventListener eventListener;
	private boolean canToggle;
	
	private float zoom = 0;

	public MarkerController(GoogleMap map, Resources res) {
		this.map = map;
		this.resources = res;
		this.sActiveMarkersAmount = 0;
		this.canToggle = true;

		sMarkers = new HashMap<LatLng, SMarker>();

		map.setOnMarkerClickListener(this);
	}

	/**
	 * Get "sign" points Warn: must be called from not activity thread
	 * 
	 * @return
	 * @throws AppException
	 */
	public RectOfPoints requestSigns() throws AppException {

		// TODO set points
		RectOfPoints rectOfPoints = httpGetMarkers(new LatLng(50.039061,
				36.152573), new LatLng(49.913209, 36.378479));
		return rectOfPoints;

	}

	/**
	 * Handle marker clicks
	 */
	@Override
	public boolean onMarkerClick(Marker marker) {
		if (canToggle) {
			SMarker sMarker = sMarkers.get(marker.getPosition());

			if (sMarker == null) {
				logger.error("marker " + marker.getPosition()
						+ "is not in table ");
			} else {
				toggleMarker(sMarker);
			}
		}
		return true;
	}

	/**
	 * Active or not
	 */
	public void toggleMarker(SMarker sMarker) {
		try {
			Thread.sleep(60, 0);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SMarker newMarker;
		newMarker = addMarker(sMarker.getType(), sMarker.getPosition(),
				sMarker.getRate(), !sMarker.isActive());
		sMarker.getMarker().remove();
		sMarkers.put(newMarker.getPosition(), newMarker);
		if (sMarker.isActive())
			sActiveMarkersAmount--;
		notifyListener(EventType.MARKER_SELECTED);
	}

	/**
	 * Add market to Map
	 * 
	 * @param type
	 * @param pos
	 * @param rate
	 * @param active
	 * @return
	 */
	private SMarker addMarker(SignType type, LatLng pos, double rate,
			boolean active) {
		if (active == true)
			sActiveMarkersAmount++;

		SMarker sMarker = new SMarker(type, pos, rate, active);
		Marker marker = map.addMarker(sMarker.getOptions(resources));
		sMarker.setMarker(marker);
		sMarker.setPosition(marker.getPosition());
		this.sMarkers.put(sMarker.getPosition(), sMarker);
		return sMarker;
	}

	public void setEventListener(MarkerEventListener listener) {
		this.eventListener = listener;
	}
	
	public void cameraChanged(CameraPosition position) {
		
		
		double border = 0;
		float zoom = position.zoom;
		
		if (this.zoom != position.zoom) {
			this.zoom = position.zoom;
			if (zoom > 0 && zoom < 9) {
				border = 1;
			} else if (zoom > 9 && zoom < 10) {
				border = 0.95;
			} else if (zoom > 10 && zoom < 11) {
				border = 0.8;
			} else if (zoom > 11 && zoom < 12) {
				border = 0.7;
			} else if (zoom > 12 && zoom < 13) {
				border = 0.5;
			} else if (zoom > 13) {
				border = 0;
			}
			for (SMarker sMarker : sMarkers.values()) {
				if (sMarker.getRate() > border || sMarker.isActive())
					sMarker.getMarker().setVisible(true);
				else
					sMarker.getMarker().setVisible(false);
			}
		}		
	}

	public int getSActiveMarkersCount() {
		return this.sActiveMarkersAmount;
	}

	/**
	 * Notify all handlers
	 * 
	 * @param type
	 */
	private void notifyListener(EventType type) {
		if (eventListener != null) {
			switch (type) {
			case MARKER_SELECTED:
				eventListener.onMarkerSelected();
				break;
			}
		}
	}

	/**
	 * @return list of active markers
	 */
	public ArrayList<LatLng> getActiveSMarkerPositions() {
		ArrayList<LatLng> list = new ArrayList<LatLng>();
		for (SMarker sMarker : this.sMarkers.values()) {
			if (sMarker.isActive()) {
				list.add(sMarker.getPosition());
			}
		}
		return list;
	}

	/**
	 * deactivate all markers i.e make their inactive
	 */
	public void deactivateAll() {
		for (SMarker sMarker : this.sMarkers.values()) {
			if (sMarker.isActive()) {
				toggleMarker(sMarker);
			}
		}
	}

	/**
	 * Request "sign" points from server
	 * 
	 * @param ltp
	 *            - left top point
	 * @param rbp
	 *            - right bottom point
	 * @return
	 * @throws AppException
	 */
	private RectOfPoints httpGetMarkers(final LatLng ltp, final LatLng rbp)
			throws AppException {

		String uri = config.get("server.points_url") + "?";

		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair("ltp_lat", String
				.valueOf(ltp.latitude)));
		params.add(new BasicNameValuePair("ltp_lon", String
				.valueOf(ltp.longitude)));
		params.add(new BasicNameValuePair("rbp_lat", String
				.valueOf(rbp.latitude)));
		params.add(new BasicNameValuePair("rbp_lon", String
				.valueOf(rbp.longitude)));
		uri = uri.concat(URLEncodedUtils.format(params, "UTF-8"));

		HttpParams httpParams = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used.
		HttpConnectionParams.setConnectionTimeout(httpParams,
				config.getInt("server.timeout", 5000));
		// Set the default socket timeout (SO_TIMEOUT)
		// in milliseconds which is the timeout for waiting for data.
		HttpConnectionParams.setSoTimeout(httpParams,
				config.getInt("server.timeout", 7000));

		logger.debug(uri);

		HttpClient client = new DefaultHttpClient(httpParams);
		HttpGet request = new HttpGet(uri);

		try {
			HttpResponse response = client.execute(request);

			StringBuilder jsonResp = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			String line;
			while ((line = reader.readLine()) != null) {
				jsonResp.append(line);
			}

			Gson gson = new Gson();
			RectOfPoints rectOfP = gson.fromJson(jsonResp.toString(),
					RectOfPoints.class);

			return rectOfP;
		} catch (ClientProtocolException e) {
			throw new AppException(e);
		} catch (IOException e) {
			throw new AppException(e);
		}
	}

	/**
	 * Add to map received points Warn: must be called from main thread
	 * 
	 * @param rectOfp
	 */
	public void handleReceivedSigns(RectOfPoints rectOfp) {
		setOfPoints = rectOfp;
		SignPoint[] points = rectOfp.getPoints();
		for (int index = 0; index < points.length; index++) {
			if (points[index].getType() != SignType.BAR) {
				addMarker(points[index].getType(),
						new LatLng(points[index].getLat(), points[index].getLon()),
						Math.random(), false);
			}
		}
	}

	/**
	 * True if markers can be toggled
	 * 
	 * @param can
	 */
	public void canToggle(boolean can) {
		this.canToggle = can;
	}

}
