package mobi.braincode.accelerometrotone.service;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
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
    private float accelerometerRawData;

    private AudioTrack audioTrack;
    private AudioSynthesisTask audioSynthTask;

    private Intent intent;

    //wywoływane za każdym razem gdy system wywołuje startService(Intent), nie wywołuj bezpośrednio
    //w zaleznosci od argumentów- uzyj odpowiedniego sensora

    //trzeba też użyć WakeLocka!
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //fajnie by było jak by sie dało wymienic sensor TODO
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        audioSynthTask = new AudioSynthesisTask();
        audioSynthTask.execute();

        Log.d(ACCELEROMETROTRONE_LOG, String.valueOf(accelerometerRawData));

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this, sensor);
        boolean ccc = true;
        audioSynthTask.cancel(ccc);
        audioTrack.stop();
        audioTrack.flush();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //działa gdy zmieniają sie wartosci sensora
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float alpha = (float) 0.8;
        accelerometerRawData = sensorEvent.values[0];

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

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { }

    private class AudioSynthesisTask extends AsyncTask<Void, Void, Void> {
        private int baseFrequency = 440;

        @Override
        protected void onPreExecute() {
            intent = new Intent();
            intent.setAction(DATA_ACTION);
            //przesyłam dane z sensora do broadcast recivera w main activity
            intent.putExtra(EXTRAS_KEY, accelerometerRawData);
        }


        @Override
        protected Void doInBackground(Void... params) {
            final int SAMPLE_RATE = 11025;
            int minSize = AudioTrack.getMinBufferSize(SAMPLE_RATE,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    SAMPLE_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, minSize,
                    AudioTrack.MODE_STREAM);
            audioTrack.play();
            short[] buffer = new short[minSize];
            float angle = 0;
            while (true) {

                for (int i = 0; i < buffer.length; i++) {
                    float angular_frequency = (float) (2 * Math.PI) * (440 + accelerometerRawData*10);
                    sendBroadcast(intent);
                    buffer[i] = (short) (Short.MAX_VALUE * ((float) Math.sin(angle)));
                    angle += angular_frequency;
                }
                audioTrack.write(buffer, 0, buffer.length);

            }
        }
    }
}

