package dev.theknife.app;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("TheKnife Application Test Suite")
@SelectClasses({
    UserTest.class,
    PasswordHasherTest.class,
    CSVManagerTest.class
})
public class TestSuite {
    // This class serves as a test suite container
    // All tests will be discovered and run automatically
}
