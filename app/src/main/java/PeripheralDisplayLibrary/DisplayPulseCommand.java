package PeripheralDisplayLibrary;

/**
 * Klasa reprezentująca komendę pulsowania wysyłaną do wyświetlacza
 * @author Mateusz Wojciechowski
 * @version 1
 */

class DisplayPulseCommand extends DisplayCommand {
    private int diode;

    /**
     * Konstruktor komendy
     * @param diode numer obsługiwanej diody
     */
    public DisplayPulseCommand(int diode) {
        this.diode = diode;
    }

    /**
     * Funkcja zwracająca treść komendy
     * @return  komenda
     */
    public String getCommand() {
        String command = "";
        command += diode + ":";
        command += "pulse:";
        return command;
    }

    /**
     * Funkcja zwracająca czas trwania wywołanej operacji na wyświetlaczu, w tej klasie zawsze zwraca 0
     * @return czas trwania operacji
     */
    public long getTime() {
        return 0;
    }
}
