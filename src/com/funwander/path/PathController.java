package com.funwander.path;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

import android.content.Context;

import com.funwander.shared.exch.HttpRoute;
import com.funwander.util.AppException;
import com.funwander.util.Configuration;
import com.funwander.util.Logger;
import com.funwander.util.LoggerFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.util.PointList;

/**
 * Class provide facilities of making new path by user points or by user
 * preferences
 * 
 * @author Nickolas Shishov
 * 
 */
public class PathController {

	private static PathController instance;

	private Logger logger = LoggerFactory.getLogger(PathController.class);
	private static Configuration config = Configuration.getInstance();
	private Context context;
	private GraphHopper hopper;

	private PathController() {
		hopper = new GraphHopper().forMobile();
		hopper.load(config.get("storage.path") + "UA");
	}

	public static PathController getInstance(Context context) {
		if (instance == null)
			instance = new PathController();
		instance.context = context;
		return instance;
	}

	/**
	 * Make path between user points
	 * 
	 * @param points
	 * @throws PathControllerException
	 */
	public Path[] makeManualPath(ArrayList<LatLng> points) throws AppException {
		return makePath(points);
	}

	/**
	 * Calculate and draw on map path between base points
	 * 
	 * @param points
	 *            - array of base points
	 * @throws PathControllerException
	 */
	private Path[] makePath(ArrayList<LatLng> points) throws AppException {

		long to = System.nanoTime();
		
		int pointCount = points.size();
		
		ArrayList<Path> path = new ArrayList<Path>();
		
		double[][] dMatrix = new double[pointCount][pointCount];
		PointList[][] routes = new PointList[pointCount][pointCount];

		for (int i = 0; i < points.size(); i++) {
			for (int j = 0; j < i; j++) {
				GHRequest req = new GHRequest(points.get(i).latitude,
						points.get(i).longitude, points.get(j).latitude,
						points.get(j).longitude).algorithm("astar");
						//.putHint("douglas.minprecision", 1);
				
				long t = System.nanoTime();		
				GHResponse resp = hopper.route(req);
				
				
				routes[i][j] = resp.points();
				dMatrix[i][j] = resp.distance();
				System.out.println("dijkstrabi - " + (System.nanoTime()-t));
			}
		}
		long t = System.nanoTime();
		int[] listOfPoints = makeZkRoute(dMatrix);
		System.out.println("zk - " + (System.nanoTime()-t));
		PointList route;
		for (int i = 0; i < pointCount - 1; i++) {
			int start = listOfPoints[i];
			int end = listOfPoints[i+1];
			if (end > start) {
				int tmp = start;
				start = end;
				end = tmp;
			}
			route = routes[start][end];
			Path spath = new Path();
			for (int j = 0; j<route.size(); j++) {
				spath.addPoint(route.latitude(j), route.longitude(j));
			}
			path.add(spath);
		}
			
		System.out.println("total - " + (System.nanoTime()-to));
		return path.toArray(new Path[path.size()]);
	}

	private int[] makeZkRoute(double[][] dMatrix) {
		int countOfPoints = dMatrix.length;
		int[] listOfPoints = new int[countOfPoints];
		boolean [] visited = new boolean[countOfPoints];
		for (int i = 0; i< countOfPoints; i++) visited[i] = false;
		
		int currentPoint = 0;
		visited[currentPoint] = true;
		listOfPoints[0] = currentPoint;
		
		for (int i = 1; i < countOfPoints; i++) {
			
			int bestPoint = 0;
			double bestDistance = Double.MAX_VALUE;
			for (int j = 0; j < countOfPoints; j++) {
				if (j == currentPoint) continue;
				int start = currentPoint;
				int end = j;
				if (end > start) {
					int tmp = start;
					start = end;
					end = tmp;
				}
				if (!visited[j] && bestDistance > dMatrix[start][end]) {
					bestDistance = dMatrix[start][end];
					bestPoint = j;
				}
			}
				
			listOfPoints[i] = bestPoint;
			visited[bestPoint] = true;
			currentPoint = bestPoint;
		}
		
		
		return listOfPoints;
	}

	/**
	 * Make request to server for making path
	 * 
	 * @param sp
	 *            - start point
	 * @param ep
	 *            - end point
	 * @return HttpRoute
	 * @throws AppException
	 */
	private HttpRoute httpGetRoute(LatLng sp, LatLng ep) throws AppException {

		String uri = config.get("server.routes_url") + "?";

		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair("sp_lat", String.valueOf(sp.latitude)));
		params.add(new BasicNameValuePair("sp_lon", String
				.valueOf(sp.longitude)));
		params.add(new BasicNameValuePair("ep_lat", String.valueOf(ep.latitude)));
		params.add(new BasicNameValuePair("ep_lon", String
				.valueOf(ep.longitude)));
		uri = uri.concat(URLEncodedUtils.format(params, "UTF-8"));

		HttpParams httpParams = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used.
		HttpConnectionParams.setConnectionTimeout(httpParams,
				config.getInt("server.timeout", 5000));
		// Set the default socket timeout (SO_TIMEOUT)
		// in milliseconds which is the timeout for waiting for data.
		HttpConnectionParams.setSoTimeout(httpParams,
				config.getInt("server.timeout", 5000));

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
			// TODO
			Gson gson = new Gson();
			HttpRoute route = gson.fromJson(jsonResp.toString(),
					HttpRoute.class);
			logger.debug(String.valueOf(route.getDistance()));

			for (int i = 0; i < route.getSize(); i++) {
				logger.debug(String.format("lat %f, lon %f", route.getPoint(i)
						.getLat(), route.getPoint(i).getLon()));
			}

			return route;
		} catch (ClientProtocolException e) {
			logger.exception(e);
			throw new AppException("http get route");
		} catch (IOException e) {
			logger.exception(e);
			throw new AppException("http get route");
		}
	}
}
