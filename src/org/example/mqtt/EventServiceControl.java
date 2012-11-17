package org.example.mqtt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class EventServiceControl extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_control);
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

}
