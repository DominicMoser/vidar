package com.dmoser.codyssey.vidar.compose;

import com.dmoser.codyssey.vidar.service.CommandResult;
import com.dmoser.codyssey.vidar.service.EnvironmentService;
import com.dmoser.codyssey.vidar.service.SystemService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ComposeTest {

    @Mock
    SystemService systemService;

    @Test
    void test_ls_noService() {
        Compose compose = new Compose("./", systemService, new EnvironmentService(systemService));
        when(systemService.executeCommand("docker compose --file .//docker-compose.yml config --services --no-interpolate")).thenReturn(new CommandResult("", 0));

        Set<String> result = compose.ls();
        Assertions.assertEquals(0, result.size());
    }

    @Test
    void test_ls_MultipleServices() {
        Compose compose = new Compose("./", systemService, new EnvironmentService(systemService));
        when(systemService.executeCommand("docker compose --file .//docker-compose.yml config --services --no-interpolate"))
                .thenReturn(new CommandResult("service1\nservice2", 0));

        Set<String> result = compose.ls();
        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.contains("service1"));
        Assertions.assertTrue(result.contains("service2"));
    }


}
