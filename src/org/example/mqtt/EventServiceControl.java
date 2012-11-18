package org.example.mqtt;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class EventServiceControl extends Activity {

	private IntentFilter intentFilter;
	private EventService serviceBinder;
	Intent eventServiceIntent;
	
	private ServiceConnection connection = new ServiceConnection() {
		
		public void onServiceDisconnected(ComponentName name) {
			serviceBinder = null;
		}
		
		public void onServiceConnected(ComponentName name, IBinder service) {
			serviceBinder = ((EventService.EventServiceBinder)service).getService();
			try {
			URL[] urls = new URL[] {
					new URL("http://www.e1.com/f1")
			};
			serviceBinder.urls = urls;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			startService(eventServiceIntent);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_control);
		
		intentFilter = new IntentFilter();
		intentFilter.addAction("FILE_DOWNLOADED_ACTION");
		
		registerReceiver(intentReceiver, intentFilter);
		
		
		
		Button btnStart = (Button) findViewById(R.id.btnStartService);
		btnStart.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				eventServiceIntent = new Intent(EventServiceControl.this, EventService.class);
				bindService(eventServiceIntent, connection, Context.BIND_AUTO_CREATE);
			}
		});
		Button btnStop = (Button) findViewById(R.id.btnStopService);
		btnStop.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				stopService(new Intent(getBaseContext(), EventService.class));
			}
		});

	}
	
	private BroadcastReceiver intentReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			Toast.makeText(getBaseContext(), "File Downloaded!", Toast.LENGTH_LONG).show();
			
		}
	};

	@Override
	protected void onDestroy() {
		unregisterReceiver(intentReceiver);
		super.onDestroy();
	}

	
}
