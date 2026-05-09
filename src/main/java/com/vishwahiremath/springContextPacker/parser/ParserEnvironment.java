package com.vishwahiremath.springContextPacker.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.nio.file.Path;

/**
 * Configures the JavaParser environment, particularly the SymbolSolver,
 * to allow resolving types and references within the parsed AST.
 */
public class ParserEnvironment {

    /**
     * Initializes the JavaParser static environment with a configured SymbolSolver.
     * 
     * @param sourceRoot The root directory of the source code to parse (e.g., "src/main/java").
     */
    public static void configureSymbolSolver(Path sourceRoot) {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        
        // Add reflection type solver for standard Java types
        combinedTypeSolver.add(new ReflectionTypeSolver());
        
        // Add the source root if provided and exists
        if (sourceRoot != null && sourceRoot.toFile().exists() && sourceRoot.toFile().isDirectory()) {
            combinedTypeSolver.add(new JavaParserTypeSolver(sourceRoot));
        }

        // Set up the SymbolSolver and configure StaticJavaParser
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
    }
}
