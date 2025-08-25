package com.dmoser.codyssey.vidar.docker;

import com.dmoser.codyssey.vidar.service.SystemService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Docker commands that are tied to containers.
 */
public class Container {

    private static final Logger log = LogManager.getLogger(Container.class);
    private static final String LOG_DEBUG_CHECK_CONTAINER_EXISTS = "Checking if container with id: {} exists. Response is {}. Exec cmd is {}";
    private static final String LOG_DEBUG_CHECK_CONTAINER_RUNNING = "Checking if container with id: {} is running. Response is {}. Exec cmd is {}";
    private static final String LOG_DEBUG_REMOVE_CONTAINER = "Remove container with id: {}. Exec cmd is {}";
    private static final String LOG_DEBUG_CREATE_CONTAINER = "Create container with name: {} from image {}. Container id is {}. Exec cmd is {}";
    private static final String LOG_DEBUG_START_CONTAINER = "Start container with id {}. Exec cmd is {}";
    private static final String LOG_DEBUG_STOP_CONTAINER = "Stop container with id {}. EXec cmd is {}";

    private static final String CMD_START = "docker start %s";
    private static final String CMD_STOP = "docker stop %s";


    private static final String CMD_EXISTS = "docker ps -a --format=\"{{.Names}}\" | grep -Fx %s";
    private static final String CMD_IS_RUNNING = "docker ps --format=\"{{.Names}}\" | grep -Fx %s";

    SystemService systemService = SystemService.get();

    /**
     * Default constructor.
     */
    public Container() {

    }

    /**
     * Checks if a container exists.
     *
     * @param containerName The name of the container.
     * @return true when the container exists.
     */
    public boolean exists(String containerName) {
        String cmd = CMD_EXISTS.formatted(containerName);
        String response = systemService.executeCommand(cmd).content();
        log.debug(LOG_DEBUG_CHECK_CONTAINER_EXISTS, containerName, response, cmd);
        return !response.equals("error") && !response.isEmpty();
    }

    /**
     * Checks if a container is running.
     *
     * @param containerName The name of the container.
     * @return true when the container is running.
     */
    public boolean isRunning(String containerName) {
        String cmd = CMD_IS_RUNNING.formatted(containerName);
        String response = systemService.executeCommand(cmd).content();
        log.debug(LOG_DEBUG_CHECK_CONTAINER_RUNNING, containerName, response, cmd);
        return !response.equals("error") && !response.isEmpty();
    }

    /**
     * Removes a container.
     * When the container is running it will be stopped.
     *
     * @param containerName The name of the container that is being removed.
     * @return true when the container is removed.
     */
    public boolean rm(String containerName) {
        if (isRunning(containerName)) {
            stop(containerName);
        }
        String cmd = "docker rm %s".formatted(containerName);
        log.debug(LOG_DEBUG_REMOVE_CONTAINER, containerName, cmd);
        systemService.executeCommand(cmd);
        return !exists(containerName);
    }


    /**
     * Create a new container.
     *
     * @param imageName     The name of the image.
     * @param containerName The name of the container.
     * @param volumes       A list of volumes which are going to be bind to the container. These are in the form of: `[VOLUME]:[PATH/TO/BIND/TO]`
     * @param ports         A list of ports which are going to be bind to the container. These are in the form of: `[EXTERNAL_PORT]:[INTERNAL_PORT]`
     * @param env           A list of environment variables which should be present at container runtime. These are in the form of: `[ENV_NAME]=[ENV_VALUE]`
     * @param networks      A list of networks this container should be attached to.
     * @return true when the container was created.
     */
    public boolean create(String imageName,
                          String containerName,
                          List<String> volumes,
                          List<String> ports,
                          List<String> env,
                          List<String> networks
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("docker create");
        for (String volume : volumes) {
            sb.append(" --volume ");
            sb.append(volume);
        }

        for (String port : ports) {
            sb.append(" --publish ");
            sb.append(port);
        }

        for (String environment : env) {
            sb.append(" --env ");
            sb.append(environment);
        }

        for (String network : networks) {
            sb.append(" --network ");
            sb.append(network);
        }

        sb.append(" --name ");
        sb.append(containerName);
        sb.append(" ");
        sb.append(imageName);

        String cmd = sb.toString();
        String containerId = systemService.executeCommand(cmd).content();
        log.debug(LOG_DEBUG_CREATE_CONTAINER, containerName, imageName, containerId, cmd);
        return exists(containerName);

    }

    /**
     * Start a container.
     *
     * @param containerName The name of the container.
     * @return true when the container is running.
     */
    public boolean start(String containerName) {
        String cmd = CMD_START.formatted(containerName);
        log.debug(LOG_DEBUG_START_CONTAINER, containerName, cmd);
        systemService.executeCommand(cmd);
        return isRunning(containerName);
    }

    /**
     * Stop a container.
     *
     * @param containerName The name of the container.
     * @return true if the container is stopped.
     */
    public boolean stop(String containerName) {
        String cmd = CMD_STOP.formatted(containerName);
        log.debug(LOG_DEBUG_STOP_CONTAINER, containerName, cmd);
        systemService.executeCommand(cmd);
        return exists(containerName) && !isRunning(containerName);
    }
}
