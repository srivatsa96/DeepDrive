package org.finalyearproject.bit.carcontroller;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Srivatsa Sinha on 29th,August 2017.
 */

public class TCPClientThread extends Thread {

    private Handler mHandler;
    private Context context;

    private TCPClient tcpClient;
    private String IP;
    private int port;
    private Handler uiHandler;

    public TCPClientThread(String ip, int port, Handler uiHandler) {
        this.IP = ip;
        this.port = port;
        this.uiHandler = uiHandler;
    }

    public void run() {
        Looper.prepare();
        tcpClient = new TCPClient(IP,port);
        final boolean status = tcpClient.run();
        Message msg = uiHandler.obtainMessage(0,status);
        msg.sendToTarget();

        mHandler = new Handler() {
            public void handleMessage(Message message) {
                String data = (String)message.obj;
                tcpClient.sendMessage(data);

            }
        };
        Looper.loop();
    }

    public Handler getmHandler() {
        return mHandler;
    }
}
