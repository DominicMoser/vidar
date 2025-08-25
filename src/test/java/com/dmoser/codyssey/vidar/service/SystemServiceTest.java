package com.dmoser.codyssey.vidar.service;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SystemServiceTest {

    @Test
    void testExecuteCommand_returnValue() {

        SystemService systemService = SystemService.get();
        String command = "exit 0";

        CommandResult result = systemService.executeCommand(command);
        Assertions.assertEquals(0, result.exitCode());

        command = "exit 1";

        result = systemService.executeCommand(command);
        Assertions.assertEquals(1, result.exitCode());
    }

    @Test
    void testExecuteCommand_existingCommand() {
        // Execute a command and get return value
        SystemService systemService = new SystemService();
        String command = "echo foobar";

        CommandResult result = systemService.executeCommand(command);
        Assertions.assertEquals(0, result.exitCode());
        Assertions.assertEquals("foobar", result.content());
    }

    @Test
    void testExecuteCommand_nonExistingCommand() {
        // Execute a command and get return value
        SystemService systemService = new SystemService();
        String command = "PROGRAM_NAME_THAT_HOPEFULLY_WILL_NEVER_EXIST";

        CommandResult result = systemService.executeCommand(command);
        Assertions.assertEquals(127, result.exitCode());
    }
}
