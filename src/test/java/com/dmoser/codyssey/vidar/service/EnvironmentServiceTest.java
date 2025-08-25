package com.dmoser.codyssey.vidar.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EnvironmentServiceTest {

    @Mock
    SystemService mockService;


    @Test
    void test_isDockerComposeMinimumVersion_versionToOld() {
        EnvironmentService environmentService = new EnvironmentService(mockService);
        when(mockService.executeCommand("docker compose version --short"))
                .thenReturn(new CommandResult("0.0.0", 0));

        boolean result = environmentService.isDockerComposeMinimumVersion();
        assertFalse(result);
    }

    @Test
    void test_isDockerComposeMinimumVersion_versionCorrect() {
        EnvironmentService environmentService = new EnvironmentService(mockService);
        when(mockService.executeCommand("docker compose version --short"))
                .thenReturn(new CommandResult("99.99.99", 0));

        boolean result = environmentService.isDockerComposeMinimumVersion();
        assertTrue(result);
    }

    @Test
    void test_isDockerInstalled_notInstalled() {
        EnvironmentService environmentService = new EnvironmentService(mockService);
        when(mockService.executeCommand("docker --version"))
                .thenReturn(new CommandResult("", 127));

        boolean result = environmentService.isDockerInstalled();
        assertFalse(result);
    }

    @Test
    void test_isDockerInstalled_installed() {
        EnvironmentService environmentService = new EnvironmentService(mockService);
        when(mockService.executeCommand("docker --version"))
                .thenReturn(new CommandResult("1.0.0", 0));

        boolean result = environmentService.isDockerInstalled();
        assertTrue(result);
    }

}
