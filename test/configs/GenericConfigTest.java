package configs;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import graph.TopicManagerSingleton;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;


public class GenericConfigTest {

    private final String CONF_DIR = "config_files";
    private final String TEST_CONF_FILE = CONF_DIR + "/test_config.conf";

    @Before
    public void setUp() throws IOException {
        // Clean the Singleton before every test to ensure isolation
        TopicManagerSingleton.get().clear();
        // Ensure the directory exists
        Files.createDirectories(Paths.get(CONF_DIR));
    }

    @After
    public void tearDown() throws IOException {
        // Clean up the temporary file after the test
        Files.deleteIfExists(Paths.get(TEST_CONF_FILE));
    }

    // Helper method to generate temporary configuration files
    private void writeTempConfig(String... lines) throws IOException {
        Files.write(Paths.get(TEST_CONF_FILE), Arrays.asList(lines));
    }

    @Test(timeout = 2000)
    public void testValidConfigAndThreadClosure() throws IOException {
        // IMPORTANT: Adjust "test.PlusAgent" to your actual package name (e.g., "configs.PlusAgent")
        writeTempConfig(
            "test.PlusAgent", 
            "A,B", 
            "C",
            "test.IncAgent", 
            "C", 
            "D"
        );

        GenericConfig config = new GenericConfig();
        config.setConfFile(TEST_CONF_FILE);
        config.create(); 
        
        // Check if agents were successfully registered to the TopicManager
        assertTrue("TopicManager should have topics registered", 
                   TopicManagerSingleton.get().getTopics().size() > 0);

        // Test thread closure. If ParallelAgents are not closed properly, 
        // this will hang and the test will fail due to the 2000ms timeout.
        try {
            config.close();
        } catch (Exception e) {
            fail("Closing the configuration threw an exception: " + e.getMessage());
        }
    }

    @Test(timeout = 1000)
    public void testEmptyConfigFile() throws IOException {
        writeTempConfig(); // 0 lines

        GenericConfig config = new GenericConfig();
        config.setConfFile(TEST_CONF_FILE);
        
        try {
            config.create();
            assertEquals("TopicManager should be empty for an empty config", 
                         0, TopicManagerSingleton.get().getTopics().size());
            config.close();
        } catch (Exception e) {
            fail("Empty file should not cause a crash: " + e.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class, timeout = 1000)
    public void testInvalidLineCountThrowsException() throws IOException {
        // 4 lines (not a multiple of 3)
        writeTempConfig(
            "test.PlusAgent", 
            "A,B", 
            "C",
            "test.IncAgent" 
        );

        GenericConfig config = new GenericConfig();
        config.setConfFile(TEST_CONF_FILE);
        config.create(); 
    }

    @Test(expected = RuntimeException.class, timeout = 1000)
    public void testClassNotFoundThrowsException() throws IOException {
        writeTempConfig(
            "test.FakeAgentThatDoesNotExist", 
            "A", 
            "B"
        );

        GenericConfig config = new GenericConfig();
        config.setConfFile(TEST_CONF_FILE);
        config.create(); 
    }

    @Test(expected = RuntimeException.class, timeout = 1000)
    public void testClassIsNotAnAgentThrowsException() throws IOException {
        // java.lang.String exists, but it does not implement the Agent interface
        writeTempConfig(
            "java.lang.String", 
            "A", 
            "B"
        );

        GenericConfig config = new GenericConfig();
        config.setConfFile(TEST_CONF_FILE);
        config.create(); 
    }

    @Test(expected = RuntimeException.class, timeout = 1000)
    public void testShortArraysTriggerAgentException() throws IOException {
        // PlusAgent constructor requires at least 2 subs. Passing only 1.
        writeTempConfig(
            "test.PlusAgent", 
            "A", 
            "C"
        );

        GenericConfig config = new GenericConfig();
        config.setConfFile(TEST_CONF_FILE);
        config.create(); 
    }

    @Test(timeout = 1000)
    public void testLongArraysAreHandledGracefully() throws IOException {
        // PlusAgent expects 2 subs, we pass 4. 
        writeTempConfig(
            "test.PlusAgent", 
            "A,B,C,D", 
            "Result"
        );

        GenericConfig config = new GenericConfig();
        config.setConfFile(TEST_CONF_FILE);
        
        try {
            config.create();
            config.close();
        } catch (Exception e) {
            fail("Providing extra arguments should not crash the program: " + e.getMessage());
        }
    }

    @Test(timeout = 1000)
    public void testMissingConfigFileIsHandled() {
        GenericConfig config = new GenericConfig();
        config.setConfFile(CONF_DIR + "/this_file_is_fake.conf");
        
        try {
            // The IOException block should catch this internally and return safely
            config.create();
        } catch (Exception e) {
            fail("Missing file should be caught internally, not thrown out: " + e.getMessage());
        }
    }
}