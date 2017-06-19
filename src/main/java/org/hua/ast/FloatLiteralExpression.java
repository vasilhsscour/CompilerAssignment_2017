/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
package org.hua.ast;

public class FloatLiteralExpression extends Expression {

    private Double literal;

    public FloatLiteralExpression(Double literal) {
        this.literal = literal;
    }

    public Double getLiteral() {
        return literal;
    }

    public void setLiteral(Double literal) {
        this.literal = literal;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
