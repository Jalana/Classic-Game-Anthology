package tru.kyle.classicgameanthology;

/*
This file (BaseActivity) is a part of the Classic Game Anthology application.
Copyright (C) <2015>  <Connor Kyle>

The Classic Game Anthology is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The Classic Game Anthology is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with the Classic Game Anthology.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.ArrayList;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

public abstract class BaseActivity extends Activity 
{
	protected ArrayList<BluetoothDevice> availableDevices = new ArrayList<BluetoothDevice>();
	protected ArrayList<String> deviceNames = new ArrayList<String>();
	protected ArrayList<BluetoothDevice> pairedDevices = new ArrayList<BluetoothDevice>();
	
	protected BluetoothService bluetooth;
	public final static int REQUEST_ENABLE = 52;
	public final static int REQUEST_DISCOVERY = 76;
	
	public static final UUID MY_UUID = UUID.fromString("6f9b5d8c-5dc4-41ed-b752-72929afaade9");
	public static final String NAME = "Anthology Bluetooth";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//if (bluetooth == null)
		{
			boolean result = getApplicationContext().bindService(new Intent(getApplicationContext(), BluetoothService.class), 
	        		service_connection, Context.BIND_AUTO_CREATE);
	        if (result == false)
	        {
	        	Log.d("Bluetooth Logs", "Unable to start binding");
	        }
		}
	}
	
	protected void enableBluetooth()
    {
    	if (bluetooth.getAdapter().isEnabled()) 
    	{
	    	//String deviceName = bluetooth.getAdapter().getName();
	    	//String macAddress = bluetooth.getAdapter().getAddress();
    	} 
    	else 
    	{
	    	Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	    	startActivityForResult(enableBluetooth, REQUEST_ENABLE);
    	}
    }
    
    protected boolean startDiscovery()
    {
    	if (bluetooth.getAdapter().isEnabled()) 
    	{
	    	//String deviceName = bluetooth.getAdapter().getName();
	    	//String macAddress = bluetooth.getAdapter().getAddress();
    	} 
    	else 
    	{
    		Log.d("Bluetooth Logs", "Adapter is not enabled at discovery attempt.");
	    	return false;
    	}
    	
    	if (bluetooth.getAdapter().isDiscovering())
    	{
    		bluetooth.getAdapter().cancelDiscovery();
    	}
    	
    	Intent discoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
    	discoverable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
    	startActivityForResult(discoverable, REQUEST_DISCOVERY);
    	return true;
    	/*
    	boolean result = bluetooth.getAdapter().startDiscovery();
    	if (result == false)
    	{
    		Log.d("Bluetooth Logs", "Error: unable to begin discovery");
    	}
    	return result;
    	*/
    }
    
    /****
     * Set the button's background to the drawable provided.
     * This function takes the user's API level into account and calls
     * the appropriate functions accordingly.
     * <p>
     * If the button provided is null, the function will do nothing.
     * 
     * @param b
     * @param d
     ****/
    @SuppressWarnings("deprecation")
	@SuppressLint("NewApi") 
    protected final void setButtonBackground(Button button, Drawable background)
    {
    	if (button == null)
    	{
    		return;
    	}
    	
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
		{
			button.setBackground(background);
		}
		else
		{
			button.setBackgroundDrawable(background);
		}
    }
    
    protected final void doUnbindService()
    {
    	this.unbindService(service_connection);
    }
    
    /****
     * This callback is triggered whenever any data is written to
     * an active Bluetooth connection.
     * 
     * @param data : the message that was sent out.
     ****/
    protected abstract void onWrite(String data);
    
    /****
     * This callback is triggered whenever an active Bluetooth connection
     * reads any data.
     * 
     * @param data : the message that was received.
     ****/
    protected abstract void onRead(String data);
    
    protected abstract void onConnectionLost();
    
    protected void onDiscoveryEnabled(int howLong)
    {
    	Log.d("Bluetooth Logs", "Discovery enabled");
    	availableDevices.clear();
    	deviceNames.clear();
    	if (bluetooth.getAdapter().isDiscovering() == false)
    	{
    		bluetooth.getAdapter().startDiscovery();
    	}
    	bluetooth.startListening();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data) 
	{
	    switch (requestCode)
	    {
		    case REQUEST_ENABLE:
		    {
			    if (responseCode == RESULT_OK) 
			    {
			    	//BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
			    	//String deviceName = bluetooth.getName();
			    	//String macAddress = bluetooth.getAddress();
			    	
			    	// do something bluetoothy
			    }
			    break;
		    }
		    case REQUEST_DISCOVERY:
		    {
			    if (responseCode == RESULT_CANCELED) 
			    {
			    	// go without Bluetooth?
			    } 
			    else 
			    {
			    	onDiscoveryEnabled(responseCode);
			    	/*
			    	Set<BluetoothDevice> pairedDevice = bluetooth.getAdapter().getBondedDevices();            
			        if(pairedDevice.size() > 0)
			        {
			        	Log.d("Bluetooth Logs", "Found paired device(s).");
			        	ArrayList<String> arrayListpaired = new ArrayList<String>();
			        	pairedDevices.clear();
			            for(BluetoothDevice device : pairedDevice)
			            {
			                arrayListpaired.add(device.getName()+"\n"+device.getAddress());
			                pairedDevices.add(device);
			                availableDevices.add(device);
			            }
			        }
			        else
			        {
			        	Log.d("Bluetooth Logs", "No paired devices.");
			        }
			        */
			    }
			    break;
		    }
	    }
    }
    
    protected void onDeviceFound(Context context, Intent intent)
    {
    	Log.d("Bluetooth Logs", "Found device");
		
		BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		boolean isPresent = false;
		for (int count = 0; count < availableDevices.size(); count++)
		{
			if (availableDevices.get(count).getAddress() == device.getAddress())
			{
				isPresent = true;
				break;
			}
		}
		if (isPresent == false)
		{
			availableDevices.add(device);
			deviceNames.add(device.getName());
		}
		else
		{
			Log.d("Bluetooth Logs", "Device was duplicate");
		}
		/*
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				BaseActivity.this, android.R.layout.simple_spinner_item, deviceNames);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		deviceSpinner.setAdapter(adapter);
		*/
    }
    
    protected void onBluetoothStateChanged(Context context, Intent intent)
    {
    	//String previousStateExtra = BluetoothAdapter.EXTRA_PREVIOUS_STATE;
        String stateExtra = BluetoothAdapter.EXTRA_STATE;
        int currentState = intent.getIntExtra(stateExtra, -1);
        //int prevState = intent.getIntExtra(previousStateExtra, -1);
        //String toasttext = "";
        switch(currentState)
        {	
        	case(BluetoothAdapter.STATE_TURNING_ON):
        	{
        		//toasttext = "Bluetooth Logs state: TURNING_ON";
        		break;
        	}
        	case(BluetoothAdapter.STATE_ON):
        	{
        		//toasttext = "Bluetooth Logs state: ON";
        		break;
        	}
        	case(BluetoothAdapter.STATE_TURNING_OFF):
        	{
        		//toasttext = "Bluetooth Logs state: TURNING_OFF";
        		break;
        	}
        	case(BluetoothAdapter.STATE_OFF):
        	{
        		deviceNames.clear();
        		availableDevices.clear();
        		//toasttext = "Bluetooth Logs state: OFF";
        		break;
        	}
        }
        //Toast.makeText(getApplicationContext(), toasttext, Toast.LENGTH_SHORT).show();
    }
    
    /****
     * This method is called when a connection is successfully created with another device.
     * At this point, the user may interact with the other device using Bluetooth.
     * <p>
     * The default implementation has no effect, and subclasses should override this method 
     * if necessary.
     * 
     * @param device : the device that the user has connected to.
     ****/
    protected void onConnection(BluetoothDevice device)
    {
    	Log.d("Bluetooth Logs", "onConnection() in BaseActivity");
    }
    
    private final BroadcastReceiver bluetoothState = new BroadcastReceiver() 
    {
	    public void onReceive(Context context, Intent intent) 
	    {
	    	String action = intent.getAction();
	    	//Log.d("Bluetooth Logs", "onReceive found action: " + action);
	    	switch (action)
	    	{
		    	case BluetoothDevice.ACTION_FOUND:
		    	{
		    		onDeviceFound(context, intent);
		    		break;
		    	}
		    	case BluetoothAdapter.ACTION_STATE_CHANGED:
		    	{
		    		onBluetoothStateChanged(context, intent);
		    		break;
		    	}
	    	}
	    }
	};
	
	private final Handler.Callback bluetooth_callback = new Handler.Callback()
	{
		@Override
		public boolean handleMessage(Message message) 
		{
			switch (message.what)
			{
				case BluetoothService.MESSAGE_STATE_CHANGE:
				{
					switch (message.arg1)
					{
						case BluetoothService.STATE_CONNECTED:
						{
							
							break;
						}
						case BluetoothService.STATE_CONNECTING:
						{
							
							break;
						}
						case BluetoothService.STATE_LISTENING:
						{
							
							break;
						}
						case BluetoothService.STATE_NONE:
						{
							
							break;
						}
					}
					
					break;
				}
				case BluetoothService.MESSAGE_DEVICE_NAME:
				{
					Log.d("Bluetooth Logs", "Connection formed");
					BluetoothDevice device = (BluetoothDevice) message.getData()
							.getParcelable(BluetoothService.DEVICE_OBJECT);
					Log.d("Bluetooth Logs", "Preparing to call onConnection()");
					onConnection(device);
					break;
				}
				case BluetoothService.MESSAGE_CONNECTION_LOST:
				{
					onConnectionLost();
					break;
				}
				case BluetoothService.MESSAGE_READ_INFO:
				{
					byte[] readBuffer = (byte[]) message.obj;
					String data = new String(readBuffer, 0, message.arg1);
					
					//Do something.
					onRead(data);
					break;
				}
				case BluetoothService.MESSAGE_WRITE_INFO:
				{
					byte[] writeBuffer = (byte[]) message.obj;
					String data = new String(writeBuffer);
					
					//Do something.
					onWrite(data);
					break;
				}
			}
			return false;
		}
	};
	
	//@SuppressLint("HandlerLeak") 
	private final Handler bluetooth_handler = new Handler(bluetooth_callback);
	
	private ServiceConnection service_connection = new ServiceConnection() 
	{
	    public void onServiceConnected(ComponentName className, IBinder service) 
	    {
	        // This is called when the connection with the service has been
	        // established, giving us the service object we can use to
	        // interact with the service.  Because we have bound to a explicit
	        // service that we know is running in our own process, we can
	        // cast its IBinder to a concrete class and directly access it.
	        bluetooth = ((BluetoothService.BluetoothBinder)service).getService();
	        bluetooth.setHandler(bluetooth_handler);
	    }

	    public void onServiceDisconnected(ComponentName className) 
	    {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	        // Because it is running in our same process, we should never
	        // see this happen.
	        bluetooth = null;
	    }
	};

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	// Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_menu_about) 
        {
        	ActionMenu.displayAboutDialog(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected synchronized void onResume()
    {
    	super.onResume();
    	String actionStateChanged = BluetoothAdapter.ACTION_STATE_CHANGED;
        String actionFound = BluetoothDevice.ACTION_FOUND;
        IntentFilter filter = new IntentFilter();
        filter.addAction(actionFound);
        filter.addAction(actionStateChanged);
        getApplicationContext().registerReceiver(bluetoothState, filter);
    }
    
    @Override
    protected synchronized void onPause() 
    {
	     super.onPause();
	     try
	     {
	    	 unregisterReceiver(bluetoothState);
	     }
	     catch (Exception e)
	     {
	    	 Log.d("Bluetooth Logs", "Receiver was not registered in onPause()");
	     }
    }
    
    @Override
    protected synchronized void onDestroy() 
    {
	     super.onDestroy();
	     if (bluetooth != null && this.isFinishing() == true)
	     {
	    	 try
	    	 {
	    		 doUnbindService();
	    	 }
	    	 catch (IllegalArgumentException i)
	    	 {
	    		 Log.d("Base Activity", "Service was not registered at attempt to unbind it.");
	    	 }
	    	 bluetooth = null;
	     }
	     //unregisterReceiver(bluetoothState);
    }
    
    
}
