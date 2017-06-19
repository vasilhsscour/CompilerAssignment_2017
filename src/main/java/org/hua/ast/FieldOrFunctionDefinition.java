/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.hua.ast;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author vasilhs12
 */
public class FieldOrFunctionDefinition extends Definition{
    private FieldDefinition fieldDefinition;
    private FunctionDefinition functionDefinition;

    public FieldOrFunctionDefinition() {
        this.fieldDefinition = new FieldDefinition();
        this.functionDefinition = new FunctionDefinition();
    }    

    public FieldOrFunctionDefinition(FieldDefinition fieldDefinition, FunctionDefinition functionDefinition) {
        this.fieldDefinition = fieldDefinition;
        this.functionDefinition = functionDefinition;
    }
    
    

    public FieldDefinition getFieldDefinition() {
        return fieldDefinition;
    }

    public void setFieldDefinition(FieldDefinition fieldDefinition) {
        this.fieldDefinition = fieldDefinition;
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
