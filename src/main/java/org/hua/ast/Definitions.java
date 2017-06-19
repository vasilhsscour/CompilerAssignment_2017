/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.hua.ast;

/**
 *
 * @author vasilhs12
 */
public class Definitions extends Definition {
    
    private ClassDefinition classDefinition;
    private FunctionDefinition functionDefinition;

    public Definitions(ClassDefinition classDefinition, FunctionDefinition functionDefinition) {
        this.classDefinition = classDefinition;
        this.functionDefinition = functionDefinition;
    }

    public ClassDefinition getClassDefinition() {
        return classDefinition;
    }

    public void setClassDefinition(ClassDefinition classDefinition) {
        this.classDefinition = classDefinition;
    }

    public FunctionDefinition getFunctionDefinition() {
        return functionDefinition;
    }

    public void setFunctionDefinition(FunctionDefinition functionDefinition) {
        this.functionDefinition = functionDefinition;
    }
    
    

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
}
