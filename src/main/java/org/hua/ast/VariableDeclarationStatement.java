/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.hua.ast;

import org.objectweb.asm.Type;

/**
 *
 * @author vasilhs12
 */
public class VariableDeclarationStatement extends Statement{

    private Type type;
    private String id;

    public VariableDeclarationStatement(Type type, String id) {
        this.type = type;
        this.id = id;
    }

    public Type getTypeSpecifier() {
        return type;
    }

    public void setTypeSpecifier(Type type) {
        this.type = type;
    }

    public String getIdentifier() {
        return id;
    }

    public void setIdentifier(String id) {
        this.id = id;
    }
    
    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
}
