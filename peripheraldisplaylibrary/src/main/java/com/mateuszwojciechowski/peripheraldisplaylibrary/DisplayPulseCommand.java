package com.mateuszwojciechowski.peripheraldisplaylibrary;

/**
 * Klasa reprezentująca komendę pulsowania wysyłaną do wyświetlacza
 * @author Mateusz Wojciechowski
 * @version 1
 */

class DisplayPulseCommand extends DisplayCommand {
    private int diode;
    private int time;

    /**
     * Konstruktor komendy z wymaganiem podania numeru diody
     * @param diode numer diody
     */
    public DisplayPulseCommand(int diode) {
        this.diode = diode;
        this.time = 1000;
    }

    /**
     * Konstruktor komendy z możliwością ustawienia okresu pulsowania
     * @param diode numer diody
     * @param time  okres pulsowania
     */
    public DisplayPulseCommand(int diode, int time) {
        this.diode = diode;
        this.time = time;
    }

    /**
     * Funkcja zwracająca treść komendy
     * @return  komenda
     */
    public String getCommand() {
        String command = "";
        command += diode + ":pulse:" + time +";";
        return command;
    }

    /**
     * Funkcja zwracająca czas trwania wywołanej operacji na wyświetlaczu
     * @return czas trwania operacji
     */
    public long getTime() {
        return 0;
    }
}
