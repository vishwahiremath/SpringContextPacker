package com.vishwahiremath.springContextPacker.discovery;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.vishwahiremath.springContextPacker.model.DataEntity;
import com.vishwahiremath.springContextPacker.model.Endpoint;
import com.vishwahiremath.springContextPacker.model.ProjectContext;
import com.vishwahiremath.springContextPacker.model.SpringBean;
import com.vishwahiremath.springContextPacker.parser.EndpointVisitor;
import com.vishwahiremath.springContextPacker.parser.EntityVisitor;
import com.vishwahiremath.springContextPacker.parser.JpaRepositoryVisitor;
import com.vishwahiremath.springContextPacker.parser.SpringBeanVisitor;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * Recursively scans a Spring Boot project for .java and .properties/.yml files,
 * extracting the context using JavaParser and SnakeYAML.
 */
public class ProjectScanner {

    public static ProjectContext scan(Path projectRoot) throws Exception {
        List<SpringBean> beans = new ArrayList<>();
        List<DataEntity> entities = new ArrayList<>();
        List<Endpoint> endpoints = new ArrayList<>();
        List<String> repositories = new ArrayList<>();
        Map<String, String> properties = new HashMap<>();

        try (Stream<Path> paths = Files.walk(projectRoot)) {
            paths.filter(Files::isRegularFile).forEach(file -> {
                String fileName = file.getFileName().toString();
                try {
                    if (fileName.endsWith(".java")) {
                        CompilationUnit cu = StaticJavaParser.parse(file);
                        new SpringBeanVisitor().visit(cu, beans);
                        new EntityVisitor().visit(cu, entities);
                        new EndpointVisitor().visit(cu, endpoints);
                        new JpaRepositoryVisitor().visit(cu, repositories);
                    } else if (fileName.equals("application.properties")) {
                        Properties props = new Properties();
                        try (InputStream is = new FileInputStream(file.toFile())) {
                            props.load(is);
                            for (String key : props.stringPropertyNames()) {
                                properties.put(key, props.getProperty(key));
                            }
                        }
                    } else if (fileName.equals("application.yml") || fileName.equals("application.yaml")) {
                        Yaml yaml = new Yaml();
                        try (InputStream is = new FileInputStream(file.toFile())) {
                            Map<String, Object> yamlProps = yaml.load(is);
                            flattenYaml("", yamlProps, properties);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Warning: Failed to parse " + file + " - " + e.getMessage());
                }
            });
        }

        Map<String, String> sanitizedProperties = ConfigSanitizer.sanitize(properties);

        return new ProjectContext(beans, entities, endpoints, repositories, sanitizedProperties);
    }

    @SuppressWarnings("unchecked")
    private static void flattenYaml(String prefix, Map<String, Object> map, Map<String, String> properties) {
        if (map == null) return;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            if (entry.getValue() instanceof Map) {
                flattenYaml(key, (Map<String, Object>) entry.getValue(), properties);
            } else {
                properties.put(key, entry.getValue() == null ? "" : entry.getValue().toString());
            }
        }
    }
}
