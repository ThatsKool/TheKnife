/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.model;

import java.time.LocalDate;

/**
 * Modello che rappresenta un utente nel sistema.
 * <p>
 * Questa classe è immutabile (Immutable Object Pattern) per garantire la sicurezza
 * in ambienti multithread e la consistenza dei dati.
 * </p>
 * <p>
 * La chiave primaria composita logica è costituita dall'email, che deve essere univoca.
 * Include informazioni personali, credenziali (password hashata) e geolocalizzazione.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public final class User {
    // CAMPI
    private final String name;
    private final String surname;
    private final String email;
    private final String password;
    private final LocalDate dateOfBirth;
    private final double latitude;
    private final double longitude;
    private final String role;

    // COSTRUTTORI
    /**
     * Costruttore principale per creare un'istanza di User.
     * <p>
     * Tutti i parametri sono obbligatori tranne dateOfBirth che può essere null.
     * Il ruolo viene internato per garantire l'uguaglianza referenziale.
     * </p>
     *
     * @param name Il nome dell'utente.
     * @param surname Il cognome dell'utente.
     * @param email L'email dell'utente (deve essere univoca, funge da username).
     * @param password La password hashata dell'utente.
     * @param dateOfBirth La data di nascita (può essere null).
     * @param latitude La latitudine della posizione dell'utente.
     * @param longitude La longitudine della posizione dell'utente.
     * @param role Il ruolo dell'utente nel sistema (es. "Cliente", "Ristoratore").
     */
    public User(String name, String surname, String email, String password, LocalDate dateOfBirth,
                double latitude, double longitude, String role) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
        this.latitude = latitude;
        this.longitude = longitude;
        this.role = role != null ? role.intern() : null;
    }

    // METODI
    /**
     * Restituisce il nome dell'utente.
     * @return Il nome.
     */
    public String getName() { return name; }

    /**
     * Restituisce il cognome dell'utente.
     * @return Il cognome.
     */
    public String getSurname() { return surname; }

    /**
     * Restituisce l'email dell'utente (username).
     * @return L'email.
     */
    public String getEmail() { return email; }

    /**
     * Restituisce la password (hashata) dell'utente.
     * @return La password hashata.
     */
    public String getPassword() { return password; }

    /**
     * Restituisce la data di nascita dell'utente.
     * @return La data di nascita.
     */
    public LocalDate getDateOfBirth() { return dateOfBirth; }

    /**
     * Restituisce la latitudine della posizione dell'utente.
     * @return La latitudine.
     */
    public double getLatitude() { return latitude; }

    /**
     * Restituisce la longitudine della posizione dell'utente.
     * @return La longitudine.
     */
    public double getLongitude() { return longitude; }

    /**
     * Restituisce il ruolo dell'utente nel sistema.
     * @return Il ruolo (es. Cliente, Ristoratore).
     */
    public String getRole() { return role; }

    /**
     * Crea una copia di questo utente con una nuova password.
     * 
     * @param newPassword La nuova password (già hashata).
     * @return Una nuova istanza di User con la password aggiornata.
     */
    public User withPassword(String newPassword) {
        return new User(name, surname, email, newPassword, dateOfBirth, latitude, longitude, role);
    }

    /**
     * Restituisce una rappresentazione stringa dell'utente in formato CSV.
     * <p>
     * Il formato è: nome,cognome,email,password,dataNascita,latitudine,longitudine,ruolo
     * </p>
     *
     * @return Stringa rappresentativa dell'utente in formato CSV.
     */
    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s",
            name, surname, email, password,
            dateOfBirth != null ? dateOfBirth.toString() : "",
            latitude, longitude, role);
    }
}
