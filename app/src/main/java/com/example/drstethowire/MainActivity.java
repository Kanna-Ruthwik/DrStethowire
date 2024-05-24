package com.example.drstethowire;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String DEVICE_NAME = "HC-06"; // change this to your device name
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // change this to your device UUID

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;

    private ToggleButton toggleButton;
    private GraphView graphView;
    private Button scanbtn;
    private boolean isScanning = false;
    private LineGraphSeries<DataPoint> series;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toggleButton = findViewById(R.id.toggleButton);
        graphView = findViewById(R.id.graphView);
        scanbtn = findViewById(R.id.scan_btn);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
        }

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toggleButton.isChecked()) {
                    // Connect to the device
                    connectDevice();
                } else {
                    // Disconnect from the device
                    disconnectDevice();
                }
            }
        });

        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(100);
        graphView.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graphView.getGridLabelRenderer().setVerticalLabelsVisible(false);

        series = new LineGraphSeries<>();
        series.setColor(Color.BLUE);
        graphView.addSeries(series);

        scanbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isScanning) {
                    // Start plotting the graph
                    isScanning = true;
                    scanbtn.setText("Stop");
                    plotGraph();
                } else {
                    // Stop plotting the graph
                    isScanning = false;
                    scanbtn.setText("Start");
                    scanbtn.setVisibility(View.GONE);

                }
            }
        });
    }

    private void connectDevice() {
        // Scan for paired devices
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Loop through paired devices
                if (device.getName().equals(DEVICE_NAME)) {
                    // Found the device
                    bluetoothDevice = device;
                    break;
                }
            }
        }

        if (bluetoothDevice == null) {
            // Device is not paired
            Toast.makeText(this, "Device is not paired", Toast.LENGTH_SHORT).show();
            toggleButton.setChecked(false);
            return;
        }

        try {
            // Create a socket to connect to the device
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
            bluetoothSocket.connect();
            inputStream = bluetoothSocket.getInputStream();
            Toast.makeText(this, "Connected to the device", Toast.LENGTH_SHORT).show();
            // Start a thread to receive and plot the data
            scanbtn.setVisibility(View.VISIBLE);
        } catch (IOException e) {
            // Connection failed
            Log.e(TAG, "Connection failed", e);
            Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
            toggleButton.setChecked(false);
        }
    }

    private void disconnectDevice() {
        try {
            // Close the socket and the input stream
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            Toast.makeText(this, "Disconnected from the device", Toast.LENGTH_SHORT).show();
            //finish(); // You may not want to finish the activity here
        } catch (IOException e) {
            // Disconnection failed
            Log.e(TAG, "Disconnection failed", e);
            Toast.makeText(this, "Disconnection failed", Toast.LENGTH_SHORT).show();
        }
    }

    // Plot graph logic remains unchanged

    private void plotGraph() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[1024];
                int bytes;
                int x = 0;
                while (isScanning) {
                    try {
                        bytes = inputStream.read(buffer);
                        String data = new String(buffer, 0, bytes).trim(); // Trim the string
                        // Split the string by newline characters
                        String[] values = data.split("\\r?\\n");
                        for (String value : values) {
                            if (!value.isEmpty()) { // Check if the value is not empty
                                final double y = Double.parseDouble(value);
                                final int finalX = x;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        series.appendData(new DataPoint(finalX, y), true, 100);
                                    }
                                });
                                x++;
                            }
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Reading failed", e);
                        break;
                    }
                }
            }
        }).start();
    }
}
