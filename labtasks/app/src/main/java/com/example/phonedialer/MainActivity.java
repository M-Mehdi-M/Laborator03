package com.example.phonedialer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // Constants formerly from a separate Constants file
    private final int[] buttonIds = {
            R.id.number_0_button,
            R.id.number_1_button,
            R.id.number_2_button,
            R.id.number_3_button,
            R.id.number_4_button,
            R.id.number_5_button,
            R.id.number_6_button,
            R.id.number_7_button,
            R.id.number_8_button,
            R.id.number_9_button,
            R.id.star_button,
            R.id.pound_button
    };
    private static final int PERMISSION_REQUEST_CALL_PHONE = 1;

    private EditText phoneNumberEditText;
    private ImageButton callImageButton;
    private ImageButton hangupImageButton;
    private ImageButton backspaceImageButton;
    private TextView oxValueTextView;

    // Accelerometer fields
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private SensorEventListener accelerometerListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneNumberEditText = findViewById(R.id.phone_number_edit_text);
        callImageButton = findViewById(R.id.call_image_button);
        hangupImageButton = findViewById(R.id.hangup_image_button);
        backspaceImageButton = findViewById(R.id.backspace_image_button);
        oxValueTextView = findViewById(R.id.ox_value_text_view);

        callImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Permission check
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.CALL_PHONE},
                            PERMISSION_REQUEST_CALL_PHONE
                    );
                } else {
                    String phoneNumber = phoneNumberEditText.getText().toString();
                    if (!phoneNumber.isEmpty()) {
                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse("tel:" + phoneNumber));
                        startActivity(intent);
                    }
                }
            }
        });

        hangupImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        backspaceImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = phoneNumberEditText.getText().toString();
                if (phoneNumber.length() > 0) {
                    phoneNumberEditText.setText(phoneNumber.substring(0, phoneNumber.length() - 1));
                }
            }
        });

        // Numeric keypad buttons
        View.OnClickListener genericButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phoneNumberEditText.setText(
                        phoneNumberEditText.getText().toString() + ((Button) view).getText().toString()
                );
            }
        };
        for (int id : buttonIds) {
            Button button = findViewById(id);
            button.setOnClickListener(genericButtonClickListener);
        }

        // Accelerometer initialization
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        accelerometerListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float x = event.values[0];
                oxValueTextView.setText(String.format("Ox: %.2f", x));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) { }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometerSensor != null) {
            sensorManager.registerListener(accelerometerListener, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (accelerometerSensor != null) {
            sensorManager.unregisterListener(accelerometerListener);
        }
    }

    // Handle permissions result
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Try calling again if user granted permission
                String phoneNumber = phoneNumberEditText.getText().toString();
                if (!phoneNumber.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + phoneNumber));
                    startActivity(intent);
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
