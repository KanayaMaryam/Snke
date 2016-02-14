package com.fluttershy.snake;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {
    final int REQUEST_ENABLE_BT=69;
    ArrayAdapter<String> listAdapter;
    Button connectNew;
    ListView listView;
    BluetoothAdapter mBluetoothAdapter;
    Set<BluetoothDevice> devicesArray;
    ArrayList<String> pairedDevices;
    IntentFilter filter;
    BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3){


    }
    public void connect (View v){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            CharSequence text = "Your device does not allow bluetooth";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(this, text, duration);
            toast.show();
        }
        else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else{
            getPairedDevices();
            startDiscovery();
        }

    }
    private void startDiscovery(){
        mBluetoothAdapter.cancelDiscovery();
        mBluetoothAdapter.startDiscovery();
    }
    public void getPairedDevices(){
        devicesArray=mBluetoothAdapter.getBondedDevices();
        if (devicesArray.size() > 0){
            for(BluetoothDevice device:devicesArray){
                pairedDevices.add(device.getName());

            }
        }
    }
    private void init(){
        connectNew=(Button)findViewById(R.id.btooth);
        listView=(ListView)findViewById(R.id.list);
        listView.setOnItemClickListener(this);
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 0);
        listView.setAdapter(listAdapter);
        pairedDevices = new ArrayList<String>();
        filter=new IntentFilter(BluetoothDevice.ACTION_FOUND);
        receiver =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action=intent.getAction();
                if(BluetoothDevice.ACTION_FOUND.equals(action)){
                    BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String s="";
                    for(int a=0; a<pairedDevices.size(); a++){
                        if(device.getName().equals(pairedDevices.get(a))){
                            s="(Paired)";
                            break;
                        }
                    }
                    listAdapter.add(device.getName()+"\n"+device.getAddress());
                }
                else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){

                }
                else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){

                }
                else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                    if(mBluetoothAdapter.getState()==mBluetoothAdapter.STATE_OFF){
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

                    }

                }
            }
        };
        this.registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        this.registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(receiver, filter);

    }
    protected void onPause(){
        super.onPause();
        unregisterReceiver(receiver);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_CANCELED){
            CharSequence text = "Y u no enable blutoth";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(this, text, duration);
            toast.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
