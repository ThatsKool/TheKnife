/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.container;

import dev.theknife.app.service.RestaurantService;
import dev.theknife.app.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite per la classe {@link DependencyContainer}.
 * <p>
 * Verifica il funzionamento del container di dependency injection,
 * inclusa la registrazione di singleton e factory, e l'inizializzazione
 * dei servizi predefiniti.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public class DependencyContainerTest {
    private DependencyContainer container;
    
    @BeforeEach
    void setUp() {
        container = DependencyContainer.getInstance();
        container.clear(); // Clear any existing registrations
    }
    
    @Test
    void testSingletonRegistration() {
        String testInstance = "test instance";
        container.registerSingleton(String.class, testInstance);
        
        String retrieved = container.get(String.class);
        assertEquals(testInstance, retrieved);
        assertSame(testInstance, retrieved); // Should be the same instance
    }
    
    @Test
    void testFactoryRegistration() {
        container.registerFactory(String.class, () -> "new instance");
        
        String instance1 = container.get(String.class);
        String instance2 = container.get(String.class);
        
        assertEquals("new instance", instance1);
        assertEquals("new instance", instance2);
        assertSame(instance1, instance2); // Should be cached as singleton
    }
    
    @Test
    void testIsRegistered() {
        assertFalse(container.isRegistered(String.class));
        
        container.registerSingleton(String.class, "test");
        assertTrue(container.isRegistered(String.class));
    }
    
    @Test
    void testGetUnregisteredClass() {
        assertThrows(IllegalArgumentException.class, () -> {
            container.get(String.class);
        });
    }
    
    @Test
    void testClear() {
        container.registerSingleton(String.class, "test");
        assertTrue(container.isRegistered(String.class));
        
        container.clear();
        assertFalse(container.isRegistered(String.class));
    }
    
    @Test
    void testInitializeAllServices() {
        container.initializeAllServices();
        
        assertTrue(container.isRegistered(RestaurantService.class));
        assertTrue(container.isRegistered(ReviewService.class));
        
        RestaurantService restaurantService = container.get(RestaurantService.class);
        ReviewService reviewService = container.get(ReviewService.class);
        
        assertNotNull(restaurantService);
        assertNotNull(reviewService);
        assertInstanceOf(RestaurantService.class, restaurantService);
        assertInstanceOf(ReviewService.class, reviewService);
    }
    
    @Test
    void testSingletonInstance() {
        DependencyContainer instance1 = DependencyContainer.getInstance();
        DependencyContainer instance2 = DependencyContainer.getInstance();
        
        assertSame(instance1, instance2);
    }
}
