package com.dartslive.stamp;

import android.app.Activity;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.stamp12cm.echosdk.EchossManager;

public class MainActivity extends Activity  implements
	LocationListener,
	GooglePlayServicesClient.ConnectionCallbacks,
	GooglePlayServicesClient.OnConnectionFailedListener{
	
	static MainActivity _this = null;
	
	MainView _mainView = null;

	EditText _editLat = null;
	EditText _editLng = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING);
		
		_editLat = (EditText)findViewById(R.id.editLat);
		_editLng = (EditText)findViewById(R.id.editLng);
		_mainView = (MainView)findViewById(R.id.mainView);
		
		// set metrics : set display info for echoss sdk
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		_mainView.init(metric);
		
		//========================================================================================//
		//=========================      Handle MultiTouch	         =============================//
		// マルチタッチは二つ以上の入力が入ってきた場合を示します。		 										  //
		// StampBaseView 中にボタンなどのような input controlがある状態でスタンプ							  //
		// を押した時、ボタンが押されるなどの input control の誤動作することを防ぐためのものです。						  //
		// 画面の中でマルチタッチを許容するためには該当の値を trueに設定してください							  //
		//========================================================================================//
		_mainView.enableMultiTouch(false);
		
		//========================================================================================//
		//=========================       Apply Stamping Effect    	 =============================//
		// スタンプが押された時、スタンプ押印アニメーションが作動します。											  //	
		//========================================================================================//
		final ToggleButton stampTB = (ToggleButton) findViewById(R.id.stampButton);
		stampTB.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				_mainView.applyStampingEffect(stampTB.isChecked());
			}
		});
		
		//========================================================================================//
		//=========================    Activate Multitouch Zoom      =============================//
		// SDK上でズームイン / アウトと様に二点同時入力動作を無視させる時に使います。 							  //
		//========================================================================================//
		_mainView.ignoreMultitouchZoomAction(false);
		
		_editLat = (EditText)findViewById(R.id.editLat);
		_editLng = (EditText)findViewById(R.id.editLng);
		
		findViewById(R.id.btnInit).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//========================================================================================//
				//=========================          Initilize SDK          ==============================//
				// 		ネットワーク接続エラーで初期化されない場合、決まった時間ごとに自動で再接続をします。				  //
				//========================================================================================//
				if (AppConst.SVC_CD.equals("")) {
					Toast.makeText(MainActivity.this, "Please input the service code in source code named AppConst.java.", Toast.LENGTH_SHORT).show();
					return;
				}
				EchossManager.initEchossLibAutoRetry(MainActivity.this, AppConst.SVC_CD, AppConst.isTestServer, AppConst.isDev, new EchossManager.OnEchossManagerListener() {
					
					@Override
					public void OnInitError(String errorCode, String errorMsg) {
						Toast.makeText(MainActivity.this, "Failed EchosSDK Initialization!\n"+"["+errorCode + "] " + errorMsg, Toast.LENGTH_SHORT).show();
					}
					
					@Override
					public void OnInit(String crdlUseTyp, String locUseTyp) {
						Toast.makeText(MainActivity.this, "Success EchosSDK Initialization!", Toast.LENGTH_SHORT).show();
						successInitEchossLib = true;
					}
				}, 3000);				
			}
		});
		
    	this.initLocation();
	}

	//========================================================================================//
	//=========================       Handle Location Info      ==============================//
	//========================================================================================//	
    boolean mUpdatesRequested = false;
    // A request to connect to Location Services
    private LocationRequest mLocationRequest;

    // Stores the current instantiation of the location client in this object
    private LocationClient mLocationClient;
    
    private void initLocation() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);
        mUpdatesRequested = true;
        mLocationClient = new LocationClient(this, this, this);
    }	

	boolean successInitEchossLib = false;    
	@Override
	protected void onPause() {
		if ( ! successInitEchossLib)
			EchossManager.stopEchossLibAutoRetry();
		super.onPause();
	}
		
	@Override
	protected void onResume() {
		if (! successInitEchossLib) {
			EchossManager.initEchossLibAutoRetry(MainActivity.this, AppConst.SVC_CD, AppConst.isTestServer, AppConst.isDev, new EchossManager.OnEchossManagerListener() {
				
				@Override
				public void OnInitError(String errorCode, String errorMsg) {
					Toast.makeText(MainActivity.this, "Failed EchosSDK Initialization!\n"+"["+errorCode + "] " + errorMsg, Toast.LENGTH_SHORT).show();
				}
				
				@Override
				public void OnInit(String crdlUseTyp, String locUseTyp) {
					Toast.makeText(MainActivity.this, "Success EchosSDK Initialization!", Toast.LENGTH_SHORT).show();
					successInitEchossLib = true;
				}
			}, 3000);
		}
		super.onResume();
	}
	
	LocationListener loclistener = null;

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            // If no resolution is available, display a dialog to the user with the error.
        }		
	}

	@Override
	public void onConnected(Bundle arg0) {
        if (mUpdatesRequested) {
            startPeriodicUpdates();
        }
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub		
	}

	@Override
	public void onLocationChanged(Location location) {
		// Register current location to EchossManager Class
		EchossManager.setLoc(location);
		
		_editLat.setText(String.valueOf(location.getLatitude()));
		_editLng.setText(String.valueOf(location.getLongitude()));
	}

    private void startPeriodicUpdates() {

        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }
	
    private void stopPeriodicUpdates() {
        mLocationClient.removeLocationUpdates(this);
    }

    @Override
    public void onStop() {
        if (mLocationClient.isConnected()) {
            stopPeriodicUpdates();
        }
        mLocationClient.disconnect();
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        mLocationClient.connect();
    }
}