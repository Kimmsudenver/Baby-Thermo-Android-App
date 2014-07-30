package com.example.babythermo;

import java.util.ArrayList;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	static int maxTemp=90;
	static int minTemp=70;	
	
	static Button hot, current, cold,dry,humid,currentH;	
	static float currentValue;
	static int maxHumd=55,minHumd=45,currentHumd;
	static boolean notifyMe,receiverStatus=false;
	static boolean notifyTo;
	static SensorManager sensors;
	static Sensor tempSen;
	ImageView lauchIcon;
	static int max,min;	
	Fragment curFragment;
	static AlarmManager repeat ;
	static PendingIntent scheduledIntent;
	static ArrayList<String> users=new ArrayList<String>();
	static ArrayList<String> phones=new ArrayList<String>();
	static BroadcastReceiver receiver;
	static Intent service;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();	
	
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
		
	}
	@Override
	public void onPause(){
		curFragment=findCurrentFragment();
		super.onPause();
		
	}

	//save instance state for orientation change
		@Override
		public void onSaveInstanceState(Bundle state){
			state.putStringArrayList("phones", phones);
			state.putBoolean("me", notifyMe);
			state.putBoolean("to", notifyTo);
			state.putBoolean("receiverStatus", receiverStatus);
			//Fragment curFragment=findCurrentFragment();
			if(curFragment!=null)
			getSupportFragmentManager().putFragment(state, "fragment", curFragment);
			state.putStringArrayList("users", users);
			
			super.onSaveInstanceState(state);
			
		}
		
		//restore instance state after orientation change
	@Override
	public void onRestoreInstanceState(Bundle state){
		super.onRestoreInstanceState(state);
		phones=state.getStringArrayList("phones");
		notifyMe=state.getBoolean("me");
		notifyTo=state.getBoolean("to");
		receiverStatus=state.getBoolean("receiverStatus");
		users=state.getStringArrayList("users");
		Fragment restoreFragment=getSupportFragmentManager().getFragment(state, "fragment");
		if(restoreFragment!=null)
		getSupportFragmentManager().beginTransaction()
		.replace(R.id.container, restoreFragment).commit();
		
	}

	
	// method to find current fragment on activity for saving instance state
	public Fragment findCurrentFragment(){
		android.support.v4.app.FragmentManager manager=MainActivity.this.getSupportFragmentManager();
		List<Fragment> list=manager.getFragments();
		
		for(Fragment item:list){
			if(item!=null&&item.isVisible()) return item;
		}
		return null;
		//return list.get(list.size()-1);
	}
	
	
	
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			setRetainInstance(true);
			Button start=(Button) rootView.findViewById(R.id.button1);
			start.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					android.support.v4.app.FragmentManager manager=getActivity().getSupportFragmentManager();
					android.support.v4.app.FragmentTransaction transaction=manager.beginTransaction();
					Fragment start=new StartFragment();
					transaction.replace(R.id.container, start);
					transaction.addToBackStack(null);
					transaction.commit();
					
				}
			});
			
			Button alert=(Button) rootView.findViewById(R.id.button2);
			alert.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					android.support.v4.app.FragmentManager manager=getActivity().getSupportFragmentManager();
					android.support.v4.app.FragmentTransaction transaction=manager.beginTransaction();
					Fragment start=new alertFragment();
					transaction.replace(R.id.container, start);
					transaction.addToBackStack(null);
					transaction.commit();
					
				}
			});
			Button guide=(Button) rootView.findViewById(R.id.button3);
			guide.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					android.support.v4.app.FragmentManager manager=getActivity().getSupportFragmentManager();
					android.support.v4.app.FragmentTransaction transaction=manager.beginTransaction();
					Fragment start=new guideFragment();
					transaction.replace(R.id.container, start);
					transaction.addToBackStack(null);
					transaction.commit();
					
				}
			});
			
			return rootView;
		}
		
		
	}
	
	/**
	 * The Fragment of main screen with temperature display
	 */
	
	public static class StartFragment extends Fragment{
		IntentFilter intentFilter;
		public StartFragment(){
			
		}
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
			View rootView=inflater.inflate(R.layout.thermo, container,false);
			
			setRetainInstance(true);			
			 max=maxTemp-minTemp;
			 min=0;							
			 current=(Button)rootView.findViewById(R.id.current);			
			 hot=(Button)rootView.findViewById(R.id.hot);
			 dry= (Button) rootView.findViewById(R.id.dry);
			 humid=(Button)rootView.findViewById(R.id.humid);
			 currentH=(Button)rootView.findViewById(R.id.currenth);
			 dry.setText(String.valueOf(minHumd)+"%");
			 humid.setText(String.valueOf(maxHumd)+"%");
			hot.setText(String.valueOf(maxTemp)+"\u2109");		
			 cold=(Button)rootView.findViewById(R.id.cold);
			cold.setText(String.valueOf(minTemp)+"\u2109");
			service=new Intent(getActivity(),sensorService.class );
			Bundle data = new Bundle();
			data.putStringArrayList("phones", phones);
			data.putInt("maxTemp", maxTemp);
			data.putInt("minTemp", minTemp);
			data.putFloat("currentValue", currentValue);
			data.putBoolean("notifyMe", notifyMe);
			data.putBoolean("notifyTo", notifyTo);
			data.putInt("minHumd", minHumd);
			data.putInt("maxHumd", maxHumd);
			data.putInt("currentHumd", currentHumd);
			service.putExtras(data);
			//this.startService(service);
			repeat = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE); //ALERT MANAGER FOR SENSORS READING.
			scheduledIntent = PendingIntent.getService(getActivity(), 0, service, PendingIntent.FLAG_UPDATE_CURRENT);
			repeat.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),(long) 30000, scheduledIntent);//30 second
			 intentFilter=new IntentFilter("com.example.babythermo.BROADCAST");
			receiver=new BroadcastReceiver() {
				
				@Override
				public void onReceive(Context context, Intent intent) {
					Bundle data=intent.getExtras();
					currentValue=data.getFloat("currentValue");
					currentHumd=data.getInt("currentHumd");
					currentH.setText(String.valueOf(currentHumd)+"%");
					current.setText(String.valueOf((int)currentValue)+"\u2109");
					Log.v("serviceResponse", "background data received:"+currentValue+","+currentHumd);
					
				}
			};
			
			getActivity().registerReceiver(receiver, intentFilter);
			receiverStatus=true;
			
			return rootView;
		}
		
		@Override
		public void onStart() {
			super.onStart();	
			
		}
		
		
		
		@Override
		public void onSaveInstanceState(Bundle state){
			super.onSaveInstanceState(state);
		}
	
		
		
	}
	

	/**
	 * The Fragment of user guide
	 */
