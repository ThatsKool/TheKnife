/*
 * ViewModel per la vista del profilo utente.
 * Incapsula l'accesso a IUserService e SessionContext.
 */
package dev.theknife.app.viewmodel;

import dev.theknife.app.model.User;
import dev.theknife.app.service.IUserService;
import dev.theknife.app.session.SessionContext;

import java.io.IOException;

public class UserProfileViewModel {

    private final IUserService userService;
    private final SessionContext sessionContext;

    public UserProfileViewModel(IUserService userService, SessionContext sessionContext) {
        this.userService = userService;
        this.sessionContext = sessionContext;
    }

    public User getCurrentUser() {
        return sessionContext != null ? sessionContext.getCurrentUser() : null;
    }

    /**
     * Aggiorna la posizione dell'utente corrente e persiste le modifiche.
     *
     * @param lat nuova latitudine.
     * @param lon nuova longitudine.
     * @throws IOException se il salvataggio fallisce.
     */
    public void updateCurrentUserLocation(double lat, double lon) throws IOException {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Nessun utente loggato");
        }

        User updatedUser = new User(
                currentUser.getName(),
                currentUser.getSurname(),
                currentUser.getEmail(),
                currentUser.getPassword(),
                currentUser.getDateOfBirth(),
                lat,
                lon,
                currentUser.getRole()
        );

        userService.updateUser(updatedUser);
        if (sessionContext != null) {
            sessionContext.setCurrentUser(updatedUser);
        }
    }
}

