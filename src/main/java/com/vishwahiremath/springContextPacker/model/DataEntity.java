package com.vishwahiremath.springContextPacker.model;

import java.util.List;

/**
 * Represents a JPA Entity
 */
public record DataEntity(
    String className,
    String tableName,
    List<String> fields
) {}
