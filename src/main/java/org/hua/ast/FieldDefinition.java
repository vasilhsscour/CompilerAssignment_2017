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
public class FieldDefinition extends Definition {
    private Type typeSpecifier;
    private String id;

    public FieldDefinition() {
        this.typeSpecifier = null;
        this.id = "";
    }
    
    public FieldDefinition(Type typeSpecifier, String id) {
        this.typeSpecifier = typeSpecifier;
        this.id = id;
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

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
}
