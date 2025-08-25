package com.dmoser.codyssey.vidar;

import com.dmoser.codyssey.vidar.service.Compose;
import com.dmoser.codyssey.vidar.service.EnvironmentService;
import com.dmoser.codyssey.vidar.service.GitService;
import com.dmoser.codyssey.vidar.service.SystemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Vidar {

    private static final Logger log = LogManager.getLogger(Vidar.class);
    private static final String LOG_WARN_SYSTEM_DEPENDENCIES_NOT_MET = "";
    private final Path path;
    SystemService systemService;
    EnvironmentService environmentService;
    GitService gitService;
    Compose compose;

    public Vidar(Path path) throws IOException {
        systemService = new SystemService();
        environmentService = new EnvironmentService(systemService);
        gitService = new GitService(systemService);

        this.path = path;

        if (!environmentService.validateEnvironment()) {
            log.warn(LOG_WARN_SYSTEM_DEPENDENCIES_NOT_MET);
        }

        // TODO Check for docker compose in directory
        compose = new Compose(path.toRealPath().toString(), systemService, environmentService);
    }

    public String add(String gitClonePath) {
        String repoName = gitService.extractRepoName(gitClonePath);
        try {
            gitService.clone(gitClonePath, path.resolve("apps"));

            // TODO CHECK FOR DOCKER COMPOSE IN GIT DIR
            // TODO ADD COMPOSE TO MAIN INCLUDE

            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

            // Load YAML into a Map
            File file = new File(path.toRealPath() + "/docker-compose.yml");
            System.out.println(file.getAbsoluteFile().getAbsolutePath());
            Map<String, Object> yaml = mapper.readValue(file, Map.class);

            // Get the list and add an item
            List<String> items = (List<String>) yaml.get("include");
            if (items == null) {
                items = new ArrayList<>();
                yaml.put("include", items);
            }


            String dockerComposePath = "apps/" + repoName + "/docker-compose.yml";
            if (!items.contains(dockerComposePath)) {
                items.add(dockerComposePath);
            }

            // Save back to the same file
            mapper.writeValue(file, yaml);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return repoName;
    }

    public void remove(String pluginName) {
        stop(pluginName);
        systemService.rm(path.resolve("apps").resolve(pluginName));
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

            // Load YAML into a Map
            File file = new File(path.toRealPath() + "/docker-compose.yml");
            System.out.println(file.getAbsoluteFile().getAbsolutePath());
            Map<String, Object> yaml = mapper.readValue(file, Map.class);

            // Get the list and add an item
            List<String> items = (List<String>) yaml.get("include");
            if (items == null) {
                items = new ArrayList<>();
                yaml.put("include", items);
            }

            items.remove("apps/" + pluginName + "/docker-compose.yml");

            // Save back to the same file
            mapper.writeValue(file, yaml);
        } catch (Exception e) {
        }
    }

    public void start(String pluginName) {
        String pluginPath = "apps/" + pluginName;
        compose.child(pluginPath)
                .ls()
                .forEach(compose::up);
    }

    public void stop(String pluginName) {
        String pluginPath = "apps/" + pluginName;
        compose.child(pluginPath)
                .ls()
                .forEach(compose::down);
    }

    public String state(String pluginName) {
        String pluginPath = "apps/" + pluginName;
        return compose.child(pluginPath)
                .ls()
                .stream()
                .map(service -> service + ":" + compose.state(service))
                .collect(Collectors.joining(",", "{", "}"));
    }

    // Prints all needed variables
    public Map<String, String> variableInfo(String pluginName) {
        Map<String, String> varMap = new HashMap<>();
        try {
            Compose c = new Compose(path.resolve("apps").resolve(pluginName).toRealPath().toString());
            // add all needed variables
            c.getVariables().forEach(var -> varMap.put(var, ""));
        } catch (IOException e) {
        }

        // Add values from the environment.
        systemService.getEnv().forEach(e -> {
            if (!varMap.containsKey(e.getKey())) {
                return;
            }
            varMap.put(e.getKey(), e.getValue());
        });

        // Add value to variable when exist in .env file.
        compose.getEnv().forEach(e -> {
            if (!varMap.containsKey(e.getKey())) {
                return;
            }
            varMap.put(e.getKey(), e.getValue());
        });
        return varMap;
    }

    /**
     * Set a new variable inside the .env file.
     *
     * @param key   The key
     * @param value The value of the variable
     */
    public void variableSet(String key, String value) {
        compose.setEnv(key, value);
    }

    /**
     * Lists all plugins
     *
     * @return
     */
    public Set<String> ls() {
        try {
            return systemService.ls(path.toRealPath() + "/apps");
        } catch (IOException e) {
            return Set.of();
        }
    }
}
