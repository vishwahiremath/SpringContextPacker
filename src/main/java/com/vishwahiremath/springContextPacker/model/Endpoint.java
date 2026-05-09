package com.vishwahiremath.springContextPacker.model;

/**
 * Represents a REST endpoint
 */
public record Endpoint(
    String httpMethod,
    String path,
    String handlerMethodSignature
) {}
