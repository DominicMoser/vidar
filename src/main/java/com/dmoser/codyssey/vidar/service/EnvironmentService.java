package com.dmoser.codyssey.vidar.service;

import com.dmoser.codyssey.vidar.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.module.ModuleDescriptor;
import java.util.stream.Collectors;

public class EnvironmentService {

    private static final String LOG_WARN_DOCKER_NOT_INSTALLED = "Could not find docker command";
    private static final String LOG_WARN_DOCKER_COMPOSE_WRONG_VERSION = "Docker compose should have version {}, but has {}";
    private static final String LOG_INFO_ENVIRONMENT_VALID = "Environment check showed no problems. You're ready to go!";
    private static final String LOG_DEBUG_COMMAND_EXIT_CODE_NOT_0 = "Command {} did exit with code {}. ErrorOutput is {}";
    private static final String LOG_DEBUG_COMMAND_THREW_EXCEPTION = "Command {} did throw an exception. ErrorOutput is {}";

    private static final String DOCKER_TEST_COMMAND = "docker --version";
    private static final String DOCKER_COMPOSE_TEST_COMMAND = "docker compose version --short";

    Logger log = LogManager.getLogger(EnvironmentService.class);

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
        log.info(LOG_INFO_ENVIRONMENT_VALID);
        return true;
    }

    /**
     * Executes a command.
     * Command will be prefixed with /bin/sh -c.
     *
     * @param command The command to be executed.
     * @return The CommandResult containing the stdout or stderr output and the exit code.
     */
    public CommandResult executeCommand(String command) {
        ProcessBuilder builder = new ProcessBuilder("/bin/sh", "-c", command);
        String error = "";

        try {
            Process process = builder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                BufferedReader r2 = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String output = reader.lines()
                        .collect(Collectors.joining("\n"))
                        .trim();

                error = r2.lines()
                        .collect(Collectors.joining("\n"))
                        .trim();
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    log.debug(LOG_DEBUG_COMMAND_EXIT_CODE_NOT_0, command, output);
                    return new CommandResult(output, exitCode);

                }
                log.warn(LOG_DEBUG_COMMAND_THREW_EXCEPTION, command, exitCode, error);
                return new CommandResult(error, exitCode);
            }

        } catch (IOException e) {
            return new CommandResult("", 5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Preserve interrupt status
            return new CommandResult("", 4);
        }
    }

    /**
     * Checks if Docker is installed by executing the `docker --version` command.
     *
     * @return true if Docker is installed and the command succeeds; false otherwise.
     */
    private boolean isDockerInstalled() {
        return isCommandInstalled(DOCKER_TEST_COMMAND);
    }

    /**
     * Checks if Docker compose has the minimal required version as set in the gradle.properties file,
     * by executing the `docker compose version --short` command.
     *
     * @return true if Docker compose has the minimal required version. false otherwise.
     */
    private boolean isDockerComposeMinimumVersion() {

        if (!isCommandInstalled(DOCKER_COMPOSE_TEST_COMMAND)) {
            return false;
        }

        var minimumVersion = ModuleDescriptor.Version.parse(Constants.COMPOSE_VERSION);
        var currentVersion = ModuleDescriptor.Version.parse(executeCommand(DOCKER_COMPOSE_TEST_COMMAND).content());

        return currentVersion.compareTo(minimumVersion) >= 0;

    }

    /**
     * Checks if a command is installed by executing it.
     *
     * @param command The command to be executed for checking its existence.
     * @return true if the command is installed and the command succeeds; false otherwise.
     */
    private boolean isCommandInstalled(String command) {
        return executeCommand(command).exitCode() == 0;
    }


}
