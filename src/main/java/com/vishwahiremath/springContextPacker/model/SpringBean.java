package com.vishwahiremath.springContextPacker.model;

import java.util.List;

/**
 * Represents a Spring bean (e.g., @Service, @Component, @RestController, etc.)
 */
public record SpringBean(
    String className,
    String stereotype,
    List<String> dependencies
) {}
