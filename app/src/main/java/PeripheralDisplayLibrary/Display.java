package PeripheralDisplayLibrary;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import java.util.LinkedList;
import java.util.UUID;


/**
 * Klasa reprezentująca wyświetlacz peryferyjny, umożliwiająca połączenie oraz sterowanie.
 * @author Mateusz Wojciechowski
 * @version 1
 */

public class Display {
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothGattService mBluetoothGattService;
    private BluetoothGattCharacteristic mBluetoothGattCharacteristic;
    private static final String DEVICE_ADDRESS = "88:4A:EA:8B:8B:CD";
    private static final UUID SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    private boolean connected = false;
    private Context mContext;
    private class DisplayThread {
        private LinkedList<DisplayCommand> queue;
        private boolean busy = false;
        private class SendCommandRunnable implements Runnable {

            @Override
            public void run() {
                Log.d("SendCommandRunnable", "Starting runnable...");
                do {
                    busy = true;
                    Log.d("Busy", "TRUE");
                    DisplayCommand command = queue.get(0);
                    Log.d("Queue", "Taking element 0, queue size=" + queue.size());
                    mBluetoothGattCharacteristic.setValue(command.getCommand());
                    mBluetoothGatt.writeCharacteristic(mBluetoothGattCharacteristic);
                    SystemClock.sleep(command.getTime());
                    queue.remove(0);
                    Log.d("Queue", "Removing element 0, queue size=" + queue.size());
                    if (queue.isEmpty()) {
                        busy = false;
                        Log.d("Busy", "FALSE");
                    }
                } while (busy);
            }
        }

        public DisplayThread() {
            queue = new LinkedList<>();
            busy = false;
        }

        public void addToQueue(DisplayCommand command) {
            queue.add(command);
            if (!busy) {
                new Thread(new SendCommandRunnable()).start();
                Log.d("AddToQueue", "New Thread started");
                SystemClock.sleep(1000);
            }
        }


    }
    private DisplayThread thread;
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    connected = true;
                    mBluetoothGatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    connected = false;
                    mBluetoothGatt.close();
                    mBluetoothGatt = null;
                    break;
                default:

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            mBluetoothGattService = gatt.getService(SERVICE_UUID);
            mBluetoothGattCharacteristic = mBluetoothGattService.getCharacteristic(CHARACTERISTIC_UUID);
        }
    };

    /**
     * Kolor czerwony.
     */
    public static final String RED = "R";
    /**
     * Kolor zielony.
     */
    public static final String GREEN = "G";
    /**
     * Kolor niebieski.
     */
    public static final String BLUE = "B";
    /**
     * Kolor żółty.
     */
    public static final String YELLOW = "Y";
    /**
     * Kolor pomarańczowy.
     */
    public static final String ORANGE = "O";
    /**
     * Kolor fioletowy.
     */
    public static final String PURPLE = "P";
    /**
     * Kolor błękitny.
     */
    public static final String LIGHT_BLUE = "L";
    /**
     * Kolor biały.
     */
    public static final String WHITE = "W";
    /**
     * Dioda wyłączona
     */
    public static final String OFF = "OFF";

    /**
     * Konstruktor klasy Display. Inicjalizuje pola potrzebne do nawiązania połączenia z wyświetlaczem.
     * @param manager instancja klasy BluetoothManager
     */
    public Display(Context context, BluetoothManager manager) {
        mBluetoothManager = manager;
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);
        mContext = context;
        thread = new DisplayThread();
    }

    /**
     * Funkcja rozpoczynająca połączenie z wyświetlaczem.
     */
    public void connect() {
        if (mBluetoothGatt == null) {
            while(!connected) {
                mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, gattCallback);
                SystemClock.sleep(2000);
            }
            new EventLog(EventLog.getConnectLog());
        }
    }

    /**
     * Funkcja rozłączająca telefon z wyświetlaczem
     */
    public void disconnect() {
        while (thread.busy) {

        }
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            new EventLog(EventLog.getDisconnectedLog());
        }
    }

    /**
     * Funkcja wysyłająca polecenie zaświecenia wybranej diody na wyświetlaczu
     * @param diode numer diody
     * @param color nowy kolor
     * @param time czas przejścia
     */
    public void fade(int diode, String color, int time) {
        if (!connected) {
            return;
        }
        thread.addToQueue(new DisplayFadeCommand(diode, color, time));
        new EventLog(EventLog.getFadeLog(diode, color, time));
    }

    /**
     * Funkcja przełączająca diodę w tryb pulsowania
     * @param diode numer diody
     */
    public void pulse(int diode) {
        if (!connected) {
            return;
        }
        thread.addToQueue(new DisplayPulseCommand(diode));
        new EventLog(EventLog.getPulseLog(diode));
    }

    /**
     * Funkcja wyłączająca diodę
     * @param diode numer diody
     */
    public void off(int diode) {
        thread.addToQueue(new DisplayFadeCommand(diode, Display.OFF, 1000));
        new EventLog(EventLog.getOffLog(diode));
    }

    /**
     * Funkcja zwracająca stan połączenia z wyświetlaczem
     * @return stan połączenia Bluetooth z wyświetlaczem
     */
    public boolean isConnected() {
        return connected;
    }
}