public static class guideFragment extends Fragment{
	public guideFragment(){
		
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View rootView=inflater.inflate(R.layout.guide, container,false);
		setRetainInstance(true);
		
		
		return rootView;
	}
	@Override
	public void onSaveInstanceState(Bundle state){
		super.onSaveInstanceState(state);
	}
}
	


/**
 * The Fragment of Alert Option
 */
	public static class alertFragment extends Fragment{
		EditText phone1,user1,phone2,user2,phone3,user3;
			public alertFragment(){
			
			}
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
			View rootView=inflater.inflate(R.layout.alert, container,false);
			setRetainInstance(true);
			CheckBox me=(CheckBox)rootView.findViewById(R.id.me);
			CheckBox to=(CheckBox)rootView.findViewById(R.id.notifybox);
			if(notifyMe) me.setChecked(true);
			if(notifyTo) to.setChecked(true);			
			me.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					// TODO Auto-generated method stub
					if(isChecked)
						notifyMe=true;
					else notifyMe=false;					
					
				}
			});
			to.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					// TODO Auto-generated method stub
					if(isChecked){
						notifyTo=true;
						
					}
					else notifyTo=false;
				}
			});
			//retrieving phone number on restore instance state
			phone1=(EditText) rootView.findViewById(R.id.phone1);
			user1=(EditText) rootView.findViewById(R.id.user1);			
			phone2=(EditText) rootView.findViewById(R.id.phone2);
			user2=(EditText) rootView.findViewById(R.id.user2);
			phone3=(EditText) rootView.findViewById(R.id.phone3);
			user3=(EditText) rootView.findViewById(R.id.user3);
			if(users!=null&& !users.isEmpty()){
				if(users.get(0)!=null) user1.setText(users.get(0));
				if(users.get(1)!=null) user2.setText(users.get(1));
				if(users.get(2)!=null) user3.setText(users.get(2));
			}
			if(phones!=null &&!phones.isEmpty()){
				if(phones.get(0)!=null) phone1.setText(phones.get(0));
				if(phones.get(1)!=null) phone2.setText(phones.get(1));
				if(phones.get(2)!=null) phone3.setText(phones.get(2));
			}
				
				
			
			return rootView;
			
	}
		
		@Override
		public void onSaveInstanceState(Bundle state){
			super.onSaveInstanceState(state);
		}
		@Override
		public void onStop(){
			//saving phone number for instance state
			if(phone1.getText()!=null){
				phones.add(phone1.getText().toString());				
			}
			if(phone2.getText()!=null){
				phones.add(phone2.getText().toString());
			}
			if(phone3.getText()!=null){
				phones.add(phone3.getText().toString());
			}
			if(user1.getText()!=null){				
				users.add(user1.getText().toString());
			}
			if(user2.getText()!=null){				
				users.add(user2.getText().toString());
			}
			if(user3.getText()!=null){
				users.add(user3.getText().toString());
			}
						
		super.onStop();
		}
	}
		

	/**
	 * The Fragment of setting the limit, got here from Start fragment
	 */
	public class settFragment extends Fragment{
		int mint,maxt;
		NumberPicker minp,maxp,minhp,maxhp;
		public settFragment(){
			
		}
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
			View rootView=inflater.inflate(R.layout.setting, container,false);	
		//	setRetainInstance(true);
			minp=(NumberPicker) rootView.findViewById(R.id.mintpicker);
			maxp=(NumberPicker) rootView.findViewById(R.id.matpicker);
			
			minhp=(NumberPicker) rootView.findViewById(R.id.minhpicker);
			maxhp=(NumberPicker) rootView.findViewById(R.id.maxhpicker);
			minhp.setMinValue(0);
			minhp.setMaxValue(100);
			minhp.setValue(minHumd);
			maxhp.setMinValue(0);
			maxhp.setMaxValue(100);
			maxhp.setValue(maxHumd);
			minp.setMinValue(0);
			minp.setMaxValue(100);
			maxp.setMinValue(0);
			maxp.setMaxValue(100);			
			maxp.setValue(maxTemp);
			minp.setValue(minTemp);
			minp.setOnValueChangedListener(new OnValueChangeListener() {
				
				@Override
				public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
					minTemp=newVal;
					picker.setValue(newVal);
					
				}
			});
				maxp.setOnValueChangedListener(new OnValueChangeListener() {
				
					@Override
					public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
						maxTemp=newVal;
						picker.setValue(newVal);
						
					}
			});
				minhp.setOnValueChangedListener(new OnValueChangeListener() {
					
					@Override
					public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
						minHumd=newVal;
						picker.setValue(newVal);
						
					}
				});
					maxhp.setOnValueChangedListener(new OnValueChangeListener() {
					
						@Override
						public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
							maxHumd=newVal;
							picker.setValue(newVal);
							
						}
				});
				
				
				
			return rootView;
		}
		@Override
		public void onSaveInstanceState(Bundle state){
			super.onSaveInstanceState(state);
		}
		
		
	}
	
