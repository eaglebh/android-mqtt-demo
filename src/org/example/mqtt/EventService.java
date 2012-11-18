package org.example.mqtt;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class EventService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
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
		try{
			new DoBackgroundTask().execute(
					new URL("http://www.amazon.com/f1.pdf")
					);
			} catch(MalformedURLException e) {
				e.printStackTrace();
			}

		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction("FILE_DOWNLOADED_ACTION");
		getBaseContext().sendBroadcast(broadcastIntent);
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
