package com.vishwahiremath.springContextPacker.model;

import java.util.List;
import java.util.Map;

/**
 * Contains the aggregated context of the entire Spring project.
 */
public record ProjectContext(
    List<SpringBean> beans,
    List<DataEntity> entities,
    List<Endpoint> endpoints,
    List<String> jpaRepositories,
    Map<String, String> properties
) {}
