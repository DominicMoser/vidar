package com.dmoser.codyssey.vidar.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Used for everything that has to do with git.
 */
public class GitService {

    private static final Logger log = LogManager.getLogger(GitService.class);
    private static final String LOG_INFO_GIT_PULL = "Pulling git repository at {}";
    private static final String LOG_DEBUG_GIT_PULL_RESULT = "{}";
    private static final String LOG_ERROR_GIT_PULL_FAILED = "Failed to pull repository at {}";
    private static final String LOG_INFO_GIT_CLONE = "Cloning git repository {}";
    private static final String LOG_DEBUG_GIT_CLONE_RESULT = "{}";
    private static final String LOG_ERROR_GIT_CLONE_FAILED = "Failed to clone git repository {}";
    private static final String GIT_IS_REPOSITORY_COMMAND = "git --git-dir %s/.git rev-parse --show-toplevel";
    private static final String GIT_PULL_COMMAND = "git --git-dir %s/.git rev-parse --show-toplevel";
    private static final String GIT_CLONE_COMMAND = "git clone %s %s";


    final SystemService systemService;

    GitService(SystemService systemService) {
        this.systemService = systemService;
    }

    public static GitService get() {
        return new GitService(SystemService.get());
    }

    public String extractRepoName(String url) {
        // Handles both HTTPS and SSH URLs
        String name = url;
        if (url.endsWith(".git")) {
            name = url.substring(0, url.length() - 4);
        }
        int slash = name.lastIndexOf('/');
        return (slash >= 0) ? name.substring(slash + 1) : name;
    }

    /**
     * Clones a git repository into a folder
     *
     * @param url  The url of the repository
     * @param path The plugin base dir where this plugin should be saved in.
     */
    public boolean clone(String url, Path path) {
        try {
            String pathString = path.toRealPath() + "/" + extractRepoName(url);
            String execString = String.format(GIT_CLONE_COMMAND, url, pathString);
            var result = systemService.executeCommand(execString);
            return result.exitCode() == 0;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Executes gitPull on a given path.
     *
     * @param path The path to the git repository.
     */
    public boolean gitPull(Path path) {
        try {
            String execString = String.format(GIT_PULL_COMMAND, path.toRealPath());
            var result = systemService.executeCommand(execString);
            return result.exitCode() == 0;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Check if a plugin folder is a git repository.
     *
     * @param path The Path to the repository. Must be the root of the repository
     * @return True when it is a git repository.
     */
    public boolean isGitRepository(Path path) {
        try {
            String execString = String.format(GIT_IS_REPOSITORY_COMMAND, path.toRealPath());

            var result = systemService.executeCommand(execString);
            if (result.exitCode() != 0) {
                return false;
            }
            return path.toRealPath().toString().equals(Path.of(result.content()).toRealPath().toString());
        } catch (IOException e) {
            return false;
        }
    }
}
