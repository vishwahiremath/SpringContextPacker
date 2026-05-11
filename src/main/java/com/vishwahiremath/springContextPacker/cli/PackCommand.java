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

@Command(name = "pack", mixinStandardHelpOptions = true, version = "1.0", description = "Scans a Spring Boot project and generates a LLM-optimized Markdown Semantic Map.")
public class PackCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "The root directory of the Spring Boot project to scan (default: current directory).", defaultValue = ".", arity = "0..1")
    private Path projectRoot;

    @Option(names = { "-o", "--output" }, description = "Output Markdown file path (default: semantic-map.md).")
    private Path outputPath = Path.of("semantic-map.md");

    @Override
    public Integer call() throws Exception {
        if (!Files.exists(projectRoot) || !Files.isDirectory(projectRoot)) {
            System.err.println("Error: Project root does not exist or is not a directory: " + projectRoot);
            return 1;
        }

        if (!isSpringProject(projectRoot)) {
            System.err.println("\nError: The specified directory does not appear to be a Spring Boot project.");
            System.err.println(
                    "   NOTE: SpringBot requires a standard Java project structure to parse the AST effectively.");
            System.err.println(
                    "   Please ensure you are running this command in the root directory containing your pom.xml or build.gradle,");
            System.err.println("   and that the project uses Spring Boot.\n");
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

    private boolean isSpringProject(Path root) {
        try {
            // Check standard build files for Spring markers
            Path pom = root.resolve("pom.xml");
            if (Files.exists(pom) && Files.readString(pom).toLowerCase().contains("spring")) {
                return true;
            }
            Path gradle = root.resolve("build.gradle");
            if (Files.exists(gradle) && Files.readString(gradle).toLowerCase().contains("spring")) {
                return true;
            }
            Path gradleKts = root.resolve("build.gradle.kts");
            if (Files.exists(gradleKts) && Files.readString(gradleKts).toLowerCase().contains("spring")) {
                return true;
            }
            // Fallback: Check if src/main/java exists as a bare minimum
            // If they are in a multi-module child project without explicit spring deps in
            // child pom
            Path srcMainJava = root.resolve("src/main/java");
            if (Files.exists(srcMainJava) && Files.exists(pom)) {
                return true; // Weak assumption, but allows edge cases
            }
        } catch (Exception e) {
            // Ignore and fall through to false
        }
        return false;
    }
}
