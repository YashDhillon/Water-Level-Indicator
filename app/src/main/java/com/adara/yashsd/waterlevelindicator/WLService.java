package com.adara.yashsd.waterlevelindicator;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class WLService extends Service {
    String address = null;

    boolean isConnected = false;
    boolean connectSuccess = false;
    boolean isConnectionAlive = true;
    boolean isConnectionAliveFOR1 = false;

    BluetoothAdapter bluetoothAdapter = null;
    BluetoothSocket bluetoothSocket = null;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private IntentFilter intentFilter;

    @Override
    public void onCreate() {
        super.onCreate();

        intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.REFRESH);
        intentFilter.addAction(MainActivity.POWER);
        intentFilter.addAction(MainActivity.TIMEFUNCTION);
        intentFilter.addAction(MainActivity.TIMEFUNCTIONFOR);

        registerReceiver(receiver, intentFilter);
        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("HandlerLeak")
    Handler bthandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            byte[] writeBuf = (byte[]) msg.obj;
            int begin = (int) msg.arg1;
            int end = (int) msg.arg2;
            switch (msg.what) {
                case 1: {
                    String writeMessage = new String(writeBuf);
                    writeMessage = writeMessage.substring(begin, end);

                    char reply = writeMessage.charAt(0);

                    if (reply == '0') {
                        isConnectionAliveFOR1 = true;

                        int flowRateBeg = writeMessage.indexOf("(");
                        int flowRateFin = writeMessage.indexOf(")");
                        String flowRate = writeMessage.substring(flowRateBeg + 1, flowRateFin);

                        int tankStatusBeg = writeMessage.indexOf("<");
                        int tankStatusFin = writeMessage.indexOf(">");
                        String tankStatus = writeMessage.substring(tankStatusBeg + 1, tankStatusFin);

                        int timeStatusBeg = writeMessage.indexOf("[");
                        int timeStatusFin = writeMessage.indexOf("]");
                        String timeStatus = writeMessage.substring(timeStatusBeg + 1, timeStatusFin);

                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction(MainActivity.REFRESHRESULT);
                        broadcastIntent.putExtra("flowRate", flowRate);
                        broadcastIntent.putExtra("tankStatus", tankStatus);
                        broadcastIntent.putExtra("timeStatus", timeStatus);
                        sendBroadcast(broadcastIntent);
                    } else if (reply == '1') {
                        isConnectionAliveFOR1 = true;

                        int flowRateBeg = writeMessage.indexOf("(");
                        int flowRateFin = writeMessage.indexOf(")");
                        String flowRate = writeMessage.substring(flowRateBeg + 1, flowRateFin);

                        int tankStatusBeg = writeMessage.indexOf("<");
                        int tankStatusFin = writeMessage.indexOf(">");
                        String tankStatus = writeMessage.substring(tankStatusBeg + 1, tankStatusFin);

                        int timeStatusBeg = writeMessage.indexOf("[");
                        int timeStatusFin = writeMessage.indexOf("]");
                        String timeStatus = writeMessage.substring(timeStatusBeg + 1, timeStatusFin);

                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction(MainActivity.NORMALUPDATERESULT);
                        broadcastIntent.putExtra("flowRate", flowRate);
                        broadcastIntent.putExtra("tankStatus", tankStatus);
                        broadcastIntent.putExtra("timeStatus", timeStatus);
                        sendBroadcast(broadcastIntent);
                    }

                    /*if (writeMessage.equals("1")) {
                        isConnectionAliveFOR1 = true;
                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction(MainActivity.REFRESHRESULT);
                        sendBroadcast(broadcastIntent);
                    } else if (writeMessage.equals("0")) {
                        /*isConnectionAlive = true;
                        isConnectionAliveFOR1 = false;
                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction(MainActivity.Alert_OFF);
                        sendBroadcast(broadcastIntent);
                    } else if (writeMessage.equals("2")) {
                        isConnectionAlive = true;
                    }/*else if (writeMessage.equals("3")) {
                        changeAlertFileStatusToZero();
                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction(MainActivity.ALERT_SEMI);
                        sendBroadcast(broadcastIntent);
                    }*/

                    break;
                }
                default:
                    break;
            }
        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MainActivity.REFRESH)) {
                try {
                    bluetoothSocket.getOutputStream().write("00000:".toString().getBytes());
                } catch (Exception e) {
                }
            } else if (intent.getAction().equals(MainActivity.POWER)) {
                try {
                    bluetoothSocket.getOutputStream().write("11111:".toString().getBytes());
                } catch (Exception e) {
                }
            } else if (intent.getAction().equals(MainActivity.TIMEFUNCTION)) {
                String starttime = intent.getExtras().getString("starttime");
                String stoptime = intent.getExtras().getString("stoptime");
                String currenttime = intent.getExtras().getString("currenttime");
                try {
                    bluetoothSocket.getOutputStream().write(("22222" + "Start" + starttime + "Stop" + stoptime + "Curr" + currenttime +":").toString().getBytes());
                } catch (Exception e) {
                }
            } else if (intent.getAction().equals(MainActivity.TIMEFUNCTIONFOR)) {
                String startForTime = intent.getExtras().getString("startForTime");
                try {
                    bluetoothSocket.getOutputStream().write(("33333" + "Start" + startForTime + "Stop0000" + ":").toString().getBytes());
                } catch (Exception e) {
                }
            }
            /* else if (intent.getAction().equals(Main2Activity.ManModeG)) {
                int change = intent.getExtras().getInt("change");
                int delta = intent.getExtras().getInt("delta");

                if(change == 1){
                    try {
                        bluetoothSocket.getOutputStream().write(("G-" + String.format("%03d", delta)+":").toString().getBytes());
                    } catch (Exception e) {
                    }
                } else if(change == 0){
                    try {
                        bluetoothSocket.getOutputStream().write(("G+" + String.format("%03d", delta)+":").toString().getBytes());
                    } catch (Exception e) {
                    }
                }
            }
            else if (intent.getAction().equals(Main2Activity.ManModeB)) {
                int change = intent.getExtras().getInt("change");
                int delta = intent.getExtras().getInt("delta");

                if(change == 1){
                    try {
                        bluetoothSocket.getOutputStream().write(("B-" + String.format("%03d", delta)+":").toString().getBytes());
                    } catch (Exception e) {
                    }
                } else if(change == 0){
                    try {
                        bluetoothSocket.getOutputStream().write(("B+" + String.format("%03d", delta)+":").toString().getBytes());
                    } catch (Exception e) {
                    }
                }

            }*/
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        address = intent.getExtras().getString("address");
        new connectBT().execute();
        return super.onStartCommand(intent, flags, startId);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class connectBT extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                if (bluetoothSocket == null || !isConnected) {
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice BTdevice = bluetoothAdapter.getRemoteDevice(address);
                    bluetoothSocket = BTdevice.createInsecureRfcommSocketToServiceRecord(myUUID);
                    bluetoothAdapter.cancelDiscovery();
                    bluetoothSocket.connect();

                    connectSuccess = true;
                }
            } catch (Exception e) {
                connectSuccess = false;
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            Toast.makeText(WLService.this, "Connecting", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (connectSuccess) {
                Toast.makeText(WLService.this, "Connected", Toast.LENGTH_SHORT).show();
                isConnected = true;

                dynamicConnectionStatus mdynamicConnectionStatus = new dynamicConnectionStatus();
                mdynamicConnectionStatus.start();
                connectedThread mConnectedThread = new connectedThread(bluetoothSocket);
                mConnectedThread.start();
            } else if (!connectSuccess) {
                Toast.makeText(WLService.this, "Connection failed try again", Toast.LENGTH_SHORT).show();
                isConnected = false;
                stopSelf();
            }
        }
    }

    private class connectedThread extends Thread {
        private final BluetoothSocket mysocket;
        private final InputStream minstr;
        private final OutputStream moutstr;

        public connectedThread(BluetoothSocket mysocket) {
            this.mysocket = mysocket;
            InputStream tempin = null;
            OutputStream tempout = null;
            try {
                tempin = mysocket.getInputStream();
                tempout = mysocket.getOutputStream();
            } catch (IOException e) {
            }
            minstr = tempin;
            moutstr = tempout;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int begin = 0;
            int bytes = 0;
            while (true) {
                try {
                    bytes += minstr.read(buffer, bytes, buffer.length - bytes);
                    for (int i = begin; i < bytes; i++) {
                        if (buffer[i] == "#".getBytes()[0]) {
                            bthandler.obtainMessage(1, begin, i, buffer).sendToTarget();
                            begin = i + 1;
                            if (i == bytes - 1) {
                                bytes = 0;
                                begin = 0;
                            }
                        }
                    }
                } catch (IOException e) {
                    break;
                }

            }
        }
    }


    private class dynamicConnectionStatus extends Thread {
        public dynamicConnectionStatus() {
        }

        public void run() {


            while (true) {

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (isConnectionAliveFOR1 == false) {

                    if (isConnectionAlive == true) {
                        isConnectionAlive = false;
                    } else if (isConnectionAlive == false) {
                        stopForeground(true);
                        stopSelf();
                    }
                }
            }
        }
    }
}
