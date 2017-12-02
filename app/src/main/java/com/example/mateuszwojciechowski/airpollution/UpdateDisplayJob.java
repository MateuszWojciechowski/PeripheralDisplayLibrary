package com.example.mateuszwojciechowski.airpollution;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.bluetooth.BluetoothManager;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import PeripheralDisplayLibrary.Display;

public class UpdateDisplayJob extends JobService {
    private final String API_KEY = "15d7010813eb199216c5781c38f92d3c824b1de7";
    private Display display;
    private HttpURLConnection connection;
    private URL url;
    private String data;
    private JSONObject jsonData;
    private int[] val = new int[3];
    private JobParameters jobParameters;
    private final int FADE_TIME = 10000;

    public class Job extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String response;
                StringBuilder builder = new StringBuilder();
                while ((response = reader.readLine()) != null) {
                    builder = builder.append(response);
                }
                data = builder.toString();
            } catch (IOException e) {

            }
            Log.i("Received data: ", data);
            try {
                JSONObject jsonData = new JSONObject(data);

                JSONObject jsondata = jsonData.optJSONObject("data");
                JSONObject rates = jsondata.optJSONObject("iaqi");

                JSONObject pm10 = rates.optJSONObject("pm10");
                val[0] = pm10.optInt("v");

                JSONObject pm25 = rates.optJSONObject("pm25");
                val[1] = pm25.optInt("v");

                JSONObject no2 = rates.optJSONObject("no2");
                val[2] = no2.optInt("v");
            } catch (JSONException e) {

            }

            display.connect();
            int diode = 0;
            int delay = 0;
            for (int v : val) {
                if (v > 0 && v <= 50) {
                    display.fade(diode, Display.GREEN, FADE_TIME);
                } else if (v > 50 && v <= 100) {
                    display.fade(diode, Display.YELLOW, FADE_TIME);
                } else if (v > 100 && v <= 150) {
                    display.fade(diode, Display.ORANGE, FADE_TIME);
                } else if (v > 150) {
                    display.fade(diode, Display.RED, FADE_TIME);
                } else {
                    display.fade(diode, Display.WHITE, FADE_TIME);
                }
                diode++;
                delay += FADE_TIME;
            }
            SystemClock.sleep(delay);
            display.disconnect();
            return null;
        }

        @Override
        protected void onPreExecute() {
            try {
                url = new URL("http://api.waqi.info/feed/geo:52.2280649;21.0055116/?token=" + API_KEY);
            } catch (MalformedURLException e) {

            }
        }

        @Override
        protected void onPostExecute(Void params) {

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        BluetoothManager manager = (BluetoothManager) this.getSystemService(BLUETOOTH_SERVICE);
        if (!manager.getAdapter().isEnabled()) {
            return false;
        }
        if (display == null) {
            display = new Display(getApplicationContext(), manager);
        }
        jobParameters = params;
        new Job().execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        display.disconnect();
        return false;
    }
}
