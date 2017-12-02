package PeripheralDisplayLibrary;

/**
 * Klasa abstrakcyjna deklarująca funkcje potrzebne do wysłania komendy
 * @author Mateusz Wojciechowski
 */

abstract class DisplayCommand {
    /**
     * Metoda zwracająca treść komendy
     * @return komenda wysyłana do wyświetlacza
     */
    public abstract String getCommand();

    /**
     * Metoda zwracająca czas trwania operacji
     * @return czas trwania operacji
     */
    public abstract long getTime();
}
