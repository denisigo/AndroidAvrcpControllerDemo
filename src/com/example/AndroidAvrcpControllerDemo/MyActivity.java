package com.example.AndroidAvrcpControllerDemo;

import android.app.Activity;
import android.bluetooth.*;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.lang.Override;
import java.util.List;

public class MyActivity extends Activity {
    private static final String TAG = "AndroidAvrcpControllerDemo";

    private BluetoothAvrcpController mAvrcpController;
    private TextView mStatusView;
    private TextView mAttrsView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mStatusView = (TextView)findViewById(R.id.status);
        mAttrsView = (TextView)findViewById(R.id.attrs);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.getProfileProxy(this, mAvrcpServiceListener, BluetoothProfile.AVRCP_CONTROLLER);
        mStatusView.setText("Connecting to the AVRCP_CONTROLLER service");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mAvrcpController.removeCallback();
        bluetoothAdapter.closeProfileProxy(BluetoothProfile.AVRCP_CONTROLLER, mAvrcpController);
    }

    private BluetoothProfile.ServiceListener mAvrcpServiceListener = new BluetoothProfile.ServiceListener(){
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.AVRCP_CONTROLLER){
                mStatusView.setText("AVRCP_CONTROLLER connected");
                Log.d(TAG, "AvrcpControllerService connected");

                mAvrcpController = (BluetoothAvrcpController) proxy;
                mAvrcpController.setCallback(new AvrcpControllerCallback());

                mStatusView.append("\r\nAvrcp devices: \r\n");
                List<BluetoothDevice> devices = mAvrcpController.getConnectedDevices();
                for (BluetoothDevice device : devices)
                    mStatusView.append(" - " + device.getName() + " " + device.getAddress()+"\r\n");
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.AVRCP_CONTROLLER) {
                mStatusView.setText("AVRCP_CONTROLLER disconnected");
                Log.d(TAG, "AvrcpControllerService disconnected");
                mAvrcpController.removeCallback();
                mAvrcpController = null;
            }
        }
    };

    private void sendCommand(int keyCode){
        if (mAvrcpController == null)
            return;

        List<BluetoothDevice> devices = mAvrcpController.getConnectedDevices();
        for (BluetoothDevice device : devices){
            Log.d(TAG, "send command to device: " + device.getName() + " " + device.getAddress());
            mAvrcpController.sendPassThroughCmd(device, keyCode, BluetoothAvrcp.PASSTHROUGH_STATE_PRESS);
            mAvrcpController.sendPassThroughCmd(device, keyCode, BluetoothAvrcp.PASSTHROUGH_STATE_RELEASE);
        }
    }

    public void onPlayButtonClick(View view){
        sendCommand(BluetoothAvrcp.PASSTHROUGH_ID_PLAY);
    }

    public void onStopButtonClick(View view){
        sendCommand(BluetoothAvrcp.PASSTHROUGH_ID_STOP);
    }

    public void onPauseButtonClick(View view){
        sendCommand(BluetoothAvrcp.PASSTHROUGH_ID_PAUSE);
    }

    public void onNextButtonClick(View view){
        sendCommand(BluetoothAvrcp.PASSTHROUGH_ID_FORWARD);
    }

    public void onPrevButtonClick(View view){
        sendCommand(BluetoothAvrcp.PASSTHROUGH_ID_BACKWARD);
    }

    public void onGetAttrsButtonClick(View view){
        if (mAvrcpController == null)
            return;

        int[] attrs = {BluetoothAvrcp.MEDIA_ATTR_TITLE,
                BluetoothAvrcp.MEDIA_ATTR_ARTIST,
                BluetoothAvrcp.MEDIA_ATTR_ALBUM,
                BluetoothAvrcp.MEDIA_ATTR_PLAYING_TIME,
                BluetoothAvrcp.MEDIA_ATTR_NUM_TRACKS,
                BluetoothAvrcp.MEDIA_ATTR_TRACK_NUM,
        };
        List<BluetoothDevice> devices = mAvrcpController.getConnectedDevices();
        for (BluetoothDevice device : devices){
            Log.d(TAG, "send getAttrs to device: " + device.getName() + " " + device.getAddress());
            mAvrcpController.getElementAttr(device, attrs.length, attrs);
        }
    }

    private class AvrcpControllerCallback extends IBluetoothAvrcpControllerCallback.Stub {

        private static final String TAG = "AvrcpControllerCallback";

        @Override
        public IBinder asBinder() {
            return this;
        }

        @Override
        public void onGetElementAttrRsp(final int numAttr, final int[] attrs, final String[] values) throws RemoteException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAttrsView.setText("");
                    for (int i = 0; i < numAttr; i++) {
                        if (attrs[i] == BluetoothAvrcp.MEDIA_ATTR_TITLE)
                            mAttrsView.append("Title: " + values[i] + "\r\n");
                        else if (attrs[i] == BluetoothAvrcp.MEDIA_ATTR_ARTIST)
                            mAttrsView.append("Artist: " + values[i] + "\r\n");
                        else if (attrs[i] == BluetoothAvrcp.MEDIA_ATTR_ALBUM)
                            mAttrsView.append("Album: " + values[i] + "\r\n");
                        else if (attrs[i] == BluetoothAvrcp.MEDIA_ATTR_GENRE)
                            mAttrsView.append("Genre: " + values[i] + "\r\n");
                        else if (attrs[i] == BluetoothAvrcp.MEDIA_ATTR_NUM_TRACKS)
                            mAttrsView.append("Num tracks: " + values[i] + "\r\n");
                        else if (attrs[i] == BluetoothAvrcp.MEDIA_ATTR_TRACK_NUM)
                            mAttrsView.append("Track num: " + values[i] + "\r\n");

                        else if (attrs[i] == BluetoothAvrcp.MEDIA_ATTR_PLAYING_TIME)
                            mAttrsView.append("Playing time: " + values[i] + " ms \r\n");

                        Log.d(TAG, "onGetElementAttrRsp attr_id:" + attrs[i] + " value: " + values[i]);
                    }
                }
            });

        }
    }



}
