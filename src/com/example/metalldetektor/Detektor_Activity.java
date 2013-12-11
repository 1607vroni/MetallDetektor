package com.example.metalldetektor;

import javax.security.auth.callback.Callback;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.FloatMath;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.metaldetector.R;



public class Detektor_Activity extends Activity implements Callback, SensorEventListener {
     private SensorManager mSensorManager;
     private Sensor mAccelerometer;
     
     public TextView betragView;
     public TextView werteView;
     public TextView textview;
     
     public int status;
     public int rundenx;
     public int rundeny;
     public int rundenz;
     
     public ProgressBar progressBar;
     public int progbarmax = 100;

     private int progbarstat = 0;
     private Handler progbarhandler = new Handler();
     
     public int percent;

     final int sensetivity = 100;
     
     private static final int SCAN_QR_CODE_REQUEST_CODE = 0;

     public Detektor_Activity() {
     }  
     
     @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        
    	setContentView(R.layout.activity_main);
     	betragView = (TextView) findViewById(R.id.textView4);
     	werteView = (TextView) findViewById(R.id.textView6);
     	
     	progressBar =  (ProgressBar) findViewById(R.id.progressBar);
     	progressBar.setMax(progbarmax);
     	
     	 mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
     	 mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
             MenuItem menuItem = menu.add("In Logbuch eintragen");
             menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {

                     @Override
                     public boolean onMenuItemClick(MenuItem item) {
                             Intent intent = new Intent(
                                             "com.google.zxing.client.android.SCAN");
                             intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                             startActivityForResult(intent, SCAN_QR_CODE_REQUEST_CODE);
                             return false;
                     }
             });

             return super.onCreateOptionsMenu(menu);
     }
     
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent intent) {
             if (requestCode == SCAN_QR_CODE_REQUEST_CODE) {
                     if (resultCode == RESULT_OK) {
                             String qrCode = intent.getStringExtra("SCAN_RESULT");
                             sendlog(qrCode);
                     }
             }
     }
     
     
     
     protected void onResume() {
         super.onResume();
         mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
     }

     protected void onPause() {
         super.onPause();
         mSensorManager.unregisterListener(this);
     }

     public void onAccuracyChanged(Sensor sensor, int accuracy) {
     }

     public void onSensorChanged(SensorEvent event) {

   		float[] mag = event.values;
   		double betrag = FloatMath.sqrt(mag[0] * mag[0] + mag[1] * mag[1] + mag[2] * mag[2]); 
   		
   		status = transFloatToPercent((float) betrag);
   		rundenx = transFloatToPercent((float) mag[0]);
   		rundeny = transFloatToPercent((float) mag[1]);
   		rundenz = transFloatToPercent((float) mag[2]);
   		
   		betragView.setText(""+status);;
   		werteView.setText("X-Wert: "+rundenx+", Y-Wert: "+rundeny+", Z-Wert: "+rundenz);

   		
   		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				if (progbarstat < progbarmax){
					
					progbarstat = status;
					
					//Update the progress Bar
					progbarhandler.post(new Runnable() {
						
						@Override
						public void run() {
							progressBar.setProgress(progbarstat);
						}
					});
				}
				
			}
		}).start();
     
		
     }
     
     public int transFloatToPercent(float value){
    	 percent = 100 / sensetivity * Math.round(value);
    	 return percent;  
     }
     
     //Log-Buch eintrag
     private void sendlog(String qrCode) {
             Intent intent = new Intent("ch.appquest.intent.LOG");
              
             if (getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
             Toast.makeText(this, "Logbook App not Installed", Toast.LENGTH_LONG).show();
             return;
             }

              
             intent.putExtra("ch.appquest.taskname", "Metall-Detektor");
             intent.putExtra("ch.appquest.logmessage", qrCode);
              
             startActivity(intent);
     }

 }
    

