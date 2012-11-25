package org.example.mqtt;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class EventServiceControl extends Activity {

  /** Messenger for communicating with service. */
  Messenger mService = null;
  /** Flag indicating whether we have called bind on the service. */
  boolean mIsBound;
  /** Some text view we are using to show state information. */
  TextView mCallbackText;

  /**
   * Handler of incoming messages from service.
   */
  class IncomingHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
      case EventService.MSG_SET_VALUE:
        mCallbackText.setText("Received from service: " + msg.arg1);
        break;
      case EventService.MSG_STR_VALUE:
        mCallbackText.setText("Received from service: " + msg.obj.toString());
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

  /**
   * Class for interacting with the main interface of the service.
   */
  private ServiceConnection mConnection = new ServiceConnection() {
    public void onServiceConnected(ComponentName className, IBinder service) {
      // This is called when the connection with the service has been
      // established, giving us the service object we can use to
      // interact with the service. We are communicating with our
      // service through an IDL interface, so get a client-side
      // representation of that from the raw service object.
      mService = new Messenger(service);
      mCallbackText.setText("Attached.");

      // We want to monitor the service for as long as we are
      // connected to it.
      try {
        Message msg = Message.obtain(null, EventService.MSG_REGISTER_CLIENT);
        msg.replyTo = mMessenger;
        mService.send(msg);
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        // Give it some value as an example.
        msg = Message.obtain(null, EventService.MSG_SET_VALUE, this.hashCode(),
            0);
        mService.send(msg);
      } catch (RemoteException e) {
        // In this case the service has crashed before we could even
        // do anything with it; we can count on soon being
        // disconnected (and then reconnected if it can be restarted)
        // so there is no need to do anything here.
      }

      // As part of the sample, tell the user what happened.
      Toast.makeText(getBaseContext(), R.string.remote_service_connected,
          Toast.LENGTH_SHORT).show();
    }

    public void onServiceDisconnected(ComponentName className) {
      // This is called when the connection with the service has been
      // unexpectedly disconnected -- that is, its process crashed.
      mService = null;
      mCallbackText.setText("Disconnected.");

      // As part of the sample, tell the user what happened.
      Toast.makeText(getBaseContext(), R.string.remote_service_disconnected,
          Toast.LENGTH_SHORT).show();
    }
  };

  void doBindService() {
    // Establish a connection with the service. We use an explicit
    // class name because there is no reason to be able to let other
    // applications replace our component.
    bindService(new Intent(this, EventService.class), mConnection,
        Context.BIND_AUTO_CREATE);
    mIsBound = true;
    mCallbackText.setText("Binding.");
  }

  void doUnbindService() {
    if (mIsBound) {
      // If we have received the service, and hence registered with
      // it, then now is the time to unregister.
      if (mService != null) {
        try {
          Message msg = Message
              .obtain(null, EventService.MSG_UNREGISTER_CLIENT);
          msg.replyTo = mMessenger;
          mService.send(msg);
        } catch (RemoteException e) {
          // There is nothing special we need to do if the service
          // has crashed.
        }
      }

      // Detach our existing connection.
      unbindService(mConnection);
      mIsBound = false;
      mCallbackText.setText("Unbinding.");
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.event_control);

    Button btnStart = (Button) findViewById(R.id.btnStartService);
    btnStart.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        doBindService();
      }
    });
    Button btnStop = (Button) findViewById(R.id.btnStopService);
    btnStop.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        doUnbindService();
      }
    });

    mCallbackText = (TextView) findViewById(R.id.callback);
    mCallbackText.setText("Not attached.");
  }

  private BroadcastReceiver intentReceiver = new BroadcastReceiver() {

    @Override
    public void onReceive(Context arg0, Intent arg1) {
      Toast.makeText(getBaseContext(), "File Downloaded!", Toast.LENGTH_LONG)
          .show();

    }
  };

  @Override
  protected Dialog onCreateDialog(int id) {
    switch (id) {
    case 0:
      AlertDialog.Builder alert = new AlertDialog.Builder(this);

      alert.setTitle("Event Service Control");
      alert.setMessage("Server address:");

      // Set an EditText view to get user input
      final LinearLayout dialogLayout = new LinearLayout(getBaseContext());
      final EditText addressInput = new EditText(this);
      final EditText userInput = new EditText(this);
      final EditText passwdInput = new EditText(this);
      dialogLayout.addView(addressInput);
      dialogLayout.addView(userInput);
      dialogLayout.addView(passwdInput);
      alert.setView(dialogLayout);
      

      alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
          String serverValue = addressInput.getText().toString().trim();
          String userValue = userInput.getText().toString().trim();
          String passwdValue = passwdInput.getText().toString().trim();
          Log.d("ESC", "sv="+serverValue + "uv="+userValue + "pv="+passwdValue);
          List<String> values = new ArrayList<String>();
          Message msg = Message.obtain(null, EventService.MSG_SET_SERVER, values);
          try {
            mService.send(msg);
          } catch (RemoteException e) {
            e.printStackTrace();
          }
        }
      });

      alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
          // Canceled.
        }
      });

      return alert.create();
    default:
      return null;
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.esc_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
    case R.id.icon:
      Log.d("ESC", "chamou menu do icone");
      showDialog(0);
      return true;
    case R.id.text:
      Log.d("ESC", "chamou menu do texto");
      return true;
    case R.id.icontext:
      Log.d("ESC", "chamou menu do icone com texto");
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }

  @Override
  protected void onDestroy() {
    // unregisterReceiver(intentReceiver);
    super.onDestroy();
  }

}
