package mobi.braincode.accelerometrotone.service;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by m on 17.01.14.
 */
//kod genrowania dźwięku: http://stackoverflow.com/questions/9106276/android-how-to-generate-a-frequency
public class AccelerometrotoneService extends Service implements SensorEventListener{

    private static final String ACCELEROMETROTRONE_LOG = "AccelerometrotoneService";
    private static final String DATA_ACTION = "DATA_ACTION";
    private static final String EXTRAS_KEY = "DATA_FLOAT_KEY";

    private SensorManager sensorManager;
    private Sensor sensor;
    private float os;

    //for sound play
    private final int duration = 3; //sekundy
    private final int sampleRate = 8000; //częstotliwość próbkowania
    private final int numSamples = duration * sampleRate; //dlugosc
    private final double sample[] = new double[numSamples]; //dzwięk (całość)
    private final double frequencyOfTone = 440; //hz

    private final byte generatedSnd[] = new byte[2 * numSamples];

    Handler handler = new Handler();




    //wywoływane za każdym razem gdy system wywołuje startService(Intent), nie wywołuj bezpośrednio
    //w zaleznosci od argumentów- uzyj odpowiedniego sensora
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //fajnie by było jak by sie dało wymienic sensor TODO
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        intent.setAction(DATA_ACTION);
        intent.putExtra("DATA_FLOAT_KEY", os);
        sendBroadcast(intent);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this, sensor);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //działa gdy zmieniają sie wartosci sensora
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float alpha = (float) 0.8;
        os = sensorEvent.values[0];

        Log.d(ACCELEROMETROTRONE_LOG, String.valueOf(os));

        //pobranie siły grawitcji
        float[] gravity = new float[3];
        gravity[0] = alpha * gravity[0] + (1 - alpha) * (int)sensorEvent.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * (int)sensorEvent.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * (int)sensorEvent.values[2];

        //usuwa grawitację za pomocą filtra górno-przepustowego
        float[] linearAcceleration = new float[3];
        linearAcceleration[0] = sensorEvent.values[0] - gravity[0];
        linearAcceleration[1] = sensorEvent.values[1] - gravity[1];
        linearAcceleration[2] = sensorEvent.values[2] - gravity[2];

    }

    //działa gdy zmienia się dokładność sensora
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {  }

    //TODO generowanie dzwięku, na wejsciu powinny być dane z sensora
    private void genTone() {
        for (int i = 0; i < numSamples; ++i) {
           // sample[i]
        }
    }

    private void playSound() {

    }

    /*public class DataSenderThread extends Thread {
        @Override
        public  void run() {
            try {
                while (true) {
                    Intent intent = new Intent();
                    intent.setAction(DATA_ACTION);
                    intent.putExtra("DATAPASSED", i);
                }
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }*/
}
