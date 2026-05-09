package com.vishwahiremath.springContextPacker.generator;

import com.vishwahiremath.springContextPacker.model.DataEntity;
import com.vishwahiremath.springContextPacker.model.Endpoint;
import com.vishwahiremath.springContextPacker.model.ProjectContext;
import com.vishwahiremath.springContextPacker.model.SpringBean;

/**
 * Renders the parsed ProjectContext into a dense, token-optimized Markdown Semantic Map.
 */
public class MarkdownRenderer {

    public static String render(ProjectContext context) {
        StringBuilder sb = new StringBuilder();

        sb.append("# Spring Project Semantic Map\n\n");

        sb.append("## Configuration Properties\n");
        if (context.properties().isEmpty()) {
            sb.append("- (None found)\n");
        } else {
            context.properties().forEach((k, v) -> sb.append("- `").append(k).append("`: ").append(v).append("\n"));
        }
        sb.append("\n");

        sb.append("## Data Entities\n");
        if (context.entities().isEmpty()) {
            sb.append("- (None found)\n");
        } else {
            for (DataEntity entity : context.entities()) {
                sb.append("- **").append(entity.className()).append("** (Table: `").append(entity.tableName()).append("`)\n");
                for (String field : entity.fields()) {
                    sb.append("  - ").append(field).append("\n");
                }
            }
        }
        sb.append("\n");

        sb.append("## JPA Repositories\n");
        if (context.jpaRepositories().isEmpty()) {
            sb.append("- (None found)\n");
        } else {
            for (String repo : context.jpaRepositories()) {
                // Since repo is already formatted with newlines (e.g. methods), we can just replace newlines with bullet indents
                String indentedRepo = repo.replace("\n", "\n  ");
                sb.append("- ").append(indentedRepo).append("\n");
            }
        }
        sb.append("\n");

        sb.append("## REST Endpoints\n");
        if (context.endpoints().isEmpty()) {
            sb.append("- (None found)\n");
        } else {
            for (Endpoint endpoint : context.endpoints()) {
                sb.append("- `").append(endpoint.httpMethod()).append(" ").append(endpoint.path()).append("`\n");
                sb.append("  - Handler: `").append(endpoint.handlerMethodSignature()).append("`\n");
            }
        }
        sb.append("\n");

        sb.append("## Spring Beans & Wiring\n");
        if (context.beans().isEmpty()) {
            sb.append("- (None found)\n");
        } else {
            for (SpringBean bean : context.beans()) {
                sb.append("- **").append(bean.className()).append("** (`").append(bean.stereotype()).append("`)\n");
                if (bean.dependencies().isEmpty()) {
                    sb.append("  - Dependencies: None\n");
                } else {
                    sb.append("  - Dependencies:\n");
                    for (String dep : bean.dependencies()) {
                        sb.append("    - ").append(dep).append("\n");
                    }
                }
            }
        }
        sb.append("\n");

        return sb.toString();
    }
}
