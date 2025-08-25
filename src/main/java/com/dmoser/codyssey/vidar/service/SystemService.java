package com.dmoser.codyssey.vidar.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SystemService {

    private static final String LOG_DEBUG_COMMAND_EXIT_CODE_NOT_0 = "Command {} did exit with code {}. ErrorOutput is {}";
    private static final String LOG_DEBUG_COMMAND_THREW_EXCEPTION = "Command {} did throw an exception. ErrorOutput is {}";


    private static final String LS_COMMAND = "ls -1 %s";
    private static final String RM_COMMAND = "rm -r %s";

    Logger log = LogManager.getLogger(SystemService.class);


    public static SystemService get() {
        return new SystemService();
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

    // Lists all folders in directory
    public Set<String> ls(String path) {
        String execString = String.format(LS_COMMAND, path);
        var result = executeCommand(execString)
                .content()
                .split("\n");
        return Stream.of(result).filter(s -> !s.isBlank()).collect(Collectors.toSet());
    }

    public void rm(Path path) {
        try {
            String execString = String.format(RM_COMMAND, path.toRealPath());
            executeCommand(execString);
        } catch (NoSuchFileException e) {
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<Map.Entry<String, String>> getEnv() {
        return System.getenv().entrySet();
    }


}
