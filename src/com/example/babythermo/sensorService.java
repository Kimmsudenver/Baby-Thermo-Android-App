package com.example.babythermo;

import java.util.ArrayList;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SyncStateContract.Constants;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class sensorService extends IntentService implements SensorEventListener {
	
	
	
	public sensorService() {
		super("sensorService");
		
	}
	static int maxHumd,minHumd;
	static String message;
	static boolean change;
	static int currentHumd;
	static float currentValue;
	static int maxTemp,minTemp;
	boolean notifyMe,notifyTo;
	boolean hasTemp=false,hasHumd=false;
	SensorManager senManager;
	SensorEventListener listener;
	ArrayList<String> phones;
	Sensor tempSen,humidSen;
	boolean temp=false,humd=false;
	 @Override
	    protected void onHandleIntent(Intent workIntent) {
		
	 }
	 @Override
	 public int onStartCommand(Intent intent, int flags, int startId){
		 Bundle data=intent.getExtras();
		// currentValue=data.getFloat("currentValue");
		 maxTemp=data.getInt("maxTemp");
		 minTemp=data.getInt("minTemp");
		 notifyMe=data.getBoolean("notifyMe");
		 notifyTo=data.getBoolean("notifyTo");
		 maxHumd=data.getInt("maxHumd");
		 minHumd=data.getInt("minHumd");
		 currentHumd=data.getInt("currentHumd");
		 phones=data.getStringArrayList("phones");
		  senManager=(SensorManager) getSystemService(Context.SENSOR_SERVICE);
		tempSen=senManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
		humidSen=senManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
		if(tempSen==null){
			Toast.makeText(this, "No Temperature Sensor Detected", Toast.LENGTH_LONG).show();				
		}
		else {
			//senManager.registerListener(this, tempSen, SensorManager.SENSOR_DELAY_NORMAL);
			hasTemp=true;
			Toast.makeText(this, "Temperature sensor used", Toast.LENGTH_SHORT).show();
		}
		if(humidSen==null){
			Toast.makeText(this, "No Humidity Sensor Detected", Toast.LENGTH_SHORT).show();
		}
		else{
			//senManager.registerListener(this, humidSen,SensorManager.SENSOR_DELAY_NORMAL);
			hasHumd=true;
			Toast.makeText(this, "Humidity sensor used", Toast.LENGTH_SHORT).show();
		}
		listener=new SensorEventListener() {
			
			@Override
			public void onSensorChanged(SensorEvent event) {
				
				Sensor source=event.sensor;
				//if(event.sensor.getType()==Sensor.TYPE_AMBIENT_TEMPERATURE){
				if(source.equals(tempSen)){
				float currentTemp = event.values[0];
				currentValue=currentTemp*9/5+32;
				temp=true;
				}
				 //if(event.sensor.getType()==Sensor.TYPE_RELATIVE_HUMIDITY){
				else if(source.equals(humidSen)){
					float current=event.values[0];
					currentHumd=(int)current;
					humd=true;
				}
				Intent result=new Intent("com.example.babythermo.BROADCAST");
				Bundle data=new Bundle();
				data.putFloat("currentValue", currentValue);
				data.putInt("currentHumd", currentHumd);
				 result.putExtras(data);

				 if(currentValue>=maxTemp||currentValue<=minTemp||currentHumd>=maxHumd||currentHumd<=minHumd){
					 message="It's getting ";
					  change=false;
					 if(currentValue>=maxTemp)
							{
						
						 message+=" hot: "+String.valueOf(currentValue);
						 change=true;
						 }
						if(currentValue<=minTemp)
							{
							if(currentValue>0){
								message+=" cold: "+String.valueOf(currentValue);
								change=true;
							}
							}
						if(currentHumd<=minHumd)
							{
							message+=" dry: "+String.valueOf(currentHumd);
							change=true;
							}
						if(currentHumd>=maxHumd)
							{
							message+=" humid: "+String.valueOf(currentHumd);
							change=true;
							}
						
						
					}
				sendBroadcast(result);
				 Log.v("service", "background job started"+currentValue+"max:"+maxTemp+"min: "+minTemp+"notify: "+notifyMe+" " +notifyTo);
				 if(temp &&humd){
					 if(notifyTo){
							if(change){
									PendingIntent pendIt=PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent("SMS_SENT"), 0);
									getApplicationContext().registerReceiver(new BroadcastReceiver() {
										
										@Override
										public void onReceive(Context context, Intent intent) {
											// TODO Auto-generated method stub
											//if(getResultCode()==context.)
												Toast.makeText(context, "SMS_SENT", Toast.LENGTH_SHORT).show();
												
										}
									},new IntentFilter("SMS_SENT"));
									android.telephony.SmsManager sms=android.telephony.SmsManager.getDefault();
									
									for(String phone:phones){								
										sms.sendTextMessage("+1"+phone,null, message, pendIt, null);
									}
								}
						}
									
						
						if(notifyMe){	
							if(change){
								NotificationCompat.Builder notif=new NotificationCompat.Builder(getApplicationContext()).setSmallIcon(R.drawable.ic_launcher).setContentTitle("Baby Thermo");
								Intent resultIntent = new Intent(getApplicationContext(),MainActivity.class);
								android.support.v4.app.TaskStackBuilder stackBuilder=android.support.v4.app.TaskStackBuilder.create(getApplicationContext());
								stackBuilder.addParentStack(MainActivity.class);
								stackBuilder.addNextIntent(resultIntent);
								PendingIntent resultPendingIntent=stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
								notif.setContentIntent(resultPendingIntent);
								NotificationManager notifManager=(NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
								notif.setPriority(1);
								notif.setContentText(message);
							//	if(currentValue<=minTemp) notif.setContentText("It's getting cold: "+currentValue);
								notifManager.notify(1,notif.build());
							}
						}
				 senManager.unregisterListener(this);
				 stopSelf();
				 temp=false;humd=false;
				 }
				 		
				
			}
			
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				// TODO Auto-generated method stub
				
			}
		};
		if(hasHumd)senManager.registerListener(listener, humidSen,SensorManager.SENSOR_DELAY_NORMAL);
		if(hasTemp)senManager.registerListener(listener, tempSen, SensorManager.SENSOR_DELAY_NORMAL);
		
		
		//super.onStartCommand(intent, flags, startId);
			return START_NOT_STICKY;
		}
	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	
		 
			
	
