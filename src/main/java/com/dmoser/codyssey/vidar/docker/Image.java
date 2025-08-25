package com.dmoser.codyssey.vidar.docker;

import com.dmoser.codyssey.vidar.service.SystemService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Docker commands that are tied to images.
 */
public class Image {

    private static final Logger log = LogManager.getLogger(Image.class);
    private static final String LOG_DEBUG_IMAGE_RM = "Removing image with name: {}. Exec cmd is {}";
    private static final String LOG_DEBUG_IMAGE_CHECK = "Checking if image {} exists. Response is {}. Exec cmd is {}";
    private static final String LOG_DEBUG_IMAGE_BUILD = "Building image with name {} and dockerfilePath {}. Exec cmd is {}";

    /**
     * Default constructor.
     */
    public Image() {

    }

    /**
     * Remove a image.
     *
     * @param imageName The name of the image to be removed.
     * @return true if the image is removed.
     */
    public boolean rm(String imageName) {
        String cmd = "docker image rm %s".formatted(imageName);
        log.debug(LOG_DEBUG_IMAGE_RM, imageName, cmd);
        SystemService.get().executeCommand(cmd);
        return !exists(imageName);
    }

    /**
     * Returns if the image exists.
     *
     * @param imageName The name of the image
     * @return true if the image exists.
     */
    public boolean exists(String imageName) {
        String cmd = "docker images --filter \"reference=%s\" --format \"{{.Repository}}:{{.Tag}}\"".formatted(imageName);
        String response = SystemService.get().executeCommand(cmd).content();
        log.debug(LOG_DEBUG_IMAGE_CHECK, imageName, response, cmd);
        return !response.equals("error") && !response.isEmpty();
    }

    /**
     * Build an Image.
     *
     * @param dockerfilePath The path to the dockerfile, without the Dockerfile.
     * @param imageName      The tag under which the image should be saved under.
     * @return true when the image was build successfully.
     */
    public boolean build(String dockerfilePath, String imageName) {
        String cmd = "docker build  -t %s %s".formatted(imageName, dockerfilePath);
        log.debug(LOG_DEBUG_IMAGE_BUILD, imageName, dockerfilePath, cmd);
        SystemService.get().executeCommand(cmd);
        return exists(imageName);
    }
}
