package com.dmoser.codyssey.vidar.service;

/**
 * A record containing the result of a called process.
 *
 * @param content  The contents of stdout when the command was executed successfully and the contents of stderr otherwise.
 * @param exitCode The exitCode of this process.
 */
public record CommandResult(String content, int exitCode) {
}
