package com.dmoser.codyssey.vidar.compose;

import com.dmoser.codyssey.vidar.service.EnvironmentService;
import com.dmoser.codyssey.vidar.service.SystemService;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Compose {

    private static final String DOCKER_COMPOSE_LS_COMMAND = "docker compose --project-directory %s config --services";
    private static final String DOCKER_COMPOSE_UP_ALL_COMMAND = "docker compose --project-directory %s up";
    private static final String DOCKER_COMPOSE_UP_MULTIPLE_COMMAND = "docker compose --project-directory %s up %s";
    private static final String DOCKER_COMPOSE_DOWN_ALL_COMMAND = "docker compose --project-directory %s down";
    private static final String DOCKER_COMPOSE_DOWN_MULTIPLE_COMMAND = "docker compose --project-directory %s down %s";
    private static final String DOCKER_COMPOSE_STATE_COMMAND = "docker compose --project-directory %s ps %s --all --format {{.State}}";

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
    Compose(String path, SystemService systemService, EnvironmentService environmentService) {
        this.path = path;
        this.systemService = systemService;
        this.environmentService = environmentService;
    }

    /**
     * Starts all services defined in the Compose file
     */
    public void up() {
        String execString = String.format(DOCKER_COMPOSE_UP_ALL_COMMAND, path);
        systemService.executeCommand(execString);
    }

    /**
     * Start one or more services with a given name.
     *
     * @param serviceNames The names of the services.
     */
    public void up(String... serviceNames) {
        String execString = String.format(DOCKER_COMPOSE_UP_MULTIPLE_COMMAND, path, String.join(" ", serviceNames));
        systemService.executeCommand(execString);
    }

    /**
     * Stops all services defined in the Compose file.
     */
    public void down() {
        String execString = String.format(DOCKER_COMPOSE_DOWN_ALL_COMMAND, path);
        systemService.executeCommand(execString);
    }

    /**
     * Sops one or more services with a given name.
     *
     * @param serviceNames The names of the services.
     */
    public void down(String... serviceNames) {
        String execString = String.format(DOCKER_COMPOSE_DOWN_MULTIPLE_COMMAND, path, String.join(" ", serviceNames));
        systemService.executeCommand(execString);
    }

    /**
     * Lists all services.
     *
     * @return A set containing the names of all services.
     */
    public Set<String> ls() {
        String execString = String.format(DOCKER_COMPOSE_LS_COMMAND, path);
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
        String execString = String.format(DOCKER_COMPOSE_STATE_COMMAND, path, serviceName);
        var result = systemService.executeCommand(execString);
        if (result.exitCode() != 0) {
            return "none";
        }
        return result.content();
    }


}
