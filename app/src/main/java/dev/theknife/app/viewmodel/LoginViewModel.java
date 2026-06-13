/*
 * ViewModel per la schermata di Login.
 * Incapsula l'accesso a IUserService.
 */
package dev.theknife.app.viewmodel;

import dev.theknife.app.model.User;
import dev.theknife.app.service.IUserService;

import java.io.IOException;

public class LoginViewModel {

    private final IUserService userService;

    public LoginViewModel(IUserService userService) {
        this.userService = userService;
    }

    /**
     * Verifica le credenziali e, se valide, restituisce l'utente corrispondente.
     *
     * Mantiene la stessa semantica della precedente implementazione nella View:
     * in caso di errore I/O solleva una RuntimeException che viene gestita a livello superiore.
     *
     * @param email    email dell'utente.
     * @param password password in chiaro.
     * @return l'utente loggato oppure null se le credenziali non sono valide.
     */
    public User login(String email, String password) {
        if (userService == null) {
            return null;
        }
        try {
            if (userService.validateCredentials(email, password)) {
                return userService.findUserByEmail(email);
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