//back button to main fragment
	public void back(View view){
		android.support.v4.app.FragmentManager manager=getSupportFragmentManager();
		android.support.v4.app.FragmentTransaction transaction=manager.beginTransaction();
		Fragment back=new PlaceholderFragment();
		transaction.replace(R.id.container, back);
		//transaction.addToBackStack(null);
		transaction.commit();
	}	
	
//setting button in Start Fragment
	public void setting(View view){
		android.support.v4.app.FragmentManager manager=getSupportFragmentManager();
		android.support.v4.app.FragmentTransaction transaction=manager.beginTransaction();
		Fragment setting=new settFragment();
		transaction.replace(R.id.container, setting);
		//transaction.addToBackStack(null);
		transaction.commit();
	}
	
//back button from setting back to start fragment
	public void backtoMonitor(View view){
		android.support.v4.app.FragmentManager manager=getSupportFragmentManager();
		android.support.v4.app.FragmentTransaction transaction=manager.beginTransaction();
		Fragment start=new StartFragment();
		transaction.replace(R.id.container, start);
		//transaction.addToBackStack(null);
		transaction.commit();
		
	}

	//not using right now
	public void runInBackground(View view){
		Intent service=new Intent(this,sensorService.class );
		Bundle data = new Bundle();
		data.putStringArrayList("phones", phones);
		data.putInt("maxTemp", maxTemp);
		data.putInt("minTemp", minTemp);
		data.putFloat("currentValue", currentValue);
		data.putBoolean("notifyMe", notifyMe);
		data.putBoolean("notifyTo", notifyTo);
		service.putExtras(data);
		this.startService(service);
	}

	//Stop monitoring button
	public void stop(View view){
		if(repeat!=null){
			repeat.cancel(scheduledIntent);
			stopService(new Intent(this,sensorService.class));
			try{
				this.unregisterReceiver(receiver);
				receiverStatus=false;
				Toast.makeText(this, "Monitor Stopped", Toast.LENGTH_SHORT).show();
			}catch(IllegalArgumentException e){
				
			}
			
		}
		super.finish();
	}
	
}
