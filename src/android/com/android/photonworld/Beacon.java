package com.android.photonworld;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.LocationManager;


import com.gimbal.android.Gimbal;
import com.gimbal.android.PlaceEventListener;
import com.gimbal.android.PlaceManager;
import com.gimbal.android.Visit;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;



public class Beacon extends CordovaPlugin {

	public static final String ACTION_INITIALIZE_BEACON = "initializeBeacon";
	private PlaceEventListener placeEventListener;
	private String onVisitStart;
	private String onVisitEnd;
	private String Checkin;
	private SharedPreferences sharedpreferences;
	private Editor editor;
	private String message;
	private NotificationManager mNotificationManager;

	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {

		if (action.equals(ACTION_INITIALIZE_BEACON)) {
			String Gimbal_Api_Key = "356b4eca-dd0f-42f6-b37f-f1f35dda865e";
			Gimbal.setApiKey(cordova.getActivity().getApplication(),
					Gimbal_Api_Key);
			sharedpreferences = cordova.getActivity().getSharedPreferences(
					"photonworld", cordova.getActivity().MODE_PRIVATE);
			editor = sharedpreferences.edit();
			if (checkBluetoothStatus() && checkLocationServiceStatus()) {
				initializeBeacon(callbackContext);
			}
		}
		return false;
	}

	private boolean checkBluetoothStatus() {
		final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		cordova.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!mBluetoothAdapter.isEnabled()) {
					 AlertDialog.Builder builder = new AlertDialog.Builder(cordova.getActivity());
					  builder.setTitle("Bluetooth Services Not Active");
					  builder.setMessage("Please turn on Bluetooth from Settings");
					  builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					  public void onClick(DialogInterface dialogInterface, int i) {
					 mBluetoothAdapter.enable();
					    }
					  });
					  Dialog alertDialog = builder.create();
					  alertDialog.setCancelable(false);
					  alertDialog.show();
				}	
			}
		});
		return true;
	}
	private boolean checkLocationServiceStatus() {
	cordova.getActivity().runOnUiThread(new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			LocationManager lm = (LocationManager)cordova.getActivity().getSystemService(cordova.getActivity().LOCATION_SERVICE);
			if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
			      !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			  // Build the alert dialog
			  AlertDialog.Builder builder = new AlertDialog.Builder(cordova.getActivity());
			  builder.setTitle("Location Services Not Active");
			  builder.setMessage("Please turn on Location Services from Settings");
			  builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialogInterface, int i) {
			    // Show location settings when the user acknowledges the alert dialog
			    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			    cordova.getActivity().startActivity(intent);
			    }
			  });
			  Dialog alertDialog = builder.create();
			  alertDialog.setCanceledOnTouchOutside(false);
			  alertDialog.show();
			}
		}
	});
	return true;		
	}


	private void initializeBeacon(final CallbackContext callbackContext) {
		cordova.getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				placeEventListener = new PlaceEventListener() {
					
				@Override
				public void onVisitStart(Visit visit) {
					super.onVisitStart(visit);
				String placeName = visit.getPlace().getName();
				onVisitStart = sharedpreferences.getString("onVisitStart", null);	
				Checkin = sharedpreferences.getString("Checkin", null);
						if (placeName.equalsIgnoreCase("Reception") || placeName.equalsIgnoreCase("OnsiteReception")) {
							if (onVisitStart == null) {
								message = "Welcome to the Photon World 2015 Conference !!!";
								displayMessageAlertForReception(placeName, message,
										callbackContext);
								displayNotificationAlert(message);
								editor.putString("onVisitStart", "onVisitStart");
								editor.commit();
							}
						}
						if (placeName.equalsIgnoreCase("Check-in") || placeName.equalsIgnoreCase("OnsiteCheckIn")) {
							if (Checkin == null) {
								message = "Skip the Queue, Register Instantly! Scan the QR code on your badge with your mobile";
								displayMessageAlert(placeName, message,
										callbackContext);

								editor.putString("Checkin", "Checkin");
								editor.commit();
							}
						}
					}
					@Override
					public void onVisitEnd(Visit visit) {
						// TODO Auto-generated method stub
						String placeName = visit.getPlace().getName();
						onVisitEnd = sharedpreferences.getString("onVisitEnd", null);	
					
						if (placeName.equalsIgnoreCase("Reception") || placeName.equalsIgnoreCase("OnsiteReception")) {
							if (onVisitEnd == null) {
								message = "Thank You for attending, See you next year!";
								displayMessageAlertForReception(placeName, message,
										callbackContext);
								displayNotificationAlert(message);
								editor.putString("onVisitEnd", "onVisitEnd");
								editor.commit();
							}
						}
					}
				};
				PlaceManager.getInstance().addListener(placeEventListener);
				PlaceManager.getInstance().startMonitoring();
			}
		});
	}
	private void displayMessageAlert(String placeName, String message,
			CallbackContext callbackContext) {
		// TODO Auto-generated method stub
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(cordova.getActivity());
		alertDialogBuilder.setTitle(placeName);
		alertDialogBuilder.setMessage(message);
		alertDialogBuilder.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int id) {
		webView.sendJavascript("javascript:CheckInCallbackFromAndroid1();");
		
		}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						
					}
				});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.show();
	}
	
	private void displayMessageAlertForReception(String placeName,
			String message, CallbackContext callbackContext) {
		// TODO Auto-generated method stub
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(cordova.getActivity());
		alertDialogBuilder.setTitle(placeName);
		alertDialogBuilder.setMessage(message);
		alertDialogBuilder.setPositiveButton("OK",new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int id) {
			dialog.cancel();
		}
		});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.show();	
	}
	
	private void displayNotificationAlert(String message) {
	   NotificationCompat.Builder mBuilder =  new NotificationCompat.Builder(cordova.getActivity());
	   mBuilder.setContentText(message);
	   mBuilder.setSmallIcon(0x7f020000);
	   mBuilder.setAutoCancel(true);
	   Notification note = mBuilder.build();
	   note.defaults |= Notification.DEFAULT_VIBRATE;
	   note.defaults |= Notification.DEFAULT_SOUND;
	   mNotificationManager =(NotificationManager)cordova.getActivity().getSystemService(cordova.getActivity().NOTIFICATION_SERVICE); 
	   mNotificationManager.notify(100, note);
	   Intent resultIntent = new Intent(cordova.getActivity(),cordova.getActivity().getClass());
	   TaskStackBuilder stackBuilder = TaskStackBuilder.create(cordova.getActivity());
	   stackBuilder.addParentStack(cordova.getActivity().getClass());
	   stackBuilder.addNextIntent(resultIntent);
	   PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
	            0,PendingIntent.FLAG_ONE_SHOT //can only be used once
	         );
	   mBuilder.setContentIntent(resultPendingIntent);
	   mNotificationManager = (NotificationManager)cordova.getActivity().getSystemService(cordova.getActivity().NOTIFICATION_SERVICE);
	   mNotificationManager.notify(100, mBuilder.build());
		
	}
}
