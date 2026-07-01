package configs;  // TODO: for project, change package name to configs

public interface Config {
    void create();
    String getName();
    int getVersion();
    void close();
}

