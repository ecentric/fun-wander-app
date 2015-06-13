package com.funwander;

import java.io.IOException;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.funwander.marker.MarkerController;
import com.funwander.marker.MarkerEventListener;
import com.funwander.path.Path;
import com.funwander.path.PathController;
import com.funwander.shared.exch.RectOfPoints;
import com.funwander.util.AppException;
import com.funwander.util.Configuration;
import com.funwander.util.ExceptionHandler;
import com.funwander.util.Helper;
import com.funwander.util.Logger;
import com.funwander.util.LoggerFactory;
import com.funwander.util.RemoteServer;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

/**
 * Main activity with map
 * 
 * @author nickolas
 * 
 */
public class MapActivity extends FragmentActivity implements
		MarkerEventListener {

	private enum MAState {
		MAKE_OWN_PATH, WALK
	}

	private static Configuration config = Configuration.getInstance();
	private Logger logger = LoggerFactory.getLogger(MapActivity.class);

	private GoogleMap map;
	private MarkerController markerController;
	private PathController pathController;

	/**
	 * state of activity
	 */
	private MAState state;
	private ProgressDialog progressDialog;

	private Path walkPath[];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);

		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(Thread
				.getDefaultUncaughtExceptionHandler()));
		initMap();
		downloadRoadMap();

		markerController = new MarkerController(map, getResources());
		
		// adds markers to map
		new Thread(new Runnable() {
			public void run() {
				try {
					final RectOfPoints signs = markerController.requestSigns();
					runOnUiThread(new Runnable() {
						public void run() {
							markerController.handleReceivedSigns(signs);
						}
					});
				} catch (AppException e) {
					logger.exception(e);
					runOnUiThread(new Runnable() {
						public void run() {
							showToast("Sorry, but problems with internet has occurred.");
						}
					});
				}
			}
		}).start();

		markerController.setEventListener(this);
		pathController = PathController.getInstance(getBaseContext());
		
		map.setOnCameraChangeListener(new OnCameraChangeListener() {
			public void onCameraChange(CameraPosition position) {
				cameraChangeHandler(position);
			}
		});

		setState(MAState.MAKE_OWN_PATH);
	}

	/**
	 * Init google map view
	 */
	private void initMap() {
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		map = mapFragment.getMap();
		if (null == map) {
			logger.error("GetMap() returned null");
			this.finish();
		}
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		map.moveCamera(CameraUpdateFactory
				.newCameraPosition(new CameraPosition(
						new LatLng(50, 36.229167), 14, 0, 0)));
	}
	
	private void downloadRoadMap() {
		progressDialog = ProgressDialog.show(this, "Getting roads",
				"Please wait...", true);
		new Thread(new Runnable() {
			public void run() {
				try {
					RemoteServer.downloadRoadMap();
					runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
						}
					});
				} catch (IOException e) {
					runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							showToast("Sorry, but problems with internet has occurred.");
						}
					});					
					e.printStackTrace();
				}
			}
		}).start();		
	}

	/**
	 * Make view visible or not, depends on current view state
	 * 
	 * @param view
	 */
	private void toggleView(View view) {
		switch (view.getVisibility()) {
		case LinearLayout.VISIBLE:
			view.setVisibility(View.GONE);
			break;
		case LinearLayout.INVISIBLE:
		default:
			view.setVisibility(View.VISIBLE);
			break;
		}
	}

	/**
	 * Change state of Activity. See MAState enum Change TextViews text,
	 * visibility of some views
	 * 
	 * @param state
	 */
	private void setState(MAState state) {
		TextView tprText;
		View view;
		switch (state) {
		case MAKE_OWN_PATH:
			markerController.deactivateAll();
			markerController.canToggle(true);

			if (walkPath != null) {
				for (int i=0;i< walkPath.length;i++)
					walkPath[i].getPolyline().remove();
				walkPath = null;
			}

			tprText = (TextView) findViewById(R.id.tp_rtext);
			tprText.setText("Please select point");

			this.state = MAState.MAKE_OWN_PATH;
			break;
		case WALK:
			tprText = (TextView) findViewById(R.id.tp_rtext);
			tprText.setText("Happy walking!");

			view = (View) findViewById(R.id.make_path_button);
			view.setVisibility(View.GONE);

			markerController.canToggle(false);

			this.state = MAState.WALK;
			break;
		}
	}
	
	/**
	 * 
	 * @param position
	 */
	private void cameraChangeHandler(CameraPosition position) {
		markerController.cameraChanged(position);
	}

	/**
	 * View "menu" button click listener
	 * 
	 * @param view
	 */
	public void menuClickHandler(View view) {
		long startTime = System.nanoTime();
		
		Helper.zk(6);
	    
	    long endTime = System.nanoTime();
	    System.out.println("Total execution time: " + (endTime-startTime) + "nano s");
		View smallMenu = findViewById(R.id.small_menu);
		toggleView(smallMenu);
	}

	/**
	 * Small "menu" buttons click listener
	 * 
	 * @param view
	 */
	public void smallMenuClickHandler(View view) {
		switch (view.getId()) {
		case R.id.make_own:
			setState(MAState.MAKE_OWN_PATH);
		default:
			break;
		}
		View smallMenu = findViewById(R.id.small_menu);
		toggleView(smallMenu);
	}

	/**
	 * Make path button click listener
	 * 
	 * @param view
	 */
	public void makePathClickHandler(View view) {
		progressDialog = ProgressDialog.show(this, "Making path",
				"Please wait...", true);

		new Thread(new Runnable() {
			public void run() {
				try {
					walkPath = pathController.makeManualPath(markerController
							.getActiveSMarkerPositions());

					runOnUiThread(new Runnable() {
						public void run() {
							for (int i = 0; i < walkPath.length; i++){
							walkPath[i].setPolyline(map.addPolyline(walkPath[i]
									.getPolylineOptions()));}
							
							progressDialog.dismiss();
							setState(MAState.WALK);
						}
					});
				} catch (AppException e) {
					runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							showToast("Sorry, can't receive data from internet. "
									+ "Please check internet connection.");
						}
					});
				}
			}
		}).start();

	}

	/**
	 * Set toast parameters and show it
	 * 
	 * @param text
	 */
	private void showToast(String text) {
		int px = getResources()
				.getDimensionPixelSize(R.dimen.top_pannel_height) + 10;

		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.toast,
				(ViewGroup) findViewById(R.id.toast));

		((TextView) layout.findViewById(R.id.toast_text)).setText(text);
		Toast toast = new Toast(getApplicationContext());
		toast.setView(layout);
		toast.setGravity(Gravity.TOP, 0, px);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.show();
	}

	/**
	 * MarkerController event listener
	 */
	@Override
	public void onMarkerSelected() {
		TextView tprText = (TextView) findViewById(R.id.tp_rtext);
		tprText.setText("added " + markerController.getSActiveMarkersCount()
				+ " points");
		if (markerController.getSActiveMarkersCount() == 2) {
			ImageButton makePath = (ImageButton) findViewById(R.id.make_path_button);
			makePath.setVisibility(ImageButton.VISIBLE);
			// TODO:
			// makePath.setEnabled(true);
		} else if (markerController.getSActiveMarkersCount() < 2) {
			ImageButton makePath = (ImageButton) findViewById(R.id.make_path_button);
			makePath.setVisibility(ImageButton.GONE);
		}
	}

	/**
	 * if debug mode function unlock android screen
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if (config.getBool("app.debug", false)) {
			KeyguardManager mKeyGuardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
			KeyguardLock mLock = mKeyGuardManager
					.newKeyguardLock("activity_classname");
			mLock.disableKeyguard();
		}
	}
}
