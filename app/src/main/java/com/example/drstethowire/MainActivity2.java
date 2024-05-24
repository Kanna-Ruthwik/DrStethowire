package com.example.drstethowire;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MainActivity2 extends AppCompatActivity {

    private static final String TAG = "MainActivity2";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION_BLUETOOTH_CONNECT = 1001;

    private static final String DEVICE_NAME = "HC-06"; // change this to your device name
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // change this to your device UUID

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;

    private ToggleButton toggleButton2;
    private GraphView graphView2;
    private Button scanbtn;
    private boolean isScanning = false;
    private LineGraphSeries<DataPoint> series;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        toggleButton2 = findViewById(R.id.toggleButton2);
        graphView2 = findViewById(R.id.graphView2);
        scanbtn = findViewById(R.id.buttonScan);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Intent intent = getIntent();
        String patientName = intent.getStringExtra("patientName");
        String patientAge = intent.getStringExtra("patientAge");
        String patientAddress = intent.getStringExtra("patientAddress");

        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
        }

        toggleButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toggleButton2.isChecked()) {
                    // Connect to the device
                    connectDevice();
                } else {
                    // Disconnect from the device
                    disconnectDevice();
                }
            }
        });

        graphView2.getViewport().setXAxisBoundsManual(true);
        graphView2.getViewport().setMinX(0);
        graphView2.getViewport().setMaxX(100);
        graphView2.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graphView2.getGridLabelRenderer().setVerticalLabelsVisible(false);

        series = new LineGraphSeries<>();
        series.setColor(Color.BLUE);
        graphView2.addSeries(series);

        scanbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isScanning) {
                    // Start plotting the graph
                    isScanning = true;
                    scanbtn.setText("Stop");
                    plotGraph();
                } else {
                    // Stop plotting the graph and generate PDF
                    isScanning = false;
                    scanbtn.setVisibility(View.GONE);
                    generatePDF(patientName,patientAge,patientAddress);
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
            toggleButton2.setChecked(false);
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
            toggleButton2.setChecked(false);
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
            finish();
        } catch (IOException e) {
            // Disconnection failed
            Log.e(TAG, "Disconnection failed", e);
            Toast.makeText(this, "Disconnection failed", Toast.LENGTH_SHORT).show();
        }
    }


    // Other methods remain unchanged

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


    private void generatePDF(String patientName, String patientAge, String patientAddress) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo1 = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page1 = document.startPage(pageInfo1);
        Canvas canvas1 = page1.getCanvas();

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(30);

// Load the logo drawable
        Drawable logoDrawable = getResources().getDrawable(R.drawable.stethowire);
        Bitmap logoBitmap = Bitmap.createBitmap(logoDrawable.getIntrinsicWidth(), logoDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas logoCanvas = new Canvas(logoBitmap);
        logoDrawable.setBounds(0, 0, logoCanvas.getWidth(), logoCanvas.getHeight());
        logoDrawable.draw(logoCanvas);

// Resize the bitmap
        int desiredWidth = 500;
        int desiredHeight = 500;
        Bitmap resizedLogoBitmap = Bitmap.createScaledBitmap(logoBitmap, desiredWidth, desiredHeight, false);

// Draw the logo above the "Medical Report" text
        int logoX = 50;
        int logoY = 20;
        canvas1.drawBitmap(resizedLogoBitmap, logoX, logoY, paint); // Adjust position as needed
        int textX = 50;
        int textY = logoY + resizedLogoBitmap.getHeight() + 30; // Adjust as needed
        canvas1.drawText("Medical Report", textX, textY, paint);

// Draw patient details
        paint.setTextSize(20); // Resetting font size for patient details
        int detailsStartY = textY + 50; // Adjust as needed
        canvas1.drawText("Patient Name: " + patientName, textX, detailsStartY, paint);
        canvas1.drawText("Patient Age: " + patientAge, textX, detailsStartY + 30, paint); // Adjust as needed
        canvas1.drawText("Patient Address: " + patientAddress, textX, detailsStartY + 60, paint); // Adjust as needed

        document.finishPage(page1);


        PdfDocument.PageInfo pageInfo2 = new PdfDocument.PageInfo.Builder(graphView2.getWidth(), graphView2.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo2);
        Canvas canvas = page.getCanvas();
        try {

            graphView2.draw(canvas);
            document.finishPage(page);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Dr.stethowire_" +patientName+timeStamp + ".pdf";
            File file = new File(filePath);
            document.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "PDF Generated Successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "PDF generation failed", e);
            Toast.makeText(this, "PDF Generation Failed", Toast.LENGTH_SHORT).show();
        } finally {
            document.close();
        }
    }

}
