package com.vishwahiremath.springContextPacker;

import com.vishwahiremath.springContextPacker.cli.SpringBotCommand;
import picocli.CommandLine;

/**
 * Main application entrypoint for SpringBot CLI.
 */
public class ContextPackerApp {

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new SpringBotCommand());
        
        // Fallback: If the user provides a wrong command or invalid parameters, print the help menu.
        cmd.setParameterExceptionHandler((ex, args1) -> {
            System.err.println(ex.getMessage());
            ex.getCommandLine().usage(System.err);
            return 1;
        });

        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }
}
