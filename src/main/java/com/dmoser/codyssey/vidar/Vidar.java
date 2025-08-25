package com.dmoser.codyssey.vidar;

import com.dmoser.codyssey.vidar.service.ComposeService;
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
    ComposeService composeService;

    public Vidar(Path path) throws IOException {
        this.path = path;

        systemService = SystemService.get();
        environmentService = EnvironmentService.get();
        gitService = GitService.get();
        composeService = ComposeService.get(path.toRealPath().toString());

        if (!environmentService.validateEnvironment()) {
            log.warn(LOG_WARN_SYSTEM_DEPENDENCIES_NOT_MET);
        }
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
        composeService.child(pluginPath)
                .ls()
                .forEach(composeService::up);
    }

    public void stop(String pluginName) {
        String pluginPath = "apps/" + pluginName;
        composeService.child(pluginPath)
                .ls()
                .forEach(composeService::down);
    }

    public String state(String pluginName) {
        String pluginPath = "apps/" + pluginName;
        return composeService.child(pluginPath)
                .ls()
                .stream()
                .map(service -> service + ":" + composeService.state(service))
                .collect(Collectors.joining(",", "{", "}"));
    }

    /**
     * create a map containing all variables used by this plugin. Variables, that are not initialized will have empty values.
     *
     * @param pluginName
     * @return
     */
    public Map<String, String> variableInfo(String pluginName) {
        String pluginPath = "apps/" + pluginName;
        Map<String, String> varMap = new HashMap<>();

        composeService.child(pluginPath)
                .getVariables()
                .forEach(variable -> varMap.put(variable, ""));


        // Add values from the environment.
        systemService.getEnv().forEach(e -> {
            if (!varMap.containsKey(e.getKey())) {
                return;
            }
            varMap.put(e.getKey(), e.getValue());
        });

        // Add value to variable when exist in .env file.
        composeService.getEnv().forEach(e -> {
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
        composeService.setEnv(key, value);
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
