package org.finalyearproject.bit.carcontroller;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.sdsmdg.harjot.crollerTest.Croller;

/**
 * Created by Srivatsa Sinha 27th August, 2017
 */

public class MainActivity extends AppCompatActivity implements RotationSensorHandler.RotationListener,IpSettingsDialogFragment.IpSettingsListener  {

    private SharedPreferences sharedPreferences;

    private RotationSensorHandler rotationSensorHandler;
    private TextView tilt;
    private TextView tiltNormalized;
    private TextView speedIndicator;

    private Button calibrateButton;
    private Button startButton;
    private Button stopButton;

    private Croller speedAdjust;

    private Boolean running;
    private Boolean connectionStatus;

    private String ip;
    private int port;

    private TCPClientThread tcpClientThread;
    private Handler uiHandler;

    public void initSharedValues() {
        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        ip = sharedPreferences.getString("IP",null);
        port = sharedPreferences.getInt("PORT",-1);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Activity self = this;
        initSharedValues();
        getHandlers();
        registerListeners();
    }

    private void getHandlers() {
        tiltNormalized = (TextView)findViewById(R.id.tiltNormalised);
        tilt = (TextView)findViewById(R.id.tilt);
        speedIndicator = (TextView)findViewById(R.id.speedIndicate);
        speedAdjust = (Croller)findViewById(R.id.croller);
        startButton = (Button)findViewById(R.id.startButton);
        stopButton = (Button)findViewById(R.id.stopButton);
        calibrateButton = (Button)findViewById(R.id.calibrate);
    }

    private void resetState() {
        running = false;
        speedIndicator.setText("Stopped");
        rotationSensorHandler.stopListening();
        tiltNormalized.setText("Normalised Angle: 0.00");
        tilt.setText("Angle: 0");
        speedAdjust.setProgress(0);

        if(tcpClientThread!=null && tcpClientThread.getmHandler()!=null) {
            tcpClientThread.getmHandler().getLooper().quitSafely();
        }
    }

    private void startState() {
        speedAdjust.setProgress(0);
        speedIndicator.setText(Integer.toString(speedAdjust.getProgress()));
        rotationSensorHandler.startListening();
        running = true;
    }

    private void registerListeners() {

        uiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                connectionStatus = (boolean)msg.obj;
                if(connectionStatus) {
                    startState();
                    raiseInfoToast("Successfully Connected");
                } else {
                    resetState();
                    raiseInfoToast("Connection to server failed");
                }
            }
        };

        calibrateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                rotationSensorHandler.reCallibrate();
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            public  void onClick(View v) {
                if(!running) {
                    if(ip!=null && port!=-1) {;
                        tcpClientThread = new TCPClientThread(ip,port,uiHandler);
                        tcpClientThread.start();
                    } else {
                        raiseInfoToast("Please enter valid IP and Port");
                    }

                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendMessage("stop");
                resetState();
            }
        });

        speedAdjust.setLabel("Speed");
        speedAdjust.setOnProgressChangedListener(new Croller.onProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                if(running) {
                    speedIndicator.setText(Integer.toString(progress));
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        rotationSensorHandler = new RotationSensorHandler(this,this);
        resetState();
    }

    @Override
    protected  void onPause() {
        super.onPause();
        resetState();
    }

    @Override
    public void onTiltChanged(float normalizedTilt, float tilt) {
        tiltNormalized.setText("Normalised Angle: "+String.format("%.2f",normalizedTilt));
        this.tilt.setText("Angle: "+Math.round(tilt));
        String message = Math.round(tilt)+"|"+speedAdjust.getProgress();
        sendMessage(message);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_ip_setting:
                DialogFragment ipSettingsDialogFragment = new IpSettingsDialogFragment();
                ipSettingsDialogFragment.show(getSupportFragmentManager(),"ip");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveIpClick(String ipString, String portString) {
        try {
            ip = ipString;
            if(!new IPAddressValidator().validate(ip)) {
                throw new Exception("Invalid IP");
            }
            port = Integer.parseInt(portString);
            if(port>65535) {
                throw new Exception("Invalid Port");
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("IP", ip);
            editor.putInt("PORT", port);
        } catch (Exception ex) {
            raiseInfoToast("Please enter valid IP and Port");
            ip = null;
            port = -1;
        }
    }

    public void raiseInfoToast(String message) {
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(getApplicationContext(), message, duration);
        toast.show();
    }

    public void sendMessage(String data) {
        if(tcpClientThread.getmHandler() != null) {
            Message message = tcpClientThread.getmHandler().obtainMessage();
            message.obj = data;
            tcpClientThread.getmHandler().sendMessage(message);
        }
    }
}
