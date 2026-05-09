package com.vishwahiremath.springContextPacker.parser;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.vishwahiremath.springContextPacker.model.SpringBean;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Visits the AST to identify Spring beans and extract their dependencies.
 */
public class SpringBeanVisitor extends VoidVisitorAdapter<List<SpringBean>> {

    @Override
    public void visit(ClassOrInterfaceDeclaration n, List<SpringBean> beans) {
        super.visit(n, beans);

        // Only process classes, not interfaces
        if (n.isInterface()) {
            return;
        }

        String stereotype = getSpringStereotype(n);
        if (stereotype != null) {
            String className = n.getNameAsString();
            List<String> dependencies = extractDependencies(n);
            
            // Deduplicate dependencies and strip fully qualified names to save tokens
            List<String> cleanDependencies = dependencies.stream()
                    .map(this::stripFullyQualifiedName)
                    .distinct()
                    .collect(Collectors.toList());
                    
            beans.add(new SpringBean(className, stereotype, cleanDependencies));
        }
    }

    private String getSpringStereotype(ClassOrInterfaceDeclaration n) {
        for (AnnotationExpr annotation : n.getAnnotations()) {
            String name = annotation.getNameAsString();
            if (name.equals("Service") || name.equals("Component") ||
                name.equals("RestController") || name.equals("Controller") || 
                name.equals("Repository") || name.equals("Configuration")) {
                return "@" + name;
            }
        }
        return null;
    }

    private List<String> extractDependencies(ClassOrInterfaceDeclaration n) {
        List<String> dependencies = new ArrayList<>();

        // 1. Constructor dependencies
        // If a class has a single constructor, Spring implicitly autowires it.
        // If there are multiple, Spring looks for @Autowired.
        List<ConstructorDeclaration> constructors = n.getConstructors();
        if (constructors.size() == 1) {
            extractParameters(constructors.get(0), dependencies);
        } else {
            for (ConstructorDeclaration constructor : constructors) {
                if (hasAutowiredAnnotation(constructor.getAnnotations())) {
                    extractParameters(constructor, dependencies);
                }
            }
        }

        // 2. @Autowired field dependencies
        for (FieldDeclaration field : n.getFields()) {
            if (hasAutowiredAnnotation(field.getAnnotations())) {
                dependencies.add(field.getElementType().asString());
            }
        }

        return dependencies;
    }

    private void extractParameters(ConstructorDeclaration constructor, List<String> dependencies) {
        for (Parameter parameter : constructor.getParameters()) {
            dependencies.add(parameter.getType().asString());
        }
    }

    private boolean hasAutowiredAnnotation(com.github.javaparser.ast.NodeList<AnnotationExpr> annotations) {
        return annotations.stream()
                .anyMatch(a -> a.getNameAsString().equals("Autowired") || 
                               a.getNameAsString().equals("Inject"));
    }

    private String stripFullyQualifiedName(String typeName) {
        if (typeName == null) {
            return "";
        }
        // Basic stripping of packages, e.g., java.util.List<String> -> List<String>
        return typeName.replaceAll("([a-zA-Z_$][a-zA-Z\\d_$]*\\.)+", "");
    }
}
