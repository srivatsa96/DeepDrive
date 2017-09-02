package org.finalyearproject.bit.carcontroller;

import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

/**
 * Created by Srivatsa Sinha on 27th, August 2017.
 */

public class TCPClient {

    private String IP;
    private int PORT;
    private Socket socket;
    private boolean status;

    private PrintWriter out;

    public TCPClient(String IP, int PORT) {
        this.IP = IP;
        this.PORT = PORT;
    }

    public void sendMessage(String message) {
        if(out!=null && !out.checkError()) {
            out.println(message);
            out.flush();
            Log.i("TCP: Sent ",message);
        }
    }

    public boolean run()  {
            try {
                InetAddress serverAddress = InetAddress.getByName(IP);
                socket = new Socket(serverAddress, PORT);
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                status = true;
            } catch (Exception ex) {
                Log.e("Exception: ",ex.toString());
                Log.e("Connection Error: ", "Unable to establish connection with " + IP + " " + PORT);
                status = false;
            }
        return status;
    }

    public void close() {
        try {
            socket.close();
        } catch(IOException ex) {
            Log.e("Socket Error","Error closing socket");
        }
    }

    public  boolean getStatus() {
        return  status;
    }
}
