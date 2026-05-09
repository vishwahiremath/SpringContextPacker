package com.vishwahiremath.springContextPacker.parser;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Identifies interfaces extending JpaRepository (or similar Spring Data interfaces)
 * and extracts their method signatures.
 */
public class JpaRepositoryVisitor extends VoidVisitorAdapter<List<String>> {

    @Override
    public void visit(ClassOrInterfaceDeclaration n, List<String> repositories) {
        super.visit(n, repositories);

        // We only care about interfaces
        if (!n.isInterface()) {
            return;
        }

        boolean extendsSpringDataRepo = false;
        for (ClassOrInterfaceType extendedType : n.getExtendedTypes()) {
            String typeName = extendedType.getNameAsString();
            if (typeName.equals("JpaRepository") ||
                typeName.equals("CrudRepository") ||
                typeName.equals("PagingAndSortingRepository") ||
                typeName.equals("Repository")) {
                extendsSpringDataRepo = true;
                break;
            }
        }

        if (extendsSpringDataRepo) {
            String repoName = n.getNameAsString();
            
            // Extract method signatures (no bodies, strip fully qualified names)
            List<String> methods = n.getMethods().stream()
                    .map(this::formatMethodSignature)
                    .map(this::stripFullyQualifiedName)
                    .collect(Collectors.toList());

            if (methods.isEmpty()) {
                repositories.add(repoName);
            } else {
                repositories.add(repoName + " {\n    " + String.join("\n    ", methods) + "\n}");
            }
        }
    }

    private String formatMethodSignature(MethodDeclaration m) {
        String params = m.getParameters().stream()
                .map(p -> p.getType().asString() + " " + p.getNameAsString())
                .collect(Collectors.joining(", "));
        return m.getType().asString() + " " + m.getNameAsString() + "(" + params + ");";
    }

    private String stripFullyQualifiedName(String typeName) {
        if (typeName == null) {
            return "";
        }
        return typeName.replaceAll("([a-zA-Z_$][a-zA-Z\\d_$]*\\.)+", "");
    }
}
