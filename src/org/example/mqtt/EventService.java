package org.example.mqtt;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class EventService extends Service {
	
	public URL[] urls;
	
	private final IBinder binder = new EventServiceBinder();
	
	public class EventServiceBinder extends Binder {
		public EventService getService() {
			return EventService.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
		
		new DoBackgroundTask().execute(urls);
		
		return START_STICKY;
	}

	private int DownloadFile(URL url) {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return 100;
	}

	private class DoBackgroundTask extends AsyncTask<URL, Integer, Long> {

		@Override
		protected Long doInBackground(URL... urls) {
			int count = urls.length;
			long totalBytesDownloaded = 0;
			for(int i=0; i < count; i++){
				totalBytesDownloaded += DownloadFile(urls[i]);
				publishProgress( ((i+1)*100 / count));
			}
			
			Intent broadcastIntent = new Intent();
			broadcastIntent.setAction("FILE_DOWNLOADED_ACTION");
			getBaseContext().sendBroadcast(broadcastIntent);
			
			return totalBytesDownloaded;
		}

		@Override
		protected void onPostExecute(Long result) {
			Toast.makeText(getBaseContext(), "Downloaded "+ result +"bytes", Toast.LENGTH_LONG).show();
			stopSelf();
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			Log.d("Downloading files", String.valueOf(progress[0]) + "% downloaded");
			Toast.makeText(getBaseContext(), progress[0]+"% downloaded", Toast.LENGTH_LONG).show();
		}
		
	}
}
