package com.fluttershy.snake;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Chen on 2/13/2016.
 */
public class BluetoothManager {
    private UUID uuid = UUID.fromString("ffb0070b-b5e6-4c35-b2ba-e05f370aa993");
    private Context c;
    private BluetoothAdapter badapter;
    private boolean isServer;
    private BluetoothServerSocket server;
    private BluetoothSocket socket;
    private BluetoothDevice target;

    public static final String ZONG_PHONE_NAME = "ffb0070b-b5e6-4c35-b2ba-e05f370aa993";

    public BluetoothManager(Context c, boolean server, String name){
        this.isServer = server;
        this.c = c;
        badapter = BluetoothAdapter.getDefaultAdapter();

        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3000);
        c.startActivity(discoverableIntent);

        Set<BluetoothDevice> devices = badapter.getBondedDevices();
        for(BluetoothDevice device: devices){
            Log.d("device Found", device.getName() + "\n" + device.getAddress());
            if(device.getName() == name){
                target = device;
            }
        }
        if(isServer){
            try {
                this.server = badapter.listenUsingInsecureRfcommWithServiceRecord("SnakeBT", uuid);
                Thread t = new Thread(new Accept());
                t.start();
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
        else{
            Thread t = new Thread(new ClientAccept());
            t.start();
        }
    }

    private class ClientAccept implements Runnable{

        @Override
        public void run() {
            try{
                socket = target.createRfcommSocketToServiceRecord(uuid);
            }
            catch (IOException e){
                e.printStackTrace();
            }
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                socket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    socket.close();
                } catch (IOException closeException) { }
                return;
            }
        }
    }

    private class Accept implements Runnable{

        @Override
        public void run() {
            try {
                socket = server.accept();
                server.close();
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}
