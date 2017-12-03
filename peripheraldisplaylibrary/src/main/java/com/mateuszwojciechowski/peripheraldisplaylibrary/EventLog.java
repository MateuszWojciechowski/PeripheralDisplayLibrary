package com.mateuszwojciechowski.peripheraldisplaylibrary;

import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by mateuszwojciechowski on 15.11.2017.
 */

class EventLog {

    private static class LogsThread {
        private LinkedList<URL> queue;
        private boolean busy = false;
        private class SendLogRunnable implements Runnable {
            @Override
            public void run() {
                Log.d("SendLogRunnable", "Starting runnable...");
                do {
                    busy = true;
                    Log.d("Logs Thread", " busy = TRUE");
                    URL url = queue.get(0);
                    Log.d("Logs Queue", "Taking element 0, queue size=" + queue.size());
                    try {
                        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                        connection.connect();
                        connection.getInputStream();
                        connection.disconnect();
                    } catch (IOException e) {
                        Log.d("Błąd HTTP", e.getMessage());
                    }
                    //uśpienie wątku na 15 sekund, ThingSpeak nie przyjmuje logów częściej
                    SystemClock.sleep(15000);
                    queue.remove(0);
                    Log.d("Logs Queue", "Removing element 0, queue size=" + queue.size());
                    if (queue.isEmpty()) {
                        busy = false;
                        Log.d("Logs Thread", "busy = FALSE");
                    }
                } while (busy);
            }
        }

        public LogsThread() {
            queue = new LinkedList<>();
        }

        public void addToQueue(URL url) {
            queue.add(url);
            if(!busy) {
                new Thread(new SendLogRunnable()).start();
                Log.d("Logs Queue", "New Thread started");
                SystemClock.sleep(1000);
            }
        }
    }
    private static LogsThread thread = new LogsThread();

    private static String getTimestamp() {
        return new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS ").format(new Date());
    }
    public static String getFadeLog(int diode, String color, int time) {
        StringBuilder builder = new StringBuilder();
        builder.append(getTimestamp()).append("FADE (diode: ").append(diode).append(", color: ").append(color).append(", time: ").append(time).append(")");
        return builder.toString().replaceAll(" ", "%20");
    }

    public static String getPulseLog(int diode) {
        StringBuilder builder = new StringBuilder();
        builder.append(getTimestamp()).append("PULSE (diode: ").append(diode).append(")");
        return builder.toString().replaceAll(" ", "%20");
    }

    public static String getConnectLog() {
        return (getTimestamp() + "CONNECTED").replaceAll(" ", "%20");
    }

    public static String getConnectionFailureLog() {
        return (getTimestamp() + "CONNECTION FAILURE").replaceAll(" ", "%20");
    }

    public static String getDisconnectedLog() {
        return (getTimestamp() + "DISCONNECTED").replaceAll(" ", "%20");
    }

    public static String getOffLog(int diode) {
        StringBuilder builder = new StringBuilder();
        builder.append(getTimestamp()).append("OFF (diode: ").append(diode).append(")");
        return builder.toString().replaceAll(" ", "%20");
    }

    public EventLog(String log) {
        String address = "https://api.thingspeak.com/update?api_key=5JECKH4NIBBFYN53&field1=\"" + log + "\"";
        URL url = null;
        try {
            url = new URL(address);
        } catch (MalformedURLException e) {
            Log.d("Błąd URL", e.getMessage());
        }
        if (url != null) {
            thread.addToQueue(url);
        }
    }
}
