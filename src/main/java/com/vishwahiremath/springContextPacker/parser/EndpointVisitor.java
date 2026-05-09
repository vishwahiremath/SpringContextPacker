package com.vishwahiremath.springContextPacker.parser;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.vishwahiremath.springContextPacker.model.Endpoint;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Identifies Spring REST Endpoints and extracts HTTP methods, paths, and method signatures.
 */
public class EndpointVisitor extends VoidVisitorAdapter<List<Endpoint>> {

    @Override
    public void visit(ClassOrInterfaceDeclaration n, List<Endpoint> endpoints) {
        super.visit(n, endpoints);

        // Check if it's a controller
        boolean isController = n.getAnnotations().stream()
                .anyMatch(a -> a.getNameAsString().equals("RestController") || a.getNameAsString().equals("Controller"));

        if (!isController) {
            return;
        }

        String basePath = extractPath(n.getAnnotations());

        for (MethodDeclaration method : n.getMethods()) {
            for (AnnotationExpr annotation : method.getAnnotations()) {
                String annName = annotation.getNameAsString();
                if (annName.endsWith("Mapping")) {
                    String httpMethod = annName.replace("Mapping", "").toUpperCase();
                    if (httpMethod.isEmpty() || httpMethod.equals("REQUEST")) {
                        httpMethod = "ANY"; // Generic @RequestMapping without explicit method
                    }
                    
                    String methodPath = extractPath(List.of(annotation));
                    String fullPath = basePath + (methodPath.startsWith("/") ? "" : "/") + methodPath;
                    fullPath = fullPath.replaceAll("//+", "/"); // Normalize slashes
                    
                    if (fullPath.endsWith("/") && fullPath.length() > 1) {
                        fullPath = fullPath.substring(0, fullPath.length() - 1);
                    }

                    String signature = formatMethodSignature(method);
                    endpoints.add(new Endpoint(httpMethod, fullPath, stripFullyQualifiedName(signature)));
                }
            }
        }
    }

    private String formatMethodSignature(MethodDeclaration m) {
        String params = m.getParameters().stream()
                .map(p -> p.getType().asString() + " " + p.getNameAsString())
                .collect(Collectors.joining(", "));
        return m.getType().asString() + " " + m.getNameAsString() + "(" + params + ")";
    }

    private String extractPath(List<AnnotationExpr> annotations) {
        for (AnnotationExpr annotation : annotations) {
            String name = annotation.getNameAsString();
            if (name.endsWith("Mapping")) {
                if (annotation instanceof SingleMemberAnnotationExpr) {
                    return ((SingleMemberAnnotationExpr) annotation).getMemberValue().toString().replace("\"", "");
                } else if (annotation instanceof NormalAnnotationExpr) {
                    for (MemberValuePair pair : ((NormalAnnotationExpr) annotation).getPairs()) {
                        if (pair.getNameAsString().equals("value") || pair.getNameAsString().equals("path")) {
                            return pair.getValue().toString().replace("\"", "");
                        }
                    }
                }
            }
        }
        return "";
    }

    private String stripFullyQualifiedName(String typeName) {
        if (typeName == null) {
            return "";
        }
        return typeName.replaceAll("([a-zA-Z_$][a-zA-Z\\d_$]*\\.)+", "");
    }
}
