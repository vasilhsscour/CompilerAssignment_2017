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
public class BreakStatement extends Statement
{

    public BreakStatement() {
    }

    
    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
}