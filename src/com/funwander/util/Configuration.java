package com.funwander.util;

import android.os.Environment;

import com.funwander.shared.util.BaseConfiguration;

/**
 * Contain application options
 * @author nickolas
 *
 */
public class Configuration extends BaseConfiguration{
	
	private static Configuration instance;
	private final static boolean debug = false; 
	
	public static Configuration getInstance() {
		if (instance == null) {
			instance = new Configuration();
		}
		return instance;
	}
	
	private Configuration() {
		super();
	}
	
	/**
	 * Sets application options
	 */
	protected void initOptions() {
		if (debug) {
			put("server.domain","http://37.57.58.78:8000/bfw/");
		} else {
			put("server.domain","http://5.61.32.57:8080/bfw/");
		}
		put("app.debug", String.valueOf(debug));

		put("server.routes_url", get("server.domain") + "routes/");
		put("server.points_url", get("server.domain") + "points/");
		put("server.files_url", get("server.domain") + "points/");
		put("server.timeout", "7000");
		
		put("gh.files", "edges;geometry;nodes;spatialNIndex");
		
		put("storage.path", Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/funWander/maps/");
	}

}


