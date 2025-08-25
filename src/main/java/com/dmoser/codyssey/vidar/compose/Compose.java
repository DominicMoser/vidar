package com.dmoser.codyssey.vidar.compose;

import com.dmoser.codyssey.vidar.service.EnvironmentService;
import com.dmoser.codyssey.vidar.service.SystemService;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Compose {

    private static final String DOCKER_COMPOSE_LS_COMMAND = "docker compose --file %s config --services --no-interpolate";
    private static final String DOCKER_COMPOSE_UP_ALL_COMMAND = "docker compose --file %s up";
    private static final String DOCKER_COMPOSE_UP_MULTIPLE_COMMAND = "docker compose --file %s up %s";
    private static final String DOCKER_COMPOSE_DOWN_ALL_COMMAND = "docker compose --file %s down";
    private static final String DOCKER_COMPOSE_DOWN_MULTIPLE_COMMAND = "docker compose --file %s down %s";
    private static final String DOCKER_COMPOSE_STATE_COMMAND = "docker compose --file %s ps %s --all --format {{.State}}";
    private static final String DOCKER_COMPOSE_CONFIG_COMMAND = "docker compose --file %s config --variables";

    final String path;
    SystemService systemService;
    EnvironmentService environmentService;

    /**
     * Create a new Compose class with the default SystemService and EnvironmentService
     */
    public Compose(String path) {
        this.path = path;
        this.systemService = SystemService.get();
        this.environmentService = EnvironmentService.get();
    }

    /**
     * Constructor for testcases, so that SystemService and EnvironmentService can be mocked.
     *
     * @param systemService      The SystemService of this Compose class
     * @param environmentService The EnvironmentService of this Compose class.
     */
    public Compose(String path, SystemService systemService, EnvironmentService environmentService) {
        this.path = path;
        this.systemService = systemService;
        this.environmentService = environmentService;
    }

    /**
     * Starts all services defined in the Compose file
     */
    public void up() {
        String execString = String.format(DOCKER_COMPOSE_UP_ALL_COMMAND, path + "/docker-compose.yml");
        systemService.executeCommand(execString);
    }

    /**
     * Start one or more services with a given name.
     *
     * @param serviceNames The names of the services.
     */
    public void up(String... serviceNames) {
        String execString = String.format(DOCKER_COMPOSE_UP_MULTIPLE_COMMAND, path + "/docker-compose.yml", String.join(" ", serviceNames));
        systemService.executeCommand(execString);
    }

    /**
     * Stops all services defined in the Compose file.
     */
    public void down() {
        String execString = String.format(DOCKER_COMPOSE_DOWN_ALL_COMMAND, path + "/docker-compose.yml");
        systemService.executeCommand(execString);
    }

    /**
     * Sops one or more services with a given name.
     *
     * @param serviceNames The names of the services.
     */
    public void down(String... serviceNames) {
        String execString = String.format(DOCKER_COMPOSE_DOWN_MULTIPLE_COMMAND, path + "/docker-compose.yml", String.join(" ", serviceNames));
        systemService.executeCommand(execString);
    }

    /**
     * Lists all services.
     *
     * @return A set containing the names of all services.
     */
    public Set<String> ls() {
        String execString = String.format(DOCKER_COMPOSE_LS_COMMAND, path + "/docker-compose.yml");
        var result = systemService.executeCommand(execString)
                .content()
                .split("\n");
        return Stream.of(result).filter(s -> !s.isBlank()).collect(Collectors.toSet());
    }


    /**
     * Returns the state of a service.
     *
     * @param serviceName The name of the service.
     * @return The state of the service. Either running, exited, none.
     */
    public String state(String serviceName) {
        String execString = String.format(DOCKER_COMPOSE_STATE_COMMAND, path + "/docker-compose.yml", serviceName);
        var result = systemService.executeCommand(execString);
        if (result.exitCode() != 0) {
            return "none";
        }
        return result.content();
    }

    public String config() {
        String execString = String.format(DOCKER_COMPOSE_CONFIG_COMMAND, path + "/docker-compose.yml");
        var result = systemService.executeCommand(execString);
        return result.content();
    }

    /**
     * Get a list of all used variables inside this compose file.
     *
     * @return
     */
    public Set<String> getVariables() {
        Set<String> varSet = new TreeSet<>();
        Pattern pattern = Pattern.compile("\\$[a-zA-Z_]+[a-zA-Z0-9_]*");
        try {
            String composeFile = Files.readString(Path.of(path + "/docker-compose.yml"));
            Matcher matcher = pattern.matcher(composeFile);
            while (matcher.find()) {
                varSet.add(matcher.group().replace("$", ""));
            }
        } catch (Exception ignored) {
        }
        return varSet;

    }


    public Set<Map.Entry<String, String>> getEnv() {
        Set<Map.Entry<String, String>> entrySet = new HashSet<>();
        Properties envFile = new Properties();
        try {
            envFile.load(new FileInputStream(path + "/.env"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        envFile.forEach((key, value) -> entrySet.add(
                Map.entry(key.toString(), value.toString())
        ));
        return entrySet;
    }

    public void setEnv(String key, String value) {
        Properties envFile = new Properties();
        try {
            envFile.load(new FileInputStream(path + "/.env"));
            envFile.setProperty(key, value);
            envFile.store(new FileOutputStream(path + "/.env"), "test");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a compose project pointing to a child folder
     *
     * @param childPath
     * @return
     */
    public Compose child(String childPath) {
        return new Compose(this.path + "/" + path, this.systemService, this.environmentService);
    }

}
