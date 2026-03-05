/*
 * ViewModel per la schermata di Registrazione.
 * Incapsula l'accesso a IUserService.
 */
package dev.theknife.app.viewmodel;

import dev.theknife.app.model.User;
import dev.theknife.app.service.IUserService;

import java.io.IOException;

public class RegisterViewModel {

    private final IUserService userService;

    public RegisterViewModel(IUserService userService) {
        this.userService = userService;
    }

    public boolean emailExists(String email) throws IOException {
        return userService.emailExists(email);
    }

    public void saveUser(User user) throws IOException {
        userService.saveUser(user);
    }
}

