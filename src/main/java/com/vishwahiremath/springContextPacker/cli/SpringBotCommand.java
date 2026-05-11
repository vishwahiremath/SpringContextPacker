package com.vishwahiremath.springContextPacker.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(name = "springbot", mixinStandardHelpOptions = true, version = "1.0", description = "SpringBot CLI - Generates LLM-optimized Semantic Maps for Spring Boot projects.", subcommands = {
        PackCommand.class,
        CommandLine.HelpCommand.class // Built-in "help" command
})
public class SpringBotCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        // If the user runs the root command without any subcommands, print the help
        // menu.
        CommandLine.usage(this, System.out);
        return 0;
    }
}
