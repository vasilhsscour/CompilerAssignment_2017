/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
package org.hua.ast;

public enum Operator {

    PLUS("+"),
    MINUS("-"),
    TIMES("*"),
    DIVISION("/"),
    MODULUS("%"),
    LOGICALAND("&&"),
    LOGICALOR("||"),
    LOGICALNOT("!"),
    EQUAL("=="),
    NOTEQUAL("!="),
    GREATEREQUALTHAN(">="),
    GREATERTHAN(">"),
    LESSTHAN("<"),
    LESSEQUALTHAN("<=");

    private String type;

    private Operator(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return type;
    }

    public boolean isUnary() {
        return this.equals(Operator.MINUS) || this.equals(Operator.LOGICALNOT);
    }

    public boolean isRelational() {
        return this.equals(Operator.EQUAL) || this.equals(Operator.NOTEQUAL)
                || this.equals(Operator.GREATERTHAN) || this.equals(Operator.GREATEREQUALTHAN)
                || this.equals(Operator.LESSTHAN) || this.equals(Operator.LESSEQUALTHAN)
                || this.equals(Operator.LOGICALAND) || this.equals(Operator.LOGICALOR);
    }

}
