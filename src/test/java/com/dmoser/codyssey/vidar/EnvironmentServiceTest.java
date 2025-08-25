package com.dmoser.codyssey.vidar;


import com.dmoser.codyssey.vidar.service.CommandResult;
import com.dmoser.codyssey.vidar.service.EnvironmentService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EnvironmentServiceTest {

    @Test
    void testExecuteCommand_returnValue() {


        EnvironmentService environmentService = new EnvironmentService();
        String command = "exit 0";

        CommandResult result = environmentService.executeCommand(command);
        Assertions.assertEquals(0, result.exitCode());

        command = "exit 1";

        result = environmentService.executeCommand(command);
        Assertions.assertEquals(1, result.exitCode());
    }

    @Test
    void testExecuteCommand_existingCommand() {
        // Execute a command and get return value
        EnvironmentService environmentService = new EnvironmentService();
        String command = "echo foobar";

        CommandResult result = environmentService.executeCommand(command);
        Assertions.assertEquals(0, result.exitCode());
        Assertions.assertEquals("foobar", result.content());
    }

    @Test
    void testExecuteCommand_nonExistingCommand() {
        // Execute a command and get return value
        EnvironmentService environmentService = new EnvironmentService();
        String command = "PROGRAM_NAME_THAT_HOPEFULLY_WILL_NEVER_EXIST";

        CommandResult result = environmentService.executeCommand(command);
        Assertions.assertEquals(127, result.exitCode());
    }
}
