package com.example.mateuszwojciechowski.airpollution;

import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import PeripheralDisplayLibrary.Display;

import static android.R.attr.button;
import static android.R.attr.start;
import static com.example.mateuszwojciechowski.airpollution.R.id.contentPanel;
import static com.example.mateuszwojciechowski.airpollution.R.id.startButton;
import static com.example.mateuszwojciechowski.airpollution.R.id.stopButton;

public class MainActivity extends AppCompatActivity {
    private JobScheduler jobScheduler;
    private JobInfo jobInfo;
    private BluetoothManager manager;
    private final int REQUEST_ENABLE_BT = 1;
    //private Display disp;
    private com.mateuszwojciechowski.peripheraldisplaylibrary.Display disp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        disp = new com.mateuszwojciechowski.peripheraldisplaylibrary.Display(getApplicationContext(), manager);

        jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(getPackageName(), UpdateDisplayJob.class.getName()));
        builder.setPeriodic(1800000);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        jobInfo = builder.build();

        final Button startButton = (Button) findViewById(R.id.startButton);
        final Button stopButton = (Button) findViewById(R.id.stopButton);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jobScheduler.schedule(jobInfo);
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
//                new UpdateTask(disp).execute();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jobScheduler.cancelAll();
                if (!disp.isConnected()) {
                    disp.connect();
                }
                disp.off(0); disp.off(1); disp.off(2);
                disp.disconnect();
                stopButton.setEnabled(false);
                startButton.setEnabled(true);
            }
        });
    }

    @Override
    protected void onDestroy() {
        jobScheduler.cancelAll();
        if (!disp.isConnected()) {
            disp.connect();
        }
        disp.off(0); disp.off(1); disp.off(2);
        disp.disconnect();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!manager.getAdapter().isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, REQUEST_ENABLE_BT);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}
