/*
 * ViewModel per il form di creazione/modifica ristorante.
 * Incapsula l'accesso a IRestaurantService e RestaurantQueryService.
 */
package dev.theknife.app.viewmodel;

import dev.theknife.app.model.Restaurant;
import dev.theknife.app.service.IRestaurantService;
import dev.theknife.app.service.RestaurantQueryService;

import java.util.List;
import java.util.stream.Collectors;

public class RestaurantFormViewModel {

    private final IRestaurantService restaurantService;
    private final RestaurantQueryService restaurantQueryService;

    public RestaurantFormViewModel(IRestaurantService restaurantService,
                                   RestaurantQueryService restaurantQueryService) {
        this.restaurantService = restaurantService;
        this.restaurantQueryService = restaurantQueryService;
    }

    public boolean addRestaurant(Restaurant restaurant) {
        return restaurantService.addRestaurant(restaurant);
    }

    /**
     * Restituisce i livelli di prezzo disponibili per il filtro.
     * Mantiene la stessa logica usata nel RestaurantListViewModel.
     */
    public List<String> getAvailablePrices() {
        return java.util.Arrays.asList("$", "$$", "$$$", "$$$$");
    }

    /**
     * Restituisce le cucine disponibili, riutilizzando la stessa logica del RestaurantListViewModel.
     */
    public List<String> getAvailableCuisines() {
        int total = restaurantQueryService.getTotalRestaurantCount();
        List<Restaurant> all = restaurantQueryService.getRestaurantsRange(0, total);
        return all.stream()
                .map(Restaurant::getCuisine)
                .filter(c -> c != null && !c.isEmpty())
                .flatMap(c -> java.util.Arrays.stream(c.split(",")))
                .map(String::trim)
                .filter(this::isValidCuisine)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private boolean isValidCuisine(String cuisine) {
        if (cuisine == null || cuisine.trim().isEmpty()) {
            return false;
        }

        try {
            Double.parseDouble(cuisine);
            return false;
        } catch (NumberFormatException e) {
            // not numeric
        }

        if (cuisine.contains("€") || cuisine.contains("$") || cuisine.contains("£") || cuisine.contains("¥")) {
            return false;
        }

        return cuisine.matches(".*[a-zA-Z].*");
    }
}

