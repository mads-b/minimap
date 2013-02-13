package com.eit.minimap.gps;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Arrays;

/**
 * Class for getting the compass output from the phone. Output is filtered to reduce noise.
 * Usage: Create class, and call registerListeners().
 * After this, one can call getOrientation() to get the angle (phone bearing off north).
 */
class CompassProcessor implements SensorEventListener {
    /* sensor data */
    private final SensorManager m_sensorManager;
    private final float []m_lastMagFields = new float[3];
    private final float []m_lastAccels = new float[3];
    private final float[] m_rotationMatrix = new float[16];
    private final float[] m_orientation = new float[4];

    private final DecayingAverageFilter m_filter = new DecayingAverageFilter();



    public CompassProcessor(Activity ac) {
        m_sensorManager = (SensorManager) ac.getSystemService(Context.SENSOR_SERVICE);
    }

    public void registerListeners() {
        m_sensorManager.registerListener(this, m_sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
        m_sensorManager.registerListener(this, m_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
    }

    public void unregisterListeners() {
        m_sensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accel(event);
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mag(event);
        }
    }

    private void accel(SensorEvent event) {
        System.arraycopy(event.values, 0, m_lastAccels, 0, 3);
    }

    private void mag(SensorEvent event) {
        System.arraycopy(event.values, 0, m_lastMagFields, 0, 3);
        computeOrientation();
    }

    /**
     * Moving average filter
     */
    private class MovingAverageFilter {
        static final int AVERAGE_BUFFER = 20;
        final float[] m_arr = new float[AVERAGE_BUFFER];
        int m_idx = 0;
        float lastVal=0;

        public void append(float val) {
            m_arr[m_idx] = val;
            //BUGFIX: filter produces bad results when angle goes from -pi to pi, even though these two are equivalent.
            if(Math.abs(val-lastVal)>Math.PI) {
                Arrays.fill(m_arr,val);
            }
            lastVal=val;

            m_idx++;
            if (m_idx == AVERAGE_BUFFER)
                m_idx = 0;
        }
        public float getFiltered() {
            float sum = 0;

            for (float x: m_arr) {
                sum += x;
            }
            return sum / AVERAGE_BUFFER;
        }
    }

    private class DecayingAverageFilter {
        //A decay factor of 0.9 means that every new measurement contributes 10% of the resulting value.
        static final double DECAY_FACTOR = 0.9;
        double avg=0;

        public void append(double val) {
            if(Math.abs(val-avg)>Math.PI) {
                avg=val;
            }
            avg = val*(1-DECAY_FACTOR)+avg*DECAY_FACTOR;
        }
        public double getFiltered() {
            return avg;
        }
    }

    private void computeOrientation () {
        if (SensorManager.getRotationMatrix(m_rotationMatrix, null, m_lastMagFields, m_lastAccels)) {
            SensorManager.getOrientation(m_rotationMatrix, m_orientation);
            /* yaw, rotation around z axis */
            float yaw = m_orientation[0];
            m_filter.append(yaw);
        }
    }

    public double getOrientation() {
        return m_filter.getFiltered();

    }

}
