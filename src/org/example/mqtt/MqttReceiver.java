package org.example.mqtt;

import org.fusesource.mqtt.client.FutureConnection;

import android.os.Process;

public class MqttReceiver implements Runnable {

	private Thread mThread;
	private volatile boolean mStopped;
	private FutureConnection connection;

	public void start() {
		if (mThread == null) {
			mStopped = false;
			mThread = new Thread(this, "MqttReceiver");
			mThread.start();
		}
	}

	public void stop() {
		if (mThread != null) {
			mStopped = true;
			mThread.interrupt();
			mThread = null;
		}
	}

	public void run() {
		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

		while (!mStopped) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Ignore
			}
		}
	}

}
