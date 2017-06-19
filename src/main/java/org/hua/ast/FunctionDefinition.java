/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.hua.ast;

import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.Type;

/**
 *
 * @author vasilhs12
 */
public class FunctionDefinition extends Definition {
    
    private Type typeSpecifier;
    private String id;
    private List<ParameterDeclaration> parameters;
    private Statement stmt;

    public FunctionDefinition() {
        this.typeSpecifier = null;
        this.id = "";
        this.parameters = new ArrayList<ParameterDeclaration>();
        this.stmt = null;
    }
    
    

    public FunctionDefinition(String id, List<ParameterDeclaration> parameters, Statement stmt) {
        this.id = id;
        this.parameters = parameters;
        this.stmt = stmt;

    }

    public FunctionDefinition(Type typeSpecifier, String id, List<ParameterDeclaration> parameters, Statement stmt) {
        this.typeSpecifier = typeSpecifier;
        this.id = id;
        this.parameters = parameters;
        this.stmt = stmt;
    }

    public Type getTypeSpecifier() {
        return typeSpecifier;
    }

    public void setTypeSpecifier(Type typeSpecifier) {
        this.typeSpecifier = typeSpecifier;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ParameterDeclaration> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterDeclaration> parameters) {
        this.parameters = parameters;
    }

    public Statement getStmt() {
        return stmt;
    }

    public void setStmt(Statement stmt) {
        this.stmt = stmt;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
}
