package com.dmoser.codyssey.vidar.docker;

import com.dmoser.codyssey.vidar.service.EnvironmentService;
import com.dmoser.codyssey.vidar.service.SystemService;

/**
 * Api for sending docker commands.
 *
 * @param volume    All command which are located under docker volume
 * @param image     All command which are located under docker image
 * @param container All command which are located under docker container
 * @param network   All command which are located under docker network
 */
public record Docker(Volume volume, Image image, Container container, Network network) {

    /**
     * The docker api in stance.
     */
    private static Docker instance;

    /**
     * Get the docker api record.
     *
     * @return This instance.
     */
    public static Docker api() {

        if (instance == null) {
            if (EnvironmentService.get().isDockerInstalled()) {
                throw new IllegalStateException("Docker needs to be installed for the pluginManager to work!");
            }
            instance = new Docker(new Volume(), new Image(), new Container(), new Network());
        }
        return instance;
    }

    /**
     * Return the version of the docker server.
     *
     * @return The version of the docker server.
     */
    public String getVersion() {
        return SystemService.get().executeCommand("docker version --format '{{.Server.Version}}'").content().trim();
    }


}
