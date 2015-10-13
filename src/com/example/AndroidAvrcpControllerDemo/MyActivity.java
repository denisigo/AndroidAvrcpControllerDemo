package com.example.AndroidAvrcpControllerDemo;

import android.app.Activity;
import android.bluetooth.*;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.lang.Override;
import java.util.List;

public class MyActivity extends Activity {
    private static final String TAG = "AndroidAvrcpControllerDemo";

    private BluetoothAvrcpController mAvrcpController;
    private TextView mStatusView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mStatusView = (TextView)findViewById(R.id.status);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.getProfileProxy(this, mAvrcpServiceListener, BluetoothProfile.AVRCP_CONTROLLER);
        mStatusView.setText("Connecting to the AVRCP_CONTROLLER service");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.closeProfileProxy(BluetoothProfile.AVRCP_CONTROLLER, mAvrcpController);
    }

    private BluetoothProfile.ServiceListener mAvrcpServiceListener = new BluetoothProfile.ServiceListener(){
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.AVRCP_CONTROLLER){
                mStatusView.setText("AVRCP_CONTROLLER connected");
                Log.d(TAG, "AvrcpControllerService connected");

                mAvrcpController = (BluetoothAvrcpController) proxy;

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
}
