package configs; 

/**
 * Represents a configurable computational setup.
 *
 * <p>Implementations create agents, subscribe them to topics,
 * and define how values flow through the system.</p>
 */
public interface Config {
    /**
 * Creates the configuration and initializes its agents.
 */
    void create();
    /**
 * @return the configuration name
 */
    String getName();
    /**
 * @return the configuration version
 */
    int getVersion();
    /**
 * Closes the configuration and releases its resources.
 */
    void close();
}

