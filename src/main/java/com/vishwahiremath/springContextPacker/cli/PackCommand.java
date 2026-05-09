package com.vishwahiremath.springContextPacker.cli;

import com.vishwahiremath.springContextPacker.discovery.ProjectScanner;
import com.vishwahiremath.springContextPacker.generator.MarkdownRenderer;
import com.vishwahiremath.springContextPacker.model.ProjectContext;
import com.vishwahiremath.springContextPacker.parser.ParserEnvironment;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(name = "pack", mixinStandardHelpOptions = true, version = "1.0",
        description = "Scans a Spring Boot project and generates a LLM-optimized Markdown Semantic Map.")
public class PackCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "The root directory of the Spring Boot project to scan.")
    private Path projectRoot;

    @Option(names = {"-o", "--output"}, description = "Output Markdown file path (default: semantic-map.md).")
    private Path outputPath = Path.of("semantic-map.md");

    @Override
    public Integer call() throws Exception {
        if (!Files.exists(projectRoot) || !Files.isDirectory(projectRoot)) {
            System.err.println("Error: Project root does not exist or is not a directory: " + projectRoot);
            return 1;
        }

        System.out.println("Initializing parser environment...");
        Path sourceRoot = projectRoot.resolve("src/main/java");
        ParserEnvironment.configureSymbolSolver(sourceRoot);

        System.out.println("Scanning project at: " + projectRoot);
        ProjectContext context = ProjectScanner.scan(projectRoot);

        System.out.println("Generating Semantic Map...");
        String markdown = MarkdownRenderer.render(context);

        Files.writeString(outputPath, markdown);
        System.out.println("Successfully generated Semantic Map to: " + outputPath.toAbsolutePath());

        return 0;
    }
}
