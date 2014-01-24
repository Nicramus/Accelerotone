package mobi.braincode.accelerometrotone.ui;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import mobi.braincode.accelerometrotone.service.AccelerometrotoneService;

public class Main extends ActionBarActivity {
    private static final String MAIN_LOG = "MAIN_ACTIVITY";
    private static final String EXTRAS_KEY = "DATA_FLOAT_KEY";
    //zamiast tego bedzie view pager albo cos podobnego
    private TextView accelerometerDataTv;
    private Button playBtn;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        accelerometerDataTv = (TextView) findViewById(R.id.accelerometer_data_tv);
        playBtn = (Button) findViewById(R.id.play_btn);


        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isAccelerometrotoneServiceRunning()) {
                    startService(new Intent(Main.this, AccelerometrotoneService.class));
                    playBtn.setText("I sobie gram...");
                } else {
                    stopService(new Intent(Main.this, AccelerometrotoneService.class));
                    playBtn.setText("Graj piękny chakierze!");
                }
            }
        });
    }

    private boolean isAccelerometrotoneServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (AccelerometrotoneService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private class DataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            float sensorData = intent.getIntExtra(EXTRAS_KEY, 0);
            Log.d(MAIN_LOG, String.valueOf(sensorData));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }

}
