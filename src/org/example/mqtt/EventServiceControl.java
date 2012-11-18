package org.example.mqtt;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class EventServiceControl extends Activity {

	private IntentFilter intentFilter;
	
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
				startService(new Intent(getBaseContext(), EventService.class));
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
