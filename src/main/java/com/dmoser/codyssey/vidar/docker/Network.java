package com.dmoser.codyssey.vidar.docker;

import com.dmoser.codyssey.vidar.service.SystemService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Docker commands that are tied to networks.
 */
public class Network {

    private static final Logger log = LogManager.getLogger(Network.class);
    private static final String LOG_NETWORK_CREATE = "Creating network with name {}. Exec cmd is {}";
    private static final String LOG_NETWORK_REMOVE = "Removing network with name {}. Exec cmd is {}";
    private static final String LOG_NETWORK_REMOVE_UNNAMED = "Can not delete unnamed network.";
    private static final String LOG_NETWORK_EXISTS = "Checking for network with name {}. Result is {}. Exec cmd is {}";
    private static final String EXCEPTION_UNNAMED_NETWORKS = "Unnamed networks are not allowed in this api";

    private static final String CMD_CREATE = "docker network create %s";
    private static final String CMD_REMOVE = "docker network rm %s";
    private static final String CMD_EXISTS = "docker network ls  --format={{.Name}} | grep -Fx %s";

    /**
     * Default constructor.
     */
    public Network() {

    }

    /**
     * Create a new network with a given name. When the network already exists, no new network will be created.
     *
     * @param networkName The name of the network.
     * @return true if the network is created.
     */
    public boolean create(String networkName) {
        if (networkName == null || networkName.isEmpty()) {
            throw new IllegalStateException(EXCEPTION_UNNAMED_NETWORKS);
        }
        String cmd = CMD_CREATE.formatted(networkName);
        log.debug(LOG_NETWORK_CREATE, networkName, cmd);
        SystemService.get().executeCommand(cmd);
        return exists(networkName);
    }

    /**
     * Removes a network with a given name.
     *
     * @param networkName The name of the network to be removed.
     * @return true when the network is removed.
     */
    public boolean rm(String networkName) {
        if (networkName == null || networkName.isEmpty()) {
            log.warn(LOG_NETWORK_REMOVE_UNNAMED);
            return true;
        }
        String cmd = CMD_REMOVE.formatted(networkName);
        log.debug(LOG_NETWORK_REMOVE, networkName, cmd);
        SystemService.get().executeCommand(cmd);
        return !exists(networkName);
    }

    /**
     * Checks whether a network exist or not.
     *
     * @param networkName The name of the network.
     * @return true when the network exists.
     */
    public boolean exists(String networkName) {
        String cmd = CMD_EXISTS.formatted(networkName);
        String response = SystemService.get().executeCommand(cmd).content();
        log.debug(LOG_NETWORK_EXISTS, networkName, response, cmd);
        return response.equals(networkName);
    }

}
