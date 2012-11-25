package org.example.mqtt;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class EventService extends Service {
  /** For showing and hiding our notification. */
  NotificationManager notificatinManager;
  /** Keeps track of all current registered clients. */
  ArrayList<Messenger> mClients = new ArrayList<Messenger>();
  /** Holds last value set by a client. */
  int mValue = 0;

  /**
   * Command to the service to register a client, receiving callbacks from the
   * service. The Message's replyTo field must be a Messenger of the client
   * where callbacks should be sent.
   */
  static final int MSG_REGISTER_CLIENT = 1;

  /**
   * Command to the service to unregister a client, ot stop receiving callbacks
   * from the service. The Message's replyTo field must be a Messenger of the
   * client as previously given with MSG_REGISTER_CLIENT.
   */
  static final int MSG_UNREGISTER_CLIENT = 2;

  /**
   * Command to service to set a new value. This can be sent to the service to
   * supply a new value, and will be sent by the service to any registered
   * clients with the new value.
   */
  static final int MSG_SET_VALUE = 3;
  static final int MSG_STR_VALUE = 4;
  static final int MSG_SET_SERVER = 5;
  private static final String TAG = "MQTTClient";

  /**
   * Handler of incoming messages from clients.
   */
  class IncomingHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      Log.d(TAG, "received msg: [" + msg.what + "]");
      switch (msg.what) {
      case MSG_REGISTER_CLIENT:
        mClients.add(msg.replyTo);
        break;
      case MSG_UNREGISTER_CLIENT:
        mClients.remove(msg.replyTo);
        break;
      case MSG_SET_VALUE:
        send();

        mValue = msg.arg1;
        for (int i = mClients.size() - 1; i >= 0; i--) {
          try {
            mClients.get(i)
                .send(Message.obtain(null, MSG_SET_VALUE, mValue, 0));
          } catch (RemoteException e) {
            // The client is dead. Remove it from the list;
            // we are going through the list from back to front
            // so this is safe to do inside the loop.
            mClients.remove(i);
          }
        }
        break;
      case MSG_SET_SERVER:
        List<String> values = (List<String>) msg.obj;
        sAddress = values.get(0);
        sUserName = values.get(1);
        sPassword = values.get(2);
        disconnect();
        connect();
        break;
      default:
        super.handleMessage(msg);
      }
    }
  }

  /**
   * Target we publish for clients to send messages to IncomingHandler.
   */
  final Messenger mMessenger = new Messenger(new IncomingHandler());
  private MQTT mqtt;
  private String sAddress = "tcp://10.183.31.231:1883";
  private String sUserName = "system";
  private String sPassword = "manager";
  private FutureConnection connection;
  // private ProgressDialog progressDialog;
  private String sDestination = "jms_mqtt_incoming";
  protected String sMessage = "Mensagem do validador.";

  @Override
  public void onCreate() {
    notificatinManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    // Display a notification about us starting.
    showNotification();

    connect();
  }

  /**
   * Show a notification while this service is running.
   */
  private void showNotification() {
    // In this sample, we'll use the same text for the ticker and the expanded
    // notification
    CharSequence text = getText(R.string.remote_service_started);

    // Set the icon, scrolling text and timestamp
    Notification notification = new Notification(R.drawable.stat_sample, text,
        System.currentTimeMillis());

    // The PendingIntent to launch our activity if the user selects this
    // notification
    PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
        new Intent(), 0);

    // Set the info for the views that show in the notification panel.
    notification.setLatestEventInfo(this,
        getText(R.string.remote_service_label), text, contentIntent);

    // Send the notification.
    // We use a string id because it is a unique number. We use it later to
    // cancel.
    notificatinManager.notify(R.string.remote_service_started, notification);
  }

  private void connect() {
    ConnectivityManager cm = (ConnectivityManager) getBaseContext()
        .getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    boolean isConnected = activeNetwork.isConnectedOrConnecting();

    if (!isConnected) {
      toast("Network connection unavailable");
      return;
    }
    
    mqtt = new MQTT();
    mqtt.setClientId("validador-152");

    try {
      mqtt.setHost(sAddress);
      Log.d(TAG, "Address set: " + sAddress);
    } catch (URISyntaxException urise) {
      Log.e(TAG, "URISyntaxException connecting to " + sAddress + " - " + urise);
    }

    if (sUserName != null && !sUserName.equals("")) {
      mqtt.setUserName(sUserName);
      Log.d(TAG, "UserName set: [" + sUserName + "]");
    }

    if (sPassword != null && !sPassword.equals("")) {
      mqtt.setPassword(sPassword);
      Log.d(TAG, "Password set: [" + sPassword + "]");
    }

    connection = mqtt.futureConnection();
    // progressDialog = ProgressDialog.show(this, "", "Connecting...", true);
    connection.connect().then(new Callback<Void>() {
      public void onSuccess(Void value) {
        // progressDialog.dismiss();
        Log.d(TAG, "Conectado");
      }

      public void onFailure(Throwable e) {
        toast("Problem connecting to host");
        Log.e(TAG, "Exception connecting to " + sAddress + " - " + e);
        // progressDialog.dismiss();
      }
    });

  }

  private void disconnect() {
    try {
      if (connection != null && connection.isConnected()) {
        connection.disconnect().then(new Callback<Void>() {
          public void onSuccess(Void value) {
            toast("Disconnected");
          }

          public void onFailure(Throwable e) {
            toast("Problem disconnecting");
            Log.e(TAG, "Exception disconnecting from " + sAddress + " - " + e);
          }
        });
      } else {
        toast("Not Connected");
      }
    } catch (Exception e) {
      Log.e(TAG, "Exception " + e);
    }
  }

  private void send() {
    if (connection != null) {
      // automatically connect if no longer connected
      if (!connection.isConnected()) {
        Log.d(TAG, "nao conectado");
        connect();
      }

      Topic[] topics = { new Topic(sDestination, QoS.AT_LEAST_ONCE) };
      connection.subscribe(topics).then(new Callback<byte[]>() {
        public void onSuccess(byte[] subscription) {

          Log.d(TAG, "Destination: " + sDestination);
          Log.d(TAG, "Message: " + sMessage);

          // publish message
          connection.publish(sDestination, sMessage.getBytes(),
              QoS.AT_LEAST_ONCE, false);
          toast("Message sent");

          // receive message
          connection.receive().then(createCallback());

        }

        private Callback<org.fusesource.mqtt.client.Message> createCallback() {
          return new Callback<org.fusesource.mqtt.client.Message>() {
            public void onSuccess(org.fusesource.mqtt.client.Message message) {
              String receivedMesageTopic = message.getTopic();
              byte[] payload = message.getPayload();
              String messagePayLoad = new String(payload);
              message.ack();

              Log.d(TAG, "msg: [" + messagePayLoad + "]");
              for (int i = mClients.size() - 1; i >= 0; i--) {
                try {
                  mClients.get(i).send(
                      Message.obtain(null, MSG_STR_VALUE, messagePayLoad));
                } catch (RemoteException e) {
                  // The client is dead. Remove it from the list;
                  // we are going through the list from back to front
                  // so this is safe to do inside the loop.
                  mClients.remove(i);
                }
              }
              connection.receive().then(createCallback());
            }

            public void onFailure(Throwable e) {
              Log.e(TAG, "Exception receiving message: " + e);
            }
          };
        }

        public void onFailure(Throwable e) {
          Log.e(TAG, "Exception sending message: " + e);
        }
      });
    } else {
      toast("No connection has been made, please create the connection");
    }
  }

  public URL[] urls;

  private final IBinder binder = new EventServiceBinder();

  public class EventServiceBinder extends Binder {
    public EventService getService() {
      return EventService.this;
    }
  }

  /**
   * When binding to the service, we return an interface to our messenger for
   * sending messages to the service.
   */
  @Override
  public IBinder onBind(Intent arg0) {
    return mMessenger.getBinder();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    disconnect();
    // Cancel the persistent notification.
    notificatinManager.cancel(R.string.remote_service_started);

    // Tell the user we stopped.
    Toast.makeText(this, R.string.remote_service_stopped, Toast.LENGTH_SHORT)
        .show();
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
      for (int i = 0; i < count; i++) {
        totalBytesDownloaded += DownloadFile(urls[i]);
        publishProgress(((i + 1) * 100 / count));
      }

      Intent broadcastIntent = new Intent();
      broadcastIntent.setAction("FILE_DOWNLOADED_ACTION");
      getBaseContext().sendBroadcast(broadcastIntent);

      return totalBytesDownloaded;
    }

    @Override
    protected void onPostExecute(Long result) {
      Toast.makeText(getBaseContext(), "Downloaded " + result + "bytes",
          Toast.LENGTH_LONG).show();
      stopSelf();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
      Log.d("Downloading files", String.valueOf(progress[0]) + "% downloaded");
      Toast.makeText(getBaseContext(), progress[0] + "% downloaded",
          Toast.LENGTH_LONG).show();
    }

  }

  private void toast(String message) {
    // Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    Log.d("toast {", message + "}");
  }
}
