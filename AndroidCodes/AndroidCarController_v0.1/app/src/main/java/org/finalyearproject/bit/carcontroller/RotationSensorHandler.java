package org.finalyearproject.bit.carcontroller;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.app.Activity;

/**
 * Created by Srivatsa Sinha on 27th August, 2017
 */

public class RotationSensorHandler {

    public interface RotationListener {
        void onTiltChanged(float normalizedTilt, float tilt);
    }

    private SensorManager sensorManager;
    private RotationListener rotationListener;
    private float startTilt;
    private boolean turningDirection = true;

    private final SensorEventListener sensorEventListener = new SensorEventListener() {

        float[] rotationMatrix = new float[16];
        float[] rotationMatrixRemaped = new float[16];
        float[] orientation = new float[3];
        float tilt;
        float tiltNormalised;

        @Override
        public void onSensorChanged(SensorEvent event) {

            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            SensorManager.remapCoordinateSystem(rotationMatrix,SensorManager.AXIS_X, SensorManager.AXIS_Z, rotationMatrixRemaped);
            SensorManager.getOrientation(rotationMatrixRemaped, orientation);

            tilt = (float)Math.toDegrees(orientation[2]);
            if(startTilt==0) {
                startTilt = tilt;
            }

            tiltNormalised = (tilt - startTilt)/90;

            if (tiltNormalised < 2){
                if (tiltNormalised < 0){
                    turningDirection = true;
                }else{
                    turningDirection = false;
                }
            }else if(turningDirection){
                tiltNormalised =-1;
            }

            if (tiltNormalised > 1){
                tiltNormalised =1;
            }else if (tiltNormalised < -1){
                tiltNormalised =-1;
            }
            rotationListener.onTiltChanged(tiltNormalised,tiltNormalised*90);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
          //TODO
        }
    };


    RotationSensorHandler(Activity activity, RotationListener listener) {
        sensorManager = (SensorManager) activity.getSystemService(Activity.SENSOR_SERVICE);
        rotationListener = listener;
    }

    void startListening() {
        startTilt = 0;
        Sensor rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorManager.registerListener(sensorEventListener, rotationSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    void stopListening() {
        sensorManager.unregisterListener(sensorEventListener);
    }

    void reCallibrate() {
        startTilt = 0;
    }
}
