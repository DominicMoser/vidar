package com.dmoser.codyssey.vidar.service;

import com.dmoser.codyssey.vidar.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.module.ModuleDescriptor;

public class EnvironmentService {

    private static final String LOG_WARN_DOCKER_NOT_INSTALLED = "Could not find docker command";
    private static final String LOG_WARN_GIT_NOT_INSTALLED = "Could not find git command";
    private static final String LOG_WARN_DOCKER_COMPOSE_WRONG_VERSION = "Docker compose should have version {}, but has {}";
    private static final String LOG_INFO_ENVIRONMENT_VALID = "Environment check showed no problems. You're ready to go!";

    private static final String DOCKER_TEST_COMMAND = "docker --version";
    private static final String GIT_TEST_COMMAND = "git --version";
    private static final String DOCKER_COMPOSE_TEST_COMMAND = "docker compose version --short";
    private final SystemService systemService;
    Logger log = LogManager.getLogger(EnvironmentService.class);

    public EnvironmentService() {
        this.systemService = SystemService.get();
    }

    public EnvironmentService(SystemService systemService) {
        this.systemService = systemService;
    }

    public static EnvironmentService get() {
        return new EnvironmentService();
    }

    /**
     * Checks if all environment dependencies are installed.
     *
     * @return True when all dependencies are installed.
     */
    public boolean validateEnvironment() {
        if (!isDockerInstalled()) {
            log.warn(LOG_WARN_DOCKER_NOT_INSTALLED);
            return false;
        }
        if (!isDockerComposeMinimumVersion()) {
            log.warn(LOG_WARN_DOCKER_COMPOSE_WRONG_VERSION);
            return false;
        }

        if (!isGitInstalled()) {
            log.warn(LOG_WARN_DOCKER_COMPOSE_WRONG_VERSION);
            return false;
        }
        log.info(LOG_INFO_ENVIRONMENT_VALID);
        return true;
    }

    /**
     * Checks if Git is installed by executing the `git --version` command.
     *
     * @return true fi git is installed and the command succeeds; false otherwise.
     */
    public boolean isGitInstalled() {
        return isCommandInstalled(GIT_TEST_COMMAND);
    }

    /**
     * Checks if Docker is installed by executing the `docker --version` command.
     *
     * @return true if Docker is installed and the command succeeds; false otherwise.
     */
    public boolean isDockerInstalled() {
        return isCommandInstalled(DOCKER_TEST_COMMAND);
    }

    /**
     * Checks if Docker compose has the minimal required version as set in the gradle.properties file,
     * by executing the `docker compose version --short` command.
     *
     * @return true if Docker compose has the minimal required version. false otherwise.
     */
    public boolean isDockerComposeMinimumVersion() {

        if (!isCommandInstalled(DOCKER_COMPOSE_TEST_COMMAND)) {
            return false;
        }

        var minimumVersion = ModuleDescriptor.Version.parse(Constants.COMPOSE_VERSION);
        var currentVersion = ModuleDescriptor.Version.parse(systemService.executeCommand(DOCKER_COMPOSE_TEST_COMMAND).content());

        return currentVersion.compareTo(minimumVersion) >= 0;

    }

    /**
     * Checks if a command is installed by executing it.
     *
     * @param command The command to be executed for checking its existence.
     * @return true if the command is installed and the command succeeds; false otherwise.
     */
    private boolean isCommandInstalled(String command) {
        return systemService.executeCommand(command).exitCode() == 0;
    }


}
