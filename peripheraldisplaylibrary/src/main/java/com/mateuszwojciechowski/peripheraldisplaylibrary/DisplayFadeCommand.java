package com.mateuszwojciechowski.peripheraldisplaylibrary;

/**
 * Klasa reprezentująca komendę zmiany koloru wysyłaną do wyświetlacza
 * @author Mateusz Wojciechowski
 * @version 1
 */

class DisplayFadeCommand extends DisplayCommand {
    private int diode;
    private String color;
    private int time;

    /**
     * Konstruktor komendy
     * @param diode numer obsługiwanej diody
     * @param color nowy kolor diody
     * @param time  czas przejścia
     */
    public DisplayFadeCommand(int diode, String color, int time) {
        this.diode = diode;
        this.color = color;
        this.time = time;
    }

    /**
     * Funkcja zwracająca treść komendy
     * @return  komenda
     */
    public String getCommand() {
        String command = "";
        command += diode + ":";
        command += color + ":";
        command += time + ";";
        return command;
    }

    /**
     * Funkcja zwracająca czas trwania operacji, którą wywołuje komenda
     * @return  czas trwania operacji
     */
    public long getTime() {
        return time;
    }
}
