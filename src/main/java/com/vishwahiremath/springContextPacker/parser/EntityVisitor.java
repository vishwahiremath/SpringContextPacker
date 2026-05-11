package com.vishwahiremath.springContextPacker.parser;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.vishwahiremath.springContextPacker.model.DataEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Identifies @Entity classes and extracts table names and field names.
 */
public class EntityVisitor extends VoidVisitorAdapter<List<DataEntity>> {

    @Override
    public void visit(ClassOrInterfaceDeclaration n, List<DataEntity> entities) {
        super.visit(n, entities);

        if (n.isInterface()) {
            return;
        }

        boolean isEntity = n.getAnnotations().stream()
                .anyMatch(a -> a.getNameAsString().equals("Entity") || a.getNameAsString().equals("Table") || a.getNameAsString().equals("Document"));

        if (isEntity) {
            String className = n.getNameAsString();
            String tableName = extractTableName(n).orElse(className.toLowerCase());
            
            List<String> fields = new ArrayList<>();
            for (FieldDeclaration field : n.getFields()) {
                for (VariableDeclarator var : field.getVariables()) {
                    String fieldType = stripFullyQualifiedName(var.getType().asString());
                    fields.add(fieldType + " " + var.getNameAsString());
                }
            }
            
            entities.add(new DataEntity(className, tableName, fields));
        }
    }

    @Override
    public void visit(RecordDeclaration n, List<DataEntity> entities) {
        super.visit(n, entities);

        String className = n.getNameAsString();
        List<String> fields = new ArrayList<>();
        
        // Records define their fields as parameters in the declaration
        for (Parameter param : n.getParameters()) {
            String fieldType = stripFullyQualifiedName(param.getType().asString());
            fields.add(fieldType + " " + param.getNameAsString());
        }
        
        entities.add(new DataEntity(className, "(Record / DTO)", fields));
    }

    private Optional<String> extractTableName(ClassOrInterfaceDeclaration n) {
        for (AnnotationExpr annotation : n.getAnnotations()) {
            if (annotation.getNameAsString().equals("Table") || annotation.getNameAsString().equals("Document")) {
                if (annotation instanceof NormalAnnotationExpr) {
                    NormalAnnotationExpr normalAnn = (NormalAnnotationExpr) annotation;
                    for (MemberValuePair pair : normalAnn.getPairs()) {
                        if (pair.getNameAsString().equals("name") || pair.getNameAsString().equals("value") || pair.getNameAsString().equals("collection")) {
                            return Optional.of(pair.getValue().toString().replace("\"", ""));
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    private String stripFullyQualifiedName(String typeName) {
        if (typeName == null) {
            return "";
        }
        return typeName.replaceAll("([a-zA-Z_$][a-zA-Z\\d_$]*\\.)+", "");
    }
}
