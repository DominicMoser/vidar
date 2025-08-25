package com.dmoser.codyssey.vidar.docker;

import com.dmoser.codyssey.vidar.service.SystemService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Docker commands that are tied to volumes.
 */
public class Volume {

    private static final Logger log = LogManager.getLogger(Volume.class);
    private static final String LOG_VOLUME_CREATE = "Creating volume with name {}. Exec cmd is {}";
    private static final String LOG_VOLUME_REMOVE = "Removing volume with name {}. Exec cmd is {}";
    private static final String LOG_VOLUME_REMOVE_UNNAMED = "Can not delete unnamed volume.";
    private static final String LOG_VOLUME_EXISTS = "Checking for volume with name {}. Result is {}. Exec cmd is {}";
    private static final String EXCEPTION_UNNAMED_VOLUMES = "Unnamed volumes are not allowed in this api";

    private static final String CMD_CREATE = "docker volume create %s";
    private static final String CMD_REMOVE = "docker volume rm %s";
    private static final String CMD_EXISTS = "docker volume ls  --format={{.Name}} | grep -Fx %s";

    /**
     * Default constructor.
     */
    public Volume() {

    }

    /**
     * Create a new volume with a given name. When the volume already exists, no new volume will be created.
     *
     * @param volumeName The name of the volume.
     * @return true when the volume is created.
     */
    public boolean create(String volumeName) {
        if (volumeName == null || volumeName.isEmpty()) {
            throw new IllegalStateException(EXCEPTION_UNNAMED_VOLUMES);
        }
        String cmd = CMD_CREATE.formatted(volumeName);
        log.debug(LOG_VOLUME_CREATE, volumeName, cmd);
        SystemService.get().executeCommand(cmd);
        return exists(volumeName);
    }

    /**
     * Removes a volume.
     *
     * @param volumeName The name of the volume to be removed.
     * @return true when the volume is removed.
     */
    public boolean rm(String volumeName) {
        if (volumeName == null || volumeName.isEmpty()) {
            log.warn(LOG_VOLUME_REMOVE_UNNAMED);
            return true;
        }
        String cmd = CMD_REMOVE.formatted(volumeName);
        log.debug(LOG_VOLUME_REMOVE, volumeName, cmd);
        SystemService.get().executeCommand(cmd);
        return !exists(volumeName);
    }

    /**
     * Checks if a volume exists.
     *
     * @param volumeName The name of the volume to be checked.
     * @return true when the volume exists.
     */
    public boolean exists(String volumeName) {
        String cmd = CMD_EXISTS.formatted(volumeName);
        String response = SystemService.get().executeCommand(cmd).content();
        log.debug(LOG_VOLUME_EXISTS, volumeName, response, cmd);
        return response.equals(volumeName);
    }

}
