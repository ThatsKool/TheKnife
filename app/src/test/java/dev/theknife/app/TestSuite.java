/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import dev.theknife.app.model.RestaurantTest;


/**
 * Suite di test principale per l'applicazione The Knife.
 * <p>
 * Raggruppa tutti i test unitari dell'applicazione per l'esecuzione
 * centralizzata tramite JUnit Platform.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
@Suite
@SuiteDisplayName("TheKnife Application Test Suite")
@SelectClasses({
    UserTest.class,
    PasswordHasherTest.class,
    RestaurantTest.class,
    CSVManagerTest.class
})
public class TestSuite {
    // This class serves as a test suite container
    // All tests will be discovered and run automatically
}
