package com.fluttershy.snake;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends Activity {
    private final int REQUEST_ENABLE_BT = 69;
    private ArrayAdapter<String> listAdapter;
    private Button connectNew;
    private ListView listView;
    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> devicesArray;
    private Context c = this;
    ArrayList<String> pairedDevices;
    IntentFilter filter;
    BroadcastReceiver receiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((Button) findViewById(R.id.btnSignUp)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(c, GameActivity.class);
                i.putExtra("mode", 1);
                startActivity(i);
            }
        });

        BluetoothManager b = new BluetoothManager(this, BluetoothAdapter.getDefaultAdapter().getAddress() == BluetoothManager.ZONG_PHONE_BT,
                BluetoothManager.ZONG_PHONE_BT);

    }

    public void connect(View v) {

        startActivity(new Intent(this, DisplayConnections.class));
    }


}