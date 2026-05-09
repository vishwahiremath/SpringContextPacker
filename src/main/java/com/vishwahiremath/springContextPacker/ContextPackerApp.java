package com.vishwahiremath.springContextPacker;

import com.vishwahiremath.springContextPacker.cli.PackCommand;
import picocli.CommandLine;

/**
 * Main application entrypoint for SpringBot CLI.
 */
public class ContextPackerApp {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new PackCommand()).execute(args);
        System.exit(exitCode);
    }
}
