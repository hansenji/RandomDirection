package com.vikingsen.randomdirection;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import java.util.Random;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import pocketknife.PocketKnife;
import pocketknife.SaveState;

public class CompassFragment extends Fragment implements SensorEventListener {

    @InjectView(R.id.compass_needle)
    View compassNeedle;
    @InjectView(R.id.direction_text)
    TextView directionText;

    @SaveState
    Direction direction =  Direction.UNKNOWN;

    Random random = new Random();
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnometer;
    private float[] lastAccelerometer = new float[3];
    private float[] lastMagnometer = new float[3];
    private boolean lastAccelerometerSet = false;
    private boolean lastMagnomaeterSet = false;
    private float[] r = new float[9];
    private float[] orientation = new float[3];
    private float currentDegree = 0f;

    private static final float EPSILON = 10.0f;

    public static CompassFragment newInstance() {
        return new CompassFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        PocketKnife.restoreInstanceState(this, savedInstanceState);

        if (direction == Direction.UNKNOWN) {
            direction = getRandomDirection();
        }

        if (direction == Direction.UNKNOWN) {
            throw new IllegalStateException("Invalid Direction: " + direction);
        }

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        updateDirection();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (accelerometer != null && magnometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(this, magnometer, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (accelerometer != null && magnometer != null) {
            sensorManager.unregisterListener(this, accelerometer);
            sensorManager.unregisterListener(this, magnometer);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        PocketKnife.saveInstanceState(this, outState);
    }

    public Direction getRandomDirection() {
        int n = random.nextInt();
        n = Math.abs(n + 1);
        Direction[] values = Direction.values();
        n %= values.length - 1;
        return values[n+1];
    }

    @OnClick(R.id.compass_needle)
    public void onCompassNeedleClick() {
        Direction newDirection;
        do {
            newDirection = getRandomDirection();
        } while (direction == newDirection);
        direction = newDirection;
        updateDirection();
    }

    private void updateDirection() {
        directionText.setText(direction.getNameId());
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.length);
            lastAccelerometerSet = true;
        } else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, lastMagnometer, 0, event.values.length);
            lastMagnomaeterSet = true;
        }
        if (lastAccelerometerSet && lastMagnomaeterSet) {
            SensorManager.getRotationMatrix(r, null, lastAccelerometer, lastMagnometer);
            SensorManager.getOrientation(r, orientation);
            float azimuthInRadians = orientation[0];
            float azimuthInDegress = (float)(Math.toDegrees(azimuthInRadians)+360)%360;
            float bearing = -(azimuthInDegress - direction.getDegrees());
            if (Math.abs(currentDegree - bearing) > EPSILON) {
                RotateAnimation ra = new RotateAnimation(
                        currentDegree,
                        bearing,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f);

                ra.setDuration(250);

                ra.setFillAfter(true);

                compassNeedle.startAnimation(ra);
                currentDegree = bearing;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Don't do anything because we don't care.
    }
}