//	@Override
//	public void onSensorChanged(SensorEvent event) {
//		Sensor source=event.sensor;
//		//if(event.sensor.getType()==Sensor.TYPE_AMBIENT_TEMPERATURE){
//		if(source.equals(tempSen)){
//		float currentTemp = event.values[0];
//		currentValue=currentTemp*9/5+32;		
//		}
//		 //if(event.sensor.getType()==Sensor.TYPE_RELATIVE_HUMIDITY){
//		if(source.equals(humidSen)){
//			float current=event.values[0];
//			currentHumd=(int)current;
//		}
//		Intent result=new Intent("com.example.babythermo.BROADCAST");
//		Bundle data=new Bundle();
//		data.putFloat("currentValue", currentValue);
//		data.putInt("currentHumd", currentHumd);
//		 result.putExtras(data);
//		 String message="It's getting ";
//		 if(currentValue>=maxTemp||currentValue<=minTemp||currentHumd>=maxHumd||currentHumd<=minHumd){
//			 if(currentValue>=maxTemp)
//					message+="hot: "+currentValue+"; ";
//				if(currentValue<=minTemp)
//					message+="cold: "+ currentValue+"; ";
//				if(currentHumd<=minHumd)
//					message+="dry: "+currentHumd+"; ";
//				if(currentValue<=minTemp)
//					message+="humid: "+currentHumd+"; ";
//				
//				if(notifyTo){
//					
//							PendingIntent pendIt=PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT"), 0);
//							getApplicationContext().registerReceiver(new BroadcastReceiver() {
//								
//								@Override
//								public void onReceive(Context context, Intent intent) {
//									// TODO Auto-generated method stub
//									//if(getResultCode()==context.)
//										Toast.makeText(context, "SMS_SENT", Toast.LENGTH_SHORT).show();
//								}
//							},new IntentFilter("SMS_SENT"));
//							android.telephony.SmsManager sms=android.telephony.SmsManager.getDefault();
//							
//							for(String phone:phones){								
//								sms.sendTextMessage("+1"+phone,null, message, pendIt, null);
//							}
//						}
//							
//				
//				if(notifyMe){									
//					NotificationCompat.Builder notif=new NotificationCompat.Builder(getApplicationContext()).setSmallIcon(R.drawable.ic_launcher).setContentTitle("Baby Thermo");
//					Intent resultIntent = new Intent(this,MainActivity.class);
//					android.support.v4.app.TaskStackBuilder stackBuilder=android.support.v4.app.TaskStackBuilder.create(this);
//					stackBuilder.addParentStack(MainActivity.class);
//					stackBuilder.addNextIntent(resultIntent);
//					PendingIntent resultPendingIntent=stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//					notif.setContentIntent(resultPendingIntent);
//					NotificationManager notifManager=(NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
//					notif.setPriority(1);
//					notif.setContentText(message);
//				//	if(currentValue<=minTemp) notif.setContentText("It's getting cold: "+currentValue);
//					notifManager.notify(1,notif.build());
//				}
//			}
//		sendBroadcast(result);
//		 Log.v("service", "background job started"+currentValue+"max:"+maxTemp+"min: "+minTemp+"notify: "+notifyMe+" " +notifyTo);
//		 senManager.unregisterListener(this);
//		 stopSelf();
////		if(!editBox.isChecked())
////			//temp.setProgress((int)currentValue-minTemp);
////			current.setText(String.valueOf(currentValue));
//		
//	}
//	@Override
//	public void onAccuracyChanged(Sensor sensor, int accuracy) {
//		// TODO Auto-generated method stub
//		
//	}
}
