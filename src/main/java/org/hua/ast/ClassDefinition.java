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
public class ClassDefinition extends Definition {
    private String id;
    private List<FieldOrFunctionDefinition> definitions;

    public ClassDefinition(String id) {
        this.id = id;
        this.definitions = new ArrayList<FieldOrFunctionDefinition>();
    }

    public ClassDefinition(String id, List<FieldOrFunctionDefinition> definitions) {
        this.id = id;
        this.definitions = definitions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<FieldOrFunctionDefinition> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(List<FieldOrFunctionDefinition> definitions) {
        this.definitions = definitions;
    }
    
    
    
    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
}
