package tru.kyle.classicgameanthology;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class BluetoothService extends Service 
{
	private BluetoothAdapter adapter;
	private Handler handler;
	private BluetoothAcceptThread launchAccept;
	private BluetoothConnectThread launchConnection;
	private BluetoothIOThread connectionThread;
	//private BluetoothDevice otherDevice;
	private int state;
	private boolean isHost = false;
	
	private final IBinder binder = new BluetoothBinder();
	
	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0;
	public static final int STATE_LISTENING = 5;
	public static final int STATE_CONNECTING = 17;
	public static final int STATE_CONNECTED = 24;
	
	//Constants for Handler messages.
	public static final int MESSAGE_STATE_CHANGE = 49;
	public static final int MESSAGE_READ_INFO = 26;
	public static final int MESSAGE_WRITE_INFO = 514;
	public static final int MESSAGE_DEVICE_NAME = 783;
	public static final int MESSAGE_CONNECTION_LOST = 964;
	
	public static final String DEVICE_OBJECT = "device";
	
	
	
	//
	//	Public Methods
	//
	
	@Override
	public void onCreate()
	{
		adapter = BluetoothAdapter.getDefaultAdapter();
		state = STATE_NONE;
	}
	
	@Override
	public void onDestroy()
	{
		
	}
	
	public synchronized int getState()
	{
		return state;
	}
	
	//public synchronized BluetoothDevice getOtherDevice()
	//{
	//	return otherDevice;
	//}
	
	public synchronized boolean isHost()
	{
		return isHost;
	}
	
	public synchronized void toggleHost()
	{
		isHost = !isHost;
	}
	
	public synchronized void setState(int newState)
	{
		state = newState;
		handler.obtainMessage(BluetoothService.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
	}
	
	public void setHandler(Handler h)
	{
		handler = h;
	}
	
	public void startListening()
	{
		if (launchConnection != null) 
		{
			launchConnection.cancel(); 
			launchConnection = null;
		}
		// Cancel any thread currently running a connection
		if (connectionThread != null) 
		{
			connectionThread.cancel(); 
			connectionThread = null;
		}
		// Start the thread to listen on a BluetoothServerSocket
		if (launchAccept == null) 
		{
			launchAccept = new BluetoothAcceptThread();
			launchAccept.start();
		}
		
		setState(STATE_LISTENING);
	}
	
	public synchronized void connect(BluetoothDevice device) 
	{
		// Cancel any thread attempting to make a connection
		if (getState() == STATE_CONNECTING) 
		{
			if (launchConnection != null) 
			{
				launchConnection.cancel(); 
				launchConnection = null;
			}
		}
		// Cancel any thread currently running a connection
		if (connectionThread != null) 
		{
			connectionThread.cancel(); 
			connectionThread = null;
		}
		// Start the thread to connect with the given device
		launchConnection = new BluetoothConnectThread(device);
		launchConnection.start();
		
		setState(STATE_CONNECTING);
	}
	
	public void write(String message)
	{
		byte[] bytes = message.getBytes();
		BluetoothIOThread io;
		synchronized (this)
		{
			if (getState() != STATE_CONNECTED)
			{
				return;
			}
			io = connectionThread;
		}
		io.write(bytes);
	}
	
	public BluetoothAdapter getAdapter()
	{
		return adapter;
	}
	
	
	
	public void getPairedDevices()
	{
		
	}
	
	public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) 
	{
		stopThreads();
		/*
		// Cancel the thread that completed the connection
		if (launchConnection != null) 
		{
			launchConnection.cancel(); 
			launchConnection = null;
		}
		// Cancel any thread currently running a connection
		if (connectionThread != null) 
		{
			connectionThread.cancel(); 
			connectionThread = null;
		}
		// Cancel the accept thread because we only want to connect to one device
		if (launchAccept != null) 
		{
			launchAccept.cancel(); 
			launchAccept = null;
		}
		*/
		
		//otherDevice = device;
		connectionThread = new BluetoothIOThread(socket);
		connectionThread.start();
		
		Message msg = handler.obtainMessage(BluetoothService.MESSAGE_DEVICE_NAME);
		Bundle bundle = new Bundle();
		bundle.putParcelable(BluetoothService.DEVICE_OBJECT, device);
		msg.setData(bundle);
		handler.sendMessage(msg);

		setState(STATE_CONNECTED);
	}
	
	public synchronized void stopThreads()
	{
		if (launchConnection != null) 
		{
			launchConnection.cancel(); 
			launchConnection = null;
		}
		if (connectionThread != null) 
		{
			connectionThread.cancel(); 
			connectionThread = null;
		}
		if (launchAccept != null) 
		{
			launchAccept.cancel(); 
			launchAccept = null;
		}
		//otherDevice = null;
		setState(STATE_NONE);
	}
	
	private void onConnectionLost()
	{
		Log.d("Bluetooth Logs", "Connection lost");
		Message message = handler.obtainMessage(BluetoothService.MESSAGE_CONNECTION_LOST);
		handler.sendMessage(message);
		stopThreads();
	}
	
	@Override
	public IBinder onBind(Intent intent) 
	{
		Log.d("Bluetooth Logs", "onBind was called.");
		return binder;
	}
	
	@Override
	public boolean onUnbind(Intent intent)
	{
		super.onUnbind(intent);
		Log.d("Bluetooth Logs", "onUnbind was called");
		return true;
	}
	
	//
	//	Supporting Classes
	//
	
	public class BluetoothBinder extends Binder
	{
		public BluetoothService getService()
		{
			return BluetoothService.this;
		}
	}
	
	//
	//	Supporting Threads
	//
	
	private class BluetoothAcceptThread extends Thread 
	{
		private final BluetoothServerSocket serverSocket;
		 
	    public BluetoothAcceptThread() 
	    {
	        BluetoothServerSocket temp = null;
	        try 
	        {
	        	Log.d("Bluetooth Logs", "Trying to listen");
	            temp = adapter.listenUsingRfcommWithServiceRecord(BaseActivity.NAME, BaseActivity.MY_UUID);
	        } 
	        catch (IOException e) 
	        { 
	        	
	        }
	        serverSocket = temp;
	        if (serverSocket == null)
	        {
	        	Log.d("Bluetooth Logs", "serverSocket is null");
	        }
	        else
	        {
	        	Log.d("Bluetooth Logs", "Created server socket");
	        }
	    }
	 
	    public void run() 
	    {
	        BluetoothSocket socket = null;
	        // Keep listening until exception occurs or a socket is returned
	        while (BluetoothService.this.getState() != STATE_CONNECTED) 
	        {
	            try 
	            {
	            	Log.d("Bluetooth Logs", "Trying to accept connection");
	                socket = serverSocket.accept();
	            } 
	            catch (IOException e) 
	            {
	                break;
	            }
	            // If a connection was accepted
	            if (socket != null) 
	            {
	            	Log.d("Bluetooth Logs", "Connection accepted");
	            	synchronized (BluetoothService.this) 
	            	{
	            		switch (BluetoothService.this.getState()) 
	            		{
	            			case BluetoothService.STATE_LISTENING:
		            		case BluetoothService.STATE_CONNECTING:
		            		{
		            			Log.d("Bluetooth Logs", "Starting connection thread");
			            		// Situation normal. Start the connected thread.
		            			isHost = true;
			            		connected(socket, socket.getRemoteDevice());
			            		break;
		            		}
		            		case BluetoothService.STATE_NONE:
		            		case BluetoothService.STATE_CONNECTED:
		            		{
		            			Log.d("Bluetooth Logs", "Already connected. Closing socket.");
			            		// Either not ready or already connected. Terminate new socket.
			            		try 
			            		{
			            			socket.close();
			            		} 
			            		catch (IOException e) 
			            		{
			            			//Log.e(TAG, "Could not close unwanted socket", e);
			            		}
			            		break;
		            		}
	            		}
	            	}
	            }
	            else
	            {
	            	Log.d("Bluetooth Logs", "Failed to accept a connection.");
	            }
	        }
	    }
	    
	    public void cancel() 
	    {
	        try 
	        {
	            serverSocket.close();
	        } 
	        catch (IOException e) 
	        { 
	        	
	        }
	    }
	}
	
	
	
	private class BluetoothConnectThread extends Thread 
	{
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;
		
		public BluetoothConnectThread(BluetoothDevice device) 
		{
			mmDevice = device;
			BluetoothSocket tmp = null;
			// Get a BluetoothSocket for a connection with the
			// given BluetoothDevice
			try 
			{
				tmp = device.createRfcommSocketToServiceRecord(BaseActivity.MY_UUID);
				
			} 
			catch (IOException e) 
			{
				Log.d("Bluetooth Logs", "Unable to find socket.");
			}
			mmSocket = tmp;
		}
		
		public void run() 
		{
			adapter.cancelDiscovery();
			try 
			{
				mmSocket.connect();
			} 
			catch (IOException e) 
			{
				Log.d("Bluetooth Logs", "Exception thrown while trying to connect");
				//connectionFailed();
				try 
				{
					mmSocket.close();
				} 
				catch (IOException e2) 
				{
					//Log.e(TAG, "unable to close() socket during connection failure", e2);
				}
				// Start the service over to restart listening mode
				BluetoothService.this.startListening();
				return;
			}
			// Reset the ConnectThread because we're done
			synchronized (BluetoothService.this) 
			{
				launchConnection = null;
				isHost = false;
			}
			// Start the connected thread
			connected(mmSocket, mmDevice);
		}
		
		public void cancel() 
		{
			try 
			{
				mmSocket.close();
			} 
			catch (IOException e) 
			{
				
			}
		}
	}
	
	
	
	private class BluetoothIOThread extends Thread 
	{
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;
		
		public BluetoothIOThread(BluetoothSocket socket) 
		{
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			try 
			{
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} 
			catch (IOException e) 
			{
				
			}
			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}
		
		public void run() 
		{
			Log.d("Bluetooth Logs", "Beginning read");
			byte[] buffer = new byte[1024];
			int bytes;
			// Keep listening to the InputStream while connected
			while (true) 
			{
				try 
				{
					// Read from the InputStream
					bytes = mmInStream.read(buffer);
					Log.d("Bluetooth Logs", "Read found data");
					// Send the obtained bytes to the UI Activity
					handler.obtainMessage(BluetoothService.MESSAGE_READ_INFO, bytes, -1, buffer)
							.sendToTarget();
				} 
				catch (IOException e) 
				{
					//Log.e(TAG, "disconnected", e);
					onConnectionLost();
					break;
				}
			}
		}
		
		public void write(byte[] buffer) 
		{
			try 
			{
				Log.d("Bluetooth Logs", "Write attempted");
				mmOutStream.write(buffer);
				// Share the sent message back to the UI Activity
				handler.obtainMessage(BluetoothService.MESSAGE_WRITE_INFO, -1, -1, buffer)
						.sendToTarget();
			} 
			catch (IOException e) 
			{
				//Log.e(TAG, "Exception during write", e);
				Log.d("Bluetooth Logs", "Write failed");
			}
		}
		
		public void cancel() 
		{
			try 
			{
				mmSocket.close();
			} 
			catch (IOException e) 
			{
				//Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}
}
